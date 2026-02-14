package assignments.Ex3.core;


/**
 * RunGame - The entry point for the Pacman game.
 * Run this class to start the application.
 */
public class RunGame {
    public static void main(String[] args) {
        // Create and start the game engine
        MyGame game = new MyGame();

        // Initialize with default settings
        game.init(0, "", true, 0, 0, 0, 0);

        // Start the game loop
        game.play();
    }
}