package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SavannaHandler extends BiomeHandler {

    private static void makeYellowPatch(int x, int y, int z, PopulatorDataAbstract data, Random random) {
        int length = GenUtils.randInt(6, 16);
        int nx = x;
        int nz = z;
        while(length-- > 0) {
            if(BlockUtils.isDirtLike(data.getType(nx, y, nz)) &&
                    data.getType(nx, y + 1, nz) == Material.AIR)
                data.setType(nx, y, nz, Material.GRASS_PATH);

            switch(random.nextInt(5)) {  // The direction chooser
                case 0:
                    nx++;
                    break;
                case 2:
                    nz++;
                    break;
                case 3:
                    nx--;
                    break;
                case 4:
                    nz--;
                    break;
            }

            y = GenUtils.getTrueHighestBlock(data, nx, nz);
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//		
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Biome getBiome() {
        return Biome.SAVANNA;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[] {GenUtils.randMaterial(rand, Material.GRASS_BLOCK, Material.GRASS_BLOCK, Material.GRASS_BLOCK, Material.GRASS_BLOCK, Material.GRASS_BLOCK,
                Material.GRASS_BLOCK, Material.GRASS_BLOCK, Material.GRASS_BLOCK, Material.GRASS_BLOCK, Material.GRASS_BLOCK, Material.GRASS_BLOCK, Material.COARSE_DIRT),
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

        for(int i = 0; i < GenUtils.randInt(random, 1, 3); i++) {
            int x = data.getChunkX() * 16 + GenUtils.randInt(0, 15);
            int z = data.getChunkZ() * 16 + GenUtils.randInt(0, 15);
            int y = GenUtils.getHighestGround(data, x, z);
            if(data.getBiome(x, y, z) != getBiome()) continue;
            makeYellowPatch(x, y, z, data, random);
        }

        //Large savanna trees are very very rare
        if(TConfigOption.TREES_SAVANNA_BIG_ENABLED.getBoolean() && GenUtils.chance(1, 100)) {
            int treeX = GenUtils.randInt(random, 0, 15) + data.getChunkX() * 16;
            int treeZ = GenUtils.randInt(random, 0, 15) + data.getChunkZ() * 16;
            if(data.getBiome(treeX, treeZ) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, treeX, treeZ);
                if(BlockUtils.isDirtLike(data.getType(treeX, treeY, treeZ)))
                    new FractalTreeBuilder(FractalTypes.Tree.SAVANNA_BIG).build(world, data, treeX, treeY, treeZ);
            }
        }
        //Savanna trees are very spaced. low chance. Only spawned when no big tree in the same chunk
        else if(GenUtils.chance(1, 10)) {
            int treeX = GenUtils.randInt(random, 0, 15) + data.getChunkX() * 16;
            int treeZ = GenUtils.randInt(random, 0, 15) + data.getChunkZ() * 16;
            if(data.getBiome(treeX, treeZ) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, treeX, treeZ);
                if(BlockUtils.isDirtLike(data.getType(treeX, treeY, treeZ)))
                    new FractalTreeBuilder(FractalTypes.Tree.SAVANNA_SMALL).build(world, data, treeX, treeY, treeZ);
            }
        }

        for(int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for(int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if(data.getBiome(x, y, z) != getBiome()) continue;

                if(data.getType(x, y, z) == Material.GRASS_BLOCK
                        && !data.getType(x, y + 1, z).isSolid()) {
                    //Dense grass
                    if(GenUtils.chance(random, 5, 10)) {
                        BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                    }

                    //Bushes
                    if(GenUtils.chance(random, 1, 200)) {
                        SimpleBlock base = new SimpleBlock(data, x, y + 1, z);
                        int rX = GenUtils.randInt(random, 2, 4);
                        int rY = GenUtils.randInt(random, 2, 4);
                        int rZ = GenUtils.randInt(random, 2, 4);
                        BlockUtils.replaceSphere(random.nextInt(999), rX, rY, rZ, base, false, Material.ACACIA_LEAVES);
                    }
                }


            }
        }
    }
}
