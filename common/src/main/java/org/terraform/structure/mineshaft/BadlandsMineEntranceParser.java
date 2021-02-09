package org.terraform.structure.mineshaft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Slab;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.GenUtils;

public class BadlandsMineEntranceParser extends SchematicParser {
    static FastNoise noise = null;
    private boolean didPlaceLantern = false;
    static double getNoise(int x, int y, int z) {
        if (noise == null) {
            noise = new FastNoise();
            noise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            noise.SetFractalOctaves(2);
            noise.SetFrequency(0.08f);
        }

        return noise.GetNoise(x, y, z);
    }

    @Override
    public void applyData(SimpleBlock block, BlockData data) {
        double noiseValue = getNoise(block.getX(), block.getY(), block.getZ());

        switch(data.getMaterial()) {
            case RED_CONCRETE: {
                if (noiseValue > 0) {
                    super.applyData(block, Bukkit.createBlockData(Material.DARK_OAK_FENCE));
                } else {
                    Slab s = (Slab) Bukkit.createBlockData(Material.DARK_OAK_SLAB);
                    s.setType(Slab.Type.TOP);
                    super.applyData(block, s);
                }
                break;
            }
            case GREEN_CONCRETE: {
                if (noiseValue > 0.5 && block.getType().isAir())
                    BlockUtils.setVines(block.getPopData(), block.getX(), block.getY(), block.getZ(), 2);

                break;
            }
            case DARK_OAK_FENCE: {
                if (willPlaceFence(block)) {
                    block.lsetBlockData(data);
                } else if (willPlaceFence(block.getRelative(0, 1, 0))
                        && !didPlaceLantern) {
                    Lantern l = (Lantern) Bukkit.createBlockData(Material.LANTERN);
                    l.setHanging(true);
                    super.applyData(block, l);

                    didPlaceLantern = true;
                }
                break;
            }
            case RED_SAND: {
                if (noiseValue < 0) {
                    SimpleBlock b = block;
                    while (b.getType() == Material.RED_SAND) {
                        b = b.getRelative(0, 1, 0);
                    }
                    b.lsetType(Material.RED_SAND);
                }
                break;
            }
            case DARK_OAK_SLAB: {
                Slab s = (Slab) data;
                if (s.getType() == Slab.Type.BOTTOM)
                    block.lsetBlockData(data);
                else super.applyData(block, data);
                break;
            }
            case OAK_STAIRS: {
                block.lsetBlockData(data);
                break;
            }
            default: super.applyData(block, data);
        }
    }

    boolean willPlaceFence(SimpleBlock block) {
        return getNoise(block.getX(), block.getY(), block.getZ()) < 0.4;
    }
}
