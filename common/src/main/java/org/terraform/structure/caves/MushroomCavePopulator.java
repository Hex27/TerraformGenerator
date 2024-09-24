package org.terraform.structure.caves;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.SeaPickle;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.StalactiteBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Objects;
import java.util.Random;

public class MushroomCavePopulator extends GenericLargeCavePopulator {

    public MushroomCavePopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    /**
     * Raise some patches of ground above the water.
     * Set stuff to mycelium
     */
    @Override
    protected void populateFloor(@NotNull SimpleBlock floor, int waterLevel) {
        TerraformWorld tw = floor.getPopData().getTerraformWorld();
        if (floor.getY() <= waterLevel) {
            int waterDepth = waterLevel - floor.getY();

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
                floor.getUp().RPillar(h, new Random(), Material.DIRT);
                floor = floor.getUp(h); // ????
            }
        }

        // Water decorations come after this
        if (floor.getY() >= waterLevel) {
            // Set mycelium if not underwater
            floor.setType(Material.MYCELIUM);

            // chance to set small mushshrooms
            if (GenUtils.chance(rand, 7, 100)) {
                SimpleBlock up = floor.getUp();
                if (!up.isSolid()) {
                    PlantBuilder.build(up, PlantBuilder.RED_MUSHROOM, PlantBuilder.BROWN_MUSHROOM);
                }
            }

            return;
        }

        // sea pickle
        if (BlockUtils.isWet(floor.getUp()) && GenUtils.chance(rand, 7, 100)) {
            SeaPickle sp = (SeaPickle) Bukkit.createBlockData(Material.SEA_PICKLE); // TODO: PlantBuilder
            sp.setPickles(GenUtils.randInt(3, 4));
            floor.getUp().setBlockData(sp);
        }
    }

    @Override
    protected void populateCeilFloorPair(@NotNull SimpleBlock ceil, @NotNull SimpleBlock floor, int height) {
        TerraformWorld tw = ceil.getPopData().getTerraformWorld();

        // Correct for mycelium ground raise
        int newHeight = height;
        while (newHeight > 0 && floor.getUp().isSolid()) {
            floor = floor.getUp();
            newHeight--;
        }
        if (newHeight <= 0) {
            return; // give up.
        }

        // Only stalactites
        if (GenUtils.chance(rand, 1, 150)) {
            int r = 2;
            int h = GenUtils.randInt(rand, (int) (height / 2.5f), (int) ((3f / 2f) * (height / 2.5f)));
            new StalactiteBuilder(BlockUtils.stoneOrSlateWall(ceil.getY())).setSolidBlockType(BlockUtils.stoneOrSlate(
                    ceil.getY())).makeSpike(ceil, r, h, false);
        }

        // Check boundaries - the mushrooms are huge and will cut into bordering areas
        // and unlike stalactites, look especially fucking bad doing so
        else if (floor.getChunkX() == floor.getPopData().getChunkX()
                 && floor.getChunkZ() == floor.getPopData()
                                              .getChunkZ()
                 && floor.getType() == Material.MYCELIUM
                 && newHeight >= 15
                 && GenUtils.chance(rand, 1, 110))
        {
            // Big Mushrooms
            new MushroomBuilder(Objects.requireNonNull(GenUtils.choice(rand, FractalTypes.Mushroom.values()))).build(
                    tw,
                    floor.getPopData(),
                    floor.getX(),
                    floor.getY(),
                    floor.getZ()
            );
        }
    }
}