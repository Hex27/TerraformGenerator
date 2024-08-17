package org.terraform.structure.ancientcity;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.structure.room.CarvedRoom;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.GenUtils;
import org.terraform.utils.StairwayBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Map.Entry;
import java.util.HashSet;
import java.util.Random;

public abstract class AncientCityAbstractRoomPopulator extends RoomPopulatorAbstract {

	TerraformWorld tw;
	protected HashSet<SimpleLocation> occupied;
	protected int shrunkenWidth = 0;
	protected RoomLayoutGenerator gen;
    public AncientCityAbstractRoomPopulator(TerraformWorld tw, HashSet<SimpleLocation> occupied, RoomLayoutGenerator gen, Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        this.tw = tw;
        this.occupied = occupied;
        this.gen = gen;
    }

    protected @Nullable CubeRoom effectiveRoom = null;
    
    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
    	
    	shrunkenWidth = GenUtils.randInt(this.rand, 2,4);
    	
    	//This variable is named depression, but can also represent elevation.
    	int depression = shrunkenWidth;
    	
    	//Sometimes the room will be higher than the path's Y.
    	if(rand.nextBoolean()) {
    		depression = depression*-1;
    	}
    	
    	this.effectiveRoom = new CarvedRoom(new CubeRoom(
    			room.getWidthX() - shrunkenWidth*2 - 1,
    			room.getWidthZ() - shrunkenWidth*2 - 1,
    			room.getHeight(),
    			room.getX(), room.getY() + depression, room.getZ()));
    	
        //Clear out space for the room
    	effectiveRoom.fillRoom(data, Material.CAVE_AIR);
    	
    	//Room flooring
        int[] lowerCorner = effectiveRoom.getLowerCorner(0);
        int[] upperCorner = effectiveRoom.getUpperCorner(0);
        int y = effectiveRoom.getY();
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                SimpleBlock b = new SimpleBlock(data, x, y, z);
                
                //Fuzz the sides to give a sense of ruin
                if(x == lowerCorner[0] || x == upperCorner[0] || z == lowerCorner[1] || z == upperCorner[1])
                {
                	if(rand.nextBoolean())
                		b.lsetType(AncientCityUtils.deepslateBricks);
                }
                else
                	b.lsetType(AncientCityUtils.deepslateBricks);
                
                //every few intervals, place a pillar
            	int relX = effectiveRoom.getX() - x;
                int relZ = effectiveRoom.getZ() - z;
                if(relX % 5 == 0 && relZ % 5 == 0 && 
                		(effectiveRoom.isPointInside(b.getRelative(BlockFace.NORTH))
                				&& effectiveRoom.isPointInside(b.getRelative(BlockFace.SOUTH))
                				&& effectiveRoom.isPointInside(b.getRelative(BlockFace.EAST))
                				&& effectiveRoom.isPointInside(b.getRelative(BlockFace.WEST))))
                	AncientCityUtils.placeSupportPillar(b.getDown());
                
            }
        }
        
        //Connect the paths to the rooms
        for(Entry<Wall, Integer> entry:room.getFourWalls(data, 0).entrySet()) {
        	Wall w = entry.getKey().getDown();
        	for(int i = shrunkenWidth; i < entry.getValue()-shrunkenWidth; i++) {
    			
        		if(this.gen.getPathPopulators().contains(new PathPopulatorData(w.getRear().getAtY(room.getY()), 3))) {
        			//w.getUp(3).setType(Material.RED_WOOL);
        			w.setType(AncientCityUtils.deepslateBricks);
        			w.getLeft().setType(AncientCityUtils.deepslateBricks);
        			w.getRight().setType(AncientCityUtils.deepslateBricks);
        			
        			if(depression < 0) {
	        			new StairwayBuilder(Material.DEEPSLATE_BRICK_STAIRS)
	        			.setDownTypes(AncientCityUtils.deepslateBricks)
	        			.setStairwayDirection(BlockFace.DOWN)
	        			.setStopAtY(effectiveRoom.getY())
	        			.build(w.getFront())
	        			.build(w.getFront().getLeft())
	        			.build(w.getFront().getRight());
        			}
        			else
        			{
	        			new StairwayBuilder(Material.DEEPSLATE_BRICK_STAIRS)
	        			.setDownTypes(AncientCityUtils.deepslateBricks)
	        			.setStairwayDirection(BlockFace.UP)
	        			.setUpwardsCarveUntilNotSolid(false)
	        			.setStopAtY(effectiveRoom.getY())
	        			.build(w.getUp().getFront())
	        			.build(w.getUp().getFront().getLeft())
	        			.build(w.getUp().getFront().getRight());
        			}
        			
        		}
        		w = w.getLeft();
        	}
        }
    }

	public void sculkUp(TerraformWorld tw, @NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        FastNoise circleNoise = NoiseCacheHandler.getNoise(
                tw,
                NoiseCacheEntry.BIOME_CAVECLUSTER_CIRCLENOISE,
                world -> {
                    FastNoise n = new FastNoise((int) (world.getSeed() * 11));
                    n.SetNoiseType(FastNoise.NoiseType.Simplex);
                    n.SetFrequency(0.09f);

                    return n;
                });
		for(int i = 0; i <= ((room.getWidthX()*room.getWidthZ())/150); i++)
		{
			//Generates 3d coords, but we will discard the y coords.
			//We will separately generate y coords later.
			int[] coords = room.randomCoords(rand);
			int y = rand.nextInt(5);
			SimpleBlock target = new SimpleBlock(data, coords[0], room.getY()+y,coords[2]);
			AncientCityUtils.spreadSculk(circleNoise, rand, 5, target);
		}
	}
}
