package org.terraform.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Stack;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;

public class MazeSpawner {

	private SimpleBlock core; //Maze center
	private int widthX; //Maze x width
	private int widthZ; //Maze z width
	private Random rand;
	private int mazeHeight = 3;
	private int mazePeriod = 1; //Number of blocks a cell's pathways extend 
	private MazeCell center;
	
	/**
	 * A hashmap of raw location 2d x,z arrays to maze cells.
	 */
	private HashMap<SimpleLocation, MazeCell> cellGrid = new HashMap<>();
	
	/**
	 * When using this constructor, be sure that random, core, widthX and widthZ will be set later on.
	 */
	public MazeSpawner() {
		
	}
	
	public MazeSpawner(Random rand,SimpleBlock core, int widthX, int widthZ) {
		this.rand = rand;
		this.core = core;
		this.widthX = widthX;
		this.widthZ = widthZ;
	}
	
	/**
	 * 
	 * @param target
	 * @return a hashmap of unvisited neighbours of that target MazeCell. The hashmap key is the blockface relative to the target MazeCell to the neighbour MazeCell
	 */
	private HashMap<BlockFace, MazeCell> getValidNeighbours(MazeCell target){
		HashMap<BlockFace, MazeCell> neighbours = new HashMap<>();
		
		//Loop NSEW
		for(BlockFace face:BlockUtils.directBlockFaces) {
			MazeCell neighbour = getAdjacentCell(target,face);
			//if(neighbour == null) Bukkit.getLogger().info("Null.");
			//else if(neighbour.hasAllWalls()) Bukkit.getLogger().info("Not all walls");
			if(neighbour != null && neighbour.hasAllWalls()) {
				neighbours.put(face, neighbour);
			}
		}
		
		return neighbours;
	}
	
	/**
	 * Call this function before carveMaze. This populates the object with relevant data to actually create the maze.
	 */
	public void prepareMaze() {

		//Initialise the cellGrid
		int closest = 99999999;
		for(int x = core.getX()-widthX/2; x <= core.getX()+widthX/2;x+=1+mazePeriod)
			for(int z = core.getZ()-widthZ/2; z <= core.getZ()+widthZ/2;z+=1+mazePeriod) {
				MazeCell cell = new MazeCell(x,z);
				cellGrid.put(new SimpleLocation(x,core.getY(),z), cell);
				//Bukkit.getLogger().info("CELL " + x + "," + z);
				int dist = (int) (Math.pow(x, 2) + Math.pow(z, 2));
				if(dist < closest) {
					center = cell;
					closest = dist;
				}
			}
		
		//Bukkit.getLogger().info("CENTER: " + center.x + "," + center.z);
		
		//Total number of cells
		int n = widthX*widthZ;
		
		Stack<MazeCell> cellStack = new Stack<>();
		MazeCell currentCell = center;
			//Total number of visited cells during maze construction
		int nv = 1;
		
		//Knock down walls until all cells have been visited before
		while(nv < n) {
			//Bukkit.getLogger().info("CurrentCell: " + currentCell.x + "," + currentCell.z);
			HashMap<BlockFace, MazeCell> neighbours = this.getValidNeighbours(currentCell);
			
			if(neighbours.size() == 0) {
				//Dead end. Go backwards.
				
				//No items in stack, break out.
				if(cellStack.isEmpty()) break;
				currentCell = cellStack.pop();
				continue;
			}
			
			//choose a random neighbouring cell and move into it.
			@SuppressWarnings("unchecked")
			Entry<BlockFace, MazeCell> entry = (Entry<BlockFace, MazeCell>) neighbours.entrySet().toArray()[rand.nextInt(neighbours.size())];
			currentCell.knockDownWall(entry.getValue(), entry.getKey());
			cellStack.push(currentCell);
			currentCell = entry.getValue();
			nv++;
		}
	}
	
	/**
	 * Carve a maze according to the provided parameters
	 * @param carveInSolid whether or not the maze is to be carved into a solid, or if walls must be created.
	 * @materials if carveInSolid is false, supply a list of materials to use for walls.
	 */
	public void carveMaze(boolean carveInSolid, Material... materials) {
		for(MazeCell cell:cellGrid.values()) {
			Wall unit = new Wall(new SimpleBlock(core.getPopData(), cell.x, core.getY(), cell.z));
			unit.Pillar(mazeHeight, rand, Material.CAVE_AIR);
			ArrayList<BlockFace> wallless = cell.getWalllessFaces();
			
			if(!carveInSolid) {
				unit.getRelative(0,mazeHeight,0).setType(GenUtils.randMaterial(materials));
				unit.getRelative(0,-1,0).setType(GenUtils.randMaterial(materials));
			}
			//Carve pathways and walls if not carving in solid.
			for(int depth = 1; depth <= mazePeriod; depth++) {
				for(BlockFace face:BlockUtils.directBlockFaces) {
					if(wallless.contains(face)) {
						unit.getRelative(face,depth).Pillar(mazeHeight, rand, Material.CAVE_AIR);
						if(!carveInSolid) {
							//Ceiling and floor
							unit.getRelative(face,depth).getRelative(0,mazeHeight,0).setType(GenUtils.randMaterial(materials));
							unit.getRelative(face,depth).getRelative(0,-1,0).setType(GenUtils.randMaterial(materials));
							
							//Hallway Walls
							if(depth > 1)
								for(BlockFace side:BlockUtils.getAdjacentFaces(face))
									unit.getRelative(face,depth).getRelative(side).Pillar(mazeHeight, rand, materials);
						}
					}else //Build blocking walls. Only do it at depth 1 because walls don't need thickness.
						if(!carveInSolid && depth == 1)
							unit.getRelative(face).Pillar(mazeHeight, rand, materials);
				}
				
			}
		}
	}
	
	/**
	 * Gets the adjacent maze cell to the target.
	 * Returns null if there is no maze cell at that location (border)
	 */
	private MazeCell getAdjacentCell(MazeCell target, BlockFace face) {
		int neighbourX = target.x + face.getModX()*(1+mazePeriod);
		int neighbourZ = target.z + face.getModZ()*(1+mazePeriod);
		//Bukkit.getLogger().info("Face: " + face.toString() + " - REL(" + face.getModX() + "," + face.getModZ() + ") === (" + neighbourX + "," + neighbourZ + ")" );
		MazeCell neighbour = cellGrid.get(new SimpleLocation(neighbourX, core.getY(), neighbourZ));
		
		return neighbour;
	}
	
	/**
	 * A maze cell refers to any one cell in the maze which can have 4 sides. 
	 * Each of the 4 sides can or cannot have a wall.
	 * 
	 * In the game, this is denoted by one block with 4 adjacent blocks.
	 * One mazecell must be separated by one mazePeriod from another maze cell.
	 * This is why the above loop in MazeSpawner() increments by 2*mazePeriod.
	 */
	private class MazeCell{
		protected int x,z;
		protected HashMap<BlockFace, Boolean> walls = new HashMap<BlockFace, Boolean>() {{
			put(BlockFace.NORTH, true);
			put(BlockFace.SOUTH, true);
			put(BlockFace.EAST, true);
			put(BlockFace.WEST, true);
		}};
		
		public MazeCell(int x, int z) {
			this.x = x;
			this.z = z;
		}
		
		public ArrayList<BlockFace> getWalllessFaces(){
			ArrayList<BlockFace> faces = new ArrayList<>();
			for(Entry<BlockFace, Boolean> entry:walls.entrySet()) {
				if(!entry.getValue()) faces.add(entry.getKey());
			}
			return faces;
		}
		
		public boolean hasAllWalls() {
			for(Boolean bool:walls.values())
				if(!bool) return false;
			return true;
		}
		
		public void knockDownWall(MazeCell other, BlockFace side) {
			this.walls.put(side,false);
			other.walls.put(side.getOppositeFace(), false);
		}
	}

	public int getMazeHeight() {
		return mazeHeight;
	}

	public void setMazeHeight(int mazeHeight) {
		this.mazeHeight = mazeHeight;
	}

	public int getMazePeriod() {
		return mazePeriod;
	}

	public void setMazePeriod(int mazePeriod) {
		this.mazePeriod = mazePeriod;
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

	
	
}
