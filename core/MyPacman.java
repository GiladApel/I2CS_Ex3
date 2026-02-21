package assignments.Ex3.core;

import exe.ex3.game.Game;
import exe.ex3.game.StdDraw;
import java.awt.Color;

/**
 * MyPacman - A simple entity class.
 * Logic is handled by MyAlgo, so this class only handles position and drawing.
 */
public class MyPacman {

    public int x, y;
    private String image = "data/p1.png";

    /**
     * Constructor to initialize Pacman at a specific location.
     * * @param startX The starting X coordinate.
     * @param startY The starting Y coordinate.
     */
    public MyPacman(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    /**
     * Hard-resets Pacman's position and visual state.
     * PSEUDO-CODE:
     * 1. Update internal X and Y coordinates.
     * 2. Reset the image to the default "facing right" state.
     * * @param x The new X coordinate.
     * @param y The new Y coordinate.
     */
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        this.image = "data/p1.png";
    }

    /**
     * Updates position and image based on direction.
     * Does NOT check validity - relies on MyAlgo to provide valid moves.
     * PSEUDO-CODE:
     * 1. Check the given direction (UP, DOWN, LEFT, RIGHT).
     * 2. Modify X or Y accordingly and change the image to face that direction.
     * 3. Apply cyclic wrapping using the board dimensions to ensure Pacman stays on the grid.
     * * @param dir   The movement direction constant from the Game class.
     * @param board The game board used for cyclic boundary checks.
     */
    public void move(int dir, GameBoard board) {
        if (dir == Game.RIGHT) { x++; image = "data/p1.png"; }
        else if (dir == Game.LEFT) { x--; image = "data/p_left.png"; }
        else if (dir == Game.UP) { y++; image = "data/p_up.png"; }
        else if (dir == Game.DOWN) { y--; image = "data/p_down.png"; }

        // Cyclic Wrap
        x = board.wrap(x, board.getWidth());
        y = board.wrap(y, board.getHeight());
    }

    /**
     * Renders Pacman on the screen.
     * PSEUDO-CODE:
     * 1. Calculate the relative scale based on the board's dimensions.
     * 2. Compute the exact center pixel coordinates (xp, yp) for Pacman's current cell.
     * 3. Attempt to draw the current directional image.
     * 4. If the image is missing, catch the exception and draw a fallback yellow circle.
     * * @param board The game board used for scale and coordinate calculations.
     */
    public void draw(GameBoard board) {
        double s = Math.min(1.0/board.getWidth(), 1.0/board.getHeight());
        double xp = (x + 0.5) * (1.0/board.getWidth());
        double yp = (y + 0.5) * (1.0/board.getHeight());
        try {
            StdDraw.picture(xp, yp, image, s, s);
        } catch (Exception e) {
            StdDraw.setPenColor(Color.YELLOW.getRGB());
            // 4 Arguments for safety
            StdDraw.filledCircle(xp, yp, s/2, 0);
        }
    }
}