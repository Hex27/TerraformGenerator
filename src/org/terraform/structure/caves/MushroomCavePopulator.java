package org.terraform.structure.caves;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.SeaPickle;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MushroomCavePopulator extends GenericLargeCavePopulator {

    public void createLargeCave(TerraformWorld tw, Random rand, PopulatorDataAbstract data, int rY, int x, int y, int z) {
        TerraformGeneratorPlugin.logger.info("Generating Large Mushroom Cave at " + x + "," + y + "," + z);
        int rX = GenUtils.randInt(rand, 30, 50);
        int rZ = GenUtils.randInt(rand, 30, 50);
        int seed = rand.nextInt(876283);

        //Create main cave hole
        carveCaveSphere(seed, rX, rY, rZ, new SimpleBlock(data, x, y, z));

        //Decrease radius to only spawn spikes away from corners
        rX -= 10;
        rZ -= 10;

        FastNoise mycelNoise = new FastNoise(seed * 5);
        mycelNoise.SetNoiseType(NoiseType.SimplexFractal);
        mycelNoise.SetFractalOctaves(3);
        mycelNoise.SetFrequency(0.05f);

        int lowestPoint = y - rY;

        for (int nx = x - rX; nx <= x + rX; nx++) {
            for (int nz = z - rZ; nz <= z + rZ; nz++) {
                double noise = mycelNoise.GetNoise(nx, nz);
                if (noise < 0) noise = 0;
                if (noise > 0.5) noise = (noise - 0.5) * 0.5 + 0.5;
                int h = (int) ((rY / 2) * noise) + 2;
                if (h < 0) h = 0;
                BlockUtils.spawnPillar(rand, data, nx, lowestPoint, nz, Material.DIRT, h, h);
                BlockUtils.downPillar(nx, lowestPoint - 1, nz, 20, data, Material.DIRT);

                data.setType(nx, lowestPoint + h, nz, Material.MYCELIUM);
            }
        }

        for (int nx = x - rX; nx <= x + rX; nx++) {
            for (int nz = z - rZ; nz <= z + rZ; nz++) {


                //Low luminosity sea pickles
                if (GenUtils.chance(rand, 4, 100)) {
                    int ground = getCaveFloor(data, nx, y, nz);
                    if (data.getType(nx, ground, nz).isSolid()
                            && data.getType(nx, ground + 1, nz) == Material.WATER) {
                        SeaPickle sp = (SeaPickle) Bukkit.createBlockData(Material.SEA_PICKLE);
                        sp.setPickles(GenUtils.randInt(3, 4));
                        data.setBlockData(nx, ground + 1, nz, sp);
                    }
                }

                //Giant shroom
                if (GenUtils.chance(1, 400)) {
                    int ground = getCaveFloor(data, nx, y, nz);
                    if (data.getType(nx, ground, nz) == Material.MYCELIUM
                            && data.getType(nx, ground + 1, nz) == Material.CAVE_AIR) {
                        FractalTypes.Mushroom type = FractalTypes.Mushroom.GIANT_RED_MUSHROOM;
                        if (rand.nextBoolean()) type = FractalTypes.Mushroom.GIANT_BROWN_MUSHROOM;
                        new MushroomBuilder(type).build(tw, data, nx, ground, nz);
                    }
                }

                //Stalagmites  &Stalactites
                if (GenUtils.chance(rand, 3, 100)) {
                    if (rand.nextBoolean()) {
                        int ceil = getCaveCeiling(data, nx, y, nz);
                        if (ceil != -1) {
                            int r = 2;
                            int h = GenUtils.randInt(rand, rY / 2, (int) ((3f / 2f) * rY));
                            stalactite(tw, rand, data, nx, ceil, nz, r, h);
                        }
                    } else {
                        int ground = getCaveFloor(data, nx, y, nz);
                        if (ground != -1) {
                            int r = 2;
                            int h = GenUtils.randInt(rand, rY / 2, (int) ((3f / 2f) * rY));
                            stalagmite(tw, rand, data, nx, ground, nz, r, h);
                        }
                    }
                }
            }
        }
    }
}
