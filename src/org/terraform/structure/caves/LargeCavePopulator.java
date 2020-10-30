package org.terraform.structure.caves;

import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class LargeCavePopulator extends SingleMegaChunkStructurePopulator {
    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        if (!TConfigOption.STRUCTURES_LARGECAVE_ENABLED.getBoolean())
            return;
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());

        int[] spawnCoords = getCoordsFromMegaChunk(tw, mc);

        int x = spawnCoords[0];//data.getChunkX()*16 + random.nextInt(16);
        int z = spawnCoords[1];//data.getChunkZ()*16 + random.nextInt(16);
        Random rand = tw.getHashedRand(x, z, 999323);

        int highest = GenUtils.getHighestGround(data, x, z);
        int rY = (highest - 20) / 2; //5 block padding bottom, 15 padding top.

        if (rand.nextBoolean())
            new GenericLargeCavePopulator().createLargeCave(tw, rand, data, rY, x, rY + 6, z);
        else
            new MushroomCavePopulator().createLargeCave(tw, rand, data, rY, x, rY + 6, z);
    }

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, ArrayList<BiomeBank> biomes) {

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[] coords = getCoordsFromMegaChunk(tw, mc);
        if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
            return GenUtils
                    .chance(this.getHashedRandom(tw, chunkX, chunkZ),
                            TConfigOption.STRUCTURES_LARGECAVE_CHANCE.getInt(),
                            100);
        }
        return false;
    }

    //Each mega chunk has 1 large cave
    @Override
    public int[] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
        return mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 78889279));
    }

    @Override
    public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                int[] loc = getCoordsFromMegaChunk(tw, mc.getRelative(nx, nz));
                double distSqr = Math.pow(loc[0] - rawX, 2) + Math.pow(loc[1] - rawZ, 2);
                if (distSqr < minDistanceSquared) {
                    minDistanceSquared = distSqr;
                    min = loc;
                }
            }
        }
        return min;
    }

    @Override
    public Random getHashedRandom(TerraformWorld tw, int chunkX, int chunkZ) {
        return tw.getHashedRand(123912, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfigOption.STRUCTURES_LARGECAVE_ENABLED.getBoolean();
    }
}
