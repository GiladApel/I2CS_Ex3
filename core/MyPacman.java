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

    public MyPacman(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        this.image = "data/p1.png";
    }

    /**
     * Updates position and image based on direction.
     * Does NOT check validity - relies on MyAlgo to provide valid moves.
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