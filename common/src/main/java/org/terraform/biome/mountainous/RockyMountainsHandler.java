package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.PhysicsUpdaterPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.Random;

public class RockyMountainsHandler extends AbstractMountainHandler {

    private static void dirtStack(@NotNull PopulatorDataAbstract data, @NotNull Random rand, int x, int y, int z) {
        data.setType(x, y, z, Material.GRASS_BLOCK);

        if (GenUtils.chance(rand, 1, 10)) {
            PlantBuilder.GRASS.build(data, x, y + 1, z);
        }

        int depth = GenUtils.randInt(rand, 3, 7);
        for (int i = 1; i < depth; i++) {
            if (!BlockUtils.isStoneLike(data.getType(x, y - i, z))) {
                break;
            }
            data.setType(x, y - i, z, Material.DIRT);
            if (BlockUtils.isExposedToNonSolid(new SimpleBlock(data, x, y - i, z))) {
                depth++;
            }
        }
    }

    public static void placeWaterFall(@NotNull TerraformWorld tw, int seed, @NotNull SimpleBlock base) {
        float radius = 4f;

        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (float x = -radius; x <= radius; x++) {
            for (float y = -radius / 2f; y <= radius / 2f; y++) {
                for (float z = -radius; z <= radius; z++) {

                    SimpleBlock rel = base.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(x, 2) / Math.pow(radius, 2)
                                            + Math.pow(y, 2) / Math.pow(radius, 2)
                                            + Math.pow(z, 2) / Math.pow(radius, 2);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())) {
                        // if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){
                        if (y > 0) { // Upper half of sphere is air
                            rel.setType(Material.AIR);
                        }
                        else if (rel.isSolid()) {
                            // Lower half is water, if replaced block was solid.
                            rel.setType(Material.WATER);
                            PhysicsUpdaterPopulator.pushChange(
                                    tw.getName(),
                                    new SimpleLocation(rel.getX(), rel.getY(), rel.getZ())
                            );
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.JAGGED_PEAKS;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                GenUtils.randChoice(
                        rand,
                        Material.STONE,
                        Material.STONE,
                        Material.STONE,
                        Material.STONE,
                        Material.COBBLESTONE
                ),
                GenUtils.randChoice(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randChoice(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randChoice(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
                GenUtils.randChoice(rand, Material.COBBLESTONE, Material.STONE, Material.STONE),
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
        // Don't touch submerged blocks
        if (surfaceY < TerraformGenerator.seaLevel) {
            return;
        }
        // Make patches of dirt that extend on the mountain sides
        if (GenUtils.chance(random, 1, 25)) {
            dirtStack(data, random, rawX, surfaceY, rawZ);
            for (int nx = -2; nx <= 2; nx++) {
                for (int nz = -2; nz <= 2; nz++) {
                    if (GenUtils.chance(random, 1, 5)) {
                        continue;
                    }
                    surfaceY = GenUtils.getHighestGround(data, rawX + nx, rawZ + nz);

                    // Another check, make sure relative position isn't underwater.
                    if (surfaceY < TerraformGenerator.seaLevel) {
                        continue;
                    }
                    dirtStack(data, random, rawX + nx, surfaceY, rawZ + nz);
                }
            }
        }

    }

    public boolean checkWaterfallSpace(@NotNull SimpleBlock b) {
        // Only bother if the waterfall is at least 15 blocks up
        if (b.getY() < TerraformGenerator.seaLevel + 15) {
            return false;
        }
        for (int i = 0; i < 5; i++) {
            if (!b.getRelative(0, -i, 0).isSolid()) {
                return false;
            }
        }
        return BlockUtils.isExposedToNonSolid(b.getDown(4));
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw, Random random, @NotNull PopulatorDataAbstract data) {

        // Waterfalls only spawn 1 in 30 times (rolled after checking position.).
        for (int rawX = data.getChunkX() * 16; rawX < data.getChunkX() * 16 + 16; rawX++) {
            for (int rawZ = data.getChunkZ() * 16; rawZ < data.getChunkZ() * 16 + 16; rawZ++) {
                int surfaceY = GenUtils.getTransformedHeight(data.getTerraformWorld(), rawX, rawZ);
                if (HeightMap.getTrueHeightGradient(data, rawX, rawZ, 3) > 1.5) {
                    if (HeightMap.CORE.getHeight(tw, rawX, rawZ) - HeightMap.getRawRiverDepth(tw, rawX, rawZ)
                        < TerraformGenerator.seaLevel)
                    {
                        // If this face is at least 4 blocks wide, carve a waterfall opening
                        SimpleBlock block = new SimpleBlock(data, rawX, surfaceY, rawZ);
                        if (checkWaterfallSpace(block) && GenUtils.chance(tw.getHashedRand(rawX, surfaceY, rawZ),
                                1,
                                30
                        ))
                        {
                            block = block.getDown(4);
                            placeWaterFall(tw, rawX + 11 * rawZ + 31 * surfaceY, block);
                            break;
                        }
                    }
                }
            }
        }

        // Small trees
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 14);

        // Trees on shallow areas
        for (SimpleLocation sLoc : trees) {
            if (HeightMap.getTrueHeightGradient(data, sLoc.getX(), sLoc.getZ(), 3) < 1.4) { // trees
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(treeY);
                if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
                    && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ())))
                {
                    new FractalTreeBuilder(FractalTypes.Tree.NORMAL_SMALL).build(
                            tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY(),
                            sLoc.getZ()
                    );
                }
            }
        }
    }

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.ROCKY_BEACH;
    }
}
