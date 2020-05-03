package game;

import java.util.StringJoiner;

public class Game {
    private final int[][] board = new int[19][19];
    private int activePlayer = 1; // 1 or 2
    private GameState gameState = GameState.pending;
    private int winner = 0;
    private final String code;

    public Game(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void start() {
        gameState = GameState.running;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void stop(int winner) {
        gameState = GameState.stopped;
        this.winner = winner;
        activePlayer = 3 - activePlayer;
    }

    public int getActivePlayer() {
        return activePlayer;
    }

    public boolean place(int row, int col) {
        if (row < 0 || row >= 19 || col < 0 || col >= 19) {
            return false;
        }
        if (board[row][col] == 0) {
            board[row][col] = activePlayer;
            activePlayer = 3 - activePlayer;
            return true;
        }
        return false;
    }

    public boolean placeByOne(int row, int col) {
        return place(row - 1, col - 1);
    }

    public String getState() {
        StringJoiner lines = new StringJoiner("\n");
        lines.add("# GAME STATE #");
        for (int row = 0; row < 19; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < 19; col++) {
                line.append(getStateForPosition(row, col));
            }
            lines.add(line.toString());
        }
        return lines.toString();
    }

    private char getStateForPosition(int row, int col) {
        int state = this.board[row][col];

        switch(state) {
            case 0:
                return '_';
            case 1:
                return 'X';
            case 2:
                return 'O';
            default:
                throw new Error();
        }
    }

    public int getWinner() {
        return winner;
    }
}
