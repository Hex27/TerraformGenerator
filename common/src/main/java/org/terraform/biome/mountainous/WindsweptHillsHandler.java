package org.terraform.biome.mountainous;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.*;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.PhysicsUpdaterPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.CommonMat;
import org.terraform.utils.version.V_1_19;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Random;

public class WindsweptHillsHandler extends AbstractMountainHandler {

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
            data.setType(x, y - i, z, Material.TUFF);
            if (BlockUtils.isExposedToNonSolid(new SimpleBlock(data, x, y - i, z))) {
                depth++;
            }
        }
    }
    private static @NotNull BiomeBlender getBiomeBlender(TerraformWorld tw) {

        return new BiomeBlender(tw, true, true).setRiverThreshold(4).setBlendBeaches(false);
    }
    /**
     * Bakes a spiral into the mountain top
     */
/*    @Override
    public double calculateHeight(@NotNull TerraformWorld tw, int x, int z) {
        double core = HeightMap.CORE.getHeight(tw, x, z);
        double height = super.calculateHeight(tw,x,z);
        return 0.5*(height-core) + core;
    }*/
    @Override
    public void transformTerrain(@NotNull ChunkCache cache,
                                 TerraformWorld tw,
                                 Random random,
                                 ChunkGenerator.@NotNull ChunkData chunk,
                                 int x,
                                 int z,
                                 int chunkX,
                                 int chunkZ)
    {
        int rawX = (chunkX<<4)+x;
        int rawZ = (chunkZ<<4)+z;
        short height = cache.getTransformedHeight(x,z);
        float[][] spiralArr = calculateSpiral();
        BiomeSection sect = BiomeBank.getBiomeSectionFromBlockCoords(tw, rawX, rawZ);
        if(!sect.equals(BiomeSection.getMostDominantSection(tw,rawX,rawZ)))
            return;
        SimpleLocation bounds = sect.getLowerBounds();
        double sprVal = spiralArr[rawX - bounds.x()][rawZ - bounds.z()];
        //               * getBiomeBlender(tw).getEdgeFactor(BiomeBank.WINDSWEPT_HILLS, rawX, rawZ);
        if(sprVal == 0) return;

        double depth = 3+5*sect.getSectionRandom(74398).nextDouble();

        short newHeight = (short) (height + depth * sprVal);

        //This writes DOWN, so newHeight is minY
        chunk.setRegion(x,newHeight+1,z,
                x+1,height+1,z+1, CommonMat.AIR);
        chunk.setBlock(x,newHeight,z, V_1_19.MUD);
        cache.writeTransformedHeight(x,z,newHeight);
    }

    private static SoftReference<float[][]> SPIRAL;

    private static final int KERNEL_RAD = 2;
    private static final int KERNEL_WIDTH = KERNEL_RAD*2+1;
    private static final int KERNEL_VOL = KERNEL_WIDTH*KERNEL_WIDTH;

    private static float[][] calculateSpiral(){
        if(SPIRAL != null){
            var arr = SPIRAL.get();
            if(arr != null) return arr;
        }
        int radius = BiomeSection.sectionWidth>>1; //sectionWidth always divides by 2
        float[][] spiralArray = new float[BiomeSection.sectionWidth][BiomeSection.sectionWidth];
        float[][] blurredArray = new float[BiomeSection.sectionWidth][BiomeSection.sectionWidth];
        for(double t = 0; t <= 4*Math.PI; t+= 0.05){
            //This equation's value is bounded by 0.35*sectionWidth
            double coeff = (BiomeSection.sectionWidth*0.35)*t/(4*Math.PI);
            int x = (int) (coeff * Math.sin(t));
            int z = (int) (coeff * Math.cos(t));
            assert x < BiomeSection.sectionWidth/2;
            assert x >= -radius;
            assert z < BiomeSection.sectionWidth/2;
            assert z >= -radius;
            spiralArray[x+radius][z+radius] = -10;
        }
        //Box Blur spiral array
        for(int nx = 0; nx < spiralArray.length; nx++)
            for(int nz = 0; nz < spiralArray[0].length; nz++)
            {
                byte agg = 0;
                for(int bx = Math.max(0,nx-KERNEL_RAD);
                    bx <= Math.min(spiralArray.length-1, nx+KERNEL_RAD);
                    bx++)
                    agg += spiralArray[bx][nz];
                blurredArray[nx][nz] = agg;
            }
        for(int nx = 0; nx < spiralArray.length; nx++)
            for(int nz = 0; nz < spiralArray[0].length; nz++)
            {
                byte agg = 0;
                for(int bz = Math.max(0,nz-KERNEL_RAD);
                    bz <= Math.min(spiralArray[0].length-1, nz+KERNEL_RAD);
                    bz++)
                    agg += spiralArray[nx][bz];
                blurredArray[nx][nz] += agg;
                blurredArray[nx][nz] = (blurredArray[nx][nz])/KERNEL_VOL;
            }

/*        TerraformGeneratorPlugin.logger.info("ARRAY CONTENTS:");
        for (float[] bytes : blurredArray) {
            TerraformGeneratorPlugin.logger.info(Arrays.toString(bytes));

        }*/
        SPIRAL = new SoftReference<>(blurredArray);
        return blurredArray;
    }
    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.WINDSWEPT_HILLS;
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
                        Material.ANDESITE
                ),
                GenUtils.randChoice(rand, Material.ANDESITE, Material.STONE, Material.STONE),
                GenUtils.randChoice(rand, Material.ANDESITE, Material.STONE, Material.STONE),
                GenUtils.randChoice(rand, Material.ANDESITE, Material.STONE, Material.STONE),
                GenUtils.randChoice(rand, Material.ANDESITE, Material.STONE, Material.STONE),
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
            //dirtStack(data, random, rawX, surfaceY, rawZ);
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
                    //dirtStack(data, random, rawX + nx, surfaceY, rawZ + nz);
                }
            }
        }

    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw, Random random, @NotNull PopulatorDataAbstract data) {
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
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.ROCKY_BEACH;
    }
}
