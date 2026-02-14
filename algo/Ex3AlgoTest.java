package assignments.Ex3.algo;

import exe.ex3.game.Game;
import exe.ex3.game.GhostCL;
import exe.ex3.game.PacmanGame;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Ex3AlgoTest - Unit Tests for the Target Locking Algorithm.
 * Focuses on ensuring the Pacman commits to a target and doesn't oscillate.
 */
class Ex3AlgoTest {

    /**
     * TEST 1: Basic Pathfinding
     * -------------------------
     * Scenario:
     * - Pacman at (1,1).
     * - Food at (3,1) [RIGHT].
     *
     * Pseudo-Code:
     * 1. Initialize Board 5x5.
     * 2. Place Food at (3,1).
     * 3. Set Pacman at "1,1,0".
     * 4. Call move().
     * 5. Assert result is RIGHT.
     */
    @Test
    void testBasicMovement() {
        Ex3Algo algo = new Ex3Algo();
        StubGame game = new StubGame();

        int[][] board = new int[5][5];
        board[3][1] = 4; // Food

        game.setBoard(board);
        game.setPos("1,1,0");

        int move = algo.move(game);
        assertEquals(Game.RIGHT, move, "Should move RIGHT towards food");
    }

    /**
     * TEST 2: Target Locking (The Critical Test)
     * ------------------------------------------
     * Scenario:
     * - Step 1: Pacman at (1,1). Target is Food A at (4,1) [RIGHT].
     * - Step 2: Pacman moves to (2,1).
     * - Step 3: SUDDENLY, Food B appears at (2,2) [UP] (Very Close!).
     *
     * Logic:
     * - Without locking: Pacman would switch to Food B (Distance 1) instead of A (Distance 2).
     * - With locking: Pacman remembers Food A and ignores Food B.
     *
     * Pseudo-Code:
     * 1. Init Algo.
     * 2. Set Board with Food A at (4,1).
     * 3. Move() -> Returns RIGHT. (Algo now locks on (4,1)).
     * 4. Update Game: Pacman moves to (2,1). Add Food B at (2,2).
     * 5. Move() AGAIN using the SAME algo instance.
     * 6. Assert result is STILL RIGHT (Towards A), NOT UP (Towards B).
     */
    @Test
    void testTargetLocking() {
        Ex3Algo algo = new Ex3Algo(); // We reuse this object to test memory
        StubGame game = new StubGame();
        int[][] board = new int[5][5];

        // --- Step 1: Setup Original Target ---
        board[4][1] = 4; // Target A (Far)
        game.setBoard(board);
        game.setPos("1,1,0");

        // Action: First move
        int move1 = algo.move(game);
        assertEquals(Game.RIGHT, move1, "First move should be towards target (4,1)");

        // --- Step 2: Simulate Progress & Distraction ---
        // Move Pacman one step right to (2,1)
        game.setPos("2,1,0");

        // Add a distraction! Food B at (2,2) is only 1 step away (UP).
        // Target A at (4,1) is 2 steps away (RIGHT).
        board[2][2] = 4;

        // Action: Second move
        int move2 = algo.move(game);

        // Assert: Must stick to original plan (RIGHT), ignoring the closer food (UP).
        assertEquals(Game.RIGHT, move2, "Pacman failed to lock target! He got distracted by closer food.");
    }

    /**
     * TEST 3: Ghost Avoidance (Virtual Walls)
     * ---------------------------------------
     * Scenario:
     * - Pacman at (1,1).
     * - Food at (3,1).
     * - Ghost at (2,1) blocking the path.
     *
     * Logic:
     * Virtual Wall should block (2,1).
     * Pacman must detour (UP, DOWN) or Retreat (LEFT).
     *
     * Pseudo-Code:
     * 1. Place Ghost at (2,1).
     * 2. Call move().
     * 3. Assert move is NOT RIGHT (into ghost).
     */
    @Test
    void testGhostAvoidance() {
        Ex3Algo algo = new Ex3Algo();
        StubGame game = new StubGame();

        int[][] board = new int[5][5];
        board[3][1] = 4; // Destination

        game.setBoard(board);
        game.setPos("1,1,0");

        // Ghost blocking the path at (2,1)
        game.setGhosts(new StubGhost[]{ new StubGhost("2,1,0") });

        int move = algo.move(game);

        assertNotEquals(Game.RIGHT, move, "Should NOT move into ghost");

        boolean isSafe = (move == Game.UP || move == Game.DOWN || move == Game.LEFT);
        assertTrue(isSafe, "Should detour (UP/DOWN) or retreat (LEFT)");
    }

    /**
     * TEST 4: Emergency Escape
     * ------------------------
     * Scenario:
     * - Ghost at (1,2) [UP] very close.
     * - Pacman at (1,1).
     *
     * Logic:
     * Must run AWAY from ghost.
     * Ghost is at y=2. Pacman at y=1.
     * Safest spot is y=0 (DOWN).
     */
    @Test
    void testEmergencyEscape() {
        Ex3Algo algo = new Ex3Algo();
        StubGame game = new StubGame();

        int[][] board = new int[5][5];
        game.setBoard(board);
        game.setPos("1,1,0");

        // Ghost above (UP)
        game.setGhosts(new StubGhost[]{ new StubGhost("1,2,0") });

        int move = algo.move(game);

        // Run DOWN (y--)
        assertEquals(Game.DOWN, move, "Should run away from ghost");
    }

    // =================================================================
    //                 STUB CLASSES (Mocks for Testing)
    // =================================================================

    static class StubGame implements PacmanGame {
        private int[][] board;
        private String pos;
        private GhostCL[] ghosts;

        public StubGame() { this.ghosts = new GhostCL[0]; }
        public void setBoard(int[][] board) { this.board = board; }
        public void setPos(String pos) { this.pos = pos; }
        public void setGhosts(GhostCL[] ghosts) { this.ghosts = ghosts; }

        @Override public int[][] getGame(int i) { return board; }
        @Override public String getPos(int i) { return pos; }
        @Override public GhostCL[] getGhosts(int i) { return ghosts; }
        @Override public boolean isCyclic() { return false; }

        // Unused
        @Override public String init(int i, String s, boolean b, long l, double v, int i1, int i2) { return ""; }
        @Override public void play() {}
        @Override public Character getKeyChar() { return null; }
        @Override public String move(int i) { return ""; }
        @Override public String end(int i) { return ""; }
        @Override public String getData(int i) { return ""; }
        @Override public int getStatus() { return 0; }
    }

    static class StubGhost implements GhostCL {
        private String pos;
        public StubGhost(String pos) { this.pos = pos; }
        @Override public String getPos(int i) { return pos; }
        @Override public int getType() { return 0; }
        @Override public String getInfo() { return ""; }
        @Override public double remainTimeAsEatable(int i) { return 0; }
        @Override public int getStatus() { return 0; }
    }
}