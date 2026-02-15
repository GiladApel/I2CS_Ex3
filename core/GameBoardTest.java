package assignments.Ex3.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GameBoardTest - Unit Tests for the Game Board Logic.
 * Verifies parsing, data access, and crucial cyclic coordinate wrapping.
 */
class GameBoardTest {

    /**
     * TEST 1: Initialization & Parsing
     * --------------------------------
     * Checks if the board loads correctly from a CSV/String format.
     * We create a small 3x3 board:
     * 0 1 0
     * 0 0 4
     * 0 0 0
     */
    @Test
    void testBoardInitialization() {
        // String representation (Tabs or Commas depending on your implementation)
        // Here assuming standard format "val\tval\nval\tval"
        String mapStr = "0\t1\t0\n0\t0\t4\n0\t0\t0";

        GameBoard board = new GameBoard(mapStr);

        // Check Dimensions
        assertEquals(3, board.getWidth(), "Width should be 3");
        assertEquals(3, board.getHeight(), "Height should be 3");

        // Check Specific Values
        assertEquals(1, board.get(1, 0), "Should contain a WALL (1) at (1,0)");
        assertEquals(0, board.get(2, 1), "Should contain a COIN (4) at (2,1)");
        assertEquals(0, board.get(0, 0), "Should contain EMPTY (0) at (0,0)");
    }

    /**
     * TEST 2: Getters & Setters
     * -------------------------
     * Verifies we can modify the board state (e.g., Pacman eating a coin).
     */
    @Test
    void testModifyBoard() {
        String mapStr = "0\t0\n0\t0"; // 2x2 Empty
        GameBoard board = new GameBoard(mapStr);

        // Verify initial state
        assertEquals(0, board.get(1, 1));

        // Simulate eating a coin / placing a ghost
        board.set(1, 1, 3); // Place Ghost (3)

        // Verify change
        assertEquals(3, board.get(1, 1), "Value should update to 3");
    }

    /**
     * TEST 3: Cyclic Wrapping Logic (CRITICAL!)
     * -----------------------------------------
     * This prevents the game from crashing when Pacman goes off-screen.
     * Logic:
     * - If x < 0 -> Go to Width-1.
     * - If x >= Width -> Go to 0.
     */
    @Test
    void testCyclicWrap() {
        String mapStr = "0\t0\t0\n0\t0\t0\n0\t0\t0"; // 3x3 Board
        GameBoard board = new GameBoard(mapStr);

        int width = board.getWidth();   // 3
        int height = board.getHeight(); // 3

        // Case A: Normal coordinate (No wrap needed)
        assertEquals(1, board.wrap(1, width), "1 should remain 1");

        // Case B: Right Overflow (x = 3 -> should become 0)
        assertEquals(0, board.wrap(3, width), "3 should wrap to 0");
        assertEquals(0, board.wrap(4, width), "4 should wrap to 0");

        // Case C: Left Overflow (x = -1 -> should become 2)
        assertEquals(2, board.wrap(-1, width), "-1 should wrap to 2 (width-1)");
        assertEquals(2, board.wrap(-2, width), "-2 should wrap to 2");

        // Case D: Vertical Overflow (y)
        assertEquals(0, board.wrap(3, height), "Height overflow should wrap to 0");
    }
}