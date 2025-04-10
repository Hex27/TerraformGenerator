package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class CoralReefOceanHandler extends AbstractOceanHandler {

    public CoralReefOceanHandler(BiomeType oceanType) {
        super(oceanType);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public @NotNull Biome getBiome() {
        if (this.oceanType == BiomeType.DEEP_OCEANIC) {
            return Biome.DEEP_LUKEWARM_OCEAN;
        }
        return Biome.LUKEWARM_OCEAN;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.SAND,
                Material.SAND,
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.SAND, Material.SAND),
                GenUtils.randChoice(rand, Material.STONE),
                GenUtils.randChoice(rand, Material.STONE)
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
        // Set ground near sea level to sand
        if (surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.SAND);
        }
        else if (surfaceY >= TerraformGenerator.seaLevel - 4) {
            if (random.nextBoolean()) {
                data.setType(rawX, surfaceY, rawZ, Material.SAND);
            }
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, surfaceY, rawZ))) {
            return;
        }
        if (GenUtils.chance(random, 10, 100)) { // SEA GRASS/KELP
            CoralGenerator.generateKelpGrowth(data, rawX, surfaceY + 1, rawZ);
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {
        // Large Corals
        SimpleLocation[] largeCorals = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 25, 0.4f);

        for (SimpleLocation sLoc : largeCorals) {
            int coralY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            if (coralY >= TerraformGenerator.seaLevel) {
                continue;
            }
            sLoc = sLoc.getAtY(coralY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()) {
                TreeDB.spawnRandomGiantCoral(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                if (data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()) == Material.GRAVEL) {
                    BlockUtils.replaceCircularPatch(random.nextInt(9999),
                            4,
                            new SimpleBlock(data, sLoc),
                            Material.SAND
                    );
                }
            }
        }

        SimpleLocation[] smallCorals = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 4, 0.5f);

        for (SimpleLocation sLoc : smallCorals) {
            int coralY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            if (coralY >= TerraformGenerator.seaLevel) {
                continue;
            }
            sLoc = sLoc.getAtY(coralY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() && !data.getType(sLoc.getX(),
                    sLoc.getY() + 1,
                    sLoc.getZ()).isSolid())
            {
                CoralGenerator.generateCoral(data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                if (data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()) == Material.GRAVEL) {
                    BlockUtils.replaceCircularPatch(random.nextInt(9999),
                            2,
                            new SimpleBlock(data, sLoc),
                            Material.SAND
                    );
                }
            }
        }

        // Spawn rocks
        SimpleLocation[] rocks = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 30, 0.4f);

        for (SimpleLocation sLoc : rocks) {
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()) {
                int rockY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(rockY);
                if (data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()) != Material.GRAVEL) {
                    continue;
                }

                BlockUtils.replaceSphere(random.nextInt(9987),
                        (float) GenUtils.randDouble(random, 3, 7),
                        (float) GenUtils.randDouble(random, 2, 4),
                        (float) GenUtils.randDouble(random, 3, 7),
                        new SimpleBlock(data, sLoc),
                        true,
                        GenUtils.randChoice(Material.STONE, Material.GRANITE, Material.ANDESITE, Material.DIORITE)
                );
            }
        }

    }


}
