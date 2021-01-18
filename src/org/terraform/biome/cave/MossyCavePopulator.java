package org.terraform.biome.cave;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MossyCavePopulator extends AbstractCavePopulator {
    private static boolean genned = false;

    @Override
    public void populate(TerraformWorld tw, Random random,
                         PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                if (!(tw.getBiomeBank(x, GenUtils.getHighestGround(data, x, z), z).getCavePop()
                        instanceof MossyCavePopulator))
                    continue;
                for (int[] pair : GenUtils.getCaveCeilFloors(data, x, z)) {
                    int ceil = pair[0]; //non-solid
                    int floor = pair[1]; //solid

                    if (!genned) {
                        genned = true;
                        TerraformGeneratorPlugin.logger.info("Spawning mossy cave at " + x + "," + floor + "," + z);
                    }
                    int caveHeight = ceil - floor;
                    if (caveHeight <= 3) continue;

                    //Don't touch slabbed floors or stalagmites
                    if (data.getType(x, floor, z).toString().endsWith("SLAB") ||
                            data.getType(x, floor, z).toString().endsWith("WALL"))
                        continue;

                    //=========================
                    //Upper decorations
                    //=========================

                    //Stalactites
                    if (GenUtils.chance(random, 1, 25)) {
                        int h = caveHeight / 4;
                        if (h < 1) h = 1;
                        if (h > 4) h = 4;
                        Wall w = new Wall(new SimpleBlock(data, x, ceil, z), BlockFace.NORTH);
                        if (w.getRelative(0, 1, 0).getType() == Material.SAND || w.getRelative(0, 1, 0).getType() == Material.SANDSTONE)
                            w.downLPillar(random, h, Material.SANDSTONE_WALL);
                        else if (BlockUtils.isStoneLike(w.getRelative(0, 1, 0).getType()))
                            w.downLPillar(random, h, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
                    }

                    //=========================
                    //Lower decorations 
                    //=========================

                    //Stalagmites
                    if (GenUtils.chance(random, 1, 25)) {
                        int h = caveHeight / 4;
                        if (h < 1) h = 1;
                        if (h > 4) h = 4;
                        Wall w = new Wall(new SimpleBlock(data, x, floor + 1, z), BlockFace.NORTH);
                        if (w.getType() == Material.CAVE_AIR) {
                            if (w.getRelative(0, 1, 0).getType() == Material.SAND || w.getRelative(0, 1, 0).getType() == Material.SANDSTONE)
                                w.LPillar(h, random, Material.SANDSTONE_WALL);
                            else if (BlockUtils.isStoneLike(w.getRelative(0, 1, 0).getType()))
                                w.LPillar(h, random, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
                        }

                    } else if (GenUtils.chance(random, 1, 25)) { //Slabbing
                        SimpleBlock base = new SimpleBlock(data, x, floor + 1, z);
                        //Only next to spots where there's some kind of solid block.
                        if (base.getType() == Material.CAVE_AIR)
                            for (BlockFace face : BlockUtils.directBlockFaces) {
                                if (base.getRelative(face).getType().isSolid()) {
                                    base.setType(Material.STONE_SLAB);
                                    break;
                                }
                            }
                    } else if (GenUtils.chance(random, 1, 35)) { //Shrooms :3
                        if (data.getType(x, floor + 1, z) == Material.CAVE_AIR)
                            data.setType(x, floor + 1, z, GenUtils.randMaterial(
                                    Material.RED_MUSHROOM,
                                    Material.BROWN_MUSHROOM));
                    }
                }
            }
        }
    }
}
