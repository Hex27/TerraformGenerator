package org.terraform.structure.room.jigsaw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class JigsawBuilder {
	protected int widthX;
	protected int widthZ;
	protected int[] lowerBounds = new int[2];
	protected int[] upperBounds = new int[2];
	protected int maxDepth = 5; //The amount of pieces before an end piece is forced.
	protected int chanceToAddNewPiece = 60;
	protected int pieceWidth = 5;
	protected SimpleBlock core;
	protected JigsawStructurePiece center;
	protected Stack<JigsawStructurePiece> traverseStack = new Stack<>();
	//protected ArrayList<JigsawStructurePiece> pieces = new ArrayList<>();
	//protected ArrayList<SimpleLocation> usedLocations = new ArrayList<>();
	protected HashMap<SimpleLocation, JigsawStructurePiece> pieces = new HashMap<>();
	protected ArrayList<JigsawStructurePiece> overlapperPieces = new ArrayList<>();
	protected JigsawStructurePiece[] pieceRegistry;
	
	public JigsawBuilder(int widthX, int widthZ, PopulatorDataAbstract data, int x, int y, int z) {
		super();
		this.widthX = widthX;
		this.widthZ = widthZ;
		this.core = new SimpleBlock(data,x,y,z);
		//this.pieceRegistry = pieceRegistry;
		this.lowerBounds[0] = x-widthX/2;
		this.lowerBounds[1] = z-widthZ/2;
		this.upperBounds[0] = x+widthX/2;
		this.upperBounds[1] = z+widthZ/2;
	}
	
	public void generate(Random random) {
		
		center = getPiece(pieceRegistry,JigsawType.STANDARD, random).getInstance(random, 0);
		center.getRoom().setX(core.getX());
		center.getRoom().setY(core.getY());
		center.getRoom().setZ(core.getZ());
		pieces.put(new SimpleLocation(core.getX(),core.getY(),core.getZ()),center);
		traverseStack.push(center);
		while(!areAllPiecesCovered())
			if(!traverseAndPopulatePieces(random)) break;
		
	}
	
	int traversalIndex = 0;
	public boolean traverseAndPopulatePieces(Random random) {
		if(traverseStack.size() == 0) {
			TerraformGeneratorPlugin.logger.info("Jigsaw stack size empty!");
			return false;
		}
		JigsawStructurePiece current = traverseStack.peek();
		//TerraformGeneratorPlugin.logger.info("Traversal Index " + traversalIndex + ", on: " + current.toString());
		traversalIndex++;
		if(traversalIndex > 100) {
			TerraformGeneratorPlugin.logger.info("Infinite loop detected! Breaking.");
			return false;
		}
		if(current.hasUnpopulatedDirections()) {
			BlockFace dir = current.getNextUnpopulatedBlockFace();
			JigsawStructurePiece toAdd = null;
			int toAddX = current.getRoom().getX() + pieceWidth*dir.getModX();
			int toAddY = current.getRoom().getY() + pieceWidth*dir.getModY();
			int toAddZ = current.getRoom().getZ() + pieceWidth*dir.getModZ();
			SimpleLocation newLoc = new SimpleLocation(toAddX, toAddY, toAddZ);
			if(!pieces.containsKey(newLoc)) {
				if(dir == BlockFace.UP) {
					//Place an upper connector piece
					toAdd = getRelativePiece(current,JigsawType.UPPERCONNECTOR,random)
							.getInstance(random, current.getDepth()+1);
					toAdd.setRotation(BlockUtils.getDirectBlockFace(random));
					toAdd.setElevation(current.getElevation()+1);
				}
				else
				{
					//Depth is too high (prevent recursion)
					if(current.getDepth() >= maxDepth) { //Place an end
						toAdd = getRelativePiece(current,JigsawType.END,random)
								.getInstance(random, current.getDepth()+1);
						toAdd.setRotation(dir);
					}else { 
						if(toAddX < lowerBounds[0] || toAddX > upperBounds[0]
						|| toAddZ < lowerBounds[1] || toAddZ > upperBounds[1]) { 
							//If outside of bounding box, just force an end piece.
							toAdd = getRelativePiece(current,JigsawType.END,random)
									.getInstance(random, current.getDepth()+1);
							toAdd.setRotation(dir);
						}else if(!GenUtils.chance(random, chanceToAddNewPiece,  100)){
							//Failed chance to add. Force wall.
							toAdd = getRelativePiece(current,JigsawType.END,random)
									.getInstance(random, current.getDepth()+1);
							toAdd.setRotation(dir);
						}else {
							//Place a standard piece
							toAdd = getRelativePiece(current,JigsawType.STANDARD,random)
									.getInstance(random, current.getDepth()+1);
						}
					}
				}
				toAdd.getRoom().setX(toAddX);
				toAdd.getRoom().setY(toAddY);
				toAdd.getRoom().setZ(toAddZ);
				
				current.setPopulated(dir);
				toAdd.setPopulated(dir.getOppositeFace());
				
				
					//TerraformGeneratorPlugin.logger.info("New location: " + new SimpleLocation(toAddX, toAddY, toAddZ));
					
					
					if(toAdd.getType() == JigsawType.END)
						overlapperPieces.add(toAdd);
					
					if(toAdd.getType() != JigsawType.END) {
						pieces.put(newLoc,toAdd);
						traverseStack.push(toAdd);
					}
			}else { //The location exists in the map already
				JigsawStructurePiece other = pieces.get(newLoc);
				current.setPopulated(dir);
				other.setPopulated(dir.getOppositeFace());
			}
			
		}else {
			traverseStack.pop(); //Go to the previous piece to continue populating
		}
		return true;
	}
	
	public boolean areAllPiecesCovered() {
		for(JigsawStructurePiece piece: pieces.values()) {
			//TerraformGeneratorPlugin.logger.info("Checking " + piece.toString());
			if(piece.hasUnpopulatedDirections())
				return false;
		}
		return true;
	}

	public void build(Random random) {
		for(JigsawStructurePiece piece:pieces.values()) {
			
			
			TerraformGeneratorPlugin.logger.info("Populating at " + piece.getClass().getSimpleName() + "::" + piece.getRoom().getX() + "," + piece.getRoom().getZ() + "," + piece.getRotation());
			piece.build(core.getPopData(),random);
		}
		
		//Overlapper pieces are stuff like walls and entrances.
		for(JigsawStructurePiece piece:overlapperPieces) {
			//Don't place overlapper objects where rooms have been placed.
			if(pieces.containsKey(new SimpleLocation(piece.getRoom().getX(),piece.getRoom().getY(),piece.getRoom().getZ())))
				continue;
			
			TerraformGeneratorPlugin.logger.info("Populating at " + piece.getClass().getSimpleName() + "::" + piece.getRoom().getX() + "," + piece.getRoom().getZ() + "," + piece.getRotation());
			piece.build(core.getPopData(),random);
		}
	}
	
	public JigsawStructurePiece getPiece(JigsawStructurePiece[] registry, JigsawType type, Random rand) {
		ArrayList<JigsawStructurePiece> validPieces = new ArrayList<>();
		for(JigsawStructurePiece piece:pieceRegistry) {
			if(piece.getType() == type)
				validPieces.add(piece);
		}
		if(validPieces.size() == 0) {
			TerraformGeneratorPlugin.logger.error("Tried to query jigsaw type that doesn't exist: " + type.toString());
			return null;
		}
		
		return validPieces.get(rand.nextInt(validPieces.size()));
	}

	public JigsawStructurePiece getRelativePiece(JigsawStructurePiece current, JigsawType type, Random random) {
		if(current.getAllowedPieces() != null && current.getAllowedPieces().length == 0) {
			return getPiece(pieceRegistry,type,random);
		}else {
			return getPiece(current.getAllowedPieces(),type,random);
		}
	}
}
