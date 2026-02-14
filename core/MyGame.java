package assignments.Ex3.core;

import exe.ex3.game.GhostCL;
import exe.ex3.game.PacManAlgo;
import exe.ex3.game.PacmanGame;
import exe.ex3.game.StdDraw;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * PSEUDO-CODE: Class MyGame
 *
 * 1. Define main objects: Board, Pacman, List<Ghosts>.
 * 2. Define game variables: Score, Lives, Status (Running/Stopped).
 * 3. Initialize SoundManager and the AutoPlayer Algorithm.
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
    private final long BIRTH_INTERVAL = 10000;
    private boolean isGameStarted = false;
    private final int MAX_GHOSTS = 6;

    public MyGame() {
        soundManager = new SoundManager();
        this.autoPlayer = MyGameInfo.ALGO;
        System.out.println("Loaded ID: " + MyGameInfo.MY_ID);
    }

    /*
     * PSEUDO-CODE: Function Init
     * 1. Reset lives to 1 and score to 0.
     * 2. Define the Map (String representation of walls, coins, apples).
     * 3. Create the Board object from the map string.
     * 4. Place Pacman at starting position (11, 14).
     * 5. Reset Ghosts list and add the first ghost.
     * 6. Loop through the board to count Total Collectibles (Coins + Apples) for victory condition.
     * 7. Print "Press Space to Start".
     */
    @Override
    public String init(int i, String s, boolean b, long l, double v, int i1, int i2) {
        this.lives = 1;
        this.score = 0;
        this.isGameStarted = false;
        this.isRunning = true;

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
                if (val == 4 || val == 5) totalCollectibles++;
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
     * Function Play
     * 1. Main loop: While game is running AND Player is alive.
     * 2. If Game has NOT started:
     * - Wait for SPACE key.
     * - Set timers and play start sound.
     * 3. Else (Game Started):
     * - Run gameLoop() (Logic).
     * 4. Call draw() to update screen.
     * 5. Sleep for 75ms (FPS control).
     * 6. Stop music when done.
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
            draw();
            try { Thread.sleep(75); } catch (Exception e) {}
        }
        if(soundManager != null) soundManager.stopBackground();
    }

    /**
     * Function GameLoop
     * 1. Get next Move Direction from Algorithm.
     * 2. Update Board: Clear Pacman from old position -> Move Pacman -> Check new cell value.
     * 3. IF (Cell is Coin OR Apple):
     * - Increase Score.
     * - IF Score >= Total -> WIN GAME.
     * 4. ELSE IF (Cell is Ghost):
     * - Decrease Lives.
     * - IF Lives == 0 -> GAME OVER.
     * - Respawn.
     * 5. Set Pacman ID on new board position.
     * 6. Move all Ghosts:
     * - Update ghost position.
     * - Check collision with Pacman again.
     * 7. Spawn new Ghost if 10 seconds passed.
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
     * Function HandleVictory
     * 1. Stop the game loop.
     * 2. Calculate time duration.
     * 3. Calculate Bonus Points (faster time = more points).
     * 4. Print "VICTORY" and stats to console.
     */
    private void handleVictory() {
        isRunning = false;
        long durationMillis = System.currentTimeMillis() - gameStartTimeStamp;
        double durationSeconds = durationMillis / 1000.0;
        int timeBonus = (int) Math.max(0, 1000 - durationSeconds * 5);
        int finalScore = (score * 10) + timeBonus;

        System.out.println("======================================");
        System.out.println("             VICTORY!                 ");
        System.out.println("======================================");
        System.out.println(" Score: " + score);
        System.out.println(" Time:  " + String.format("%.2f", durationSeconds) + "s");
        System.out.println(" Bonus: " + timeBonus);
        System.out.println(" FINAL: " + finalScore);
        System.out.println("======================================");
    }

    /**
     * Function Respawn
     * 1. Play death sound.
     * 2. Clear Pacman and Ghosts from board.
     * 3. Reset positions to start.
     * 4. Reset spawn timer.
     * 5. Pause game briefly.
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

    /**
     * Function Draw
     * 1. Set background color (Black).
     * 2. Call Board.draw() (Walls, Coins).
     * 3. Call Pacman.draw().
     * 4. Call Ghosts.draw().
     * 5. Show frame.
     */
    private void draw() {
        StdDraw.setPenColor(Color.BLACK.getRGB());
        StdDraw.filledSquare(0.5, 0.5, 0.5, 0);
        board.draw();
        pacman.draw(board);
        for (Ghosts g : ghosts) g.draw(board);
        StdDraw.show(0);
    }

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