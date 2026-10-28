package taskflow;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Atiende UNA conexión de cliente en su propio hilo:
 *   leer petición -> enrutar -> escribir respuesta -> cerrar socket.
 */
public class ClientHandler implements Runnable {

    private static final Pattern TASK_BY_ID = Pattern.compile("^/api/tasks/(\\d+)$");

    private final Socket socket;
    private final TaskStore store;

    public ClientHandler(Socket socket, TaskStore store) {
        this.socket = socket;
        this.store = store;
    }

    @Override
    public void run() {
        // try-with-resources: el socket siempre se cierra
        try (Socket s = socket) {
            s.setSoTimeout(5000); // evita hilos colgados por clientes lentos
            InputStream in = new BufferedInputStream(s.getInputStream());
            OutputStream out = s.getOutputStream();

            HttpResponse response;
            String logLine;
            try {
                HttpRequest req = HttpRequest.read(in);
                if (req == null) return; // conexión vacía
                response = route(req);
                logLine = req.method + " " + req.path;
            } catch (HttpRequest.BadRequestException e) {
                response = HttpResponse.error(e.status, e.getMessage());
                logLine = "(petición inválida)";
            } catch (Exception e) {
                e.printStackTrace();
                response = HttpResponse.error(500, "Error interno del servidor");
                logLine = "(error)";
            }
            response.writeTo(out);
            System.out.printf("[%s] %-28s -> %d%n", Thread.currentThread().getName(), logLine, response.status);
        } catch (SocketTimeoutException e) {
            // cliente que no envió nada a tiempo: se ignora
        } catch (IOException e) {
            System.err.println("Error de E/S con el cliente: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Enrutador
    // ------------------------------------------------------------------
    private HttpResponse route(HttpRequest req) {
        String path = req.path;

        // Preflight CORS: el navegador pregunta antes de un POST/PATCH/DELETE con JSON
        if (req.method.equals("OPTIONS")) return HttpResponse.noContent();

        if (path.equals("/")) {
            return req.method.equals("GET") ? info() : HttpResponse.error(405, "Método no permitido");
        }

        if (path.equals("/api/tasks")) {
            return switch (req.method) {
                case "GET"  -> HttpResponse.json(200, store.findAll());
                case "POST" -> createTask(req);
                default     -> HttpResponse.error(405, "Método no permitido");
            };
        }

        Matcher m = TASK_BY_ID.matcher(path);
        if (m.matches()) {
            long id;
            try {
                id = Long.parseLong(m.group(1));
            } catch (NumberFormatException e) {
                return HttpResponse.error(404, "Tarea no encontrada.");
            }
            return switch (req.method) {
                case "PATCH"  -> updateTask(id, req);
                case "DELETE" -> deleteTask(id);
                default       -> HttpResponse.error(405, "Método no permitido");
            };
        }

        return HttpResponse.error(404, "Ruta no encontrada.");
    }

    // ------------------------------------------------------------------
    // Handlers de cada endpoint
    // ------------------------------------------------------------------
    private HttpResponse createTask(HttpRequest req) {
        Map<String, Object> body = parseJsonObject(req.body);
        if (body == null) return HttpResponse.error(400, "El cuerpo debe ser un objeto JSON válido.");

        Object title = body.get("title");
        if (!(title instanceof String s) || s.trim().isEmpty()) {
            return HttpResponse.error(400, "El título de la tarea es requerido.");
        }
        if (s.trim().length() > 200) {
            return HttpResponse.error(400, "El título no puede superar 200 caracteres.");
        }
        return HttpResponse.json(201, store.add(s.trim()));
    }

    private HttpResponse updateTask(long id, HttpRequest req) {
        Map<String, Object> body = parseJsonObject(req.body);
        if (body == null) return HttpResponse.error(400, "El cuerpo debe ser un objeto JSON válido.");

        Object completed = body.get("completed");
        if (!(completed instanceof Boolean flag)) {
            return HttpResponse.error(400, "El campo 'completed' (booleano) es requerido.");
        }
        Optional<Task> updated = store.update(id, flag);
        return updated.isPresent()
            ? HttpResponse.json(200, updated.get())
            : HttpResponse.error(404, "Tarea no encontrada.");
    }

    private HttpResponse deleteTask(long id) {
        if (!store.remove(id)) return HttpResponse.error(404, "Tarea no encontrada.");
        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("message", "Tarea eliminada exitosamente");
        ok.put("id", id);
        return HttpResponse.json(200, ok);
    }

    private HttpResponse info() {
        Map<String, Object> endpoints = new LinkedHashMap<>();
        endpoints.put("listar", "GET http://localhost:3001/api/tasks");
        endpoints.put("crear", "POST http://localhost:3001/api/tasks (body: {\"title\":\"...\"})");
        endpoints.put("actualizar", "PATCH http://localhost:3001/api/tasks/:id (body: {\"completed\":true})");
        endpoints.put("eliminar", "DELETE http://localhost:3001/api/tasks/:id");
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("mensaje", "API de tareas TaskFlow sobre sockets TCP (Java) — Computación en Internet I");
        info.put("endpoints", endpoints);
        return HttpResponse.json(200, info);
    }

    /** Devuelve el objeto JSON como Map, o null si el texto no es un objeto JSON válido. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJsonObject(String text) {
        try {
            Object parsed = Json.parse(text);
            return parsed instanceof Map ? (Map<String, Object>) parsed : null;
        } catch (Json.JsonException e) {
            return null;
        }
    }
}
