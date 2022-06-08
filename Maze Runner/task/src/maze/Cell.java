package maze;

public class Cell {
    int x;
    int y;
    boolean isVisited;

    Cell(int x, int y) {
        this.x = x;
        this.y = y;
        isVisited = false;
    }

    public void visit() {
        isVisited = true;
    }
}
