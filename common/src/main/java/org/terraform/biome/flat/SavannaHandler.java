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

public class SavannaHandler extends BiomeHandler {

    private static void makeYellowPatch(int x,
                                        int y,
                                        int z,
                                        @NotNull PopulatorDataAbstract data,
                                        @NotNull Random random)
    {
        int length = GenUtils.randInt(6, 16);
        int nx = x;
        int nz = z;
        while (length-- > 0) {
            if (BlockUtils.isDirtLike(data.getType(nx, y, nz)) && data.getType(nx, y + 1, nz) == Material.AIR) {
                data.setType(nx, y, nz, Material.DIRT_PATH);
            }

            switch (random.nextInt(5)) {  // The direction chooser
                case 0 -> nx++;
                case 2 -> nz++;
                case 3 -> nx--;
                case 4 -> nz--;
            }

            y = GenUtils.getHighestGround(data, nx, nz);
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SAVANNA;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                GenUtils.randChoice(rand,
                        Material.GRASS_BLOCK,
                        Material.GRASS_BLOCK,
                        Material.GRASS_BLOCK,
                        Material.GRASS_BLOCK,
                        Material.GRASS_BLOCK,
                        Material.GRASS_BLOCK,
                        Material.GRASS_BLOCK,
                        Material.GRASS_BLOCK,
                        Material.GRASS_BLOCK,
                        Material.GRASS_BLOCK,
                        Material.GRASS_BLOCK,
                        Material.COARSE_DIRT
                ),
                Material.DIRT,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        if (GenUtils.chance(random, 1, 128)) {
            makeYellowPatch(rawX, surfaceY, rawZ, data, random);
        }

        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK && !data.getType(rawX, surfaceY + 1, rawZ)
                                                                               .isSolid())
        {
            // Dense grass
            if (GenUtils.chance(random, TConfig.c.BIOME_SAVANNA_TALLGRASSCHANCE, 10000)) {
                PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {

        boolean spawnedLargeSavannaTree = false;

        // large trees
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 112, 0.6f);

        if (TConfig.c.TREES_SAVANNA_BIG_ENABLED) {
            for (SimpleLocation sLoc : trees) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(treeY);
                if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
                    && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ())))
                {
                    new FractalTreeBuilder(FractalTypes.Tree.SAVANNA_BIG).build(tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY(),
                            sLoc.getZ()
                    );
                    spawnedLargeSavannaTree = true;
                }
            }
        }

        // Small trees
        if (!spawnedLargeSavannaTree) {
            trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 32);
            for (SimpleLocation sLoc : trees) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(treeY);
                if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
                    && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ())))
                {
                    new FractalTreeBuilder(FractalTypes.Tree.SAVANNA_SMALL).build(
                            tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY(),
                            sLoc.getZ()
                    );
                }
            }
        }

        // Grass Poffs
        if (TConfig.arePlantsEnabled()) {
            SimpleLocation[] poffs = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 31);
            for (SimpleLocation sLoc : poffs) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(treeY);
                if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
                    && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()))
                    && !data.getType(sLoc.getX(), sLoc.getY() + 1, sLoc.getZ()).isSolid())
                {
                    SimpleBlock base = new SimpleBlock(data, sLoc.getX(), sLoc.getY() + 1, sLoc.getZ());
                    int rX = GenUtils.randInt(random, 2, 4);
                    int rY = GenUtils.randInt(random, 2, 4);
                    int rZ = GenUtils.randInt(random, 2, 4);
                    BlockUtils.replaceSphere(random.nextInt(999), rX, rY, rZ, base, false, Material.ACACIA_LEAVES);
                }
            }
        }
    }
}
