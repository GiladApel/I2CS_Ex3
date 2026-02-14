package assignments.Ex3.core;
import assignments.Ex3.algo.MyAlgo;
import exe.ex3.game.PacManAlgo;
import assignments.Ex3.utils.ManualAlgo;

/**
 * MyGameInfo
 * Central configuration class for the Pacman game.
 * Stores constants for ID, Game Speed, Map Elements, and Algorithm selection.
 */
public class MyGameInfo {

    public static final String MY_ID = "323830091";
    public static final int DT = 100; // Game speed
    public static final boolean CYCLIC_MODE = true;
    private static PacManAlgo _manualAlgo = new ManualAlgo();
    public static final PacManAlgo ALGO = new MyAlgo();
    //public static final PacManAlgo ALGO = _manualAlgo;
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int PACMAN = 2;
    public static final int GHOST = 3;
    public static final int COIN = 3;
    public static final int APPLE = 5;
    public static final int TOTAL_LIVES = 1;
}