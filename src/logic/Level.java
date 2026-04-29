package logic;

/**
 * Represents the difficulty levels of the game, 
 * each with specific grid dimensions and a title for display purposes.
 */
public enum Level {
    EASY(10, 10, "Easy (10x10)"),
    MEDIUM(15, 15, "Medium (15x15)"),
    HARD(20, 20, "Hard (20x20)");

    private final int rows;
    private final int cols;
    private final String title;

    Level(int rows, int cols, String title) {
        this.rows = rows;
        this.cols = cols;
        this.title = title;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public String getTitle() {
        return title;
    }
}