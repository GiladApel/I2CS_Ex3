package assignments.Ex3.algo;

import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import exe.ex3.game.PacmanGame;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * MyAlgoTest - Unit Tests for the Intelligent Algorithm.
 * Uses "Stub" (Mock) classes to simulate game states without the GUI.
 */
class MyAlgoTest {

    /**
     * TEST 1: Basic Pathfinding
     * Scenario:
     * - Pacman at (1,1)
     * - Coin (Value 4) at (2,1) [To the Right]
     * - No walls, no ghosts.
     *
     * Pseudo-Code:
     * 1. Init Empty Board[5][5].
     * 2. Board[2][1] = 4 (Coin).
     * 3. Set Pacman Position = "1,1,0".
     * 4. NextDir = algo.move(game).
     * 5. ASSERT NextDir == Game.RIGHT.
     */
    @Test
    void testMoveTowardsFood() {
        MyAlgo algo = new MyAlgo();
        StubGame game = new StubGame();

        int[][] board = new int[5][5];
        board[2][1] = 4; // Coin is here

        game.setBoard(board);
        game.setPos("1,1,0");

        int move = algo.move(game);
        assertEquals(Game.RIGHT, move, "Pacman should move RIGHT towards the coin");
    }

    /**
     * TEST 2: Apple Priority Logic
     * ----------------------------
     * Scenario:
     * - Pacman at (1,1).
     * - Coin (4) at (1,0) [DOWN] -> Distance = 1.
     * - Apple (5) at (3,1) [RIGHT] -> Distance = 2.
     *
     * Logic:
     * Apple has a "Special Bonus" (Cost - 50).
     * Cost(Coin) = 1.
     * Cost(Apple) = 2 - 50 = -48 (Lower is better).
     *
     * Pseudo-Code:
     * 1. Place Coin at dist 1.
     * 2. Place Apple at dist 2.
     * 3. NextDir = algo.move(game).
     * 4. ASSERT NextDir == Game.RIGHT (Towards Apple).
     */
    @Test
    void testApplePriority() {
        MyAlgo algo = new MyAlgo();
        StubGame game = new StubGame();

        int[][] board = new int[5][5];

        board[1][0] = 4; // Coin (Close)
        board[3][1] = 5; // Apple (Far, but high value)

        game.setBoard(board);
        game.setPos("1,1,0");

        int move = algo.move(game);

        // Even though Coin is closer, we expect RIGHT towards Apple
        assertEquals(Game.RIGHT, move, "Pacman should prioritize Apple over Coin due to score bonus");
    }

    /**
     * TEST 3: Virtual Wall (Ghost Avoidance)
     * Scenario:
     * - Pacman at (0,1).
     * - Food at (2,1) [RIGHT].
     * - Ghost at (1,1) blocking the direct path.
     *
     * Logic:
     * The algorithm marks the Ghost's cell as a WALL (1).
     * BFS cannot pass through (1,1).
     *
     * Pseudo-Code:
     * 1. Set Ghost at (1,1).
     * 2. NextDir = algo.move(game).
     * 3. ASSERT NextDir != RIGHT (Into the ghost).
     * 4. ASSERT NextDir IS IN {UP, DOWN, LEFT}.
     * (Any move that isn't suicide is valid here).
     */
    @Test
    void testGhostAvoidance() {
        MyAlgo algo = new MyAlgo();
        StubGame game = new StubGame();

        int[][] board = new int[5][5];
        board[2][1] = 4; // Food destination

        game.setBoard(board);
        game.setPos("0,1,0");

        // Inject a Ghost blocking the path
        game.setGhosts(new StubGhost[]{ new StubGhost("1,1,0") });

        int move = algo.move(game);

        // CHECK 1: Must not run into the ghost
        assertNotEquals(Game.RIGHT, move, "Pacman must NOT move RIGHT into the ghost");

        // CHECK 2: Must choose a safe direction (Detour or Retreat)
        boolean isSafeMove = (move == Game.UP || move == Game.DOWN || move == Game.LEFT);
        assertTrue(isSafeMove, "Pacman should detour (UP, DOWN) or run away (LEFT)");
    }

    /**
     * TEST 4: Emergency Escape (Panic Mode)
     * Scenario:
     * - Ghost is adjacent at (1,2) [UP].
     * - Pacman at (1,1).
     * - No food nearby.
     *
     * Logic:
     * Distance to ghost < PANIC_DISTANCE (3).
     * Trigger emergencyEscape().
     * Calculate max distance from threat.
     *
     * Pseudo-Code:
     * 1. Ghost is UP (y=2).
     * 2. Pacman is at y=1.
     * 3. Best move to maximize distance is DOWN (y=0).
     * 4. ASSERT NextDir == DOWN.
     */
    @Test
    void testPanicMode() {
        MyAlgo algo = new MyAlgo();
        StubGame game = new StubGame();

        int[][] board = new int[5][5];
        game.setBoard(board);
        game.setPos("1,1,0");

        // Ghost is directly UP
        game.setGhosts(new StubGhost[]{ new StubGhost("1,2,0") });

        int move = algo.move(game);

        // In Matrix coordinates: UP is y++, DOWN is y--.
        // Ghost is at y=2. Pacman at y=1. Escape is y=0 (DOWN).
        assertEquals(Game.DOWN, move, "Pacman should run DOWN away from the ghost");
    }

    // =================================================================
    //                 STUB CLASSES (Mocks for Testing)
    // =================================================================

    /**
     * StubGame:
     * A lightweight implementation of PacmanGame used to feed data
     * to the algorithm without opening a GUI window.
     */
    static class StubGame implements PacmanGame {
        private int[][] board;
        private String pos;
        private GhostCL[] ghosts;

        public StubGame() {
            this.ghosts = new GhostCL[0]; // Default: No ghosts
        }

        public void setBoard(int[][] board) { this.board = board; }
        public void setPos(String pos) { this.pos = pos; }
        public void setGhosts(GhostCL[] ghosts) { this.ghosts = ghosts; }

        @Override public int[][] getGame(int i) { return board; }
        @Override public String getPos(int i) { return pos; }
        @Override public GhostCL[] getGhosts(int i) { return ghosts; }
        @Override public boolean isCyclic() { return true; }

        // Unused methods required by interface
        @Override public String init(int i, String s, boolean b, long l, double v, int i1, int i2) { return ""; }
        @Override public void play() {}
        @Override public Character getKeyChar() { return null; }
        @Override public String move(int i) { return ""; }
        @Override public String end(int i) { return ""; }
        @Override public String getData(int i) { return ""; }
        @Override public int getStatus() { return 0; }
    }

    /**
     * StubGhost:
     * Represents a ghost with a specific position string.
     */
    static class StubGhost implements GhostCL {
        private String pos;

        public StubGhost(String pos) { this.pos = pos; }

        @Override public String getPos(int i) { return pos; }

        // Unused methods
        @Override public int getType() { return 0; }
        @Override public String getInfo() { return ""; }
        @Override public double remainTimeAsEatable(int i) { return 0; }
        @Override public int getStatus() { return 0; }
    }
}