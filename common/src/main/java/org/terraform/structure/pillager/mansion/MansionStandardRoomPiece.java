package org.terraform.structure.pillager.mansion;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public abstract class MansionStandardRoomPiece extends JigsawStructurePiece {

	public HashMap<BlockFace, MansionStandardRoomPiece> adjacentPieces = new HashMap<>();
	public HashMap<BlockFace, Boolean> internalWalls = new HashMap<>();
	
	//Mansion standard pieces decorate themselves with a special populator.
	//If it is null, it will not do anything.
	private MansionRoomPopulator roomPopulator = null;
	
    public MansionStandardRoomPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
    }
    
    public void setupInternalAttributes(HashMap<SimpleLocation, JigsawStructurePiece> pieces) {
    	for(BlockFace face:BlockUtils.directBlockFaces) {
    		SimpleLocation otherLoc = this.getRoom().getSimpleLocation().getRelative(face, MansionJigsawBuilder.groundFloorRoomWidth);
    		if(!pieces.containsKey(otherLoc))
    			continue;
    		this.adjacentPieces.put(face, (MansionStandardRoomPiece) pieces.get(otherLoc));
			this.internalWalls.put(face, true);
    	}
    }
    
    public void buildWalls(Random random, PopulatorDataAbstract data) {
    	for(BlockFace face:this.internalWalls.keySet()) {
    		Entry<Wall, Integer> entry = this.getRoom().getWall(data, face, 0);
    		Wall w = entry.getKey();
    		Wall center = null;
    		for(int i = 0; i < entry.getValue(); i++) {
    			w.Pillar(this.getRoom().getHeight(), Material.DARK_OAK_PLANKS);
    			
    			if(i == entry.getValue()/2 && !this.internalWalls.get(face)) {
    				center = w.clone();
    			}
    			
    			w = w.getLeft();
    		}
    		if(center != null) {
    			
    			center.Pillar(5, Material.AIR);
    			center.getLeft().Pillar(5, Material.AIR);
    			center.getRight().Pillar(5, Material.AIR);
    			center.getLeft(2).Pillar(5, Material.AIR);
    			center.getRight(2).Pillar(5, Material.AIR);
    			
    			new SlabBuilder(Material.DARK_OAK_SLAB)
    			.setType(Type.TOP)
    			.apply(center.getRelative(0,5,0));
    			
    			new StairBuilder(Material.DARK_OAK_STAIRS)
    			.setHalf(Half.TOP)
    			.setFacing(BlockUtils.getLeft(center.getDirection()))
    			.apply(center.getRelative(0,4,0).getLeft(2))
    			.setFacing(BlockUtils.getRight(center.getDirection()))
    			.apply(center.getRelative(0,4,0).getRight(2));
    			
    			center.getLeft(3).Pillar(this.getRoom().getHeight(), Material.DARK_OAK_LOG);
    			center.getRight(3).Pillar(this.getRoom().getHeight(), Material.DARK_OAK_LOG);
    		}
    	}
    }
    
    public void decorateInternalRoom(Random random, PopulatorDataAbstract data) {
    	if(roomPopulator != null) {
    		roomPopulator.decorateRoom(data, random);
    	}
    }

    public boolean areInternalWallsFullyBlocked() {
    	for(BlockFace face:this.internalWalls.keySet()) {
    		if(!this.internalWalls.get(face))
    			return false;
    	}
    	return true;
    }
    
    @Override
    public JigsawStructurePiece getInstance(Random rand, int depth) {
    	MansionStandardRoomPiece clone = (MansionStandardRoomPiece) super.getInstance(rand, depth);
        if(clone == null) return null;
    	clone.adjacentPieces = new HashMap<>();
        clone.internalWalls = new HashMap<>();
        return clone;
    }
    
    public Collection<BlockFace> getShuffledInternalWalls(){
    	ArrayList<BlockFace> shuffled = new ArrayList<>();
    	for(BlockFace face:internalWalls.keySet()) shuffled.add(face);
    	Collections.shuffle(shuffled);
    	return shuffled;
    }

	public MansionRoomPopulator getRoomPopulator() {
		return roomPopulator;
	}

	public void setRoomPopulator(MansionRoomPopulator roomPopulator) {
		//TerraformGeneratorPlugin.logger.info("Setting " + roomPopulator.getClass().getSimpleName() + " at " + this.getRoom().getSimpleLocation());
		this.roomPopulator = roomPopulator;
	}
}
