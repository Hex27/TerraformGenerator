package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.OrientableBuilder;

import java.util.Random;

public class PlainsHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.PLAINS;
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
    public void populateSmallItems(TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK
            && data.getType(rawX, surfaceY+1, rawZ) == Material.AIR
            && !BlockUtils.isWet(new SimpleBlock(data,
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
                else {
                    switch (GenUtils.randInt(random, 1, 10)) {
                        case 0, 1 -> PlantBuilder.BUSH.build(data, rawX, surfaceY + 1, rawZ);
                        case 2 -> BlockUtils.pickTallFlower().build(data, rawX, surfaceY + 1, rawZ);
                        default -> BlockUtils.pickFlower().build(data, rawX, surfaceY + 1, rawZ);
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
        // Pumpkin Patch
        if (GenUtils.chance(1, 1000)) {
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
                    PlantBuilder.PUMPKIN.build(target);
                }
            }
        }

        // Melon Patch
        if (GenUtils.chance(1, 1000)) {
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
                    PlantBuilder.MELON.build(target);
                }
            }
        }

        // Small trees or grass poffs
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), TConfig.c.BIOME_PLAINS_TREE_INTERVAL);

        for (SimpleLocation sLoc : trees) {
            int highestY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            if (BlockUtils.isWet(new SimpleBlock(data, sLoc.getX(), highestY + 1, sLoc.getZ()))) {
                continue;
            }

            sLoc = sLoc.getAtY(highestY);
            switch(random.nextInt(5)){
                case 0,1 -> {
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
                case 2,3 ->{
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
                //Fallen tree
                case 4 -> {
                    Wall w = new Wall(data, sLoc.getUp(), BlockUtils.getDirectBlockFace(random));
                    int length = GenUtils.randInt(1,3);
                    for(int i = -length; i <= length; i++){
                        if(!w.getFront(i).isAir()
                           || !w.getFront(i).getDown().isSolid()) break;
                        w.getFront(i).setBlockData(new OrientableBuilder(Material.OAK_LOG)
                                .setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection())).get());
                    }
                }
            }

        }

    }
}
