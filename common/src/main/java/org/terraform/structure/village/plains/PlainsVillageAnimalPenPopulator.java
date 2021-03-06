package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Map.Entry;
import java.util.Random;

public class PlainsVillageAnimalPenPopulator extends RoomPopulatorAbstract {

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
    	SimpleBlock jobBlock = null;
    	boolean spawnedWater = false;
    	//Place fence
    	for(Entry<Wall,Integer> entry:room.getFourWalls(data, 2).entrySet()) {
    		Wall w = entry.getKey().getGroundOrSeaLevel();
    		
    		for(int i = 0; i < entry.getValue(); i++) {
    			int heightOne = w.getFront().getLeft().getY()-w.getY();
    			int heightTwo = w.getFront().getRight().getY()-w.getY();
    			int heightThree = w.getFront().getY()-w.getY();
    			
    			int wallHeight = Math.max(heightOne, heightTwo);
    			wallHeight = Math.max(wallHeight, heightThree);
    			
    			wallHeight = 2+wallHeight;
    			if(wallHeight < 2) wallHeight = 2;
    			
    			w = w.getRelative(0,wallHeight,0);
    			
    			if(w.getDirection().getOppositeFace() == ((DirectionalCubeRoom) room).getDirection()) {
    				if(i == entry.getValue()/2) {
    					jobBlock = w.getRear(2).getGroundOrSeaLevel().getRelative(0,1,0).get();
    					new Wall(jobBlock.getRelative(0,-1,0)).downUntilSolid(new Random(), Material.DIRT);
    				}
    			}
    			if(i % 2 == 0) {
                    w.downUntilSolid(rand, plainsVillagePopulator.woodLog);
    				w.getRelative(0,1,0).setType(Material.COBBLESTONE_SLAB);
    			} else {
    				w.downUntilSolid(new Random(), plainsVillagePopulator.woodFence);
    			}
    			int y = w.getGround().getY();
				w.getAtY(y).CorrectMultipleFacing(1+w.getY()-y);
    			
    			w = w.getLeft().getGroundOrSeaLevel();
    		}
    	}
    	
    	//Decorations
    	int[] lowerCorner = room.getLowerCorner(3);
    	int[] upperCorner = room.getUpperCorner(3);
    	
    	//Change the floor
    	for(int x = lowerCorner[0]; x <= upperCorner[0]; x++)
    		for(int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
				int highest = GenUtils.getHighestGroundOrSeaLevel(data, x, z);
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
    					Wall core = new Wall(new SimpleBlock(data, x,0,z),BlockUtils.getDirectBlockFace(rand)).getGroundOrSeaLevel().getRelative(0,1,0);
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
    					
                        BlockUtils.replaceUpperSphere(x + 7 * z + 17 * 17, 1.5f, 2.5f, 1.5f,
                                new SimpleBlock(data, x, 0, z).getGround(),
                                false, Material.HAY_BLOCK);
                        break;
    				}
    			}
    		}
    	
    	//Spawn animals
    	EntityType animal = farmAnimals[rand.nextInt(farmAnimals.length)];
        //Spawn animals
        for (int i = 0; i < GenUtils.randInt(3, 7); i++) {
        	int[] coords = new int[] {room.getX(),0,room.getZ()};
            int highest = GenUtils.getTrueHighestBlock(data, coords[0], coords[2]);
            data.addEntity(coords[0], highest + 1, coords[2], animal);
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
