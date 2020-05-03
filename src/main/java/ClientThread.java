import exceptions.ClientQuitException;
import game.Game;
import game.GameManager;
import game.GameState;

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
                System.out.printf("[D%d] Not my turn, waiting.\n", player);
                game.wait();
            }
            if (game.getGameState() != GameState.running) {
                System.out.printf("[D%d] Game died while waiting, cancelling move.\n", player);
                return;
            }
            System.out.printf("[D%d] Sending game-state before turn.\n", player);
            reply(to, game.getState());
            try {
                String request;

                while (true) {
                    request = from.readLine();
                    System.out.printf("[D%d] Got command: %s\n", player, request);
                    if (request == null) {
                        throw new ClientQuitException();
                    }
                    if (request.equals("exit")) {
                        System.out.printf("[D%d] Player quit prematurely.\n", player);
                        reply(to, "Quitting game and returning to lobby.");
                        game.stop(0);
                        game.notify();
                        return;
                    }

                    String[] words = request.split(" ");
                    try {
                        if (words.length == 2) {
                            int row = Integer.parseInt(words[0]);
                            int col = Integer.parseInt(words[1]);
                            if (game.placeByOne(row, col)) {
                                break;
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    }

                    reply(to, "Invalid move, try again.");
                    System.out.printf("[D%d] Player executed bad move.\n", player);
                }

                System.out.printf("[D%d] Sending game-state after turn.\n", player);
                reply(to, game.getState());
                game.notify();
            } catch (IOException | ClientQuitException e) {
                System.out.printf("[D%d] Client quit, or other problem. Stopping game prematurely.\n", player);
                game.stop(0);
                game.notify();
                throw e;
            }
        }
    }

    private void host(BufferedReader from, PrintWriter to) throws InterruptedException, IOException, ClientQuitException {
        System.out.println("[Dh] Attempting to host.");
        final Game game = GameManager.getInstance().createGame();
        String code = game.getCode();

        reply(to, String.format("Game is up, code is %s.", code));
        System.out.println("[Dh] Generated game, waiting to start.");

        synchronized (game) {
            while (game.getGameState() == GameState.pending) {
                game.wait(1000);
            }
        }

        reply(to, "The other player has joined, starting game.");
        System.out.println("[Dh] Started game.");

        while (game.getGameState() == GameState.running) {
            waitTurnAndMove(from, to, 1, game);
        }
        System.out.println("[Dh] Game ended.");
        reply(to, getGameOverResponse(game, 1));
    }

    private void join(BufferedReader from, PrintWriter to, String code) throws InterruptedException, IOException, ClientQuitException {
        System.out.println("[Dj] Attempting to join.");
        final Game game = GameManager.getInstance().getGame(code);
        System.out.println("[Dj] Got a game code...");
        if (game == null) {
            reply(to, "Invalid game code.");
            System.out.println("[Dj] Invalid game code.");
            return;
        }
        if (game.getGameState() == GameState.running) {
            reply(to, "This game is already running!");
            System.out.println("[Dj] Tried to join running game.");
            return;
        }
        if (game.getGameState() == GameState.stopped) {
            reply(to, "This game has already ended!");
            System.out.println("[Dj] Tried to join stopped game.");
            return;
        }

        reply(to, "Joined game. Please wait your turn.");
        System.out.println("[Dj] Joined, waiting for turn.");
        synchronized (game) {
            game.start();
            game.notify();
        }

        while (game.getGameState() == GameState.running) {
            waitTurnAndMove(from, to, 2, game);
        }
        System.out.println("[Dj] Game ended.");
        reply(to, getGameOverResponse(game, 2));
    }

    private String getGameOverResponse(Game game, int player) {
        if (game.getWinner() == 0) {
            return "The other player quit, or something similar. Returning to lobby...";
        }
        if (game.getWinner() != player) {
            return "Game over, you lose! Returning to lobby.";
        }
        return "Game over, you win! Returning to lobby.";
    }

    private void reply(PrintWriter writer, String response) {
        writer.println(response);
        writer.flush();
    }


    public void run() {
        System.out.println("[D] Connection established.");
        try (socket) {
            BufferedReader from = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter to = new PrintWriter(socket.getOutputStream());

            while (true) {
                String request = from.readLine();
                if (request == null) {
                    throw new ClientQuitException();
                }
                System.out.printf("[D] Got request: %s\n", request);
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
            System.out.println("[D] The client has quit, shutting down thread...");
        }
    }
}
