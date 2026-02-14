package assignments.Ex3.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JUnit 5 Testing class for Map logic.
 * Focuses on BFS pathfinding, Cyclic logic, and Wall avoidance.
 */
public class MapTest {

    /**
     * Test 1: Simple Straight Line
     * Checks if the algorithm finds a path on an empty board.
     */
    @Test
    void testBasicPath() {
        // Create a 10x10 empty board
        int[][] board = new int[10][10];
        Map map = new Map(board);

        Pixel2D start = new Index2D(0, 0);
        Pixel2D end = new Index2D(0, 5);

        // Calculate path (obstacle color is 1)
        Pixel2D[] path = map.shortestPath(start, end, 1);

        // Assertions
        assertNotNull(path, "Path should not be null on empty board");
        assertEquals(6, path.length, "Path length from (0,0) to (0,5) should be 6 nodes");
        assertEquals(end.getX(), path[path.length-1].getX(), "Last step should be target X");
        assertEquals(end.getY(), path[path.length-1].getY(), "Last step should be target Y");
    }

    /**
     * Test 2: Wall Avoidance
     * Places a wall between start and end. Path must go around.
     */
    @Test
    void testWallAvoidance() {
        int[][] board = new int[5][5];

        // Build a wall at x=2
        board[2][0] = 1;
        board[2][1] = 1;
        board[2][2] = 1;

        Map map = new Map(board);
        Pixel2D start = new Index2D(1, 1);
        Pixel2D end = new Index2D(3, 1);

        Pixel2D[] path = map.shortestPath(start, end, 1);

        assertNotNull(path);
        // The direct distance is 2 steps, but with wall it must be longer
        assertTrue(path.length > 3, "Path must be longer to bypass the wall");
    }

    /**
     * Test 3: Cyclic World (Wrap-around)
     * Checks if the path crosses from right edge to left edge efficiently.
     */
    @Test
    void testCyclicPath() {
        int[][] board = new int[10][10];
        Map map = new Map(board);
        map.setCyclic(true); // Enable Cyclic Mode

        Pixel2D start = new Index2D(0, 5);
        Pixel2D end = new Index2D(9, 5); // Just 1 step away in a cyclic world!

        Pixel2D[] path = map.shortestPath(start, end, 1);

        assertNotNull(path);
        assertEquals(2, path.length, "In cyclic world, (0,5) and (9,5) are neighbors (length 2)");
    }

    /**
     * Test 4: Unreachable Target
     * Target is completely surrounded by walls.
     */
    @Test
    void testUnreachable() {
        int[][] board = new int[5][5];

        // Surround (3,3) with walls
        board[2][3] = 1; board[4][3] = 1;
        board[3][2] = 1; board[3][4] = 1;

        Map map = new Map(board);
        Pixel2D start = new Index2D(0, 0);
        Pixel2D end = new Index2D(3, 3);

        Pixel2D[] path = map.shortestPath(start, end, 1);

        assertNull(path, "Path should be null if target is unreachable");
    }

    /**
     * Test 5: Same Point
     * Start and End are the same.
     */
    @Test
    void testSamePoint() {
        int[][] board = new int[5][5];
        Map map = new Map(board);
        Pixel2D p = new Index2D(2, 2);

        Pixel2D[] path = map.shortestPath(p, p, 1);

        assertNotNull(path);
        assertEquals(1, path.length, "Path to self should be length 1 (just the start node)");
    }
}
