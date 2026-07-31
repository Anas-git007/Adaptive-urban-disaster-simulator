package simulation;

import java.util.*;


public class CityMap {

   
    private static final int ROAD     = 0;
    private static final int BUILDING = 1;
    private static final int SAFEZONE = 2;

    private final int       width;
    private final int       height;
    private final int[][]   grid;
    private final boolean[][] blocked;


    public CityMap(int width, int height) {
        this.width   = width;
        this.height  = height;
        this.grid    = new int[width][height];
        this.blocked = new boolean[width][height];
        buildCity();
    }

 

    private void buildCity() {
         
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                grid[x][y] = BUILDING;

      
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (x % 4 == 0 || y % 4 == 0)
                    grid[x][y] = ROAD;

         
        for (int x = 2; x < width; x += 8)
            for (int y = 2; y < height; y += 8)
                if (x < width && y < height)
                    grid[x][y] = SAFEZONE;
    }
 
    public int[] bfsNextStep(int startX, int startY, int targetX, int targetY) {
        if (startX == targetX && startY == targetY) return null;

        boolean[][] visited = new boolean[width][height];
        int[][][]   parent  = new int[width][height][2];

        for (int[][] row : parent)
            for (int[] cell : row)
                Arrays.fill(cell, -1);

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        visited[startX][startY] = true;

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int cx = cur[0], cy = cur[1];
            if (cx == targetX && cy == targetY)
                return traceFirstStep(parent, startX, startY, targetX, targetY);

            for (int[] d : dirs) {
                int nx = cx + d[0];
                int ny = cy + d[1];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                if (visited[nx][ny] || !isPassable(nx, ny))          continue;
                visited[nx][ny]    = true;
                parent[nx][ny][0]  = cx;
                parent[nx][ny][1]  = cy;
                queue.add(new int[]{nx, ny});
            }
        }
        return null;    
    } 
    
    private int[] traceFirstStep(int[][][] parent, int sx, int sy, int tx, int ty) {
        int cx = tx, cy = ty;
        while (parent[cx][cy][0] != sx || parent[cx][cy][1] != sy) {
            int px = parent[cx][cy][0];
            int py = parent[cx][cy][1];
            cx = px;
            cy = py;
        }
        return new int[]{cx, cy};
    }
 
    public boolean isRoad(int x, int y) {
        return inBounds(x, y)
            && (grid[x][y] == ROAD || grid[x][y] == SAFEZONE);
    }
 
    public boolean isPassable(int x, int y) {
        return inBounds(x, y) && isRoad(x, y) && !blocked[x][y];
    }
 
    public boolean isBlocked(int x, int y) {
        return !inBounds(x, y) || blocked[x][y];
    }

 
    public int getCellType(int x, int y) {
        return inBounds(x, y) ? grid[x][y] : BUILDING;
    }
 
    public static int getCellTypeConstant(String name) {
        return switch (name.toUpperCase()) {
            case "ROAD"     -> ROAD;
            case "BUILDING" -> BUILDING;
            case "SAFEZONE" -> SAFEZONE;
            default -> throw new IllegalArgumentException(
                    "Unknown cell type: " + name);
        };
    }
 
    public void blockCell(int x, int y)   { if (inBounds(x, y)) blocked[x][y] = true; }
    public void unblockCell(int x, int y) { if (inBounds(x, y)) blocked[x][y] = false; }
 
    public void clearBlocks() {
        for (boolean[] row : blocked) Arrays.fill(row, false);
    }

    public int[] randomRoadCell() {
        Random rnd = new Random();
        for (int i = 0; i < 300; i++) {
            int x = rnd.nextInt(width);
            int y = rnd.nextInt(height);
            if (grid[x][y] == ROAD) return new int[]{x, y};
        }
        return new int[]{0, 0};
    }

  
    private boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
 
    public int getWidth()  { return width; }
    public int getHeight() { return height; }
}