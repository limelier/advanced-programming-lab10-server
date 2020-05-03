import exceptions.ClientQuitException;
import game.Game;
import game.GameManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ClientThread extends Thread {
    private final ServerSocket serverSocket;
    private final Socket socket;


    public ClientThread(ServerSocket serverSocket, Socket socket) {
        this.serverSocket = serverSocket;
        this.socket = socket;
    }

    public void waitTurnAndMove(BufferedReader from, PrintWriter to, int player, final Game game) throws InterruptedException, IOException, ClientQuitException {
        synchronized (game) {
            while (game.getActivePlayer() != player) {
                game.wait();
            }
            if (game.isRunning()) {
                return;
            }
            reply(to, game.getState());
            try {
                String request = from.readLine();

                if (request == null) {
                    throw new ClientQuitException();
                }

                String[] words = request.split(" ");
                if (words.length != 2 || !game.place(Integer.parseInt(words[0]), Integer.parseInt(words[1]))) {
                    reply(to, "Invalid move, try again");
                } else {
                    reply(to, game.getState());
                }
                game.notify();
            } catch (IOException | ClientQuitException e) {
                game.stop(0);
                game.notify();
                throw e;
            }
        }
    }

    private void host(BufferedReader from, PrintWriter to) throws InterruptedException, IOException, ClientQuitException {
        final Game game = GameManager.getInstance().createGame();
        String code = game.getCode();

        reply(to, String.format("Game is up, code is %s.", code));

        synchronized (game) {
            while (!game.isRunning()) {
                game.wait(1000);
            }
        }

        reply(to, "The other player has joined, starting game.");

        while (game.isRunning()) {
            waitTurnAndMove(from, to, 1, game);
        }
    }

    private void join(BufferedReader from, PrintWriter to, String code) throws InterruptedException, IOException, ClientQuitException {
        final Game game = GameManager.getInstance().getGame(code);
        if (game == null) {
            reply(to, "Invalid game code.");
            return;
        }

        reply(to, "Joined game. Please wait your turn.");
        synchronized (game) {
            game.start();
            game.notify();
        }

        while(game.isRunning()) {
            waitTurnAndMove(from, to, 2, game);
        }
    }

    private void reply(PrintWriter writer, String response) {
        writer.println(response);
        writer.flush();
    }


    public void run() {
        try (socket) {
            BufferedReader from = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter to = new PrintWriter(socket.getOutputStream());

            boolean clientAlive = true;
            int gameThread = -1;


            while (true) {
                String request = from.readLine();
                if (request == null) {
                    throw new ClientQuitException();
                }
                List<String> words = Arrays.asList(request.split(" "));
                String command = words.get(0);

                switch (command) {
                    case "host":
                        host(from, to);
                        break;
                    case "join":
                        join(from, to, words.get(1));
                        break;
                    default:
                        reply(to, "Bad command. Use 'host' to host a game, or 'join <code>' to join one.");
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (ClientQuitException e) {
            System.out.println("The client has quit, shutting down thread...");
        }
    }
}
