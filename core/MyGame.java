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
     * PSEUDO-CODE:
     * 1. Reset game state (Lives, Score).
     * 2. Load Map String.
     * 3. Initialize Board, Pacman, and Ghosts.
     * 4. Count total collectibles for victory condition.
     *
     * @param i Level index (unused).
     * @param s Map string (unused, using internal map).
     * @param b Boolean flag (unused).
     * @param l Long parameter (unused).
     * @param v Double parameter (unused).
     * @param i1 Int parameter (unused).
     * @param i2 Int parameter (unused).
     * @return String description of the level.
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

    private void addGhost(int x, int y, boolean isStationary) {
        board.set(x, y, 3);
        ghosts.add(new Ghosts(x, y, ghosts.size(), 0, isStationary));
    }

    /**
     * Main Game Loop Manager.
     * PSEUDO-CODE:
     * 1. While running & alive:
     * 2.   Check for Start (Space Key).
     * 3.   Execute Game Logic (gameLoop).
     * 4.   DELEGATE DRAWING to GameGraphics.
     * 5.   Wait (FPS Control).
     * 6. End Game: Stop Music & Show Result Screen via GameGraphics.
     */
    @Override
    public void play() {
        while (isRunning && lives > 0) {
            if (!isGameStarted) {
                if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE)) {
                    isGameStarted = true;
                    gameStartTimeStamp = System.currentTimeMillis();
                    lastBirthTime = System.currentTimeMillis();
                    if(soundManager != null) soundManager.playSound("pacman_beginning.wav", true);
                }
            } else {
                gameLoop();
            }

            //
            GameGraphics.drawFrame(this);
            //

            try { Thread.sleep(75); } catch (Exception e) {}
        }
        if(soundManager != null) soundManager.stopBackground();
    }

    /**
     * Executes one cycle of game logic.
     * PSEUDO-CODE:
     * 1. Get Pacman Move (from Algo).
     * 2. Move Pacman & Check Collisions (Coin/Apple/Ghost).
     * 3. Move Ghosts & Check Collisions.
     * 4. Spawn new Ghosts periodically.
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

    private void handleVictory() {
        isRunning = false;
        isWin = true;
        long durationMillis = System.currentTimeMillis() - gameStartTimeStamp;
        double durationSeconds = durationMillis / 1000.0;
        int timeBonus = (int) Math.max(0, 1000 - durationSeconds * 5);
        int finalScore = (score * 10) + timeBonus;
        System.out.println("VICTORY! Score: " + finalScore);
    }

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

    // Getters for the View (GameGraphics)
    public GameBoard getBoard() { return board; }
    public MyPacman getPacman() { return pacman; }
    public ArrayList<Ghosts> getGhosts() { return ghosts; }
    public int getScore() { return score; }
    public int getLives() { return lives; }

    // Interface Methods
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