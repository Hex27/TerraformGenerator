package org.terraform.structure.ancientcity;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.GenUtils;
import org.terraform.utils.StairwayBuilder;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Map.Entry;
import java.util.Random;

public abstract class AncientCityAbstractRoomPopulator extends RoomPopulatorAbstract {

	protected RoomLayoutGenerator gen;
    public AncientCityAbstractRoomPopulator(RoomLayoutGenerator gen, Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        this.gen = gen;
    }

    protected CubeRoom effectiveRoom = null;
    
    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	
    	int shrunkenWidth = GenUtils.randInt(this.rand, 2,4);
    	
    	//This variable is named depression, but can also represent elevation.
    	int depression = shrunkenWidth;
    	
    	//Sometimes the room will be higher than the path's Y.
    	if(rand.nextBoolean()) {
    		depression = depression*-1;
    	}
    	
    	this.effectiveRoom = new CubeRoom(
    			room.getWidthX() - shrunkenWidth*2 - 1,
    			room.getWidthZ() - shrunkenWidth*2 - 1,
    			room.getHeight(),
    			room.getX(), room.getY() + depression, room.getZ());
    	
        //Clear out space for the room
    	effectiveRoom.fillRoom(data, Material.CAVE_AIR);
    	
    	//Room flooring
        int[] lowerCorner = effectiveRoom.getLowerCorner(0);
        int[] upperCorner = effectiveRoom.getUpperCorner(0);
        int y = effectiveRoom.getY();
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                SimpleBlock b = new SimpleBlock(data, x, y, z);
                b.lsetType(AncientCityUtils.deepslateBricks);
                
                //every few intervals, place a pillar
                int relX = effectiveRoom.getX() - x;
                int relZ = effectiveRoom.getZ() - z;
                if(relX % 5 == 0 && relZ % 5 == 0)
                	AncientCityUtils.placeSupportPillar(b.getDown());
                
            }
        }
        
        //Connect the paths to the rooms
        for(Entry<Wall, Integer> entry:room.getFourWalls(data, 0).entrySet()) {
        	Wall w = entry.getKey().getDown();
        	for(int i = shrunkenWidth; i < entry.getValue()-shrunkenWidth; i++) {
    			//w.getRear().setType(Material.RED_WOOL);
        		if(this.gen.getPathPopulators().contains(new PathPopulatorData(w.getRear().getAtY(room.getY()), 3))) {
        			
        			w.setType(AncientCityUtils.deepslateBricks);
        			w.getLeft().setType(AncientCityUtils.deepslateBricks);
        			w.getRight().setType(AncientCityUtils.deepslateBricks);
        			
        			if(depression < 0)
	        			new StairwayBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
	        			.setDownTypes(AncientCityUtils.deepslateBricks)
	        			.setStairwayDirection(BlockFace.DOWN)
	        			.setStopAtY(effectiveRoom.getY())
	        			.build(w.getFront())
	        			.build(w.getFront().getLeft())
	        			.build(w.getFront().getRight());
        			else
	        			new StairwayBuilder(OneOneSevenBlockHandler.DEEPSLATE_BRICK_STAIRS)
	        			.setDownTypes(AncientCityUtils.deepslateBricks)
	        			.setStairwayDirection(BlockFace.UP)
	        			.setUpwardsCarveUntilNotSolid(false)
	        			.setStopAtY(effectiveRoom.getY())
	        			.build(w.getUp().getFront())
	        			.build(w.getUp().getFront().getLeft())
	        			.build(w.getUp().getFront().getRight());
        		}
        		w = w.getLeft();
        	}
        }
    }
}
