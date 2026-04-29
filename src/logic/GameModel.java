package logic;

import java.awt.Point;
import java.util.*;

public class GameModel {

    // Basic game settings
    private final int rows;
    private final int cols;
    private final Level currentLevel;

    // Where the player and the exit are currently located
    private int playerX, playerY;
    private int exitX, exitY;

    // These arrays store the map structure (walls, visited areas, etc.)
    private boolean[][] walls;
    private boolean[][] northWall;
    private boolean[][] eastWall;
    private boolean[][] visitedTiles;

    // Lists for our enemies and items
    private final List<Point> viruses = new ArrayList<>();
    private final List<Point> bitcoins = new ArrayList<>();

    // Status variables
    private boolean godMode = false; // This keeps track if the cheat is active
    private int score = 0;
    private int timeSeconds = 0;
    private boolean levelComplete = false;

    // Constants
    private static final int VISION_RADIUS = 4;
    private final Random rand = new Random();

    public GameModel(Level level) {
        this.currentLevel = level;
        this.rows = level.getRows();
        this.cols = level.getCols();
        initLevel();
    }

    // Sets up the map, places walls, and resets the player
    public void initLevel() {
        walls = new boolean[rows][cols];
        visitedTiles = new boolean[rows][cols];
        northWall = new boolean[rows][cols];
        eastWall = new boolean[rows][cols];

        playerX = 0; playerY = 0;
        exitX = cols - 1; exitY = rows - 1;

        score = 0;
        timeSeconds = 0;
        levelComplete = false;
        godMode = false; // Always turn off cheats when a new level starts

        boolean isHard = (currentLevel == Level.HARD);

        // Fill the grid with walls initially
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                northWall[i][j] = true;
                eastWall[i][j] = true;
                walls[i][j] = true;
                // If it's not Hard mode, we show the whole map immediately
                visitedTiles[i][j] = !isHard;
            }
        }

        if (isHard) updateFogOfWar();

        // Create the maze paths
        generateBraidedMaze();

        // Make sure start and end are actually open
        walls[playerY][playerX] = false;
        walls[exitY][exitX] = false;

        spawnEntitiesFairly();
    }

    // This makes sure enemies don't spawn right next to the start
    private void spawnEntitiesFairly() {
        // Calculate the best path to the exit so we don't block it instantly
        List<Point> safePath = findShortestPath(new Point(playerX, playerY), new Point(exitX, exitY));

        viruses.clear();
        bitcoins.clear();

        while (viruses.size() < rows) {
            Point p = findOpenLocation(true);
            // Don't put a virus on the player's immediate path
            if (!viruses.contains(p) && !safePath.contains(p)) {
                viruses.add(p);
            }
        }

        while (bitcoins.size() < cols) {
            Point p = findOpenLocation(false);
            if (!bitcoins.contains(p) && !viruses.contains(p)) {
                bitcoins.add(p);
            }
        }
    }

    public void updateTime() {
        if (!levelComplete) timeSeconds++;
    }

    // Cheat code activation  (the "Suprise")
    public void toggleGodMode() {
        this.godMode = !this.godMode;
        System.out.println("God Mode is now: " + godMode);
    }

    public boolean isGodMode() {
        return godMode;
    }

    // Handles moving the player and checking for walls
    public void movePlayer(int dx, int dy) {
        if (levelComplete) return;

        int nextX = playerX + dx;
        int nextY = playerY + dy;

        // Check if we are inside the map boundaries
        if (nextX >= 0 && nextX < cols && nextY >= 0 && nextY < rows) {
            boolean hitWall = false;

            // Check walls in every direction
            if (nextX > playerX && eastWall[playerY][playerX]) hitWall = true;
            else if (nextX < playerX && eastWall[nextY][nextX]) hitWall = true;
            else if (nextY > playerY && northWall[nextY][nextX]) hitWall = true;
            else if (nextY < playerY && northWall[playerY][playerX]) hitWall = true;

            if (!hitWall) {
                playerX = nextX;
                playerY = nextY;

                // Update the flashlight fog only on Hard mode
                if (currentLevel == Level.HARD) updateFogOfWar();

                checkCollisions();
            }
        }
    }

    // Logic for moving the viruses
    public void moveEnemies() {
        if (levelComplete) return;

        // On Easy mode, viruses act like static mines and don't move
        if (currentLevel == Level.EASY) return;

        // On Medium/Hard, they move every single update (fast!)
        for (Point v : viruses) {
            List<Point> options = getMovableNeighbors(v);

            if (!options.isEmpty()) {
                Point next = options.get(rand.nextInt(options.size()));
                boolean spaceOccupied = false;

                // 1. They are not allowed to step on the Exit
                if (next.x == exitX && next.y == exitY) spaceOccupied = true;

                // 2. They can't camp the spawn point
                if (next.x == 0 && next.y == 0) spaceOccupied = true;

                // 3. IMPORTANT: Safety Zone
                // Enemies cannot enter the area around the exit.
                // This ensures the player can always reach the finish line.
                if (Math.abs(next.x - exitX) + Math.abs(next.y - exitY) < 4) {
                    spaceOccupied = true;
                }

                // 4. Avoid stacking on other viruses
                for (Point other : viruses) {
                    if (other != v && other.equals(next)) spaceOccupied = true;
                }

                if (!spaceOccupied) {
                    v.x = next.x;
                    v.y = next.y;
                }
            }
        }
        checkCollisions();
    }

    // Checks if player hit a coin, a virus, or the exit
    private void checkCollisions() {
        // Remove coins if the player touches them
        bitcoins.removeIf(p -> {
            if (p.x == playerX && p.y == playerY) {
                score++;
                return true;
            }
            return false;
        });

        // Check for virus hits
        for (Point v : viruses) {
            if (v.x == playerX && v.y == playerY) {

                // If God Mode is ON, we ignore the hit and don't die
                if (godMode) {
                    System.out.println("God Mode saved you!");
                } else {
                    // Otherwise, reset the level state
                    respawnAfterDeath();
                }
                break;
            }
        }

        if (playerX == exitX && playerY == exitY) {
            levelComplete = true;
        }
    }

    // Handle death fairly so the player isn't trapped
    private void respawnAfterDeath() {
        playerX = 0;
        playerY = 0;

        // Reset the fog so it looks fresh
        if (currentLevel == Level.HARD) {
            for(int i=0; i<rows; i++) Arrays.fill(visitedTiles[i], false);
            updateFogOfWar();
        }

        // Shuffle all viruses to new random spots
        // This prevents "camping" where you died
        viruses.clear();
        while (viruses.size() < rows) {
            Point p = findOpenLocation(true);
            if (!viruses.contains(p)) {
                viruses.add(p);
            }
        }
    }

    // Calculates which tiles are visible in the fog
    private void updateFogOfWar() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double dist = Math.sqrt(Math.pow(j - playerX, 2) + Math.pow(i - playerY, 2));
                if (dist <= VISION_RADIUS) visitedTiles[i][j] = true;
            }
        }
    }

    // Generates a maze that has loops (braids) so you don't hit dead ends constantly
    private void generateBraidedMaze() {
        boolean[][] visited = new boolean[rows][cols];
        Stack<Point> stack = new Stack<>();
        stack.push(new Point(0, 0));
        visited[0][0] = true;
        walls[0][0] = false;

        // Standard Depth-First Search maze generation
        while (!stack.isEmpty()) {
            Point current = stack.pop();
            List<Point> neighbors = getUnvisitedNeighbors(current, visited);
            if (!neighbors.isEmpty()) {
                stack.push(current);
                Point next = neighbors.get(rand.nextInt(neighbors.size()));
                removeWall(current, next);
                visited[next.y][next.x] = true;
                walls[next.y][next.x] = false;
                stack.push(next);
            }
        }

        // This loop removes 50% of the remaining walls
        // This creates a "Swiss Cheese" effect, making the map very open
        // giving the player multiple ways to escape enemies.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (rand.nextDouble() < 0.50) {
                    if (i > 0 && northWall[i][j]) northWall[i][j] = false;
                    else if (j < cols - 1 && eastWall[i][j]) eastWall[i][j] = false;
                }
            }
        }
    }

    // Helper to find valid moves
    private List<Point> getMovableNeighbors(Point p) {
        List<Point> list = new ArrayList<>();
        int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}};
        for (int[] d : dirs) {
            int nx = p.x + d[0], ny = p.y + d[1];
            if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                boolean wall = false;
                if (nx > p.x && eastWall[p.y][p.x]) wall = true;
                else if (nx < p.x && eastWall[ny][nx]) wall = true;
                else if (ny > p.y && northWall[ny][nx]) wall = true;
                else if (ny < p.y && northWall[p.y][p.x]) wall = true;
                if (!wall) list.add(new Point(nx, ny));
            }
        }
        return list;
    }

    // Helper for maze generation
    private List<Point> getUnvisitedNeighbors(Point p, boolean[][] visited) {
        List<Point> list = new ArrayList<>();
        int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}};
        for(int[] d:dirs) {
            int nx = p.x+d[0], ny = p.y+d[1];
            if(nx>=0 && nx<cols && ny>=0 && ny<rows && !visited[ny][nx]) list.add(new Point(nx,ny));
        }
        return list;
    }

    // Removes the wall between two points
    private void removeWall(Point c, Point n) {
        if(n.x > c.x) eastWall[c.y][c.x]=false;
        else if(n.x < c.x) eastWall[n.y][n.x]=false;
        else if(n.y > c.y) northWall[n.y][n.x]=false;
        else if(n.y < c.y) northWall[c.y][c.x]=false;
    }

    // Finds a random safe spot on the map
    private Point findOpenLocation(boolean isVirus) {
        int x, y;
        do {
            x = rand.nextInt(cols); y = rand.nextInt(rows);
            // Ensure we don't pick the start, end, or the safety zone near the exit
        } while ((x == playerX && y == playerY) ||
                (x == exitX && y == exitY) ||
                (isVirus && Math.abs(x - exitX) + Math.abs(y - exitY) < 4));
        return new Point(x, y);
    }

    // Used to check pathing during spawn
    private List<Point> findShortestPath(Point start, Point end) {
        Queue<Point> q = new LinkedList<>(); q.add(start);
        Map<Point,Point> parent = new HashMap<>();
        Set<Point> vis = new HashSet<>(); vis.add(start);
        while(!q.isEmpty()){
            Point curr = q.poll();
            if(curr.equals(end)) {
                List<Point> path = new ArrayList<>();
                while(curr!=null){ path.add(curr); curr=parent.get(curr); }
                return path;
            }
            for(Point n : getMovableNeighbors(curr)) {
                if(!vis.contains(n)) { vis.add(n); parent.put(n, curr); q.add(n); }
            }
        }
        return new ArrayList<>();
    }

    // Getters for the UI
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getExitX() { return exitX; }
    public int getExitY() { return exitY; }
    public int getScore() { return score; }
    public int getTimeSeconds() { return timeSeconds; }
    public boolean isLevelComplete() { return levelComplete; }

    public boolean[][] getNorthWalls() { return northWall; }
    public boolean[][] getEastWalls() { return eastWall; }
    public boolean[][] getVisitedTiles() { return visitedTiles; }
    public List<Point> getViruses() { return viruses; }
    public List<Point> getBitcoins() { return bitcoins; }
}