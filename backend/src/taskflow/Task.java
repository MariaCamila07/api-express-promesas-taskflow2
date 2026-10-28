package taskflow;

import java.util.LinkedHashMap;
import java.util.Map;

/** Modelo de dominio: una tarea. Inmutable (record de Java 17+). */
public record Task(long id, String title, boolean completed) {

    public Task withCompleted(boolean value) {
        return new Task(id, title, value);
    }

    /** Representación como Map para serializarla a JSON. */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("title", title);
        m.put("completed", completed);
        return m;
    }
}
