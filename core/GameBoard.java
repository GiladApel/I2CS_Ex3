package assignments.Ex3.core;

import exe.ex3.game.StdDraw;
import java.awt.Color;

/**
 * Class GameBoard
 * 1. Purpose: Manages the logical grid (2D array) of the game.
 * 2. Data: Stores integers representing Walls, Coins, Apples, or Empty space.
 * 3. Functionality:
 * - Parses a String map into the 2D array.
 * - Draws the static elements of the board (Walls, Food) to the screen.
 */
public class GameBoard {
    private int[][] data;
    private int rows, cols;

    /**
     * Constructor for the GameBoard.
     * * @param mapStr The raw string representation of the map grid.
     */
    public GameBoard(String mapStr) {
        parseMap(mapStr);
    }

    /**
     * Function ParseMap
     * 1. Split the raw map string into lines (rows).
     * 2. Determine the number of columns based on the first line.
     * 3. Initialize the 2D 'data' array with [Cols][Rows].
     * 4. Nested Loop (Iterate through rows and columns):
     * - Read the number at the current position.
     * - Convert the raw number to Game Constants:
     * (1 -> WALL, 3 -> COIN, 5 -> APPLE, Else -> EMPTY).
     * - Handle parsing errors (default to 0).
     * * @param mapStr The raw string representation of the map to parse.
     */
    private void parseMap(String mapStr) {
        String[] lines = mapStr.split("\n");
        rows = lines.length;
        // Determine columns from the first line
        String[] colsArr = lines[0].trim().split("\\s+");
        cols = colsArr.length;

        data = new int[cols][rows];

        for (int i = 0; i < rows; i++) {
            String[] values = lines[i].trim().split("\\s+");
            for (int x = 0; x < cols; x++) {
                if (x >= values.length) continue;
                try {
                    int val = Integer.parseInt(values[x]);

                    if (val == 1) {
                        data[x][i] = MyGameInfo.WALL;
                    } else if (val == 3) {
                        data[x][i] = MyGameInfo.COIN;
                    } else if (val == 5) {
                        data[x][i] = MyGameInfo.APPLE;
                    } else {
                        data[x][i] = MyGameInfo.EMPTY;
                    }
                } catch (Exception e) {
                    data[x][i] = MyGameInfo.EMPTY;
                }
            }
        }
    }

    /**
     * Function Draw
     * 1. Calculate the scale (width/height) of a single cell relative to the window.
     * 2. Loop through every cell in the grid (x, y):
     * 3. Calculate the center pixel coordinates (xp, yp) for the current cell.
     * 4. Check the cell Type:
     * - IF Wall: Draw using 'drawItem' (Blue, Wall Image).
     * - IF Coin: Draw using 'drawItem' (Pink, Coin Image).
     * - IF Apple: Draw using 'drawItem' (Green/Red, Apple Image).
     * Note: Pacman and Ghosts are dynamic and drawn in their own classes.
     */
    public void draw() {
        double rx = 1.0 / cols;
        double ry = 1.0 / rows;
        double s = Math.min(rx, ry);

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                double xp = (x + 0.5) * rx;
                double yp = (y + 0.5) * ry;
                int type = data[x][y]; // Current cell value

                if (type == MyGameInfo.WALL) {
                    drawItem(xp, yp, "data/wall.png", s, Color.BLUE, true);
                } else if (type == MyGameInfo.COIN) {
                    drawItem(xp, yp, "data/coin.png", s * 0.6, Color.PINK, false);
                } else if (type == MyGameInfo.APPLE) {
                    try {
                        StdDraw.picture(xp, yp, "data/APPLE.jpg", s * 0.8, s * 0.8);
                    } catch (Exception e) {
                        StdDraw.setPenColor(Color.GREEN.getRGB());
                        StdDraw.filledCircle(xp, yp, s * 0.4, 0);
                    }
                }
            }
        }
    }

    /**
     * Function DrawItem
     * 1. Try to draw the image from the "data" folder.
     * 2. IF image loading fails (Exception caught):
     * - Set the pen color to the fallback color.
     * - Draw a geometric shape (Square for walls, Circle for items).
     * * @param x        The exact x-coordinate (in window scale) to draw the item.
     *
     * @param y        The exact y-coordinate (in window scale) to draw the item.
     * @param path     The relative file path to the image in the data folder.
     * @param size     The calculated size/scale for the item.
     * @param c        The fallback color to use if the image file is missing.
     * @param isSquare True if the fallback shape should be a square, False for a circle.
     */
    private void drawItem(double x, double y, String path, double size, Color c, boolean isSquare) {
        try {
            StdDraw.picture(x, y, path, size, size);
        } catch (Exception e) {
            StdDraw.setPenColor(c.getRGB());
            // Fallback rendering
            if (isSquare) {
                StdDraw.filledSquare(x, y, size / 2, 0);
            } else {
                StdDraw.filledCircle(x, y, size / 2, 0);
            }
        }
    }

    //
    // GETTERS, SETTERS & HELPERS
    //

    /**
     * Retrieves the item value at a specific cell.
     *
     * @param x The column index.
     * @param y The row index.
     * @return The integer value representing what is currently in that cell.
     */
    public int get(int x, int y) {
        return data[x][y];
    }

    /**
     * Updates the item value at a specific cell.
     *
     * @param x   The column index.
     * @param y   The row index.
     * @param val The new integer value to set in that cell.
     */
    public void set(int x, int y, int val) {
        data[x][y] = val;
    }

    /**
     * @return The total number of columns in the grid.
     */
    public int getWidth() {
        return cols;
    }

    /**
     * @return The total number of rows in the grid.
     */
    public int getHeight() {
        return rows;
    }

    /**
     * @return The entire 2D array representing the game board.
     */
    public int[][] getGrid() {
        return data;
    }

    /**
     * Helper to handle cyclic borders if necessary.
     * Ensures coordinates wrap around screen edges.
     * * @param val The current coordinate value (x or y).
     *
     * @param max The maximum limit for that coordinate (width or height).
     * @return The corrected, wrapped coordinate.
     */
    public int wrap(int val, int max) {
        return (val < 0) ? max - 1 : (val >= max) ? 0 : val;
    }
}

