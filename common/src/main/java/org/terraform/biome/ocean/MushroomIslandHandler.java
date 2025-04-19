package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

/**
 * Mushroom Islands are only oceans that have a custom height generator.
 * They will not actually generate mycelium, they will only build the
 * island itself
 */
public class MushroomIslandHandler extends AbstractOceanHandler {

    public MushroomIslandHandler() {
        super(BiomeType.DEEP_OCEANIC);
        // TODO Auto-generated constructor stub
    }

    @Override
    public double calculateHeight(@NotNull TerraformWorld tw, int x, int z) {

        double height = super.calculateHeight(tw, x, z);
        BiomeSection currentSection = BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z);

        if (currentSection.getBiomeBank() != BiomeBank.MUSHROOM_ISLANDS) {
            currentSection = BiomeSection.getMostDominantSection(tw, x, z);
        }

        // CHECK 4 SURROUNDING BIOMES.
        // If ANY of them are dry, the mushroom island radius will be
        // aggressively shrunk to avoid land connections.
        // If they're still connected uh. Lol I guess.
        float islandRadius = BiomeSection.sectionWidth / 2.5f;
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (currentSection.getRelative(face.getModX(), face.getModZ()).getBiomeBank().isDry()) {
                islandRadius *= 0.65f;
                break;
            }
        }

        // The island itself is a distorted circle.
        // Everything within the circle's radius is aggressively raised above
        // sea level.

        FastNoise circleNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_MUSHROOMISLAND_CIRCLE, world -> {
            FastNoise n = new FastNoise((int) world.getSeed());
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(3);
            n.SetFrequency(0.04f);
            return n;
        });
        SimpleLocation center = currentSection.getCenter();
        int relX = x - center.getX();
        int relZ = z - center.getZ();

        // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
        double equationResult = Math.pow(relX, 2) / Math.pow(islandRadius, 2) + Math.pow(relZ, 2) / Math.pow(
                islandRadius,
                2
        );
        double noise = 1 + 0.7 * circleNoise.GetNoise(relX, relZ);
        if (equationResult <= noise) {

            double supplement = TerraformGenerator.seaLevel - height;

            if (equationResult >= noise * 0.9) {
                return height + supplement * 0.6;
            }
            else if (equationResult >= noise * 0.7) {
                return height + supplement + 10;
            }
            else if (equationResult >= noise * 0.6) {
                return height + supplement + 9;
            }
            else if (equationResult >= noise * 0.5) {
                return height + supplement + 8;
            }
            else if (equationResult >= noise * 0.4) {
                return height + supplement + 7.5;
            }
            else {
                return height + supplement + 7;
            }
        }
        return height;
    }

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.MUSHROOM_FIELDS;
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
        if (rawX >= TerraformGenerator.seaLevel) {
            return;
        }

        if (!BlockUtils.isStoneLike(data.getType(rawX, rawX, rawZ))) {
            return;
        }
        if (GenUtils.chance(random, 1, 150)) { // SEA GRASS/KELP
            CoralGenerator.generateKelpGrowth(data, rawX, rawX + 1, rawZ);
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

                if (rockY >= TerraformGenerator.seaLevel) {
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
        return BiomeBank.MUSHROOM_BEACH;
    }

    @Override
    public boolean forceDefaultToBeach() {
        return true;
    }

}
