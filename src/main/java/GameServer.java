import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import static java.lang.System.exit;

public class GameServer implements Closeable {
    static final int PORT = 8531;
    final ServerSocket serverSocket;

    public GameServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    public void run() throws IOException {
        System.out.printf("Server listening on port %d...\n", PORT);
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (SocketException e) {
                System.out.println("Socket closed, server closing.");
                exit(1);
            }
            new ClientThread(serverSocket, socket).start();
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }
}
