package org.terraform.biome.flat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Rotatable;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class DarkForestHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.DARK_FOREST;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld tw, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
      
        boolean spawnHeads = TConfigOption.BIOME_DARK_FOREST_SPAWN_HEADS.getBoolean() 
        		&& GenUtils.chance(random, 1, 100);

        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK) {
            if (GenUtils.chance(random, 1, 10)) {
                if (data.getType(rawX, surfaceY + 1, rawZ) != Material.AIR) return;
                //Only grass and mushrooms
                data.setType(rawX, surfaceY + 1, rawZ, Material.GRASS);
                if (random.nextInt(3) != 0) {
                    BlockUtils.setDoublePlant(data, rawX, surfaceY + 1, rawZ, Material.TALL_GRASS);
                } else {
                    Material mushroom = Material.RED_MUSHROOM;
                    if (random.nextBoolean())
                        mushroom = Material.BROWN_MUSHROOM;
                    data.setType(rawX, surfaceY + 1, rawZ, mushroom);
                }
            }
        }

        if (spawnHeads && GenUtils.chance(random, 1, 50)) {
            if (BlockUtils.isDirtLike(data.getType(rawX, surfaceY, rawZ))) {
                Rotatable skull = (Rotatable) Bukkit.createBlockData(Material.PLAYER_HEAD);
                skull.setRotation(BlockUtils.getXZPlaneBlockFace(random));

                data.setBlockData(rawX, surfaceY + 1, rawZ, skull);
            }
        }

    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
		SimpleLocation[] bigTrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 20);
		SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 10);
		SimpleLocation[] smallDecorations = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 7);
		
		// Big trees and giant mushrooms
        for (SimpleLocation sLoc : bigTrees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
            sLoc.setY(treeY);
            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
                if (GenUtils.chance(random, 2, 10)) {
                	int choice = random.nextInt(3);
                	FractalTypes.Mushroom type = switch(choice) {
                        case 0 -> FractalTypes.Mushroom.GIANT_RED_MUSHROOM;
                        case 1 -> FractalTypes.Mushroom.GIANT_BROWN_MUSHROOM;
                        default -> FractalTypes.Mushroom.GIANT_BROWN_FUNNEL_MUSHROOM;
                    };
                    new MushroomBuilder(type).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ());
                } else if (TConfigOption.TREES_DARK_FOREST_BIG_ENABLED.getBoolean()) {
                    FractalTypes.Tree.DARK_OAK_BIG_TOP.build(tw, new SimpleBlock(data,sLoc));
                }
            }
        }

        // Small trees
        for (SimpleLocation sLoc : trees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc.setY(treeY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
            	//new FractalTreeBuilder(FractalTypes.Tree.DARK_OAK_SMALL)
                FractalTypes.Tree.DARK_OAK_SMALL.build(tw, new SimpleBlock(data,sLoc));
            }
        }

        // Small mushrooms and rocks
        for (SimpleLocation sLoc : smallDecorations) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc.setY(treeY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
                int choice = random.nextInt(5);
                switch(choice) {
                case 0:
                	new MushroomBuilder(FractalTypes.Mushroom.SMALL_POINTY_RED_MUSHROOM).build(tw, data, sLoc.getX(),sLoc.getY()+1,sLoc.getZ());
                	break;
                case 1:
                	new MushroomBuilder(FractalTypes.Mushroom.SMALL_BROWN_MUSHROOM).build(tw, data, sLoc.getX(),sLoc.getY()+1,sLoc.getZ());
                	break;
                case 2:
                	new MushroomBuilder(FractalTypes.Mushroom.SMALL_RED_MUSHROOM).build(tw, data, sLoc.getX(),sLoc.getY()+1,sLoc.getZ());
                	break;
                case 3:
                    for (int i = 0; i < GenUtils.randInt(3, 6); i++) {
                        spawnRock(random, data, sLoc.getX(), sLoc.getY() + i + 1, sLoc.getZ());
                    }
                    break;
                default:
                	new MushroomBuilder(FractalTypes.Mushroom.TINY_RED_MUSHROOM).build(tw, data, sLoc.getX(),sLoc.getY()+1,sLoc.getZ());
                	break;
                }
            }
        }
	}
	
    private static void spawnRock(Random rand, PopulatorDataAbstract data, int x, int y, int z) {
        ArrayList<int[]> locations = new ArrayList<>();
        locations.add(new int[]{x, y, z});
        locations.add(new int[]{x, y + 2, z});

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

        for (int[] coords : locations) {
            int Tx = coords[0];
            int Ty = coords[1];
            int Tz = coords[2];
            if (!data.getType(Tx, Ty, Tz).isSolid() ||
                    data.getType(Tx, Ty, Tz).toString().contains("LEAVES")) {
                BlockUtils.setDownUntilSolid(Tx, Ty, Tz, data,
                        Material.COBBLESTONE, Material.MOSSY_COBBLESTONE,
                        Material.STONE, Material.CHISELED_STONE_BRICKS,
                        Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
                        Material.MOSSY_STONE_BRICKS);
            }
        }
    }

    public BiomeBank getBeachType() {
    	return BiomeBank.DARK_FOREST_BEACH;
    }
    
    //River type. This will be used instead if the heightmap got carved into a river.
    public BiomeBank getRiverType() {
    	return BiomeBank.DARK_FOREST_RIVER;
    }
    
}
