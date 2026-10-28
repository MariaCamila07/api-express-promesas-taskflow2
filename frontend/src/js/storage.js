// storage.js: capa de persistencia.
// Antes hablaba con localStorage. Ahora habla con la API en Java (sockets).
// Fijate que dom.js y app.js casi no se enteran del cambio: eso es la responsabilidad unica.

const API_URL = 'http://localhost:3001/api/tasks';

/** Hace el fetch y convierte los fallos (red o HTTP) en un Error con mensaje legible. */
async function request(url, options) {
  let response;
  try {
    response = await fetch(url, options);
  } catch {
    // fetch solo rechaza cuando no hay red / el servidor esta apagado
    throw new Error('No se pudo conectar con el servidor. ¿Está corriendo en el puerto 3001?');
  }

  let data = null;
  try {
    data = await response.json();
  } catch {
    // respuesta sin JSON valido
  }

  // fetch NO rechaza con 400/404/500, hay que revisar response.ok a mano
  if (!response.ok) {
    throw new Error(data?.error ?? `Error del servidor (HTTP ${response.status})`);
  }
  return data;
}

const JSON_HEADERS = { 'Content-Type': 'application/json' };

export function loadTasksFromStorage() {
  return request(API_URL);
}

export function createTaskInStorage(title) {
  return request(API_URL, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ title })
  });
}

export function setTaskCompleted(id, completed) {
  return request(`${API_URL}/${id}`, {
    method: 'PATCH',
    headers: JSON_HEADERS,
    body: JSON.stringify({ completed })
  });
}

export function deleteTaskFromStorage(id) {
  return request(`${API_URL}/${id}`, { method: 'DELETE' });
}
