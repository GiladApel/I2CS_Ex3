package assignments.Ex3.algo;

import assignments.Ex3.utils.Index2D;
import assignments.Ex3.utils.Map;
import assignments.Ex3.utils.Pixel2D;
import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;

/**
 * Ex3Algo - "The Smart Pathfinder with Target Locking"
 * This class implements a smart auto Pacman for  the Pacman game using a hybrid
 * pathfinding strategy. The algorithm combines Breadth-First Search (BFS) with
 * a persistent target-locking mechanism to prevent oscillations (loops).
 * * --- The logic operates in three hierarchical states: ---
 * 1. Target Locking (Consistency):
 * Ensures the agent commits to a specific food item until consumed.
 * 2. Cautious State (Virtual Walls):
 * Navigates using safety zones around ghosts to naturally avoid threats.
 * 3. Fallback & Emergency:
 * Handles optimistic pathfinding or emergency escapes when trapped.
 *
 * @author Gilad Apel
 */
public class Ex3Algo implements PacManAlgo {

    // Persistent state to prevent loops
    private Pixel2D _targetFood = null;

    // Constants for algorithm tuning
    private static final int SAFETY_RADIUS = 1;
    private static final int PANIC_DISTANCE = 3;
    private static final int SPECIAL_FOOD_BONUS = 5;

    public Ex3Algo() {}

    @Override
    public String getInfo() {
        return "Ex3Algo: BFS with Persistent Target Locking & Virtual Walls";
    }

    /**
     * The main execution method.
     * --- Algorithm Flow: ---
     * 1. Environment Analysis: Parse positions and create map copies.
     * 2. Virtual Walls: Mark ghost surroundings as obstacles on a "Safe Map".
     * 3. Target Management: Lock onto a specific food item to prevent looping.
     * 4. Safe Pathfinding: Attempt BFS on the Safe Map toward the locked target.
     * 5. Threat Fallback: Trigger Optimistic pathfinding or Emergency escape if blocked.
     */
    @Override
    public int move(PacmanGame game) {

        //Step 1: Environment Analysis
        int[][] board = game.getGame(0);
        String posString = game.getPos(0);
        Pixel2D pacmanPos = parsePosition(posString);

        //Step 2: Construct Safe Map (Virtual Walls)
        int[][] safeBoard = cloneBoard(board);
        markGhostsAsWalls(game, safeBoard, SAFETY_RADIUS);

        Map safeMap = new Map(safeBoard);
        safeMap.setCyclic(game.isCyclic());

        Map regularMap = new Map(board);
        regularMap.setCyclic(game.isCyclic());

        // Step 3: Target Locking Logic
        // Check if current target is still valid (contains food)
        if (_targetFood != null) {
            int targetVal = board[_targetFood.getX()][_targetFood.getY()];
            if (targetVal <= 1) { // 0=Empty, 1=Wall -> Target no longer exists
                _targetFood = null;
            }
        }

        // If no target is locked, find the best new food item on the Safe Map
        if (_targetFood == null) {
            _targetFood = findBestFood(safeMap, pacmanPos);
        }

        // --- Step 4: Primary Strategy - Safe Path to Locked Target ---
        if (_targetFood != null) {
            Pixel2D[] path = safeMap.shortestPath(pacmanPos, _targetFood, 1);
            if (path != null && path.length > 1) {
                return getDirection(pacmanPos, path[1], safeMap.getWidth(), safeMap.getHeight());
            }
        }

        // --- Step 5: Fallbacks (If Safe Path Fails) ---

        // Immediate Threat? Panic!
        if (isGhostTooClose(game, pacmanPos, PANIC_DISTANCE)) {
            return emergencyEscape(game, regularMap, pacmanPos);
        }

        // Optimistic Search (Ignore virtual walls, just reach food)
        Pixel2D optimisticFood = findBestFood(regularMap, pacmanPos);
        if (optimisticFood != null) {
            Pixel2D[] path = regularMap.shortestPath(pacmanPos, optimisticFood, 1);
            if (path != null && path.length > 1) {
                return getDirection(pacmanPos, path[1], regularMap.getWidth(), regularMap.getHeight());
            }
        }

        // Absolute Fallback
        return emergencyEscape(game, regularMap, pacmanPos);
    }

    // ==================================================================================
    //                                  HELPER METHODS
    // ==================================================================================

    private boolean isGhostTooClose(PacmanGame game, Pixel2D pacmanPos, int limitDist) {
        GhostCL[] ghosts = game.getGhosts(0);
        if (ghosts == null) return false;
        for (GhostCL g : ghosts) {
            Pixel2D gPos = parsePosition(g.getPos(0));
            if (pacmanPos.distance2D(gPos) <= limitDist) return true;
        }
        return false;
    }

    private void markGhostsAsWalls(PacmanGame game, int[][] board, int radius) {
        GhostCL[] ghosts = game.getGhosts(0);
        if (ghosts == null) return;
        int w = board.length;
        int h = board[0].length;
        for (GhostCL g : ghosts) {
            Pixel2D gPos = parsePosition(g.getPos(0));
            int gx = gPos.getX();
            int gy = gPos.getY();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int x = gx + dx; int y = gy + dy;
                    if (game.isCyclic()) { x = (x + w) % w; y = (y + h) % h; }
                    if (x >= 0 && x < w && y >= 0 && y < h) { board[x][y] = 1; }
                }
            }
        }
    }

    private int emergencyEscape(PacmanGame game, Map map, Pixel2D pacmanPos) {
        GhostCL[] ghosts = game.getGhosts(0);
        if(ghosts == null) return Game.UP;
        double maxDist = -1;
        int bestDir = Game.UP;
        int[] dirs = {Game.UP, Game.DOWN, Game.LEFT, Game.RIGHT};
        for(int d : dirs) {
            Pixel2D next = nextPixel(pacmanPos, d, map.getWidth(), map.getHeight());
            if(map.getPixel(next) != 1) {
                double distToClosestGhost = Double.MAX_VALUE;
                for(GhostCL g : ghosts) {
                    Pixel2D gPos = parsePosition(g.getPos(0));
                    double dist = next.distance2D(gPos);
                    if(dist < distToClosestGhost) distToClosestGhost = dist;
                }
                if(distToClosestGhost > maxDist) {
                    maxDist = distToClosestGhost;
                    bestDir = d;
                }
            }
        }
        return bestDir;
    }

    private Pixel2D findBestFood(Map map, Pixel2D start) {
        Pixel2D bestTarget = null;
        double minScore = Double.MAX_VALUE;
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int pixelValue = map.getPixel(x, y);
                if (pixelValue > 1) { // Anything that is not Wall(1) or Empty(0)
                    Pixel2D target = new Index2D(x, y);
                    Pixel2D[] path = map.shortestPath(start, target, 1);
                    if (path != null) {
                        double score = path.length;
                        if (pixelValue > 3) score -= SPECIAL_FOOD_BONUS;
                        if (score < minScore) {
                            minScore = score;
                            bestTarget = target;
                        }
                    }
                }
            }
        }
        return bestTarget;
    }

    private int[][] cloneBoard(int[][] board) {
        int[][] newBoard = new int[board.length][board[0].length];
        for(int i=0; i<board.length; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, board[i].length);
        }
        return newBoard;
    }

    private Pixel2D parsePosition(String pos) {
        String[] parts = pos.split(",");
        return new Index2D(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    private Pixel2D nextPixel(Pixel2D p, int dir, int w, int h) {
        int x = p.getX(), y = p.getY();
        if (dir == Game.UP) y++;
        else if (dir == Game.DOWN) y--;
        else if (dir == Game.RIGHT) x++;
        else if (dir == Game.LEFT) x--;
        if (x < 0) x = w - 1; else if (x >= w) x = 0;
        if (y < 0) y = h - 1; else if (y >= h) y = 0;
        return new Index2D(x, y);
    }

    private int getDirection(Pixel2D current, Pixel2D next, int w, int h) {
        int dx = next.getX() - current.getX();
        int dy = next.getY() - current.getY();
        if (Math.abs(dx) > 1) return (dx > 0) ? Game.LEFT : Game.RIGHT;
        if (Math.abs(dy) > 1) return (dy > 0) ? Game.DOWN : Game.UP;
        if (next.getX() > current.getX()) return Game.RIGHT;
        if (next.getX() < current.getX()) return Game.LEFT;
        if (next.getY() > current.getY()) return Game.UP;
        return Game.DOWN;
    }
}