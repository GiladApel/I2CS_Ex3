package assignments.Ex3.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GhostsTest - Unit Tests for Ghost Logic.
 * Verifies that ghosts initialize correctly, move towards Pacman, and stay within bounds.
 */
class GhostsTest {

    /**
     * TEST 1: Ghost Initialization
     * Verify that a ghost is created at the correct coordinates.
     *
     * 1. Create Ghost at (5, 5).
     * 2. Assert Ghost.x == 5.
     * 3. Assert Ghost.y == 5.
     */
    @Test
    void testGhostCreation() {
        Ghosts ghost = new Ghosts(5, 5, 1, 0, false);
        assertEquals(5, ghost.x, "Ghost X coordinate should match initialization");
        assertEquals(5, ghost.y, "Ghost Y coordinate should match initialization");
    }

    /**
     * TEST 2: Ghost Movement Logic
     * Verify that the ghost actually moves when `moveOneStep` is called.
     * We place Pacman nearby to encourage movement.
     *
     * 1. Create a simple 3x3 Board (all empty).
     * 2. Place Ghost at (1,1).
     * 3. Place Pacman target at (1,2) [UP].
     * 4. Call ghost.moveOneStep().
     * 5. Assert that (Ghost.x != startX OR Ghost.y != startY).
     */
    @Test
    void testGhostMovement() {
        // Create 3x3 Empty Board
        GameBoard board = new GameBoard("0\t0\t0\n0\t0\t0\n0\t0\t0");

        // Ghost at Center (1,1)
        Ghosts ghost = new Ghosts(1, 1, 1, 0, false);

        // Target (Pacman) is at (1,2)
        int pacmanX = 1;
        int pacmanY = 2;

        int startX = ghost.x;
        int startY = ghost.y;

        // Action: Move ghost
        ghost.moveOneStep(board, pacmanX, pacmanY);

        // Verification: Ghost must have moved somewhere
        boolean moved = (ghost.x != startX || ghost.y != startY);
        assertTrue(moved, "Ghost should move towards Pacman and not stay static");
    }

    /**
     * TEST 3: Boundary Safety Check (CRITICAL)
     * Verify that the ghost NEVER goes out of the board limits.
     * This prevents ArrayOutOfBounds exceptions during the game.
     *
     * 1. Create small 2x2 Board.
     * 2. Init Ghost at (0,0).
     * 3. Loop 100 times:
     * a. Move ghost randomly/towards target.
     * b. ASSERT Ghost.x is between 0 and Width-1.
     * c. ASSERT Ghost.y is between 0 and Height-1.
     */
    @Test
    void testGhostBoundaries() {
        GameBoard board = new GameBoard("0\t0\n0\t0"); // 2x2 Board
        Ghosts ghost = new Ghosts(0, 0, 1, 0, false);

        // Stress Test: Move 100 times
        for (int i = 0; i < 100; i++) {
            // Target is arbitrary (1,1)
            ghost.moveOneStep(board, 1, 1);

            // Check Boundaries after every single step
            assertTrue(ghost.x >= 0, "Ghost X underflow!");
            assertTrue(ghost.x < board.getWidth(), "Ghost X overflow!");

            assertTrue(ghost.y >= 0, "Ghost Y underflow!");
            assertTrue(ghost.y < board.getHeight(), "Ghost Y overflow!");
        }
    }
}