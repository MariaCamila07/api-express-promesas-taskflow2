package taskflow;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 3001;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Puerto inválido: " + args[0]);
                System.exit(1);
            }
        }
        new TaskServer(port).start();
    }
}
