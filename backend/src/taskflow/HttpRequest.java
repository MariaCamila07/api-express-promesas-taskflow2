package taskflow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Petición HTTP/1.1 leída "a mano" desde el InputStream de un Socket.
 *
 * Formato de lo que llega por el socket (texto plano):
 *
 *   POST /api/tasks HTTP/1.1\r\n
 *   Host: localhost:3001\r\n
 *   Content-Type: application/json\r\n
 *   Content-Length: 27\r\n
 *   \r\n                        <-- línea vacía = fin de cabeceras
 *   {"title":"Estudiar sockets"}  <-- cuerpo (Content-Length bytes)
 */
public class HttpRequest {

    public static final int MAX_BODY_BYTES = 1_000_000;
    private static final int MAX_LINE_BYTES = 8192;
    private static final int MAX_HEADERS = 100;

    /** Error de protocolo con el código HTTP con el que debemos responder. */
    public static class BadRequestException extends Exception {
        private static final long serialVersionUID = 1L;
        public final int status;
        public BadRequestException(int status, String msg) {
            super(msg);
            this.status = status;
        }
    }

    public final String method;
    public final String path;
    public final Map<String, String> headers;
    public final String body;

    private HttpRequest(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Devuelve null si el cliente cerró la conexión sin enviar nada
     * (los navegadores abren sockets "especulativos" a veces).
     */
    public static HttpRequest read(InputStream in) throws IOException, BadRequestException {
        String requestLine = readLine(in);
        if (requestLine == null || requestLine.isEmpty()) return null;

        String[] parts = requestLine.split(" ");
        if (parts.length != 3 || !parts[2].startsWith("HTTP/")) {
            throw new BadRequestException(400, "Línea de petición inválida");
        }
        String method = parts[0].toUpperCase();
        String target = parts[1];
        int q = target.indexOf('?');
        String path = q >= 0 ? target.substring(0, q) : target;

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = readLine(in)) != null && !line.isEmpty()) {
            if (headers.size() >= MAX_HEADERS) throw new BadRequestException(431, "Demasiadas cabeceras");
            int colon = line.indexOf(':');
            if (colon <= 0) throw new BadRequestException(400, "Cabecera inválida");
            headers.put(line.substring(0, colon).trim().toLowerCase(), line.substring(colon + 1).trim());
        }

        String body = "";
        String cl = headers.get("content-length");
        if (cl != null) {
            int length;
            try {
                length = Integer.parseInt(cl);
            } catch (NumberFormatException e) {
                throw new BadRequestException(400, "Content-Length inválido");
            }
            if (length < 0) throw new BadRequestException(400, "Content-Length inválido");
            if (length > MAX_BODY_BYTES) throw new BadRequestException(413, "Cuerpo demasiado grande");
            byte[] bytes = in.readNBytes(length);
            if (bytes.length < length) throw new BadRequestException(400, "Cuerpo incompleto");
            body = new String(bytes, StandardCharsets.UTF_8);
        }
        return new HttpRequest(method, path, headers, body);
    }

    /** Lee una línea terminada en \r\n (o \n) byte a byte, sin consumir el cuerpo. */
    private static String readLine(InputStream in) throws IOException, BadRequestException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                byte[] raw = buf.toByteArray();
                int len = raw.length;
                if (len > 0 && raw[len - 1] == '\r') len--;
                return new String(raw, 0, len, StandardCharsets.ISO_8859_1);
            }
            buf.write(b);
            if (buf.size() > MAX_LINE_BYTES) throw new BadRequestException(414, "Línea demasiado larga");
        }
        return buf.size() == 0 ? null : buf.toString(StandardCharsets.ISO_8859_1);
    }
}
