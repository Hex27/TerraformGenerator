package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class FrozenOceansHandler extends AbstractOceanHandler {

    public FrozenOceansHandler(BiomeType oceanType) {
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
            return Biome.DEEP_FROZEN_OCEAN;
        }
        return Biome.FROZEN_OCEAN;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.GRAVEL,
                Material.GRAVEL,
                GenUtils.randChoice(rand, Material.STONE, Material.GRAVEL, Material.STONE),
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

        // Set ground near sea level to gravel
        if (surfaceY >= TerraformGenerator.seaLevel - 2) {
            data.setType(rawX, surfaceY, rawZ, Material.GRAVEL);
        }
        else if (surfaceY >= TerraformGenerator.seaLevel - 4) {
            if (random.nextBoolean()) {
                data.setType(rawX, surfaceY, rawZ, Material.GRAVEL);
            }
        }

        // Full ice-sheets
        if (!data.getType(rawX, TerraformGenerator.seaLevel, rawZ).isSolid()) {
            data.setType(rawX, TerraformGenerator.seaLevel, rawZ, Material.ICE);
        }

    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {

        // Spawn rocks
        SimpleLocation[] rocks = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 25, 0.4f);

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

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.ICY_BEACH;
    }

}
