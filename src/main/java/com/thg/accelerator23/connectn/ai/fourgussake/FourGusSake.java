/*
package com.thg.accelerator23.connectn.ai.fourgussake;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thehutgroup.accelerator.connectn.player.Player;


public class FourGusSake extends Player {
  public FourGusSake(Counter counter) {
       //TODO: fill in your name here
    super(counter, FourGusSake.class.getName());
  }

  @Override
  public int makeMove(Board board) {
    //TODO: some crazy analysis
    //TODO: make sure said analysis uses less than 2G of heap and returns within 10 seconds on whichever machine is running it

    return 4;
  }
}
*/

package com.thg.accelerator23.connectn.ai.fourgussake;

import java.util.Arrays;

public class FourGusSake {

    private static final int N = 4; // Number of tokens in a row needed to win

    private Counter counter;
    private String name;

    public FourGusSake(Counter counter) {
        this.counter = counter;
        this.name = FourGusSake.class.getName();
    }

    public String getName() {
        return this.name;
    }

    public Counter getCounter() {
        return this.counter;
    }

    public int makeMove(Board board) {
        try {
            int move = getBestMove(board, getCounter(), getCounter().getOther(), 5); // Depth set to 5
            board.placeCounterAtPosition(getCounter(), move); // Apply the move to the board
            board.printBoard(); // Display the board after the move
            return move;
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Fallback to column 0 in case of errors
        }
    }

    private int getBestMove(Board board, Counter player, Counter opponent, int depth) {
        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int col = 0; col < board.getConfig().getWidth(); col++) {
            if (isValidMove(board, col)) {
                try {
                    Board newBoard = new Board(board, col, player);
                    int score = minimax(newBoard, depth - 1, false, player, opponent, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = col;
                    }
                } catch (InvalidMoveException e) {
                    // Ignore invalid moves
                }
            }
        }
        return bestMove;
    }

    private int minimax(Board board, int depth, boolean isMaximizing, Counter player, Counter opponent, int alpha, int beta) {
        if (checkWin(board, player)) return 1000;
        if (checkWin(board, opponent)) return -1000;
        if (isBoardFull(board) || depth == 0) return evaluateBoard(board, player, opponent);

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int col = 0; col < board.getConfig().getWidth(); col++) {
                if (isValidMove(board, col)) {
                    try {
                        Board newBoard = new Board(board, col, player);
                        int eval = minimax(newBoard, depth - 1, false, player, opponent, alpha, beta);
                        maxEval = Math.max(maxEval, eval);
                        alpha = Math.max(alpha, eval);
                        if (beta <= alpha) break;
                    } catch (InvalidMoveException e) {
                        // Ignore invalid moves
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col = 0; col < board.getConfig().getWidth(); col++) {
                if (isValidMove(board, col)) {
                    try {
                        Board newBoard = new Board(board, col, opponent);
                        int eval = minimax(newBoard, depth - 1, true, player, opponent, alpha, beta);
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) break;
                    } catch (InvalidMoveException e) {
                        // Ignore invalid moves
                    }
                }
            }
            return minEval;
        }
    }

    private boolean isValidMove(Board board, int col) {
        Position position = new Position(col, 0);
        return board.isWithinBoard(position) && !board.hasCounterAtPosition(position);
    }

    private boolean checkWin(Board board, Counter player) {
        int width = board.getConfig().getWidth();
        int height = board.getConfig().getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Position pos = new Position(x, y);
                if (board.getCounterAtPosition(pos) == player) {
                    if (checkDirection(board, pos, player, 1, 0) || // Horizontal
                            checkDirection(board, pos, player, 0, 1) || // Vertical
                            checkDirection(board, pos, player, 1, 1) || // Diagonal down-right
                            checkDirection(board, pos, player, 1, -1)) { // Diagonal up-right
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkDirection(Board board, Position start, Counter player, int dx, int dy) {
        int count = 0;
        for (int i = 0; i < N; i++) {
            int x = start.getX() + i * dx;
            int y = start.getY() + i * dy;
            Position pos = new Position(x, y);
            if (board.isWithinBoard(pos) && board.getCounterAtPosition(pos) == player) {
                count++;
            } else {
                break;
            }
        }
        return count == N;
    }

    private boolean isBoardFull(Board board) {
        for (int x = 0; x < board.getConfig().getWidth(); x++) {
            if (!board.hasCounterAtPosition(new Position(x, 0))) {
                return false;
            }
        }
        return true;
    }

    private int evaluateBoard(Board board, Counter player, Counter opponent) {
        // Evaluate board for potential alignments, prioritize lines with more consecutive tokens
        int score = 0;
        // Add scoring logic here if needed
        return score;
    }

    static class Board {
        private Counter[][] counterPlacements;
        private GameConfig config;

        public Board(Counter[][] counterPlacements, GameConfig config) {
            this.counterPlacements = counterPlacements;
            this.config = config;
        }

        public Board(Board board, int x, Counter counter) throws InvalidMoveException {
            this.counterPlacements = deepCopy(board.counterPlacements);
            this.config = board.getConfig();
            this.placeCounterAtPosition(counter, x);
        }

        public Board(GameConfig config) {
            this.config = config;
            this.counterPlacements = new Counter[config.getWidth()][config.getHeight()];
        }

        public GameConfig getConfig() {
            return this.config;
        }

        public void printBoard() {
            for (int y = config.getHeight() - 1; y >= 0; y--) { // Print from top to bottom
                for (int x = 0; x < config.getWidth(); x++) {
                    Counter counter = counterPlacements[x][y];
                    System.out.print(counter == null ? "." : counter.getStringRepresentation());
                    System.out.print(" ");
                }
                System.out.println();
            }
            System.out.println();
        }

        public void placeCounterAtPosition(Counter counter, int x) throws InvalidMoveException {
            if (!this.isWithinBoard(new Position(x, 0))) {
                throw new InvalidMoveException("Outside the bounds of the board");
            } else {
                Position position = new Position(x, this.getMinVacantY(x));
                if (this.hasCounterAtPosition(position)) {
                    throw new InvalidMoveException("Column is full");
                } else {
                    this.counterPlacements[position.getX()][position.getY()] = counter;
                }
            }
        }

        public Counter[][] getCounterPlacements() {
            return this.counterPlacements;
        }

        private int getMinVacantY(int x) {
            for (int i = this.config.getHeight() - 1; i >= 0; --i) {
                if (i == 0 || this.counterPlacements[x][i - 1] != null) {
                    return i;
                }
            }
            throw new RuntimeException("no y is vacant");
        }

        public Counter getCounterAtPosition(Position position) {
            return this.isWithinBoard(position) ? this.counterPlacements[position.getX()][position.getY()] : null;
        }

        public boolean hasCounterAtPosition(Position position) {
            return this.isWithinBoard(position) && this.counterPlacements[position.getX()][position.getY()] != null;
        }

        public boolean isWithinBoard(Position position) {
            return position.getX() >= 0 && position.getX() < this.config.getWidth() && position.getY() >= 0 && position.getY() < this.config.getHeight();
        }

        private Counter[][] deepCopy(Counter[][] matrix) {
            return Arrays.stream(matrix).map(Counter[]::clone).toArray(Counter[][]::new);
        }
    }

    static class Counter {
        private String stringRepresentation;

        public Counter(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }

        public String getStringRepresentation() {
            return this.stringRepresentation;
        }

        public Counter getOther() {
            return this.stringRepresentation.equals("X") ? new Counter("O") : new Counter("X");
        }
    }

    static class Position {
        private int x;
        private int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    static class GameConfig {
        private int width;
        private int height;

        public GameConfig(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    static class InvalidMoveException extends Exception {
        public InvalidMoveException(String message) {
            super(message);
        }
    }
}


