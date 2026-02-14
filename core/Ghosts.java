package assignments.Ex3.core;

import exe.ex3.game.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Ghosts - Represents a ghost entity on the board.
 * Logic: Random movement while respecting walls and other ghosts.
 */
public class Ghosts implements GhostCL {
    public int x, y;
    public int itemUnderneath; // Stores the item (coin/apple) the ghost is standing on
    private String image;
    private long birthTime;
    private boolean isStationary;

    public Ghosts(int x, int y, int imgIdx, int itemUnder, boolean isStationary) {
        this.x = x;
        this.y = y;
        this.image = "data/g" + (imgIdx % 4) + ".png";
        this.itemUnderneath = itemUnder;
        this.birthTime = System.currentTimeMillis();
        this.isStationary = isStationary;
    }

    /**
     * Moves the ghost one step in a random valid direction.
     */
    public void moveOneStep(GameBoard board, int pacX, int pacY) {
        // 1. If stationary (e.g., spawner), do nothing
        if (isStationary) return;

        // 2. Collect valid moves
        List<int[]> validMoves = new ArrayList<>();
        int[][] candidates = {{Game.RIGHT, 1, 0}, {Game.LEFT, -1, 0}, {Game.UP, 0, 1}, {Game.DOWN, 0, -1}};

        for (int[] cand : candidates) {
            int tx = board.wrap(x + cand[1], board.getWidth());
            int ty = board.wrap(y + cand[2], board.getHeight());

            // Check: Must not be a wall AND must not be another ghost (val 3)
            if (!board.isWall(tx, ty) && board.get(tx, ty) != 3) {
                validMoves.add(new int[]{0, tx, ty});
            }
        }

        if (validMoves.isEmpty()) return;

        // 3. Choose a random move from valid options
        int randIndex = (int)(Math.random() * validMoves.size());
        int[] chosen = validMoves.get(randIndex);

        int nx = chosen[1];
        int ny = chosen[2];
        int targetVal = board.get(nx, ny);

        // 4. Update position on board
        if (targetVal != 2) { // If not stepping on Pacman
            board.set(x, y, itemUnderneath); // Restore item
            itemUnderneath = targetVal;      // Save new item
            board.set(nx, ny, 3);            // Set ghost ID
            x = nx;
            y = ny;
        } else {
            // Collision with Pacman (handled in game loop)
            x = nx;
            y = ny;
        }
    }

    public void draw(GameBoard board) {
        double s = Math.min(1.0/board.getWidth(), 1.0/board.getHeight());
        double xp = (x + 0.5) * (1.0/board.getWidth());
        double yp = (y + 0.5) * (1.0/board.getHeight());

        // Growth animation for new ghosts
        long age = System.currentTimeMillis() - birthTime;
        double currentSize = (age < 3000) ? s / 2.0 : s;

        try {
            StdDraw.picture(xp, yp, image, currentSize, currentSize);
        } catch (Exception e) {
            // Fallback: Red circle (4 arguments)
            StdDraw.setPenColor(Color.RED.getRGB());
            StdDraw.filledCircle(xp, yp, s/2, 0);
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
            // Control: W = Up, X = Down, A = Left, D = Right
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
        public String getInfo() {
            return "Manual Control: W-Up, X-Down, A-Left, D-Right";
        }
    }
}