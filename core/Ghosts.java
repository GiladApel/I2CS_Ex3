package assignments.Ex3.core;

import exe.ex3.game.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * PSEUDO-CODE:
 * Class Ghosts
 *
 * 1. Represents an enemy entity on the grid.
 * 2. Stores position (x,y) and the item currently hidden underneath it.
 * 3. LOGIC UPDATE: Uses "Smart Random" movement.
 * - It remembers the last direction.
 * - It tries NOT to reverse direction immediately (to avoid shaking back and forth).
 */
public class Ghosts implements GhostCL {

    public int x, y;
    public int itemUnderneath;
    private String image;
    private long birthTime;
    private boolean isStationary;
    private int id;

    // Memory for movement logic (prevents loops)
    private int lastDirection = -1;

    public Ghosts(int x, int y, int id, int itemUnder, boolean isStationary) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.image = "data/g" + (id % 4) + ".png";
        this.itemUnderneath = itemUnder;
        this.birthTime = System.currentTimeMillis();
        this.isStationary = isStationary;
        this.lastDirection = -1; // No previous move yet
    }

    /**
     * Function MoveOneStep
     * 1. If Stationary, return.
     * 2. Get all valid neighbors (not Walls, not other Ghosts).
     * 3. Filter the moves:
     * - Create a preferred list that EXCLUDES the "Reverse" direction.
     * - (Example: If moved UP last time, don't put DOWN in the preferred list).
     * 4. Selection:
     * - If preferred list is not empty -> Pick random from preferred.
     * - If preferred list IS empty (Dead End) -> Pick from original valid moves (allow turning back).
     * 5. Execute Move & Update Board.
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

        // Board
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

    // Helper to detect 180-degree turns
    private boolean isOpposite(int lastDir, int currentDir) {
        if (lastDir == Game.UP && currentDir == Game.DOWN) return true;
        if (lastDir == Game.DOWN && currentDir == Game.UP) return true;
        if (lastDir == Game.LEFT && currentDir == Game.RIGHT) return true;
        if (lastDir == Game.RIGHT && currentDir == Game.LEFT) return true;
        return false;
    }

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


    @Override public String getPos(int i) { return x + "," + y + ",0"; }
    @Override public int getType() { return 0; }
    @Override public String getInfo() { return ""; }
    @Override public double remainTimeAsEatable(int i) { return 0; }
    @Override public int getStatus() { return 0; }

    public static class Manualalgo implements PacManAlgo {
        private int _lastDirection = 0;
        public Manualalgo() {}
        @Override
        public int move(PacmanGame game) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (key == 'w' || key == 'W') _lastDirection = Game.UP;
                else if (key == 'x' || key == 'X') _lastDirection = Game.DOWN;
                else if (key == 'a' || key == 'A') _lastDirection = Game.LEFT;
                else if (key == 'd' || key == 'D') _lastDirection = Game.RIGHT;
            }
            return _lastDirection;
        }
        @Override
        public String getInfo() { return "Manual"; }
    }
}