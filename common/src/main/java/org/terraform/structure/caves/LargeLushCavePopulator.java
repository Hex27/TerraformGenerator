package org.terraform.structure.caves;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.type.SeaPickle;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.cavepopulators.LushClusterCavePopulator;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.StalactiteBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class LargeLushCavePopulator extends GenericLargeCavePopulator {

    public LargeLushCavePopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    /**
     * Raise some patches of ground above the water
     */
    @Override
    protected void populateFloor(@NotNull SimpleBlock floor, int waterLevel) {
        if (floor.getY() > waterLevel) {
            return;
        }
        int waterDepth = waterLevel - floor.getY();
        TerraformWorld tw = floor.getPopData().getTerraformWorld();

        FastNoise raisedGroundNoise = NoiseCacheHandler.getNoise(tw,
                NoiseCacheEntry.STRUCTURE_LARGECAVE_RAISEDGROUNDNOISE,
                world -> {
                    FastNoise n = new FastNoise((int) (world.getSeed() * 5));
                    n.SetNoiseType(NoiseType.SimplexFractal);
                    n.SetFractalOctaves(3);
                    n.SetFrequency(0.06f);
                    return n;
                }
        );

        // Raise some ground up
        double noise = raisedGroundNoise.GetNoise(floor.getX(), floor.getZ());
        if (noise > 0) {
            int h = (int) Math.round(4.3f * waterDepth * noise);
            if (h > waterDepth) {
                h = (int) Math.round(waterDepth + Math.sqrt(h - waterDepth));
            }
            floor.getUp().RPillar(h, new Random(), Material.CLAY);
            floor = floor.getUp(h - 1);
        }

        // Place pickles
        if (floor.getY() > waterLevel) {
            return;
        }

        // sea pickle
        if (TConfig.arePlantsEnabled() && BlockUtils.isWet(floor.getUp()) && GenUtils.chance(rand, 7, 100)) {
            SeaPickle sp = (SeaPickle) Bukkit.createBlockData(Material.SEA_PICKLE);
            sp.setPickles(GenUtils.randInt(3, 4));
            floor.getUp().setBlockData(sp);
        }

        // Lilypads
        if (GenUtils.chance(rand, 1, 200) && BlockUtils.isWet(floor.getAtY(waterLevel)) && floor.getAtY(waterLevel + 1)
                                                                                                .isAir())
        {
            PlantBuilder.LILY_PAD.build(floor.getAtY(waterLevel + 1));
        }

        // Stalagmites
        if (GenUtils.chance(rand, 1, 130)) {
            int r = 2;
            int h = GenUtils.randInt(rand, 6 * waterDepth, (int) ((3f / 2f) * (6 * waterDepth)));
            new StalactiteBuilder(BlockUtils.stoneOrSlateWall(floor.getY())).setSolidBlockType(BlockUtils.stoneOrSlate(
                    floor.getY())).makeSpike(floor, r, h, true);
        }
    }

    @Override
    protected void populateCeilFloorPair(@NotNull SimpleBlock ceil, @NotNull SimpleBlock floor, int height) {
        TerraformWorld tw = ceil.getPopData().getTerraformWorld();

        // Correct for clay ground raise
        int cutoff = height;
        while (cutoff > 0 && floor.getUp().isSolid()) {
            floor = floor.getUp();
            cutoff--;
        }
        if (cutoff <= 0) {
            return; // give up.
        }

        // Invoke OneUnit from the lush cave populator
        new LushClusterCavePopulator(10, true).oneUnit(tw, new Random(), ceil, floor, false);

        // Spawn potential stalactites and stalagmites
        if (GenUtils.chance(rand, 1, 150)) {
            int r = 2;
            int h = GenUtils.randInt(rand, (int) (height / 2.5f), (int) ((3f / 2f) * (height / 2.5f)));
            new StalactiteBuilder(BlockUtils.stoneOrSlateWall(ceil.getY())).setSolidBlockType(BlockUtils.stoneOrSlate(
                    ceil.getY())).makeSpike(ceil, r, h, false);
        }

        // set biome
        PopulatorDataICABiomeWriterAbstract biomeWriter = (PopulatorDataICABiomeWriterAbstract) TerraformGeneratorPlugin.injector.getICAData(
                ceil.getPopData());
        for (int ny = floor.getY(); ny <= ceil.getY(); ny++) {
            biomeWriter.setBiome(floor.getX(), ny, floor.getZ(), Biome.LUSH_CAVES);
        }
    }

}
