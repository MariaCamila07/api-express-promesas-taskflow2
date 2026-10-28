package taskflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Almacén de tareas en memoria.
 * Varios hilos (uno por cliente) lo usan a la vez, por eso los métodos
 * son synchronized: evitamos condiciones de carrera.
 */
public final class TaskStore {

    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public TaskStore() {
        seed("Comprender la estructura básica de HTML y CSS", true);
        seed("Aprender a seleccionar y modificar elementos del DOM", true);
        seed("Entender qué es una Promesa y sus tres estados en JS", false);
        seed("Consumir una API externa usando fetch y async/await", false);
        seed("Renderizar dinámicamente los datos en la interfaz web", false);
    }

    private void seed(String title, boolean completed) {
        tasks.add(new Task(nextId.getAndIncrement(), title, completed));
    }

    public synchronized List<Task> findAll() {
        return List.copyOf(tasks);
    }

    public synchronized Task add(String title) {
        Task t = new Task(nextId.getAndIncrement(), title, false);
        tasks.add(t);
        return t;
    }

    /** Cambia el estado de completado. Devuelve la tarea actualizada o vacío si no existe. */
    public synchronized Optional<Task> update(long id, boolean completed) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).id() == id) {
                Task updated = tasks.get(i).withCompleted(completed);
                tasks.set(i, updated);
                return Optional.of(updated);
            }
        }
        return Optional.empty();
    }

    public synchronized Optional<Task> findById(long id) {
        return tasks.stream().filter(t -> t.id() == id).findFirst();
    }

    public synchronized boolean remove(long id) {
        return tasks.removeIf(t -> t.id() == id);
    }
}
