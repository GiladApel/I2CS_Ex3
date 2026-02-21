package assignments.Ex3.core;

import exe.ex3.game.GhostCL;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;
import exe.ex3.game.StdDraw;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Class MyGame (Model & Controller)
 * Manages the game logic, state, and rules.
 * Delegates rendering to GameGraphics class.
 */
public class MyGame implements PacmanGame {

    private GameBoard board;
    private MyPacman pacman;
    private ArrayList<Ghosts> ghosts = new ArrayList<>();

    private int score = 0;
    private int lives = 1;
    private boolean isRunning = true;
    private SoundManager soundManager;
    private PacManAlgo autoPlayer;

    // Scoring & Time
    private int totalCollectibles = 0;
    private long gameStartTimeStamp = 0;
    private long lastBirthTime = 0;
    private final long BIRTH_INTERVAL = 8000;
    private boolean isGameStarted = false;
    private final int MAX_GHOSTS = 6;
    private boolean isWin = false;

    public MyGame() {
        soundManager = new SoundManager();
        this.autoPlayer = MyGameInfo.ALGO;
        System.out.println("Loaded ID: " + MyGameInfo.MY_ID);
    }

    /**
     * Initializes the game board and objects.
     * 1. Reset game state (Lives, Score, Status flags).
     * 2. Load the hardcoded Map String.
     * 3. Initialize Board, Pacman, and the first Ghost.
     * 4. Count total collectibles (Coins/Apples) to determine the victory condition.
     *
     * @return String description of the current level/mode.
     */
    @Override
    public String init(int i, String s, boolean b, long l, double v, int i1, int i2) {
        this.lives = 1;
        this.score = 0;
        this.isGameStarted = false;
        this.isRunning = true;
        this.isWin = false;

        String rawMap =
                "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\n" +
                        "1\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t1\n" +
                        "1\t3\t1\t1\t1\t1\t1\t1\t1\t1\t3\t1\t3\t1\t1\t1\t1\t1\t1\t1\t1\t3\t1\n" +
                        "1\t3\t3\t3\t3\t3\t1\t3\t3\t3\t3\t1\t3\t3\t3\t3\t1\t3\t3\t3\t3\t3\t1\n" +
                        "1\t1\t1\t3\t1\t3\t1\t3\t1\t1\t1\t1\t1\t1\t1\t3\t1\t3\t1\t3\t1\t1\t1\n" +
                        "1\t3\t3\t5\t1\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t1\t5\t3\t3\t1\n" +
                        "1\t3\t1\t1\t1\t3\t1\t1\t1\t1\t3\t1\t3\t1\t1\t1\t1\t3\t1\t1\t1\t3\t1\n" +
                        "1\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t1\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t1\n" +
                        "1\t3\t1\t1\t1\t3\t1\t3\t1\t1\t1\t1\t1\t1\t1\t3\t1\t3\t1\t1\t1\t3\t1\n" +
                        "3\t3\t3\t3\t3\t3\t1\t3\t3\t3\t3\t3\t3\t3\t3\t3\t1\t3\t3\t3\t3\t3\t3\n" +
                        "1\t1\t3\t1\t1\t3\t1\t3\t1\t1\t1\t1\t1\t1\t1\t3\t1\t3\t1\t1\t3\t1\t1\n" +
                        "3\t3\t3\t3\t3\t3\t3\t3\t1\t0\t0\t0\t0\t0\t1\t3\t3\t3\t3\t3\t3\t3\t3\n" +
                        "1\t1\t3\t1\t1\t3\t1\t3\t1\t0\t0\t0\t0\t0\t1\t3\t1\t3\t1\t1\t3\t1\t1\n" +
                        "1\t3\t3\t3\t3\t3\t1\t3\t1\t1\t1\t0\t1\t1\t1\t3\t1\t3\t3\t3\t3\t3\t1\n" +
                        "1\t1\t1\t1\t1\t3\t1\t3\t3\t3\t3\t0\t3\t3\t3\t3\t1\t3\t1\t1\t1\t1\t1\n" +
                        "1\t3\t3\t3\t3\t3\t1\t1\t1\t1\t3\t1\t3\t1\t1\t1\t1\t3\t3\t3\t3\t3\t1\n" +
                        "1\t3\t1\t3\t1\t3\t1\t3\t3\t3\t3\t1\t3\t3\t3\t3\t1\t3\t1\t3\t1\t3\t1\n" +
                        "1\t3\t1\t3\t1\t3\t1\t3\t1\t1\t1\t1\t1\t1\t1\t3\t1\t3\t1\t3\t1\t3\t1\n" +
                        "1\t3\t1\t5\t1\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t1\t5\t1\t3\t1\n" +
                        "1\t3\t1\t3\t1\t1\t1\t1\t1\t1\t3\t1\t3\t1\t1\t1\t1\t1\t1\t3\t1\t3\t1\n" +
                        "1\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t1\t3\t3\t3\t3\t3\t3\t3\t3\t3\t3\t1\n" +
                        "1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1\t1";

        board = new GameBoard(rawMap);
        pacman = new MyPacman(11, 14);
        board.set(11, 14, 2);

        ghosts.clear();
        addGhost(11, 11, true);

        // Count Total Items
        totalCollectibles = 0;
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                int val = board.get(x, y);
                if (val == MyGameInfo.COIN || val == MyGameInfo.APPLE) totalCollectibles++;
            }
        }
        System.out.println(">>> Total Collectibles: " + totalCollectibles);
        System.out.println(">>> Press Space to Start <<<");
        return "Apple Mode";
    }

    /**
     * Helper function to spawn a new ghost on the board.
     * @param x            The X coordinate for the new ghost.
     * @param y            The Y coordinate for the new ghost.
     * @param isStationary True if the ghost should not move initially.
     */
    private void addGhost(int x, int y, boolean isStationary) {
        board.set(x, y, 3);
        ghosts.add(new Ghosts(x, y, ghosts.size(), 0, isStationary));
    }

    /**
     * Main Game Loop Manager.
     * 1. While the game is running and player is alive:
     * 2.   Wait for player to press SPACE to start.
     * 3.   Once started, execute one frame of game logic (gameLoop).
     * 4.   DELEGATE DRAWING to the GameGraphics class.
     * 5.   Sleep briefly to control frame rate (FPS).
     * 6. When loop exits, stop background music.
     */
    @Override
    public void play() {
        while (isRunning && lives > 0) {
            if (!isGameStarted) {
                if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                    isGameStarted = true;
                    gameStartTimeStamp = System.currentTimeMillis();
                    lastBirthTime = System.currentTimeMillis();
                    if (soundManager != null) soundManager.playSound("pacman_beginning.wav", true);
                }
            } else {
                gameLoop();
            }

            //
            GameGraphics.drawFrame(this);
            //

            try { Thread.sleep(75); } catch (Exception e) {}
        }
        if (soundManager != null) soundManager.stopBackground();
    }

    /**
     * Executes one cycle of game logic.
     * 1. Query the autoPlayer algorithm for the next Pacman movement direction.
     * 2. Move Pacman on the board and evaluate the target cell.
     * 3. Check Collisions:
     * - If Food: Increase score. Trigger Victory if all food is collected.
     * - If Ghost: Decrease life. Trigger Game Over or Respawn.
     * 4. Move all Ghosts one step and re-check collisions with Pacman.
     * 5. Periodically spawn a new Ghost if the maximum limit hasn't been reached.
     */
    private void gameLoop() {
        int dir = autoPlayer.move(this);

        board.set(pacman.x, pacman.y, MyGameInfo.EMPTY);
        pacman.move(dir, board);

        int target = board.get(pacman.x, pacman.y);

        if (target == MyGameInfo.COIN || target == MyGameInfo.APPLE) {
            score++;
            if (score >= totalCollectibles) {
                handleVictory();
                return;
            }
        }
        else if (target == MyGameInfo.GHOST) {
            lives--;
            if (lives == 0) System.out.println("GAME OVER! You lost.");
            respawn();
            return;
        }

        board.set(pacman.x, pacman.y, MyGameInfo.PACMAN);

        for (Ghosts g : ghosts) {
            g.moveOneStep(board, pacman.x, pacman.y);

            if (g.x == pacman.x && g.y == pacman.y) {
                lives--;
                if (lives == 0) System.out.println("GAME OVER! Caught by ghost.");
                respawn();
                return;
            }
        }

        if (System.currentTimeMillis() - lastBirthTime > BIRTH_INTERVAL && ghosts.size() < MAX_GHOSTS) {
            addGhost(ghosts.get(0).x, ghosts.get(0).y, false);
            lastBirthTime = System.currentTimeMillis();
        }
    }

    /**
     * Handles the logic when the player wins the game.
     * 1. Stop the main game loop flag.
     * 2. Set the victory flag to true.
     * 3. Calculate total play time.
     * 4. Calculate final score including a time bonus (faster completion = higher bonus).
     */
    private void handleVictory() {
        isRunning = false;
        isWin = true;
        long durationMillis = System.currentTimeMillis() - gameStartTimeStamp;
        double durationSeconds = durationMillis / 1000.0;
        int timeBonus = (int) Math.max(0, 1000 - durationSeconds * 5);
        int finalScore = (score * 10) + timeBonus;
        System.out.println("VICTORY! Score: " + finalScore);
    }

    /**
     * Resets the board state after Pacman is caught by a ghost.
     * PSEUDO-CODE:
     * 1. Stop background music and play death sound.
     * 2. Clear Pacman's current position and restore items under all ghosts.
     * 3. Reset Pacman to the starting coordinates.
     * 4. Clear all active ghosts and spawn a single new stationary ghost at the center.
     * 5. Reset timers and pause the game until the player presses SPACE again.
     */
    private void respawn() {
        if(soundManager != null) {
            soundManager.stopBackground();
            soundManager.playSound("pacman_death.wav", false);
        }
        board.set(pacman.x, pacman.y, 0);
        for (Ghosts g : ghosts) board.set(g.x, g.y, g.itemUnderneath);
        pacman.setPos(11, 14);
        board.set(11, 14, 2);
        ghosts.clear();
        addGhost(11, 11, true);
        lastBirthTime = System.currentTimeMillis();
        isGameStarted = false;
        try { Thread.sleep(MyGameInfo.DT); } catch (Exception e) {}
    }

    // GETTERS FOR THE VIEW

    public GameBoard getBoard() { return board; }
    public MyPacman getPacman() { return pacman; }
    public ArrayList<Ghosts> getGhosts() { return ghosts; }
    public int getScore() { return score; }
    public int getLives() { return lives; }

    // INTERFACE IMPLEMENTATIONS
    @Override public int[][] getGame(int i) { return (board != null) ? board.getGrid() : null; }
    @Override public String getPos(int i) { return pacman.x + "," + pacman.y + ",0"; }
    @Override public GhostCL[] getGhosts(int i) { return ghosts.toArray(new GhostCL[0]); }
    @Override public boolean isCyclic() { return MyGameInfo.CYCLIC_MODE; }
    @Override public Character getKeyChar() { return null; }
    @Override public String move(int i) { return ""; }
    @Override public String end(int i) { return ""; }
    @Override public String getData(int i) { return ""; }
    @Override public int getStatus() { return 0; }
}