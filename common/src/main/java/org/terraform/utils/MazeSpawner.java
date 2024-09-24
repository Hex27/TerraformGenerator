package org.terraform.utils;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;

import java.util.*;
import java.util.Map.Entry;

public class MazeSpawner {
    public final @NotNull List<PathPopulatorData> pathPopDatas = new ArrayList<>();
    /**
     * A hashmap of raw location 2d x,z arrays to maze cells.
     * SIMPLE LOCATION HERE REFERS TO MAZECELL RELATIVE COORDINATES,
     * NOT REAL IN-WORLD COORDS.
     */
    private final Map<SimpleLocation, MazeCell> cellGrid = new HashMap<>();
    private SimpleBlock core; // Maze center
    private int widthX = -1; // Maze x width
    private int widthZ = -1; // Maze z width
    private Random rand;
    private int mazeHeight = 3;
    private MazeCell center;
    private int mazePathWidth = 1;
    private int mazePeriod = 1;
    private PathPopulatorAbstract pathPop;
    private boolean covered = false;

    /**
     * When using this constructor, be sure that random, core, widthX and widthZ will be set later on.
     */
    public MazeSpawner() {

    }

    public MazeSpawner(Random rand, SimpleBlock core, int widthX, int widthZ) {
        this.rand = rand;
        this.core = core;
        this.widthX = widthX;
        this.widthZ = widthZ;
    }

    /**
     * @return a hashmap of unvisited neighbours of that target MazeCell. The hashmap key is the blockface relative to the target MazeCell to the neighbour MazeCell
     */
    private @NotNull Map<BlockFace, MazeCell> getValidNeighbours(@NotNull MazeCell target) {
        Map<BlockFace, MazeCell> neighbours = new EnumMap<>(BlockFace.class);

        // Loop NSEW
        for (BlockFace face : BlockUtils.directBlockFaces) {
            MazeCell neighbour = getAdjacentCell(target, face);
            if (neighbour != null && neighbour.hasAllWalls()) {
                neighbours.put(face, neighbour);
            }
        }

        return neighbours;
    }

    /**
     * Call this function before carveMaze.
     * This populates the object with relevant data to actually create the maze.
     */
    public void prepareMaze() {


        // Initialise the cellGrid
        int mazeCellsWidthX = widthX / (mazePathWidth + mazePeriod);
        int mazeCellsWidthZ = widthZ / (mazePathWidth + mazePeriod);
        for (int x = -mazeCellsWidthX / 2; x <= mazeCellsWidthX / 2; x++) {
            for (int z = -mazeCellsWidthZ / 2; z <= mazeCellsWidthZ / 2; z++) {
                MazeCell cell = new MazeCell(x, z);
                cellGrid.put(new SimpleLocation(x, core.getY(), z), cell);
                // Bukkit.getLogger().info("CELL " + x + "," + z);
                if (x == 0 && z == 0) {
                    center = cell;
                }
            }
        }

        // Bukkit.getLogger().info("CENTER: " + center.x + "," + center.z);

        // Total number of cells
        int n = mazeCellsWidthX * mazeCellsWidthZ;

        Stack<MazeCell> cellStack = new Stack<>();
        MazeCell currentCell = center;
        // Total number of visited cells during maze construction
        int nv = 1;

        // Knock down walls until all cells have been visited before
        while (nv < n) {
            // Bukkit.getLogger().info("CurrentCell: " + currentCell.x + "," + currentCell.z);
            Map<BlockFace, MazeCell> neighbours = this.getValidNeighbours(currentCell);

            if (neighbours.isEmpty()) {
                // Dead end. Go backwards.

                // No items in stack, break out.
                if (cellStack.isEmpty()) {
                    break;
                }
                currentCell = cellStack.pop();
                continue;
            }

            // choose a random neighbouring cell and move into it.
            @SuppressWarnings("unchecked") Entry<BlockFace, MazeCell> entry = (Entry<BlockFace, MazeCell>) neighbours.entrySet()
                                                                                                                     .toArray()[rand.nextInt(
                    neighbours.size())];

            currentCell.knockDownWall(entry.getValue(), entry.getKey());
            cellStack.push(currentCell);
            currentCell = entry.getValue();
            nv++;
        }
        // Bukkit.getLogger().info(nv+"/"+n);
    }


    /**
     * Carve a maze according to the provided parameters
     *
     * @param carveInSolid whether or not the maze is to be carved into a solid, or if walls must be created.
     * @materials if carveInSolid is false, supply a list of materials to use for walls.
     */
    public void carveMaze(boolean carveInSolid, Material... materials) {
        int cellRadius = (mazePathWidth - 1) / 2;

        for (MazeCell cell : cellGrid.values()) {
            int realWorldX = cell.x * (mazePathWidth + mazePeriod);
            int realWorldZ = cell.z * (mazePathWidth + mazePeriod);
            Wall cellCore = new Wall(core.getRelative(realWorldX, 0, realWorldZ));
            pathPopDatas.add(new PathPopulatorData(cellCore.getDown().get(), BlockFace.UP, mazePathWidth, false));

            // Carve 1 cell
            for (int nx = -cellRadius; nx <= cellRadius; nx++) {
                for (int nz = -cellRadius; nz <= cellRadius; nz++) {
                    cellCore.getRelative(nx, 0, nz).Pillar(mazeHeight, rand, Material.CAVE_AIR);
                    if (covered) {
                        cellCore.getRelative(nx, mazeHeight, nz).setType(GenUtils.randChoice(materials));
                        cellCore.getRelative(nx, -1, nz).setType(GenUtils.randChoice(materials));
                    }
                }
            }


            Set<BlockFace> wallllllllless = cell.getWalllessFaces();

            // Connect
            for (BlockFace dir : BlockUtils.directBlockFaces) {
                Wall startPoint = new Wall(core.getRelative(realWorldX, 0, realWorldZ), dir).getRelative(
                        dir,
                        cellRadius + 1
                );

                // Carve Pathway
                if (wallllllllless.contains(dir)) {
                    for (int i = 0; i < Math.ceil(((float) this.mazePeriod) / 2f); i++) {
                        pathPopDatas.add(new PathPopulatorData(startPoint.getDown().get(), dir, mazePathWidth, false));

                        startPoint.Pillar(mazeHeight, rand, Material.CAVE_AIR);
                        if (covered) {
                            startPoint.getRelative(0, mazeHeight, 0).setType(GenUtils.randChoice(materials));
                            startPoint.getDown().setType(GenUtils.randChoice(materials));
                        }
                        for (int w = 1; w <= cellRadius; w++) {
                            startPoint.getLeft(w).Pillar(mazeHeight, rand, Material.CAVE_AIR);
                            startPoint.getRight(w).Pillar(mazeHeight, rand, Material.CAVE_AIR);
                            if (covered) {
                                startPoint.getLeft(w).getDown().setType(GenUtils.randChoice(materials));
                                startPoint.getRight(w).getDown().setType(GenUtils.randChoice(materials));
                                startPoint.getLeft(w)
                                          .getRelative(0, mazeHeight, 0)
                                          .setType(GenUtils.randChoice(materials));
                                startPoint.getRight(w)
                                          .getRelative(0, mazeHeight, 0)
                                          .setType(GenUtils.randChoice(materials));
                            }
                        }
                        startPoint.getLeft(cellRadius + 1).Pillar(mazeHeight, rand, materials);
                        startPoint.getRight(cellRadius + 1).Pillar(mazeHeight, rand, materials);
                        startPoint = startPoint.getRelative(dir);
                    }
                }
                else { // Set Wall
                    startPoint.Pillar(mazeHeight, rand, materials);
                    for (int w = 1; w <= cellRadius; w++) {
                        startPoint.getLeft(w).Pillar(mazeHeight, rand, materials);
                        startPoint.getRight(w).Pillar(mazeHeight, rand, materials);
                    }
                }
            }
        }
    }

    /**
     * Gets the adjacent maze cell to the target.
     * Returns null if there is no maze cell at that location (border)
     */
    private MazeCell getAdjacentCell(@NotNull MazeCell target, @NotNull BlockFace face) {
        int neighbourX = target.x + face.getModX();
        int neighbourZ = target.z + face.getModZ();
        // Bukkit.getLogger().info("Face: " + face.toString() + " - REL(" + face.getModX() + "," + face.getModZ() + ") === (" + neighbourX + "," + neighbourZ + ")" );
        return cellGrid.get(new SimpleLocation(neighbourX, core.getY(), neighbourZ));
    }

    public int getMazeHeight() {
        return mazeHeight;
    }

    public void setMazeHeight(int mazeHeight) {
        this.mazeHeight = mazeHeight;
    }

    public int getWidthX() {
        return widthX;
    }

    public void setWidthX(int widthX) {
        this.widthX = widthX;
    }

    public int getWidthZ() {
        return widthZ;
    }

    public void setWidthZ(int widthZ) {
        this.widthZ = widthZ;
    }

    public void setWidth(int width) {
        this.widthX = width;
        this.widthZ = width;
    }

    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }

    public SimpleBlock getCore() {
        return core;
    }

    public void setCore(SimpleBlock core) {
        this.core = core;
    }

    public int getMazePathWidth() {
        return mazePathWidth;
    }

    public void setMazePathWidth(int mazePathWidth) {
        if (mazePathWidth % 2 == 0) {
            throw new IllegalArgumentException("Maze Path Width must be odd!");
        }
        this.mazePathWidth = mazePathWidth;
    }

    public int getMazePeriod() {
        return mazePeriod;
    }

    public void setMazePeriod(int mazePeriod) {
        this.mazePeriod = mazePeriod;
    }

    public PathPopulatorAbstract getPathPop() {
        return pathPop;
    }

    public void setPathPop(PathPopulatorAbstract pathPop) {
        this.pathPop = pathPop;
    }

    public boolean isCovered() {
        return covered;
    }

    public void setCovered(boolean covered) {
        this.covered = covered;
    }

    /**
     * A maze cell refers to any one cell in the maze which can have 4 sides.
     * Each of the 4 sides can or cannot have a wall.
     * <p>
     * In the game, this is denoted by a square of side mazePathWidth.
     */
    private static class MazeCell {
        protected final int x;
        protected final int z;
        protected final @NotNull Map<BlockFace, Boolean> walls = new EnumMap<>(BlockFace.class);

        public MazeCell(int x, int z) {
            this.x = x;
            this.z = z;

            walls.put(BlockFace.NORTH, true);
            walls.put(BlockFace.SOUTH, true);
            walls.put(BlockFace.EAST, true);
            walls.put(BlockFace.WEST, true);
        }

        public @NotNull Set<BlockFace> getWalllessFaces() {
            Set<BlockFace> faces = EnumSet.noneOf(BlockFace.class);
            for (Entry<BlockFace, Boolean> entry : walls.entrySet()) {
                if (!entry.getValue()) {
                    faces.add(entry.getKey());
                }
            }
            return faces;
        }

        public boolean hasAllWalls() {
            for (Boolean bool : walls.values()) {
                if (!bool) {
                    return false;
                }
            }
            return true;
        }

        public void knockDownWall(@NotNull MazeCell other, @NotNull BlockFace side) {
            this.walls.put(side, false);
            other.walls.put(side.getOppositeFace(), false);
        }
    }


}
