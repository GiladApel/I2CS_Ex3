package assignments.Ex3.core;

import exe.ex3.game.StdDraw;
import java.awt.Color;

public class GameBoard {
    private int[][] data;
    private int rows, cols;

    public GameBoard(String mapStr) {
        parseMap(mapStr);
    }

    private void parseMap(String mapStr) {
        String[] lines = mapStr.split("\n");
        rows = lines.length;
        String[] colsArr = lines[0].trim().split("\\s+");
        cols = colsArr.length;

        data = new int[cols][rows];

        for (int i = 0; i < rows; i++) {
            String[] values = lines[i].trim().split("\\s+");
            for (int x = 0; x < cols; x++) {
                if (x >= values.length) continue;
                try {
                    int val = Integer.parseInt(values[x]);
                    // 1=קיר, 3=מטבע, 5=תפוח
                    if (val == 1) data[x][i] = 1;
                    else if (val == 3) data[x][i] = 4;
                    else if (val == 5) data[x][i] = 5;
                    else data[x][i] = 0;
                } catch (Exception e) {
                    data[x][i] = 0;
                }
            }
        }
    }

    public void draw() {
        double rx = 1.0 / cols;
        double ry = 1.0 / rows;
        double s = Math.min(rx, ry);

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                double xp = (x + 0.5) * rx;
                double yp = (y + 0.5) * ry;
                int t = data[x][y];

                if (t == 1) {
                    // קיר כחול
                    drawItem(xp, yp, "data/wall.png", s, Color.BLUE, true);
                }
                else if (t == 4) {
                    // מטבע ורוד
                    drawItem(xp, yp, "data/coin.png", s * 0.6, Color.PINK, false);
                }
                else if (t == 5) {
                    // *** תפוח ***
                    try {
                        // מנסה לצייר תמונה של תפוח
                        StdDraw.picture(xp, yp, "data/APPLE.jpg", s * 0.8, s * 0.8);
                    } catch (Exception e) {
                        // גיבוי: עיגול ירוק (חובה 4 פרמטרים אצלך!)
                        StdDraw.setPenColor(Color.GREEN.getRGB());
                        StdDraw.filledCircle(xp, yp, s * 0.4, 0);
                    }
                }
            }
        }
    }

    private void drawItem(double x, double y, String path, double size, Color c, boolean isSquare) {
        try {
            StdDraw.picture(x, y, path, size, size);
        } catch (Exception e) {
            StdDraw.setPenColor(c.getRGB());
            // *** תיקון קריטי: אך ורק 4 פרמטרים! ***
            if (isSquare) {
                StdDraw.filledSquare(x, y, size / 2, 0);
            } else {
                StdDraw.filledCircle(x, y, size / 2, 0);
            }
        }
    }

    public int get(int x, int y) { return data[x][y]; }
    public void set(int x, int y, int val) { data[x][y] = val; }
    public int getWidth() { return cols; }
    public int getHeight() { return rows; }
    public int[][] getGrid() { return data; }
    public int wrap(int val, int max) { return (val < 0) ? max - 1 : (val >= max) ? 0 : val; }
    public boolean isWall(int x, int y) {
        if(x<0||x>=cols||y<0||y>=rows) return true;
        return data[x][y] == 1;
    }
}