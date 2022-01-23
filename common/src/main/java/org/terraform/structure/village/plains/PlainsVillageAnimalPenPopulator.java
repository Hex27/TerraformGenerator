package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Map.Entry;
import java.util.Random;

public class PlainsVillageAnimalPenPopulator extends PlainsVillageAbstractRoomPopulator {

    private static final EntityType[] farmAnimals = {
            EntityType.PIG,
            EntityType.SHEEP,
            EntityType.COW,
            EntityType.HORSE,
            EntityType.CHICKEN
    };

	private PlainsVillagePopulator plainsVillagePopulator;
    public PlainsVillageAnimalPenPopulator(PlainsVillagePopulator plainsVillagePopulator, Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	int roomY = super.calculateRoomY(data, room);
    	boolean areaFailedTolerance = true;
    	//if(areaFailedTolerance)
    	
    	//For animal farms, they look increasingly stupid when tilted.
    	//Just give up and place a platform underneath.
		super.placeFixerPlatform(roomY, data, room);
    	
    	SimpleBlock jobBlock = null;
    	boolean spawnedWater = false;
    	//Place fence
    	for(Entry<Wall,Integer> entry:room.getFourWalls(data, 2).entrySet()) {
            Wall w;
            
//            if(!areaFailedTolerance) {
//            	w = entry.getKey().getGroundOrSeaLevel();
//            }
//            else
            	w = entry.getKey().getAtY(roomY);
    		
    		for(int i = 0; i < entry.getValue(); i++) {
    			
    			if(Math.abs(w.getY()-roomY) <= TConfigOption.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE.getInt()) {
    				//Wtf are you doing here, all these calculations just always lead to 2
	//    			int heightOne = w.getFront().getLeft().getY()-w.getY();
	//    			int heightTwo = w.getFront().getRight().getY()-w.getY();
	//    			int heightThree = w.getFront().getY()-w.getY();
	//    			
	//    			int wallHeight = Math.max(heightOne, heightTwo);
	//    			wallHeight = Math.max(wallHeight, heightThree);
	//    			
	//    			wallHeight = 2+wallHeight;
	//    			if(wallHeight < 2) wallHeight = 2;
	    			
	    			int wallHeight = 2;
	    			w = w.getRelative(0,wallHeight,0);
	    			
	    			//Entrance
	    			if(w.getDirection().getOppositeFace() == ((DirectionalCubeRoom) room).getDirection()) {
	    				if(i == entry.getValue()/2) {
	
//	    		            if(!areaFailedTolerance) {
//	    		            	jobBlock = w.getRear(2).getGroundOrSeaLevel().getRelative(0,1,0);
//	    		            }
//	    		            else
	    		            	jobBlock = w.getRear(2).getAtY(roomY).getRelative(0,1,0);
	    		            
	    					
	    					new Wall(jobBlock.getRelative(0,-1,0)).downUntilSolid(new Random(), Material.DIRT);
	    				}
	    			}
	    			
	    			int toCorrect = 0;
	    			if(i % 2 == 0) {
	    				toCorrect = w.downUntilSolid(rand, plainsVillagePopulator.woodLog);
	    				w.getRelative(0,1,0).setType(Material.COBBLESTONE_SLAB);
	    			} else {
	    				toCorrect = w.downUntilSolid(new Random(), plainsVillagePopulator.woodFence);
	    			}
	    			
	    			w.getDown(toCorrect).CorrectMultipleFacing(toCorrect);
    			}

//                if(!areaFailedTolerance) {
//        			w = w.getLeft().getGroundOrSeaLevel();
//                }
//                else
        			w = w.getLeft().getAtY(roomY);
    		}
    	}
    	
    	//Decorations
    	int[] lowerCorner = room.getLowerCorner(3);
    	int[] upperCorner = room.getUpperCorner(3);
    	
    	//Change the floor
    	for(int x = lowerCorner[0]; x <= upperCorner[0]; x++)
    		for(int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
    			
				int highest;
//                if(!areaFailedTolerance) {
//        			highest = GenUtils.getHighestGroundOrSeaLevel(data, x, z);
//                }
//                else
                	highest = roomY;
                
                if(Math.abs(highest-roomY) > TConfigOption.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE.getInt())
                	continue;
                
				BlockUtils.setDownUntilSolid(x, highest, z, data, Material.DIRT);

    			if(rand.nextBoolean()) {
    				data.setType(x, highest, z, GenUtils.randMaterial(Material.PODZOL, Material.COARSE_DIRT, Material.GRASS_BLOCK));
    			}else if(rand.nextBoolean()) {
    				if(!data.getType(x, highest+1, z).isSolid())
	    				BlockUtils.setDoublePlant(data, x, highest+1, z, Material.TALL_GRASS);
    			}
    		}
    	

    	lowerCorner = room.getLowerCorner(5);
    	upperCorner = room.getUpperCorner(5);
    	//Place objects
    	for(int x = lowerCorner[0]; x <= upperCorner[0]; x++)
    		for(int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
    			
    			if(GenUtils.chance(rand, 1, 70)) {
    				if(!spawnedWater && rand.nextBoolean()) { //Water 
    					spawnedWater = true;
    					Wall core = new Wall(new SimpleBlock(data, x,0,z),BlockUtils.getDirectBlockFace(rand));

//    		            if(!areaFailedTolerance) {
//    		                core = core.getGroundOrSeaLevel().getRelative(0,1,0);
//    		            }
//    		            else
    		            	core.getAtY(roomY+1);

    	                if(Math.abs(core.getY()-roomY) > TConfigOption.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE.getInt())
    	                	continue;
    	                
    					new StairBuilder(Material.COBBLESTONE_STAIRS)
    					.setHalf(Half.TOP)
    					.setFacing(core.getDirection())
    					.apply(core.getRear())
    					.setFacing(core.getDirection().getOppositeFace())
    					.apply(core.getFront(2))
    					.setFacing(BlockUtils.getRight(core.getDirection()))
    					.apply(core.getFront().getLeft())
    					.apply(core.getLeft())
    					.setFacing(BlockUtils.getLeft(core.getDirection()))
    					.apply(core.getFront().getRight())
    					.apply(core.getRight());
    					
    					new SlabBuilder(Material.COBBLESTONE_SLAB)
    					.setWaterlogged(true)
    					.apply(core).apply(core.getFront());
    					
    					core.getRelative(0,-1,0).downUntilSolid(new Random(),Material.DIRT);
    					core.getFront().getRelative(0,-1,0).downUntilSolid(new Random(),Material.DIRT);
    					break;
    				} else { //Haybales
    					int hayY;
    		            if(!areaFailedTolerance) {
    		            	hayY = GenUtils.getHighestGround(data, x,z);
    		            }
    		            else
    		            	hayY = roomY;

    	                if(Math.abs(hayY-roomY) > TConfigOption.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE.getInt())
    	                	continue;
    	                
                        BlockUtils.replaceUpperSphere(x + 7 * z + 17 * 17, 1.5f, 2.5f, 1.5f,
                                new SimpleBlock(data, x, hayY, z),
                                false, Material.HAY_BLOCK);
                        break;
    				}
    			}
    		}
    	
    	//Spawn animals
    	EntityType animal = farmAnimals[rand.nextInt(farmAnimals.length)];
        //Spawn animals
    	int[] coords = new int[] {room.getX(),0,room.getZ()};

		int highest;
//            if(!areaFailedTolerance) {
//                highest = GenUtils.getTrueHighestBlock(data, coords[0], coords[2]);
//            }
//            else
    	highest = roomY;

    	int threshold = 0;
        while(data.getType(coords[0], highest + 1, coords[2]).isSolid() &&
        		threshold < 6) {
        	threshold++;
        	highest++;
        }
        if(threshold < 6) {
            if(Math.abs(highest-roomY) <= TConfigOption.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE.getInt())
            {
                for (int i = 0; i < GenUtils.randInt(3, 7); i++) 
                	data.addEntity(coords[0], highest + 1, coords[2], animal);
            }
        }
        
    	switch(animal) {
    	case PIG:
    	case CHICKEN:
    		new DirectionalBuilder(Material.SMOKER)
    		.setFacing(((DirectionalCubeRoom) room).getDirection())
    		.apply(jobBlock);
    		break;
    	case SHEEP:
    		jobBlock.setType(Material.LOOM);
    		break;
    	case COW:
    	case HORSE:
    		jobBlock.setType(Material.CAULDRON);
    		break;
		default:
			break;
    	}
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return room.getWidthX() >= 15 && (room.getWidthX() < 18 || room.getWidthZ() < 18);
    }
}
