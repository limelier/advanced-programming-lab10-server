package game;

import java.util.StringJoiner;

class Direction {
    public final int d_row;
    public final int d_col;

    Direction(int d_row, int d_col) {
        this.d_row = d_row;
        this.d_col = d_col;
    }
}

class Position {
    public final int row;
    public final int col;
    Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
    Position shift(Direction dir, int times) {
        return new Position(row + dir.d_row * times, col + dir.d_col * times);
    }

    boolean valid() {
        return row >= 0 && row < 19 && col >= 0 && col < 19;
    }
}

public class Game {
    private final static Direction[] dirs = {
            new Direction(-1, -1),
            new Direction(-1, 0),
            new Direction(-1, 1),
            new Direction(0, -1),
            new Direction(0, 1),
            new Direction(1, -1),
            new Direction(1, 0),
            new Direction(1, 1),
    };

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
        if (board[row][col] != 0) {
            return false;
        }

        board[row][col] = activePlayer;

        if (makesWin(row, col)) {
            gameState = GameState.stopped;
            this.winner = activePlayer;
        }

        activePlayer = 3 - activePlayer;
        return true;
    }

    private int checkPosition(Position pos) {
        return board[pos.row][pos.col];
    }

    private boolean makesWin(int row, int col) {
        int player = board[row][col];
        Position curr = new Position(row, col);

        for (Direction dir : dirs) {
            Position end = curr.shift(dir, 4);
            if (end.valid() && checkPosition(end) == player) {
                boolean good = true;
                for (int times = 1; times <= 3 && good; times++) {
                    if (checkPosition(curr.shift(dir, times)) != player) {
                        good = false;
                    }
                }
                if (good) {
                    return true;
                }
            }
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
