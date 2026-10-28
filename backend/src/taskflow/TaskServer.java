package taskflow;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor TCP: abre un ServerSocket y, por cada conexión entrante (accept),
 * delega el trabajo a un hilo del pool para poder atender clientes en paralelo.
 */
public class TaskServer {

    private final int port;
    private final TaskStore store = new TaskStore();
    private final ExecutorService pool = Executors.newFixedThreadPool(16);

    public TaskServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Servidor de tareas (sockets TCP) iniciado en: http://localhost:" + port);
            System.out.println("Endpoints: GET/POST /api/tasks · PATCH/DELETE /api/tasks/:id");
            System.out.println("Ctrl+C para detener.");

            while (true) {
                Socket client = server.accept();          // bloquea hasta que llega un cliente
                pool.execute(new ClientHandler(client, store));
            }
        } finally {
            pool.shutdown();
        }
    }
}
