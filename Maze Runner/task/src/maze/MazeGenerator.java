package maze;

import java.io.*;
import java.util.*;

public class MazeGenerator {
    private final Scanner scanner;
    private Maze maze;

    MazeGenerator() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        initialMenu();
        while (maze != null) {
            fullMenu();
        }
    }

    private void initialMenu() {
        // ensures there is a maze object

        while (maze == null) {
            System.out.println("=== Menu ===\n" +
                               "1. Generate a new maze\n" +
                               "2. Load a maze\n" +
                               "0. Exit");

            int choice = Integer.parseInt(scanner.nextLine().trim());
            try {
                switch (choice) {
                    case 1:
                        createMaze();
                        break;
                    case 2:
                        loadMaze();
                        break;
                    case 0:
                        exitProgram();
                        break;
                    default:
                        System.out.println("Incorrect option. Please try again");
                }
            } catch (InputMismatchException e) {
                System.out.println("Incorrect option. Please try again");
            }
        }
    }

    private void fullMenu() {
        System.out.println("=== Menu ===\n" +
                           "1. Generate a new maze\n" +
                           "2. Load a maze\n" +
                           "3. Save the maze\n" +
                           "4. Display the maze\n" +
                           "5. Find the escape\n" +
                           "0. Exit");

        int choice = Integer.parseInt(scanner.nextLine().trim());
        try {
            switch (choice) {
                case 1:
                    createMaze();
                    break;
                case 2:
                    loadMaze();
                    break;
                case 3:
                    saveMaze();
                    break;
                case 4:
                    displayMaze();
                    break;
                case 5:
                    displaySolvedMaze(dfsMazeSolution());
                    break;
                case 0:
                    exitProgram();
                    break;
                default:
                    System.out.println("Incorrect option. Please try again");
            }
        } catch (InputMismatchException e) {
            System.out.println("Incorrect option. Please try again");
        }
    }

    private void createMaze() {
        System.out.println("Please, enter the size of a maze");
        String[] input = scanner.nextLine().trim().split(" ");
        if (input.length == 1) {
            String temp = input[0];
            input = new String[2];
            input[0] = temp;
            input[1] = temp;
        }
        try {
            int height = Integer.parseInt(input[0]);
            int width = Integer.parseInt(input[1]);
            if (width == 0) {
                width = Integer.parseInt(input[0]);
            }
            maze = new Maze(height, width);
            displayMaze();
        } catch (InputMismatchException e) {
            System.out.println("Incorrect option. Please try again");
        }
    }

    private void loadMaze() {
        System.out.println("Enter the file to load:");
        String fileName = scanner.nextLine().trim();
        File mazeFile = new File(fileName);

        if (mazeFile.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(mazeFile))) {
                String[] mazeDimensions = bufferedReader.readLine().trim().split(" ");
                int height = Integer.parseInt(mazeDimensions[0]);
                int width = Integer.parseInt(mazeDimensions[1]);
                int[][] mazeData = new int[height][width];

                for (int i = 0; i < height; i++) {
                    String[] currentLine = bufferedReader.readLine().split(" ");
                    for (int j = 0; j < width; j++) {
                        mazeData[i][j] = Integer.parseInt(currentLine[j]);
                    }
                }
                maze = new Maze(mazeData);

            } catch (IOException e) {
                System.out.println("Cannot load the maze. It has an invalid format");
            }
        } else {
            System.out.printf("The file %s does not exist%n", fileName);
        }
    }

    private void saveMaze() {
        System.out.println("Enter the file name to save as:");
        String fileName = scanner.nextLine().trim();
        File mazeFile = new File(fileName);

        System.out.println();
        if (maze != null) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(mazeFile))) {
                int[][] mazeData = maze.getMaze();
                int height = mazeData.length;
                int width = mazeData[0].length;

                String mazeDimensions = String.format("%d %d", height, width);
                bufferedWriter.write(mazeDimensions);
                for (int[] mazeDatum : mazeData) {
                    StringBuilder currentLine = new StringBuilder();
                    for (int j = 0; j < width; j++) {
                        currentLine.append(mazeDatum[j]).append(" ");
                    }
                    bufferedWriter.append("\n");
                    bufferedWriter.write(currentLine.toString().trim());
                }

            } catch (IOException e) {
                System.out.println("Error: Cannot save or write to file.");
            }
        } else {
            System.out.println("Error: Maze is not created or loaded.");
        }
    }

    private void displayMaze() {
        if (maze != null) {
            int[][] mazeData = maze.getMaze();
            for (int[] arr : mazeData) {
                for (int num : arr) {
                    System.out.print(num == 1 ? "\u2588\u2588" : "  ");
                }
                System.out.println();
            }
        } else {
            System.out.println("Error: Maze is not loaded:");
        }
        System.out.println();
    }

    private void exitProgram() {
        System.out.println("Bye!");
        System.exit(0);
    }

    private Set<int[]> dfsMazeSolution() {
        Set<int[]> dfsToFillIn = new LinkedHashSet<>();
        int[] entry = locateEntrance();
        int[] exit = locateExit();

        Stack<int[]> stack = new Stack<>();
        Set<int[]> visitedLocations = new HashSet<>();
        int[] currentLocation = entry;

        while (!Arrays.equals(currentLocation, exit)) {
            dfsToFillIn.add(currentLocation);

            visitedLocations.add(currentLocation);

            List<int[]> neighbors = getNeighbors(currentLocation, visitedLocations);
            if (setContainsLocation(visitedLocations, currentLocation) || neighbors.size() > 0) {
                stack.push(currentLocation);
            }
            if (neighbors.size() > 0) {
                //attempts to go in one direction the most before next
                //order = down, right, left, up
                currentLocation = neighbors.get(0);
            } else {
                dfsToFillIn.remove(currentLocation);
                currentLocation = stack.pop();
            }
        }

        dfsToFillIn.add(exit);

        return dfsToFillIn;
    }

    private int[] locateEntrance() {
        int[] entrance = new int[2];
        int[][] mazeData = maze.getMaze();

        //test for top wall first;
        for (int i = 0; i < mazeData[0].length; i++) {
            if (mazeData[0][i] == 0) {
                return new int[]{0, i};
            }
        }

        //test for left wall
        for (int i = 1; i < mazeData.length - 1; i++) {
            if (mazeData[i][0] == 0) {
                entrance = new int[]{i, 0};
                break;
            }
        }

        return entrance;
    }

    private int[] locateExit() {
        int[] exit = new int[2];
        int[][] mazeData = maze.getMaze();

        //test for bottom wall first;
        for (int i = 0; i < mazeData[mazeData.length - 1].length; i++) {
            if (mazeData[mazeData.length - 1][i] == 0) {
                return new int[]{mazeData.length - 1, i};
            }
        }

        //test for right wall
        for (int i = 1; i < mazeData.length - 1; i++) {
            if (mazeData[i][mazeData[i].length - 1] == 0) {
                return new int[]{i, mazeData[i].length - 1};
            }
        }

        //test for left wall
        for (int i = 1; i < mazeData.length - 1; i++) {
            if (mazeData[i][0] == 0) {
                exit = new int[]{i, 0};
                break;
            }
        }

        return exit;
    }

    private List<int[]> getNeighbors(int[] location, Set<int[]> visitedLocations) {
        List<int[]> availableNeighbors = new ArrayList<>();
        int[] potentialLocation;
        int[][] mazeData = maze.getMaze();
        int bottomEdge = mazeData.length;
        int rightEdge = mazeData[0].length;

        //down
        if (location[0] + 1 < bottomEdge && mazeData[location[0] + 1][location[1]] == 0) {
            potentialLocation = new int[]{location[0] + 1, location[1]};
            if (setContainsLocation(visitedLocations, potentialLocation)) {
                availableNeighbors.add(potentialLocation);
            }
        }

        //right
        if (location[1] + 1 < rightEdge && mazeData[location[0]][location[1] + 1] == 0) {
            potentialLocation = new int[]{location[0], location[1] + 1};
            if (setContainsLocation(visitedLocations, potentialLocation)) {
                availableNeighbors.add(potentialLocation);
            }
        }

        //left
        if (location[1] - 1 >= 0 && mazeData[location[0]][location[1] - 1] == 0) {
            potentialLocation = new int[]{location[0], location[1] - 1};
            if (setContainsLocation(visitedLocations, potentialLocation)) {
                availableNeighbors.add(potentialLocation);
            }
        }

        //up
        if (location[0] - 1 >= 0 && mazeData[location[0] - 1][location[1]] == 0) {
            potentialLocation = new int[]{location[0] - 1, location[1]};
            if (setContainsLocation(visitedLocations, potentialLocation)) {
                availableNeighbors.add(potentialLocation);
            }
        }

        return availableNeighbors;
    }

    private boolean setContainsLocation(Set<int[]> locations, int[] currentLocation) {
        for (int[] location : locations) {
            if (Arrays.equals(location, currentLocation)) {
                return false;
            }
        }
        return true;
    }

    private void displaySolvedMaze(Set<int[]> dfsList) {
        int[][] solvedMaze = new int[maze.getMaze().length][maze.getMaze()[0].length];
        for (int i = 0; i < solvedMaze.length; i++) {
            solvedMaze[i] = maze.getMaze()[i].clone();
        }

        for (int[] coordinate : dfsList) {
            solvedMaze[coordinate[0]][coordinate[1]] = 7;
        }

        for (int[] array : solvedMaze) {
            for (int num : array) {
                System.out.print(num == 1 ? "\u2588\u2588" : num == 7 ? "//" : "  ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
