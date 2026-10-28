// dom.js: todo lo que toca el arbol DOM vive aca. No sabe nada de fetch ni de la API.
// Regla: nada de innerHTML con datos del usuario. Todo con createElement + textContent (anti XSS).

export function filterTasks(tasks, filter = 'all') {
  if (filter === 'pending') return tasks.filter(t => !t.completed);
  if (filter === 'completed') return tasks.filter(t => t.completed);
  return tasks;
}

function buildTaskItem(task) {
  const li = document.createElement('li');
  li.className = 'task-item';
  li.classList.toggle('is-completed', task.completed);
  li.dataset.id = task.id;

  const check = document.createElement('input');
  check.type = 'checkbox';
  check.className = 'task-check';
  check.checked = task.completed;
  check.setAttribute('aria-label', 'Marcar como completada');

  const title = document.createElement('span');
  title.className = 'task-title';
  title.textContent = task.title;            // seguro: el navegador lo trata como texto

  const del = document.createElement('button');
  del.type = 'button';
  del.className = 'btn-delete';
  del.title = 'Eliminar';
  del.setAttribute('aria-label', 'Eliminar tarea');
  del.textContent = '×';

  li.append(check, title, del);
  return li;
}

export function renderTaskList(tasks, filter = 'all') {
  const listEl = document.querySelector('#task-list');
  const visible = filterTasks(tasks, filter);

  // Armamos todo en un fragmento y lo insertamos de una vez (un solo reflow)
  const fragment = document.createDocumentFragment();

  if (visible.length === 0) {
    const empty = document.createElement('li');
    empty.className = 'empty-message';
    empty.textContent = tasks.length === 0
      ? 'No hay tareas todavía. ¡Agrega la primera!'
      : 'No hay tareas en este filtro.';
    fragment.append(empty);
  } else {
    visible.forEach(task => fragment.append(buildTaskItem(task)));
  }

  listEl.replaceChildren(fragment);
}

export function renderCounter(tasks) {
  const pending = tasks.filter(t => !t.completed).length;
  document.querySelector('#task-counter').textContent =
    `${pending} ${pending === 1 ? 'pendiente' : 'pendientes'}`;
}

export function renderActiveFilter(filter) {
  document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.filter === filter);
  });
}

export function showLoading() {
  const status = document.querySelector('#status');
  const box = document.createElement('div');
  box.className = 'loading';
  const spinner = document.createElement('div');
  spinner.className = 'spinner';
  const text = document.createElement('span');
  text.textContent = 'Cargando...';
  box.append(spinner, text);
  status.replaceChildren(box);
}

export function showError(message) {
  const status = document.querySelector('#status');
  const box = document.createElement('div');
  box.className = 'error-alert';
  const text = document.createElement('span');
  text.textContent = message;
  const close = document.createElement('button');
  close.type = 'button';
  close.className = 'btn-retry';
  close.dataset.action = 'dismiss';
  close.textContent = 'Cerrar';
  box.append(text, close);
  status.replaceChildren(box);
}

export function clearStatus() {
  document.querySelector('#status').replaceChildren();
}

export function setFormBusy(busy) {
  document.querySelector('#task-submit').disabled = busy;
}
