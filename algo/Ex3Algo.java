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
 * This class implements an auto Pacman for the Pacman game using a hybrid
 * pathfinding strategy. The algorithm combines Breadth-First Search (BFS) with
 * a persistent target-locking mechanism to prevent oscillations (loops).
 * The logic operates in three hierarchical states:
 * 1. Target Locking (Consistency):
 * Ensures the pacman commits to a specific food item until consumed.
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
     * Algorithm Flow:
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
        markGhostsAsWalls(game, safeBoard);

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

        // Step 4: Primary Strategy
        if (_targetFood != null) {
            Pixel2D[] path = safeMap.shortestPath(pacmanPos, _targetFood, 1);
            if (path != null && path.length > 1) {
                return getDirection(pacmanPos, path[1]);
            }
        }

        // Step 5: Fallbacks (If Safe Path Fails)

        // Immediate Threat? Panic!
        if (isGhostTooClose(game, pacmanPos)) {
            return emergencyEscape(game, regularMap, pacmanPos);
        }

        // Optimistic Search (Ignore virtual walls, just reach food)
        Pixel2D optimisticFood = findBestFood(regularMap, pacmanPos);
        if (optimisticFood != null) {
            Pixel2D[] path = regularMap.shortestPath(pacmanPos, optimisticFood, 1);
            if (path != null && path.length > 1) {
                return getDirection(pacmanPos, path[1]);
            }
        }

        // Absolute Fallback
        return emergencyEscape(game, regularMap, pacmanPos);
    }

    // HELPER METHODS

    /**
     * Checks if any ghost is dangerously close to Pacman.
     * 1. Get all ghosts from the game.
     * 2. Loop through each ghost and calculate its 2D distance to Pacman.
     * 3. Return true if ANY ghost is within the limit distance.
     *
     * @param game      The current game state.
     * @param pacmanPos The current pixel position of Pacman.
     * @return true if a ghost is too close, false otherwise.
     */
    private boolean isGhostTooClose(PacmanGame game, Pixel2D pacmanPos) {
        GhostCL[] ghosts = game.getGhosts(0);
        if (ghosts == null) return false;
        for (GhostCL g : ghosts) {
            Pixel2D gPos = parsePosition(g.getPos(0));
            if (pacmanPos.distance2D(gPos) <= PANIC_DISTANCE) return true;
        }
        return false;
    }

    /**
     * Expands the walls around ghosts to create "Safety Zones".
     * 1. Loop through all active ghosts.
     * 2. For each ghost, iterate through a square area defined by 'radius'.
     * 3. Calculate target coordinates, adjusting for cyclic board if needed.
     * 4. Change the value of those coordinates to 1 (Wall).
     *
     * @param game   The current game state.
     * @param board  The 2D array representation of the board (to be modified).
     */
    private void markGhostsAsWalls(PacmanGame game, int[][] board) {
        GhostCL[] ghosts = game.getGhosts(0);
        if (ghosts == null) return;
        int w = board.length;
        int h = board[0].length;
        for (GhostCL g : ghosts) {
            Pixel2D gPos = parsePosition(g.getPos(0));
            int gx = gPos.getX();
            int gy = gPos.getY();
            for (int dx = -SAFETY_RADIUS; dx <= SAFETY_RADIUS; dx++) {
                for (int dy = -SAFETY_RADIUS; dy <= SAFETY_RADIUS; dy++) {
                    int x = gx + dx; int y = gy + dy;
                    if (game.isCyclic()) { x = (x + w) % w; y = (y + h) % h; }
                    if (x >= 0 && x < w && y >= 0 && y < h) { board[x][y] = 1; }
                }
            }
        }
    }

    /**
     * Calculates the best move to maximize distance from the closest threat.
     * 1. Iterate through all 4 possible movement directions.
     * 2. Determine the next pixel for each direction.
     * 3. If the next pixel is NOT a wall:
     * a. Calculate its distance to the closest ghost.
     * b. If this distance is the largest found so far, save this direction.
     * 4. Return the direction that maximizes survival chances.
     *
     * @param game      The current game state.
     * @param map       The map object for collision checking.
     * @param pacmanPos The current position of Pacman.
     * @return The best direction integer (Game.UP, DOWN, LEFT, RIGHT).
     */
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

    /**
     * Scans the map to find the most optimal food target based on distance and value.
     * 1. Iterate over every cell in the 2D grid.
     * 2. If the cell contains an item (>1):
     * a. Calculate the BFS shortest path to it.
     * b. Base score = Path Length.
     * c. Subtract BONUS if the item is special (e.g., Apple).
     * 3. Update the best target if this score is the lowest (most optimal).
     *
     * @param map   The map to search on (Safe or Regular).
     * @param start The starting point (Pacman's position).
     * @return The Pixel2D coordinates of the best food target, or null if none found.
     */
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

    /**
     * Creates a deep copy of a 2D integer array.
     * 1. Initialize a new 2D array with identical dimensions.
     * 2. Loop through each row and copy its contents using arraycopy.
     *
     * @param board The original 2D array.
     * @return A completely independent clone of the board.
     */
    private int[][] cloneBoard(int[][] board) {
        int[][] newBoard = new int[board.length][board[0].length];
        for(int i=0; i<board.length; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, board[i].length);
        }
        return newBoard;
    }

    /**
     * Parses a string representation of coordinates into a Pixel2D object.
     * 1. Split the input string by commas.
     * 2. Parse the first two string parts into X and Y integers.
     *
     * @param pos String containing coordinates (e.g., "11,14,0").
     * @return A new Index2D object representing the coordinates.
     */
    private Pixel2D parsePosition(String pos) {
        String[] parts = pos.split(",");
        return new Index2D(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    /**
     * Calculates the adjacent pixel given a direction, supporting cyclic boards.
     * 1. Adjust X or Y based on the given direction.
     * 2. Wrap coordinates around to the opposite side if they exceed board limits.
     *
     * @param p   The starting pixel.
     * @param dir The direction to move.
     * @param w   Board width.
     * @param h   Board height.
     * @return The calculated next Pixel2D.
     */
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

    /**
     * Determines the movement direction required to go from current to next pixel.
     * 1. Calculate the difference in X and Y axes.
     * 2. If the difference is > 1, it implies a cyclic wrap-around move.
     * 3. Otherwise, return standard adjacent direction.
     *
     * @param current The starting pixel.
     * @param next    The destination pixel (must be adjacent).
     * @return The direction code (Game.UP, DOWN, LEFT, RIGHT).
     */
    private int getDirection(Pixel2D current, Pixel2D next) {
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