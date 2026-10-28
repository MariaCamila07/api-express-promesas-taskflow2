package taskflow;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Respuesta HTTP/1.1 escrita a mano en el OutputStream del Socket. */
public class HttpResponse {

    private static final Map<Integer, String> REASONS = Map.of(
        200, "OK", 201, "Created", 204, "No Content",
        400, "Bad Request", 404, "Not Found", 405, "Method Not Allowed",
        413, "Payload Too Large", 414, "URI Too Long",
        431, "Request Header Fields Too Large", 500, "Internal Server Error"
    );

    public final int status;
    public final String jsonBody; // puede ser null (204)

    private HttpResponse(int status, String jsonBody) {
        this.status = status;
        this.jsonBody = jsonBody;
    }

    public static HttpResponse json(int status, Object payload) {
        return new HttpResponse(status, Json.stringify(payload));
    }

    public static HttpResponse error(int status, String message) {
        return json(status, Map.of("error", message));
    }

    public static HttpResponse noContent() {
        return new HttpResponse(204, null);
    }

    public void writeTo(OutputStream out) throws IOException {
        byte[] body = jsonBody == null ? new byte[0] : jsonBody.getBytes(StandardCharsets.UTF_8);

        StringBuilder head = new StringBuilder();
        head.append("HTTP/1.1 ").append(status).append(' ')
            .append(REASONS.getOrDefault(status, "Unknown")).append("\r\n");
        if (jsonBody != null) {
            head.append("Content-Type: application/json; charset=utf-8\r\n");
        }
        head.append("Content-Length: ").append(body.length).append("\r\n");
        // CORS: el frontend (localhost:3000) y la API (localhost:3001) son orígenes distintos
        head.append("Access-Control-Allow-Origin: *\r\n");
        head.append("Access-Control-Allow-Methods: GET, POST, PATCH, DELETE, OPTIONS\r\n");
        head.append("Access-Control-Allow-Headers: Content-Type\r\n");
        head.append("Connection: close\r\n");
        head.append("\r\n"); // línea vacía = fin de cabeceras

        out.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.write(body);
        out.flush();
    }
}
