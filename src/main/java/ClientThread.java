import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientThread extends Thread {
    private final ServerSocket serverSocket;
    private final Socket socket;


    public ClientThread(ServerSocket serverSocket, Socket socket) {
        this.serverSocket = serverSocket;
        this.socket = socket;
    }

    public void run() {
        try (socket) {
            BufferedReader from = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter to = new PrintWriter(socket.getOutputStream());

            while (true) {
                String request = from.readLine();
                if (request == null) {
                    System.out.println("Client quit, shutting down thread.");
                    break;
                }
                if (request.equals("stop")) {
                    to.println("Server stopping.");
                    to.flush();
                    System.out.println("Client called 'stop', closing socket...");
                    socket.close();
                    serverSocket.close();
                    break;
                }
                String response = String.format("Server received the command: %s.", request);
                to.println(response);
                to.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
