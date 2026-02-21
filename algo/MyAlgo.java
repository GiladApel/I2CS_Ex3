package assignments.Ex3.algo;

import assignments.Ex3.utils.Index2D;
import assignments.Ex3.utils.Map;
import assignments.Ex3.utils.Pixel2D;
import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;

/**
 * MyAlgo -
 * (Ex3Algo - Renamed for internal use in MyGame)
 */
public class MyAlgo implements PacManAlgo {

    // Constants for algorithm tuning
    private static final int SAFETY_RADIUS = 1;
    private static final int PANIC_DISTANCE = 3;
    private static final int SPECIAL_FOOD_BONUS = 5; // Priority for Apples

    public MyAlgo() {}

    @Override
    public String getInfo() {
        return "MyAlgo: BFS with Virtual Walls (Stateless)";
    }

    @Override
    public int move(PacmanGame game) {
        // Step 1: Environment Analysis
        // We initialize variables first to avoid errors
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

        // Step 3: Primary Strategy - Safe Pathfinding
        // We calculate the best food FRESH every turn to avoid looping due to map errors
        Pixel2D bestFood = findBestFood(safeMap, pacmanPos);

        if (bestFood != null) {
            Pixel2D[] path = safeMap.shortestPath(pacmanPos, bestFood, 1);
            if (path != null && path.length > 1) {
                return getDirection(pacmanPos, path[1], safeMap.getWidth(), safeMap.getHeight());
            }
        }

        //Step 4: Threat Assessment & Fallback
        if (isGhostTooClose(game, pacmanPos, PANIC_DISTANCE)) {
            return emergencyEscape(game, regularMap, pacmanPos);
        }

        //Step 5: Secondary Strategy - Optimistic Pathfinding
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

    // HELPER METHODS
    /**
     * Checks if any ghost is within the panic distance.
     * 1. Retrieve all active ghosts.
     * 2. Loop through each ghost and calculate its 2D distance to Pacman.
     * 3. If any ghost is closer than limitDist, return true.
     * * @param game      The current game state.
     * @param pacmanPos Pacman's current coordinates.
     * @param limitDist The threshold distance that triggers panic mode.
     * @return true if a threat is near, false otherwise.
     */
    private boolean isGhostTooClose(PacmanGame game, Pixel2D pacmanPos, int limitDist) {
        GhostCL[] ghosts = game.getGhosts(0);
        if (ghosts == null) return false;
        for (GhostCL g : ghosts) {
            Pixel2D gPos = parsePosition(g.getPos(0));
            if (pacmanPos.distance2D(gPos) <= limitDist) return true;
        }
        return false;
    }

    /**
     * Creates "Virtual Walls" around ghosts to prevent Pacman from getting too close.
     * 1. Get all ghosts.
     * 2. For each ghost, iterate over a square area defined by 'radius'.
     * 3. Apply modulo arithmetic to handle cyclic wrap-around on board edges.
     * 4. Set the board value at those coordinates to 1 (Wall).
     * * @param game   The current game state.
     * @param board  The 2D map array to modify.
     * @param radius The size of the safety padding around the ghost.
     */
    private void markGhostsAsWalls(PacmanGame game, int[][] board, int radius) {
        GhostCL[] ghosts = game.getGhosts(0);
        if (ghosts == null) return;
        int w = board.length; int h = board[0].length;
        for (GhostCL g : ghosts) {
            Pixel2D gPos = parsePosition(g.getPos(0));
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int x = (gPos.getX() + dx + w) % w;
                    int y = (gPos.getY() + dy + h) % h;
                    board[x][y] = 1;
                }
            }
        }
    }

    /**
     * Calculates the safest adjacent move to maximize distance from ghosts.
     * 1. Randomize the checking order of directions to prevent getting stuck in loops.
     * 2. For every valid adjacent cell (not a wall):
     * a. Measure the distance from that cell to the nearest ghost.
     * 3. Pick the direction that results in the largest distance from any ghost.
     * * @param game      The current game state.
     * @param map       The logical map to check for walls.
     * @param pacmanPos Pacman's current coordinates.
     * @return The integer code for the safest direction (UP, DOWN, LEFT, RIGHT).
     */
    private int emergencyEscape(PacmanGame game, Map map, Pixel2D pacmanPos) {
        GhostCL[] ghosts = game.getGhosts(0);
        if(ghosts == null) return Game.UP;
        double maxDist = -1; int bestDir = Game.UP;
        int[] dirs = {Game.UP, Game.DOWN, Game.LEFT, Game.RIGHT};

        // Randomize checking order to break symmetrical loops
        if(Math.random() < 0.5) { dirs = new int[]{Game.DOWN, Game.UP, Game.RIGHT, Game.LEFT}; }

        for(int d : dirs) {
            Pixel2D next = nextPixel(pacmanPos, d, map.getWidth(), map.getHeight());
            if(map.getPixel(next) != 1) {
                double distToClosestGhost = Double.MAX_VALUE;
                for(GhostCL g : ghosts) {
                    Pixel2D gPos = parsePosition(g.getPos(0));
                    double dist = next.distance2D(gPos);
                    if(dist < distToClosestGhost) distToClosestGhost = dist;
                }
                if(distToClosestGhost > maxDist) { maxDist = distToClosestGhost; bestDir = d; }
            }
        }
        return bestDir;
    }


    /**
     * Scans the map to find the most optimal food target.
     * 1. Iterate through the entire 2D grid.
     * 2. If a cell contains a Coin (4) or Apple (5), calculate the BFS shortest path to it.
     * 3. Base score = Path length. Subtract SPECIAL_FOOD_BONUS if it's an Apple.
     * 4. Keep updating and return the target with the lowest score.
     * * @param map   The map to search on (Safe or Regular).
     * @param start Pacman's current position.
     * @return A Pixel2D object representing the best food location, or null if none found.
     */
    private Pixel2D findBestFood(Map map, Pixel2D start) {
        Pixel2D bestTarget = null;
        double minScore = Double.MAX_VALUE;
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                int pixelValue = map.getPixel(x, y);
                if (pixelValue >= 4) { // 4=Coin, 5=Apple
                    Pixel2D target = new Index2D(x, y);
                    Pixel2D[] path = map.shortestPath(start, target, 1);
                    if (path != null) {
                        double score = path.length;
                        // Prioritize Apple
                        if (pixelValue == 5) score -= SPECIAL_FOOD_BONUS;
                        if (score < minScore) { minScore = score; bestTarget = target; }
                    }
                }
            }
        }
        return bestTarget;
    }

    /**
     * Creates a deep copy of a 2D integer array to prevent mutating the original state.
     * 1. Create a new array with the same dimensions.
     * 2. Use System.array copy for fast row-by-row duplication.
     * * @param board The original game board.
     * @return A fresh, independent copy of the board.
     */
    private int[][] cloneBoard(int[][] board) {
        int[][] newBoard = new int[board.length][board[0].length];
        for(int i=0; i<board.length; i++) System.arraycopy(board[i], 0, newBoard[i], 0, board[i].length);
        return newBoard;
    }

    /**
     * Parses a string formatted position into a Pixel2D object.
     * 1. Split the string by the comma delimiter.
     * 2. Parse the X and Y substrings into integers.
     * * @param pos The position string (e.g., "11,14,0").
     * @return A Pixel2D object representing the coordinates.
     */
    private Pixel2D parsePosition(String pos) {
        String[] parts = pos.split(",");
        return new Index2D(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    /**
     * Calculates the adjacent pixel given a direction, supporting cyclic wrap-around.
     * 1. Modify X or Y based on the given direction.
     * 2. Apply modulo math to wrap coordinates to the other side if they cross boundaries.
     * * @param p   The starting pixel.
     * @param dir The direction to move.
     * @param w   Board width.
     * @param h   Board height.
     * @return The next Pixel2D coordinate.
     */
    private Pixel2D nextPixel(Pixel2D p, int dir, int w, int h) {
        int x = p.getX(), y = p.getY();
        if (dir == Game.UP) y++;
        else if (dir == Game.DOWN) y--;
        else if (dir == Game.RIGHT) x++;
        else if (dir == Game.LEFT) x--;
        x = (x + w) % w; y = (y + h) % h;
        return new Index2D(x, y);
    }

    /**
     * Determines the directional command needed to move between two adjacent pixels.
     * 1. Find the difference (dx, dy) between the target and current positions.
     * 2. If the absolute difference is > 1, it implies a cyclic wrap-around movement.
     * 3. Otherwise, determine standard direction based on which axis changed.
     * * @param current The starting pixel.
     * @param next    The destination pixel.
     * @param w       Board width.
     * @param h       Board height.
     * @return The corresponding Game direction constant (UP, DOWN, LEFT, RIGHT).
     */
    private int getDirection(Pixel2D current, Pixel2D next, int w, int h) {
        int dx = next.getX() - current.getX();
        int dy = next.getY() - current.getY();

        // Cyclic handling
        if (Math.abs(dx) > 1) return (dx > 0) ? Game.LEFT : Game.RIGHT;
        if (Math.abs(dy) > 1) return (dy > 0) ? Game.DOWN : Game.UP;

        if (next.getX() > current.getX()) return Game.RIGHT;
        if (next.getX() < current.getX()) return Game.LEFT;

        // Standard Y movement
        if (next.getY() > current.getY()) return Game.UP;
        if (next.getY() < current.getY()) return Game.DOWN;

        return Game.UP;
    }
}