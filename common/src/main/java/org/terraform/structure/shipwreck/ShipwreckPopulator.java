package org.terraform.structure.shipwreck;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class ShipwreckPopulator extends MultiMegaChunkStructurePopulator {
    private static final String[] SCHEMATICS = {"upright-shipwreck-1",
            "tilted-shipwreck-1"};

    private static void dropDownBlock(SimpleBlock block) {
        if (block.getType().isSolid()) {
            Material type = block.getType();
            if (type == Material.CHEST) return;
            block.setType(Material.WATER);
            int depth = 0;
            while (!block.getType().isSolid()) {
                block = block.getRelative(0, -1, 0);
                depth++;
                if (depth > 50) return;
            }

            block.getRelative(0, 1, 0).setType(type);
        }
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {

        if (!TConfigOption.STRUCTURES_SHIPWRECK_ENABLED.getBoolean())
            return;
        Random random = this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ());
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        for (int[] coords : getCoordsFromMegaChunk(tw, mc)) {
            int x = coords[0];
            int z = coords[1];
            int height = GenUtils.getHighestGround(data, x, z) - 1 - random.nextInt(5);
            
            spawnShipwreck(tw, random, data, x, height + 1, z);
        }
    }

    public void spawnShipwreck(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        try {
        	//If the ground is dry, force the whole ship down into the ground to bury it.
        	if(!BlockUtils.isWet(new SimpleBlock(data,x,0,z).getGround().getRelative(0,1,0))) {
        		y -= GenUtils.randInt(random, 4, 7);
        	}
            y += GenUtils.randInt(random, -1, 1);
            TerraSchematic shipwreck = TerraSchematic.load(SCHEMATICS[random.nextInt(SCHEMATICS.length)], new Location(tw.getWorld(), x, y, z));
            shipwreck.parser = new ShipwreckSchematicParser(tw.getBiomeBank(x, z), random, data);
            shipwreck.setFace(BlockUtils.getDirectBlockFace(random));
            shipwreck.apply();

            TerraformGeneratorPlugin.logger.info("Spawning shipwreck at " + x + ", " + y + ", " + z + " with rotation of " + shipwreck.getFace());

            //Generate holes and damage
            for (int i = 0; i < GenUtils.randInt(random, 0, 3); i++) {
                int nx = x + GenUtils.randInt(random, -8, 8);
                int nz = z + GenUtils.randInt(random, -8, 8);
                int ny = y + GenUtils.randInt(random, 0, 5);
                BlockUtils.replaceWaterSphere(nx * 7 * ny * 23 * nz, GenUtils.randInt(1, 3), new SimpleBlock(data, nx, ny, nz));
            }

            //Dropdown blocks
            for (int i = 0; i < GenUtils.randInt(random, 5, 15); i++) {
                int nx = x + GenUtils.randInt(random, -8, 8);
                int nz = z + GenUtils.randInt(random, -8, 8);
                int ny = y + GenUtils.randInt(random, 0, 5);
                dropDownBlock(new SimpleBlock(data, nx, ny, nz));
            }

            data.addEntity(x, y + 12, z, EntityType.DROWNED); //Two Drowneds
            data.addEntity(x, y + 15, z, EntityType.DROWNED);


        } catch (Throwable e) {
            TerraformGeneratorPlugin.logger.error("Something went wrong trying to place shipwreck at " + x + ", " + y + ", " + z);
            e.printStackTrace();
        }
    }

    @Override
    public int[][] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
        int num = TConfigOption.STRUCTURES_SHIPWRECK_COUNT_PER_MEGACHUNK.getInt();
        int[][] coords = new int[num][2];
        for (int i = 0; i < num; i++)
            coords[i] = mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 191921*i));
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
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 12422),
                (int) (TConfigOption.STRUCTURES_SHIPWRECK_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }
    
    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX,
                            int chunkZ) {
        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        for (int[] coords : getCoordsFromMegaChunk(tw, mc)) {
            if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
            	ArrayList<BiomeBank> biomes = GenUtils.getBiomesInChunk(tw, chunkX, chunkZ);
                double numWet = 0;
                double numDry = 0;
            	for(BiomeBank b:biomes)
                	if(b.getType().isDry())
                		numDry++;
                	else
                		numWet++;
            	
            	return (numWet/(numWet+numDry)) > 0.5 &&  rollSpawnRatio(tw,chunkX,chunkZ);
            }
        }
        return false;
    }

    
    
    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(221819019, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_SHIPWRECK_ENABLED.getBoolean();
    }
    
    @Override
    public int getChunkBufferDistance() {
    	return 1;
    }
}
