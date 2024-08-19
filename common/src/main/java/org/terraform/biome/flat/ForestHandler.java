package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.ArrayList;
import java.util.Random;

public class ForestHandler extends BiomeHandler {

    private static void spawnRock(@NotNull Random rand, @NotNull PopulatorDataAbstract data, int x, int y, int z) {
        ArrayList<int[]> locations = new ArrayList<>(20);
        locations.add(new int[]{x, y, z});

        locations.add(new int[]{x, y + 1, z});
        locations.add(new int[]{x + 1, y + 1, z});
        locations.add(new int[]{x - 1, y + 1, z});
        locations.add(new int[]{x, y + 1, z + 1});
        locations.add(new int[]{x, y + 1, z - 1});

        locations.add(new int[]{x + 1, y, z});
        locations.add(new int[]{x - 1, y, z});
        locations.add(new int[]{x, y, z + 1});
        locations.add(new int[]{x, y, z - 1});
        locations.add(new int[]{x + 1, y, z});
        locations.add(new int[]{x - 1, y, z + 1});
        locations.add(new int[]{x + 1, y, z + 1});
        locations.add(new int[]{x - 1, y, z - 1});

        locations.add(new int[]{x, y - 1, z});
        locations.add(new int[]{x + 1, y - 1, z});
        locations.add(new int[]{x - 1, y - 1, z});
        locations.add(new int[]{x, y - 1, z + 1});
        locations.add(new int[]{x, y - 1, z - 1});

        for (int[] coords : locations) {
            int Tx = coords[0];
            int Ty = coords[1];
            int Tz = coords[2];
            if (!data.getType(Tx, Ty, Tz).isSolid() ||
                    data.getType(Tx, Ty, Tz).toString().contains("LEAVES")) {
                data.setType(Tx, Ty, Tz, GenUtils.randChoice(rand,
                        Material.COBBLESTONE, Material.STONE, Material.MOSSY_COBBLESTONE));
            }
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.FOREST;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.GRASS_BLOCK, 35, Material.PODZOL, 3),
                Material.DIRT,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld tw, @NotNull Random random, int rawX, int surfaceY, int rawZ, @NotNull PopulatorDataAbstract data) {
        FastNoise pathNoise = NoiseCacheHandler.getNoise(
        		tw, 
        		NoiseCacheEntry.BIOME_FOREST_PATHNOISE, 
        		world -> {
        	        FastNoise n = new FastNoise((int) (world.getSeed() * 12));
        	        n.SetNoiseType(NoiseType.SimplexFractal);
        	        n.SetFractalOctaves(3);
        	        n.SetFrequency(0.07f);
        	        return n;
        });

        if (pathNoise.GetNoise(rawX, rawZ) > 0.3) {
            if (GenUtils.chance(random, 99, 100) &&
                    data.getBiome(rawX, rawZ) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType(rawX, surfaceY, rawZ)))
                data.setType(rawX, surfaceY, rawZ, Material.DIRT_PATH);
        }
        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK) {
            if (GenUtils.chance(random, 1, 10)) {
                if (data.getType(rawX, surfaceY + 1, rawZ) != Material.AIR) return;
                // Grass & Flowers
                PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                if (random.nextBoolean()) {
                    PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
                } else {
                    BlockUtils.pickFlower().build(data, rawX, surfaceY + 1, rawZ);
                }
            }
        }
    }

	@Override
	public void populateLargeItems(@NotNull TerraformWorld tw, @NotNull Random random, @NotNull PopulatorDataAbstract data) {
        // Most forest chunks have a big tree
        if (TConfigOption.TREES_FOREST_BIG_ENABLED.getBoolean() && GenUtils.chance(random, 6, 10)) {
            int treeX = GenUtils.randInt(random, 2, 12) + data.getChunkX() * 16;
            int treeZ = GenUtils.randInt(random, 2, 12) + data.getChunkZ() * 16;
            if (data.getBiome(treeX, treeZ) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, treeX, treeZ);

                if (BlockUtils.isDirtLike(data.getType(treeX, treeY, treeZ)))
                    FractalTypes.Tree.FOREST.build(tw, new SimpleBlock(data, treeX, treeY, treeZ));
            }
        }

        // Small trees
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 8);

        for (SimpleLocation sLoc : trees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
            sLoc.setY(treeY);
            if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
                FractalTypes.Tree.NORMAL_SMALL.build(tw, new SimpleBlock(data, sLoc.getX(),sLoc.getY(),sLoc.getZ()));
            }
        }

        // Small rocks
        SimpleLocation[] rocks = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 10);

        for (SimpleLocation sLoc : rocks) {
            sLoc.setY(GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ()));
            if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome()) {
            	if (BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ())) ||
                        data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()) == Material.COBBLESTONE ||
                        data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()) == Material.MOSSY_COBBLESTONE ||
                        data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()) == Material.STONE) {
                    int ny = GenUtils.randInt(random, -1, 1);
                    spawnRock(random, data, sLoc.getX(),sLoc.getY()+ny,sLoc.getZ());
                    if (GenUtils.chance(random, 1, 3))
                        spawnRock(random, data, GenUtils.randInt(random, -1, 1) + sLoc.getX(), sLoc.getY() + ny + 1, sLoc.getZ() + GenUtils.randInt(random, -1, 1));
                }
            }
        }
	}
}
