package org.terraform.structure.dungeon;

import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SmallDungeonPopulator extends MultiMegaChunkStructurePopulator {
    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
//		ArrayList<BiomeBank> banks = new ArrayList<>();
//		int numOceanic = 0;
        int totalHeight = 0;
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                totalHeight += HeightMap.getBlockHeight(tw, x, z);
            }
        }

        if (totalHeight / 256 <= TConfigOption.STRUCTURES_DROWNEDDUNGEON_MIN_DEPTH.getInt()
                && GenUtils.chance(tw.getHashedRand(1223, data.getChunkX(), data.getChunkZ()), TConfigOption.STRUCTURES_DROWNEDDUNGEON_CHANCE.getInt(), 1000)) {
            //Only spawn these in full oceans
            if (!TConfigOption.STRUCTURES_DROWNEDDUNGEON_ENABLED.getBoolean())
                return;
            new DrownedDungeonPopulator().populate(tw, data);
        } else {
            if (!TConfigOption.STRUCTURES_UNDERGROUNDDUNGEON_ENABLED.getBoolean())
                return;
            new UndergroundDungeonPopulator().populate(tw, data);
        }
    }
    
    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 12222),
                (int) (TConfigOption.STRUCTURES_DUNGEONS_SPAWNRATIO
                        .getDouble() * 10000),
                10000);
    }

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ) {

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[][] allCoords = getCoordsFromMegaChunk(tw, mc);
        for (int[] coords : allCoords) {
            if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
                return rollSpawnRatio(tw,chunkX,chunkZ);
            }
        }
        return false;
    }

    //Each mega chunk has config option dungeons
    @Override
    public int[][] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
    	int num = TConfigOption.STRUCTURES_DUNGEONS_COUNT_PER_MEGACHUNK.getInt();
        int[][] coords = new int[num][2];
        for (int i = 0; i < num; i++)
            coords[i] = mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 1317324*(1+i)));
        return coords;
    }

    @Override
    public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                for (int[] loc : getCoordsFromMegaChunk(tw, mc.getRelative(nx, nz))) {
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

    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(48772719, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_DROWNEDDUNGEON_ENABLED.getBoolean() || TConfigOption.STRUCTURES_UNDERGROUNDDUNGEON_ENABLED.getBoolean();
    }
}
