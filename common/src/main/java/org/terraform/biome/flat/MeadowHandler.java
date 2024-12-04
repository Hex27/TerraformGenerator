package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MeadowHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.MEADOW;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(TerraformWorld tw,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK && !BlockUtils.isWet(new SimpleBlock(data,
                rawX,
                surfaceY,
                rawZ)))
        {
            if (GenUtils.chance(random, 1, 10)) { // Grass
                if (GenUtils.chance(random, 6, 10)) {
                    PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    if (random.nextBoolean()) {
                        PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {
        //This logic crosses chunk borders, so we put it here to be safe
        SimpleLocation[] flowerCenters = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 23);
        //Spawn several dense clusters of flowers
        for(SimpleLocation center:flowerCenters)
        {
            Random hashRand = tw.getHashedRand(78019432, center.getX(),center.getZ());
            PlantBuilder flowerer = BlockUtils.pickFlower(hashRand);
            BlockUtils.lambdaCircularPatch(
                    hashRand.nextInt(211312),
                    GenUtils.randInt(5,7),
                    new SimpleBlock(data, center.getX(), 0, center.getZ())
                            .getGround(),
                    (b)->{
                        if(data.getBiome(b.getX(),b.getZ()) == getBiome()
                           && hashRand.nextInt(4) == 0)
                            flowerer.build(b.getUp());
                    });
        }


        boolean isPumpkin = random.nextBoolean();
        // Pumpkin Patch
        if (GenUtils.chance(1, 70)) {
            for (int i = 0; i < GenUtils.randInt(5, 10); i++) {
                int[] loc = GenUtils.randomSurfaceCoordinates(random, data);
                if (data.getBiome(loc[0], loc[2]) != getBiome()) {
                    continue;
                }
                SimpleBlock target = new SimpleBlock(
                        data,
                        loc[0],
                        GenUtils.getHighestGround(data, loc[0], loc[2]) + 1,
                        loc[2]
                );
                if (!target.isSolid()) {
                    if(isPumpkin) PlantBuilder.PUMPKIN.build(target);
                    else PlantBuilder.MELON.build(target);
                }
            }
        }

        // Only poffs in meadows
        SimpleLocation[] poffLocs = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 16);

        for (SimpleLocation sLoc : poffLocs) {
            int highestY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            if (BlockUtils.isWet(new SimpleBlock(data, sLoc.getX(), highestY + 1, sLoc.getZ()))) {
                continue;
            }

            sLoc.setY(highestY);
            if (TConfig.arePlantsEnabled()
                && data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
                && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ())))
            {
                BlockUtils.replaceSphere(random.nextInt(424444),
                        2,
                        2,
                        2,
                        new SimpleBlock(data, sLoc.getX(), sLoc.getY() + 1, sLoc.getZ()),
                        false,
                        Material.OAK_LEAVES
                );
            }
        }
    }
}
