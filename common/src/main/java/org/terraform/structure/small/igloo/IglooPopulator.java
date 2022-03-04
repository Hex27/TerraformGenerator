package org.terraform.structure.small.igloo;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CylinderBuilder;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.SphereBuilder.SphereType;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.blockdata.TrapdoorBuilder;

public class IglooPopulator extends MultiMegaChunkStructurePopulator {
	
    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {

        if (!TConfigOption.STRUCTURES_RUINEDPORTAL_ENABLED.getBoolean())
            return;
        Random random = this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ());
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        for (int[] coords : getCoordsFromMegaChunk(tw, mc)) {
            int x = coords[0];
            int z = coords[1];
		    if(x >> 4 != data.getChunkX() || z >> 4 != data.getChunkZ())
		    	continue;
            int height = GenUtils.getHighestGround(data, x, z);
            
            spawnIgloo(tw, random, data, x, height + 1, z);
        }
    }

    public void spawnIgloo(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
    	Wall core = new Wall(data,x,y,z,BlockUtils.getDirectBlockFace(random));

    	int size = GenUtils.randInt(random, 4, 7);
    	TerraformGeneratorPlugin.logger.info("Placing igloo of size " + size);
    	new CylinderBuilder(random, core.getDown(), Material.SPRUCE_PLANKS)
    	.setHardReplace(false)
    	.setRX(size*1.5f).setRY(0.5f).setRZ(size*1.5f)
    	.setMinRadius(1.0f)
    	.setSingleBlockY(true)
    	.build();
    	
    	//
    	new SphereBuilder(random, core, Material.SNOW_BLOCK)
    	.setSphereType(SphereType.UPPER_SEMISPHERE)
    	.setRadius(size)
    	.setSmooth(true)
    	.build();
    	new SphereBuilder(random, core, Material.AIR)
    	.setSphereType(SphereType.UPPER_SEMISPHERE)
    	.setRadius(size-1)
    	.setSmooth(true)
    	.setHardReplace(true)
    	.build();
    	
    	spawnSpire(core.getRelative(size-1,0,size-1));
    	spawnSpire(core.getRelative(-size+1,0,size-1));
    	spawnSpire(core.getRelative(-size+1,0,-size+1));
    	spawnSpire(core.getRelative(size-1,0,-size+1));
    	
    	core.getUp(size+1).setType(Material.SPRUCE_SLAB);
    	
    	//Side Decorations
    	spawnTrapdoorDecors(new Wall(core.getUp(size), BlockFace.NORTH), size);
    	spawnTrapdoorDecors(new Wall(core.getUp(size), BlockFace.SOUTH), size);
    	spawnTrapdoorDecors(new Wall(core.getUp(size), BlockFace.EAST), size);
    	spawnTrapdoorDecors(new Wall(core.getUp(size), BlockFace.WEST), size);
    	
    	//Entrance
    	core.getFront(size-1).setType(Material.AIR);
    	core.getFront(size).setType(Material.AIR);
    	core.getFront(size-1).getUp().setType(Material.AIR);
    	BlockUtils.placeDoor(data, Material.SPRUCE_DOOR, 
    			core.getFront(size-1));
    	
    	Wall entranceCore = core.getFront(size);
    	entranceCore.getLeft().Pillar(2, Material.SPRUCE_LOG);
    	entranceCore.getRight().Pillar(2, Material.SPRUCE_LOG);
    	entranceCore.getFront().getLeft().setType(Material.SPRUCE_PLANKS);
    	entranceCore.getFront().getRight().setType(Material.SPRUCE_PLANKS);
    	
    	new OrientableBuilder(Material.SPRUCE_LOG)
    	.setAxis(BlockUtils.getAxisFromBlockFace(core.getDirection()))
    	.apply(entranceCore.getUp(2));
    	
    	new StairBuilder(Material.SPRUCE_STAIRS)
    	.setFacing(core.getDirection().getOppositeFace())
    	.apply(entranceCore.getFront().getLeft().getUp())
    	.apply(entranceCore.getFront().getRight().getUp())
    	.setFacing(BlockUtils.getLeft(core.getDirection()))
    	.apply(entranceCore.getRight().getUp(2))
    	.setFacing(BlockUtils.getRight(core.getDirection()))
    	.apply(entranceCore.getLeft().getUp(2));
    	
    	new TrapdoorBuilder(Material.SPRUCE_TRAPDOOR)
    	.setOpen(true)
    	.setFacing(BlockUtils.getLeft(core.getDirection()))
    	.apply(entranceCore.getLeft(2))
    	.apply(entranceCore.getLeft(2).getUp())
    	.setFacing(BlockUtils.getRight(core.getDirection()))
    	.apply(entranceCore.getRight(2))
    	.apply(entranceCore.getRight(2).getUp());
    	
    }
    
    private void spawnTrapdoorDecors(Wall w, int size) {
    	int lowest = 9999;
    	for(int i = 1; i < size; i++) {
    		Wall target = w.getFront(i);
    		if(i <= 2) {
    			target.setType(Material.SNOW_BLOCK);
    			if(i == 1) {
        			target.getLeft().setType(Material.SNOW_BLOCK);
        			target.getRight().setType(Material.SNOW_BLOCK);
    			}
    			new TrapdoorBuilder(Material.SPRUCE_TRAPDOOR)
    			.setFacing(w.getDirection()).apply(target.getUp());
    			continue;
    		}
    		
    		target = target.getDown(i-3);
    		if(!target.getType().isSolid()) {
				new StairBuilder(Material.SPRUCE_STAIRS)
				.setFacing(target.getDirection().getOppositeFace())
				.apply(target);
    		}
    		if(target.getY() < lowest) lowest = target.getY();
    	}

    	int y = w.getDown(size).getY();
    	
    	while(y <= lowest) {
			new TrapdoorBuilder(Material.SPRUCE_TRAPDOOR)
			.setOpen(true)
			.setFacing(w.getDirection())
			.apply(w.getFront(size).getAtY(y));
			y++;
    	}
    	
    }
    
    private void spawnSpire(SimpleBlock block) {
    	block.Pillar(3, Material.SPRUCE_LOG);
    	block.getUp(3).setType(Material.COBBLESTONE_WALL);
    	block.getUp(4).setType(Material.SPRUCE_FENCE);
    	for(BlockFace face:BlockUtils.directBlockFaces) {
    		new StairBuilder(Material.SPRUCE_STAIRS)
    		.setFacing(face.getOppositeFace())
    		.lapply(block.getRelative(face));
    		
    		new TrapdoorBuilder(Material.SPRUCE_TRAPDOOR)
    		.setFacing(face)
    		.setOpen(true)
    		.apply(block.getUp(2).getRelative(face));
    	}
    	
    }
    
    @Override
    public int[][] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
        int num = TConfigOption.STRUCTURES_RUINEDPORTAL_COUNT_PER_MEGACHUNK.getInt();
        int[][] coords = new int[num][2];
        for (int i = 0; i < num; i++)
            coords[i] = mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 4363463*(1+i)));
        return coords;

    }

    public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                for (int[] loc : getCoordsFromMegaChunk(tw, mc)) {
                    double distSqr = Math.pow(loc[0] - rawX, 2) + Math.pow(loc[1] - rawZ, 2);
                    if (distSqr < minDistanceSquared) {
                        minDistanceSquared = distSqr;
                        min = loc;
                    }
                }
            }
        }
        return min;
    }

    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 976123),
                (int) (TConfigOption.STRUCTURES_RUINEDPORTAL_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }
    
    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX,
                            int chunkZ) {
        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        for (int[] coords : getCoordsFromMegaChunk(tw, mc)) {
            if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
            	return rollSpawnRatio(tw,chunkX,chunkZ);
            }
        }
        return false;
    }

    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(729384234, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_RUINEDPORTAL_ENABLED.getBoolean();
    }
    
    @Override
    public int getChunkBufferDistance() {
    	return 1;
    }
}
