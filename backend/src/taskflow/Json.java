package taskflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mini-librería JSON (parser + serializador) escrita a mano para el taller,
 * de modo que el backend no dependa de ninguna librería externa.
 *
 * parse():     String -> Map | List | String | Double | Boolean | null
 * stringify(): Map | List | String | Number | Boolean | Task | null -> String
 */
public final class Json {

    private Json() {}

    public static class JsonException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public JsonException(String msg) { super(msg); }
    }

    // ------------------------------------------------------------------
    // Serialización
    // ------------------------------------------------------------------
    public static String stringify(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    private static void write(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof Task t) {
            write(sb, t.toMap());
        } else if (v instanceof String s) {
            writeString(sb, s);
        } else if (v instanceof Boolean || v instanceof Number) {
            sb.append(v);
        } else if (v instanceof Map<?, ?> m) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                writeString(sb, String.valueOf(e.getKey()));
                sb.append(':');
                write(sb, e.getValue());
            }
            sb.append('}');
        } else if (v instanceof Iterable<?> it) {
            sb.append('[');
            boolean first = true;
            for (Object o : it) {
                if (!first) sb.append(',');
                first = false;
                write(sb, o);
            }
            sb.append(']');
        } else {
            throw new JsonException("Tipo no serializable: " + v.getClass());
        }
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
    }

    // ------------------------------------------------------------------
    // Parseo (descenso recursivo)
    // ------------------------------------------------------------------
    public static Object parse(String text) {
        Parser p = new Parser(text);
        p.skipWs();
        Object result = p.readValue();
        p.skipWs();
        if (!p.atEnd()) throw new JsonException("Texto sobrante tras el JSON");
        return result;
    }

    private static final class Parser {
        private final String s;
        private int pos = 0;

        Parser(String s) { this.s = s; }

        boolean atEnd() { return pos >= s.length(); }

        void skipWs() {
            while (!atEnd() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        char peek() {
            if (atEnd()) throw new JsonException("Fin inesperado del JSON");
            return s.charAt(pos);
        }

        void expect(char c) {
            if (peek() != c) throw new JsonException("Se esperaba '" + c + "' en la posición " + pos);
            pos++;
        }

        Object readValue() {
            skipWs();
            char c = peek();
            if (c == '{') return readObject();
            if (c == '[') return readArray();
            if (c == '"') return readString();
            if (c == 't' || c == 'f' || c == 'n') return readLiteral();
            return readNumber();
        }

        Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWs();
            if (peek() == '}') { pos++; return map; }
            while (true) {
                skipWs();
                String key = readString();
                skipWs();
                expect(':');
                map.put(key, readValue());
                skipWs();
                if (peek() == ',') { pos++; continue; }
                expect('}');
                return map;
            }
        }

        List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWs();
            if (peek() == ']') { pos++; return list; }
            while (true) {
                list.add(readValue());
                skipWs();
                if (peek() == ',') { pos++; continue; }
                expect(']');
                return list;
            }
        }

        String readString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = peek();
                pos++;
                if (c == '"') return sb.toString();
                if (c != '\\') { sb.append(c); continue; }
                char e = peek();
                pos++;
                switch (e) {
                    case '"', '\\', '/' -> sb.append(e);
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (pos + 4 > s.length()) throw new JsonException("Escape \\u incompleto");
                        try {
                            sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        } catch (NumberFormatException ex) {
                            throw new JsonException("Escape \\u inválido");
                        }
                        pos += 4;
                    }
                    default -> throw new JsonException("Escape inválido: \\" + e);
                }
            }
        }

        Object readLiteral() {
            if (s.startsWith("true", pos))  { pos += 4; return Boolean.TRUE; }
            if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            if (s.startsWith("null", pos))  { pos += 4; return null; }
            throw new JsonException("Literal inválido en la posición " + pos);
        }

        Object readNumber() {
            int start = pos;
            while (!atEnd() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
            if (start == pos) throw new JsonException("Valor inválido en la posición " + pos);
            try {
                return Double.parseDouble(s.substring(start, pos));
            } catch (NumberFormatException ex) {
                throw new JsonException("Número inválido");
            }
        }
    }
}
