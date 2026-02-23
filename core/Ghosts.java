package assignments.Ex3.core;

import exe.ex3.game.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Class Ghosts
 * Represents an enemy entity on the grid.
 * Uses "Smart Random" movement: remembers the last direction
 * and tries NOT to reverse direction immediately to avoid shaking.
 */
public class Ghosts implements GhostCL {

    public int x, y;
    public int itemUnderneath;
    private final String image;
    private final long birthTime;
    private final boolean isStationary;

    // Memory for movement logic (prevents loops)
    private int lastDirection;

    /**
     * Constructor to initialize a new Ghost.
     * * @param x            The starting X coordinate.
     * @param y            The starting Y coordinate.
     * @param id           The ghost's ID (used to determine its image).
     * @param itemUnder    The item currently located under the ghost (e.g., Coin, Empty).
     * @param isStationary True if this ghost is a stationary target.
     */
    public Ghosts(int x, int y, int id, int itemUnder, boolean isStationary) {
        this.x = x;
        this.y = y;
        this.image = "data/g" + (id % 4) + ".png";
        this.itemUnderneath = itemUnder;
        this.birthTime = System.currentTimeMillis();
        this.isStationary = isStationary;
        this.lastDirection = -1; // No previous move yet
    }


    /**
     * Executes one movement step for the ghost.
     * PSEUDO-CODE:
     * 1. Get all valid neighbors (Not Walls/Ghosts).
     * 2. Filter moves: Exclude the "Reverse" direction to prevent shaking back and forth.
     * 3. Pick a random preferred move, or fallback to any valid move if trapped.
     * 4. Update the board state.
     * * @param board The current game board.
     * @param pacX  Pacman's X coordinate (for future AI expansion).
     * @param pacY  Pacman's Y coordinate (for future AI expansion).
     */
    public void moveOneStep(GameBoard board, int pacX, int pacY) {
        if (isStationary) return;

        List<int[]> allValidMoves = new ArrayList<>();
        List<int[]> preferredMoves = new ArrayList<>();

        // Directions: {Direction_Code, DX, DY}
        int[][] candidates = {
                {Game.RIGHT, 1, 0},
                {Game.LEFT, -1, 0},
                {Game.UP, 0, 1},
                {Game.DOWN, 0, -1}
        };

        for (int[] cand : candidates) {
            int dirCode = cand[0];
            int dx = cand[1];
            int dy = cand[2];

            // Calculate next position (Cyclic safe)
            int tx = (x + dx + board.getWidth()) % board.getWidth();
            int ty = (y + dy + board.getHeight()) % board.getHeight();

            int targetContent = board.get(tx, ty);

            // Check if valid (Not Wall, Not another Ghost)
            if (targetContent != MyGameInfo.WALL && targetContent != MyGameInfo.GHOST) {
                int[] move = {dirCode, tx, ty};
                allValidMoves.add(move);

                // Check if this move is the exact opposite of the last move
                if (!isOpposite(lastDirection, dirCode)) {
                    preferredMoves.add(move);
                }
            }
        }

        // Deadlock check
        if (allValidMoves.isEmpty()) return;

        // Decision making: Prefer NOT to turn back, unless it's a dead end
        int[] chosen;
        if (!preferredMoves.isEmpty()) {
            int randIndex = (int)(Math.random() * preferredMoves.size());
            chosen = preferredMoves.get(randIndex);
        } else {
            // Only way is back (Dead end)
            int randIndex = (int)(Math.random() * allValidMoves.size());
            chosen = allValidMoves.get(randIndex);
        }

        // Execute the move
        int newDir = chosen[0];
        int nx = chosen[1];
        int ny = chosen[2];
        int targetVal = board.get(nx, ny);

        // Board Update
        board.set(x, y, itemUnderneath); // Put back what we stood on

        if (targetVal != MyGameInfo.PACMAN) {
            itemUnderneath = targetVal;
        } else {
            itemUnderneath = MyGameInfo.EMPTY;
        }

        board.set(nx, ny, MyGameInfo.GHOST);
        x = nx;
        y = ny;
        lastDirection = newDir; // Remember direction for next time
    }

    /**
     * Helper to detect 180-degree turns.
     * * @param lastDir    The previous movement direction code.
     * @param currentDir The new candidate direction code.
     * @return true if the new direction is directly opposite to the last one.
     */
    private boolean isOpposite(int lastDir, int currentDir) {
        if (lastDir == Game.UP && currentDir == Game.DOWN) return true;
        if (lastDir == Game.DOWN && currentDir == Game.UP) return true;
        if (lastDir == Game.LEFT && currentDir == Game.RIGHT) return true;
        if (lastDir == Game.RIGHT && currentDir == Game.LEFT) return true;
        return false;
    }

    /**
     * Draws the ghost on the game screen.
     * Includes a visual "spawn" effect where the ghost grows in size during its first 3 seconds.
     * * @param board The game board used for scale and coordinate calculations.
     */
    public void draw(GameBoard board) {
        double s = Math.min(1.0/board.getWidth(), 1.0/board.getHeight());
        double xp = (x + 0.5) * (1.0/board.getWidth());
        double yp = (y + 0.5) * (1.0/board.getHeight());

        long age = System.currentTimeMillis() - birthTime;
        double currentSize = (age < 3000) ? s * 0.5 : s;

        try {
            StdDraw.picture(xp, yp, image, currentSize, currentSize);
        } catch (Exception e) {
            StdDraw.setPenColor(Color.RED.getRGB());
            StdDraw.filledCircle(xp, yp, s * 0.4, 0);
        }
    }

    //INTERFACE IMPLEMENTATIONS

    @Override public String getPos(int i) { return x + "," + y + ",0"; }
    @Override public int getType() { return 0; }
    @Override public String getInfo() { return ""; }
    @Override public double remainTimeAsEatable(int i) { return 0; }
    @Override public int getStatus() { return 0; }
}