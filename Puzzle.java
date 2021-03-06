package pentominoes;

import java.util.*;

/**
 * Class for representing pentomino puzzles.
 * @author Aaron Anderson 8649682
 * @author James Strathern 5028791
 * @author Josh Whitney 4442561
 * @author Ryan Collins 5955140
 */
public class Puzzle implements Comparable<Puzzle> {

    int[][] grid;
    BoardTile[][][] boardTiles;
    ArrayList<PentominoShape> availableShapes = new ArrayList<PentominoShape>();

    public Puzzle (String[][] strGrid) {
        int[][] intGrid = new int[strGrid.length][];
        int highestNum = 0;
        for (int i = 0; i < strGrid.length; i++) {
            intGrid[i] = new int[strGrid[i].length];
            for (int j = 0; j < intGrid[i].length; j++) {
                String str = strGrid[i][j];
                if (str.equals("*")) {
                    intGrid[i][j] = 0;
                } else if (str.equals(".")) {
                    intGrid[i][j] = 1;
                } else {
                    intGrid[i][j] = Integer.parseInt(str);
                }
                if (intGrid[i][j] > highestNum) {
                    highestNum = intGrid[i][j];
                }
            }
        }
        grid = intGrid;
        boardTiles = new BoardTile[highestNum][grid.length][grid[0].length];
    }

    public static Puzzle findSolution(Puzzle p){
        int layer = p.getNumLayers();
        if (layer == 0) {
            return p;
        }
        
        Coordinate target = null;
        for(int x = 0; x < p.getGrid().length && target == null; x++){
            for(int y = 0; y < p.getGrid()[0].length && target == null; y++){
                BoardTile tile = p.getBoard()[layer - 1][x][y];
                if(tile == null && p.getGrid()[x][y] == layer){
                    target = new Coordinate(x, y);
                }
            }
        }
        if(target == null){
            return null;
        }
        int x = target.getX();
        int y = target.getY();
        ArrayList<Puzzle> puzzleQueue = new ArrayList<Puzzle>();
        for(PentominoShape shape : p.getShapes()){
            for(Pentomino pent : PuzzleSolver.getUniqueForms(shape)){
                Puzzle attemptPuzzle = p.getClone();
                if(attemptPuzzle.addPiece(pent, x, y, layer, p.getShapes(), target)){
                    puzzleQueue.add(attemptPuzzle);
                }
                
                
            }
        }
        puzzleQueue.sort(null);
        for (Puzzle nextPuzzle : puzzleQueue) {
            Puzzle solution = findSolution(nextPuzzle.getClone());
            if(solution != null){
                return solution;
            }
        }
        return null;
    }

    @Override
    public int compareTo (Puzzle otherPuzzle) {
        int layer = getNumLayers();
        int thisNum = 0;
        int otherNum = 0;
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                if (grid[x][y] == layer) thisNum++;
                if (otherPuzzle.getGrid()[x][y] == layer) otherNum++;
            }
        }
        return thisNum - otherNum;
    }

    
    public Puzzle(int[][] cloneGrid, BoardTile[][][] cloneTiles, ArrayList<PentominoShape> shapes) {
        grid = cloneGrid;
        boardTiles = cloneTiles;
        availableShapes = shapes;
    }

    public void setShapes(ArrayList<PentominoShape> shapes){
        availableShapes = shapes;
    }

    public int[][] getGrid() {
        int[][] gridClone = new int[grid.length][grid[0].length];
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                gridClone[x][y] = grid[x][y];
            }
        }
        return gridClone;
    }

    public ArrayList<PentominoShape> getShapes() {
        return new ArrayList<PentominoShape>(availableShapes);
    }
    
    public Puzzle getClone () {
        BoardTile[][][] tiles = new BoardTile[boardTiles.length][boardTiles[0].length][boardTiles[0][0].length];
        int[][] gridCopy = new int[grid.length][grid[0].length];
        for (int l = 0; l < tiles.length; l++) {
            for (int x = 0; x < tiles[0].length; x++) {
                for (int y = 0; y < tiles[0][0].length; y++) {
                    if (boardTiles[l][x][y] != null) {
                        tiles[l][x][y] = boardTiles[l][x][y].copy();
                    }
                    gridCopy[x][y] = grid[x][y];
                }
            }
        }
        Puzzle clonePuzzle = new Puzzle(gridCopy, tiles, new ArrayList<PentominoShape>(availableShapes));
        return clonePuzzle;
    }

    public void printBoard () {
        for (int l = 0; l < boardTiles.length; l++) {
            for (int x = 0; x < boardTiles[l].length; x++) {
                for (int y = 0; y < boardTiles[l][x].length; y++) {
                    if (boardTiles[l][x][y] == null) {
                        System.out.print(".");
                    } else {
                        System.out.print(boardTiles[l][x][y]);
                    }
                }
                System.out.println();
            }
            System.out.println();
        }
    }
    
    public boolean finishedAtLayer (int layer) {
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                if (grid[x][y] == layer) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getNumLayers () {
        int maxValue = grid[0][0];
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                if (grid[x][y] >= maxValue) {
                    maxValue = grid[x][y];
                }
            }
        }
        return maxValue;
    }

    // return true iff successful
    public boolean addPiece (Pentomino pentomino, int x, int y, int layer, ArrayList<PentominoShape> remainingShapes, Coordinate target) {
        Coordinate[] blocks = pentomino.determineBlocks(x, y);
        boolean violation = isViolation(blocks, layer, remainingShapes, target);
        if (!violation) {
            for (Coordinate coord : blocks) {
                grid[coord.getX()][coord.getY()]--;
                boardTiles[layer - 1][coord.getX()][coord.getY()] = new BoardTile(x, y, layer, pentomino.getShape());
            }
            availableShapes.remove(pentomino.getShape());
            return true;
        } else {
            return false;
        }
    }

    // return true if violation occurs
    public boolean isViolation (Coordinate[] blocks, int layer, ArrayList<PentominoShape> remainingShapes, Coordinate target) {
        boolean foundTarget = false;
        for (Coordinate coord : blocks) {
            int x = coord.getX();
            int y = coord.getY();
            if (coord.equals(target)){
                foundTarget = true;
            }
            if (x < 0 || y < 0 || x >= grid.length || y >= grid[0].length) {
                return true;
            }
            if (boardTiles[layer - 1][x][y] != null) {
                if (boardTiles[layer - 1][x][y].getPentomino() != null) {
                    return true;
                }
            }
            if (grid[x][y] == 0) {
                return true;
            }
            
        }
        if(!foundTarget){
            return true;
        }
        int[][] gridClone = getGrid();
        for (Coordinate coord : blocks) {
            gridClone[coord.getX()][coord.getY()]--; 
        }
        if (!checkLayer1(gridClone, remainingShapes)) {
            return true;
        }
        return false;
    }

    public boolean outOfBounds (Coordinate coord) {
        int x = coord.getX();
        int y = coord.getY();
        if (x < 0 || y < 0 || x >= grid.length || y >= grid[0].length) {
            return true;
        }
        return false;
    }

    // return true if layer 1 is still solvable
    public boolean checkLayer1 (int[][] testGrid, ArrayList<PentominoShape> remainingShapes) {
        ArrayList<Coordinate> validated = new ArrayList<Coordinate>();
        for (int x = 0; x < testGrid.length; x++) {
            for (int y = 0; y < testGrid[x].length; y++) {
                int count = 0;
                Coordinate newCoord = new Coordinate(x, y);
                if (!validated.contains(newCoord) && testGrid[x][y] != 0) {
                    ArrayList<Coordinate> queue = new ArrayList<Coordinate>();
                    ArrayList<Coordinate> visited = new ArrayList<Coordinate>();
                    queue.add(new Coordinate(x,y));
                    
                    while (queue.size() > 0) {
                        
                        count += 1;
                        
                        Coordinate coord = queue.remove(0);
                        
                        visited.add(coord);
                        Coordinate[] neighbours = new Coordinate[]{
                            new Coordinate(coord.getX() + 1, coord.getY()),
                            new Coordinate(coord.getX() - 1, coord.getY()),
                            new Coordinate(coord.getX(), coord.getY() + 1),
                            new Coordinate(coord.getX(), coord.getY() - 1)
                        };
                        for (Coordinate neighbour : neighbours) {
                            if (!outOfBounds(neighbour) && !visited.contains(neighbour) && !queue.contains(neighbour)) {
                                if (testGrid[neighbour.getX()][neighbour.getY()] != 0) {
                                    queue.add(neighbour);
                                }
                            }
                        }
                    }
                    if (count < 5) {
                        return false;
                    }
                    validated.addAll(visited);
                    if (count == 5) {
                        boolean foundShape = false;
                        for (PentominoShape shape : remainingShapes) {
                            for (Pentomino pentomino : PuzzleSolver.getUniqueForms(shape)) {
                                if (Coordinate.compareArrangement(visited, pentomino.getOffsets())) {
                                    foundShape = true;
                                }
                            }
                        }
                        if (!foundShape) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    

    public BoardTile[][][] getBoard() {
        return boardTiles;
    }

    public String toString () {
        String returnString = "";
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                returnString += grid[x][y] + " ";
            }
            returnString += "\n";
        }
        return returnString;
    }
    
}
