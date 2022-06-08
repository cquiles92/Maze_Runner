package maze;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Maze {
    private final int MAZE_HEIGHT;
    private final int MAZE_WIDTH;
    private final int[][] maze;

    private final int cellStructureHeight;
    private final int cellStructureLength;
    private final Cell[][] cellStructure;

    Maze(int height, int width) {
        MAZE_HEIGHT = height;
        MAZE_WIDTH = width;
        maze = new int[MAZE_HEIGHT][MAZE_WIDTH];

        cellStructureHeight = MAZE_HEIGHT % 2 == 0 ? (MAZE_HEIGHT - 1) / 2 : MAZE_HEIGHT / 2;
        cellStructureLength = MAZE_WIDTH % 2 == 0 ? (MAZE_WIDTH - 1) / 2 : MAZE_WIDTH / 2;

        cellStructure = new Cell[cellStructureHeight][cellStructureLength];

        setMaze();
        setCellStructure();
        generateMaze();
    }


    Maze(int[][] loadedMaze) {
        maze = loadedMaze;
        MAZE_HEIGHT = maze.length;
        MAZE_WIDTH = maze[0].length;

        cellStructureHeight = MAZE_HEIGHT % 2 == 0 ? (MAZE_HEIGHT - 1) / 2 : MAZE_HEIGHT / 2;
        cellStructureLength = MAZE_WIDTH % 2 == 0 ? (MAZE_WIDTH - 1) / 2 : MAZE_WIDTH / 2;

        cellStructure = new Cell[cellStructureHeight][cellStructureLength];

        setCellStructure();
    }


    private void setMaze() {
        for (int i = 0; i < MAZE_HEIGHT; i++) {
            for (int j = 0; j < MAZE_WIDTH; j++) {
                if (i % 2 == 0 || j % 2 == 0 ||
                    (MAZE_WIDTH % 2 == 0 && j == MAZE_WIDTH - 1)
                    || (MAZE_HEIGHT % 2 == 0 && i == MAZE_HEIGHT - 1)) {
                    maze[i][j] = 1;
                }
            }
        }
    }


    private void setCellStructure() {
        for (int i = 0; i < cellStructure.length; i++) {
            for (int j = 0; j < cellStructure[i].length; j++) {
                Cell currentCell = new Cell((2 * i + 1), (2 * j + 1));
                cellStructure[i][j] = currentCell;
            }
        }
    }

    private List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int row = cell.x / 2;
        int column = cell.y / 2;

        Cell neighbor;

        //above cell
        if (row - 1 >= 0) {
            neighbor = cellStructure[row - 1][column];
            addToNeighbors(neighbors, neighbor);
        }

        //below cell
        if (row + 1 < cellStructure.length) {
            neighbor = cellStructure[row + 1][column];
            addToNeighbors(neighbors, neighbor);
        }

        //left cell
        if (column - 1 >= 0) {
            neighbor = cellStructure[row][column - 1];
            addToNeighbors(neighbors, neighbor);
        }

        //right cell
        if (column + 1 < cellStructure[row].length) {
            neighbor = cellStructure[row][column + 1];
            addToNeighbors(neighbors, neighbor);
        }
        return neighbors;
    }

    private void addToNeighbors(List<Cell> neighbors, Cell neighbor) {
        if (!neighbor.isVisited) {
            neighbors.add(neighbor);
        }
    }

    private void generateMaze() {
        Cell currentCell = cellStructure[0][0];
        Set<Cell> visitedCells = new HashSet<>();

        Stack<Cell> cellStack = new Stack<>();

        while (visitedCells.size() < cellStructureLength * cellStructureHeight) {
            //visit current cell and add it to the list
            currentCell.visit();
            visitedCells.add(currentCell);
            List<Cell> neighbors = getNeighbors(currentCell);


            //if there's nowhere left to go, backtrack to the last cell with more than 1 neighbor
            if (neighbors.size() == 0) {
                currentCell = cellStack.pop();
            }
            //if there's more than 1 direction to go. between 2 or 3 realistically on a 2-d grid without diagonals,
            //and you cannot go backwards.
            else {
                Cell nextCell;
                if (neighbors.size() > 1) {
                    //backtrack marker back here sometime in the future once I am stuck
                    cellStack.push(currentCell);
                    //pick a random spot
                    nextCell = neighbors.get(ThreadLocalRandom.current().nextInt(0, neighbors.size()));
                } else {
                    //pick default last spot available
                    nextCell = neighbors.get(0);
                }
                //erase the wall
                eraseWall(currentCell, nextCell);

                //move to next cell
                currentCell = nextCell;

            }
            //create entrance and exit
        }
        generateEntries();
    }

    private void eraseWall(Cell currentCell, Cell nextCell) {
        int xWall = currentCell.x == nextCell.x ? currentCell.x : currentCell.x > nextCell.x ? currentCell.x - 1 : currentCell.x + 1;
        int yWall = currentCell.y == nextCell.y ? currentCell.y : currentCell.y > nextCell.y ? currentCell.y - 1 : currentCell.y + 1;
        maze[xWall][yWall] = 0;
    }

    private void generateEntries() {
        //random between left and right or top and bottom

        if (MAZE_WIDTH % 2 == 0 && MAZE_HEIGHT % 2 == 0) {
            //need to grab the left and top walls
            leftWallEntry();
            topWallEntry();

        } else if (MAZE_WIDTH % 2 == 0) {
            //right wall has more than 1. Use top and bottom
            topWallEntry();
            bottomWallEntry();

        } else if (MAZE_HEIGHT % 2 == 0) {
            //bottom wall has more than 1. Use left and right
            leftWallEntry();
            rightWallEntry();

        } else {
            int sides = ThreadLocalRandom.current().nextInt();

            if (sides % 2 == 0) {
                //top and bottom
                //0 and regular array length -1
                topWallEntry();
                bottomWallEntry();

            } else {
                //left and right
                leftWallEntry();
                rightWallEntry();
            }
        }
    }

    private void leftWallEntry() {
        int random = ThreadLocalRandom.current().nextInt(0, MAZE_HEIGHT);

        while (maze[random][1] == 1) {
            random = ThreadLocalRandom.current().nextInt(0, MAZE_HEIGHT);
        }
        maze[random][0] = 0;
    }

    private void rightWallEntry() {
        int random = ThreadLocalRandom.current().nextInt(0, MAZE_HEIGHT);

        while (maze[random][MAZE_WIDTH - 2] == 1) {
            random = ThreadLocalRandom.current().nextInt(0, MAZE_HEIGHT);
        }
        maze[random][MAZE_WIDTH - 1] = 0;
    }

    private void topWallEntry() {
        int random = ThreadLocalRandom.current().nextInt(0, MAZE_WIDTH);

        while (maze[1][random] == 1) {
            random = ThreadLocalRandom.current().nextInt(0, MAZE_WIDTH);
        }
        maze[0][random] = 0;
    }

    private void bottomWallEntry() {
        int random = ThreadLocalRandom.current().nextInt(0, MAZE_WIDTH);

        while (maze[maze.length - 2][random] == 1) {
            random = ThreadLocalRandom.current().nextInt(0, MAZE_WIDTH);
        }
        maze[MAZE_HEIGHT - 1][random] = 0;
    }

    protected int[][] getMaze() {
        return maze;
    }
}
