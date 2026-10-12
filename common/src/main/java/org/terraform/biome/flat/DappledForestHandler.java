package org.terraform.biome.flat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.NewFractalTreeBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import org.terraform.utils.version.V_26_3;
import org.terraform.utils.version.Version;

import java.util.ArrayList;
import java.util.Random;

public class DappledForestHandler extends BiomeHandler {
    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return V_26_3.DAPPLED_FOREST;
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
        FastNoise pathNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_FOREST_PATHNOISE, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() * 12));
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(3);
            n.SetFrequency(0.07f);
            return n;
        });

        if (pathNoise.GetNoise(rawX, rawZ) > 0.32) {
            if (GenUtils.chance(random, 99, 100) && data.getBiome(rawX, rawZ) == getBiome() && BlockUtils.isDirtLike(
                    data.getType(rawX, surfaceY, rawZ)))
            {
                data.setType(rawX, surfaceY, rawZ, Material.COARSE_DIRT);
            }
        }
        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK) {
            if (GenUtils.chance(random, 1, 10)) {
                //Air check skipped, as PlantBuilder will check
                // Grass & mushrooms
                switch(random.nextInt(4)){
                    case 0 -> PlantBuilder.BROWN_MUSHROOM.build(data, rawX, surfaceY + 1, rawZ);
                    case 1 -> PlantBuilder.RED_SHRUB.build(data, rawX, surfaceY + 1, rawZ);
                    //50%
                    default -> PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                }
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {
        // Most forest chunks have a big tree
        if (TConfig.c.TREES_DAPPLEDFOREST_BIG_ENABLED && GenUtils.chance(random, 7, 10)) {
            int treeX = GenUtils.randInt(random, 2, 12) + data.getChunkX() * 16;
            int treeZ = GenUtils.randInt(random, 2, 12) + data.getChunkZ() * 16;
            if (data.getBiome(treeX, treeZ) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, treeX, treeZ);

                if (BlockUtils.isDirtLike(data.getType(treeX, treeY, treeZ))) {
                    if(GenUtils.chance(random, 8,10))
                        FractalTypes.Tree.FOREST
                                .build(tw, new SimpleBlock(data, treeX, treeY, treeZ),
                                        DappledForestHandler::PoplarMutator);
                    else
                        FractalTypes.Tree.TAIGA_BIG
                                .build(tw, new SimpleBlock(data, treeX, treeY, treeZ));
                }
            }
        }

        // Small trees
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 9);

        for (SimpleLocation sLoc : trees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() && BlockUtils.isDirtLike(data.getType(sLoc.getX(),
                    sLoc.getY(),
                    sLoc.getZ())))
            {
                if(random.nextInt(6) == 0)
                {
                    //Fallen trees
                    Wall w = new Wall(data, sLoc.getUp(), BlockUtils.getDirectBlockFace(random));
                    int length = GenUtils.randInt(2, 4);

                    if(TConfig.c.FEATURE_PLANTS_ENABLED){
                        for(int i = -length; i <= length; i++) {
                            if(w.getFront(i).isSolid()
                               || !w.getFront(i).getDown().isSolid()) break;
                            Wall target = w.getFront(i);
                            target.setBlockData(new OrientableBuilder(V_26_3.POPLAR_LOG)
                                     .setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection())).get());
                            if(target.getUp().isAir()
                               && random.nextInt(5) == 0)
                                PlantBuilder.build(target.getUp(), PlantBuilder.BROWN_MUSHROOM);

                            if(Version.VERSION.isAtLeast(Version.v26_3))
                                for(BlockFace face:BlockUtils.getAdjacentFaces(w.getDirection())){
                                    if(!target.getRelative(face).isAir()
                                       || random.nextInt(3) != 0) continue;

                                    BlockData shelfShroom = Bukkit.createBlockData(V_26_3.SHELF_MUSHROOM);
                                    ((Directional) shelfShroom).setFacing(face);
                                    ((Ageable) shelfShroom).setAge(random.nextInt(((Ageable)shelfShroom).getMaximumAge()));
                                    target.getRelative(face).setBlockData(shelfShroom);
                                }
                        }
                    }
                }
                else
                    FractalTypes.Tree.NORMAL_SMALL.build(tw, new SimpleBlock(data, sLoc.getX(), sLoc.getY(), sLoc.getZ()),
                            DappledForestHandler::PoplarMutator);
            }
        }

     }

    //Also for sapling class
    public static void PoplarMutator(NewFractalTreeBuilder nt){

        nt.getFractalLeaves().setMaterial(
                GenUtils.randChoice(
                        V_26_3.RED_POPLAR_LEAVES,
                        V_26_3.ORANGE_POPLAR_LEAVES,
                        V_26_3.YELLOW_POPLAR_LEAVES
                )
        );
        nt.setBranchMaterial(V_26_3.POPLAR_LOG)
          .setRootMaterial(V_26_3.POPLAR_WOOD)
          .setSpawnBees(false);
    }

    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.DAPPLEDFOREST_BEACH;
    }
}
