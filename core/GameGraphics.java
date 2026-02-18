package assignments.Ex3.core;

import exe.ex3.game.StdDraw;
import java.awt.Color;
import java.util.ArrayList;

/**
 * Class GameGraphics (View Component)
 * Handles all rendering operations to keep the game logic clean.
 */
public class GameGraphics {

    /**
     * Draws the entire game frame: Background, Board, Pacman, Ghosts.
     *
     * @param game The main game object containing the data to be drawn.
     */
    public static void drawFrame(MyGame game) {
        // 1. Clear Screen
        StdDraw.setPenColor(Color.BLACK.getRGB());
        StdDraw.filledSquare(0.5, 0.5, 0.5, 0);

        // 2. Draw Board
        if (game.getBoard() != null) {
            game.getBoard().draw();
        }

        // 3. Draw Pacman
        if (game.getPacman() != null) {
            game.getPacman().draw(game.getBoard());
        }

        // 4. Draw Ghosts
        ArrayList<Ghosts> ghosts = game.getGhosts();
        if (ghosts != null) {
            for (Ghosts g : ghosts) {
                g.draw(game.getBoard());
            }
        }

        // 5. Show
        StdDraw.show(0);
    }

}