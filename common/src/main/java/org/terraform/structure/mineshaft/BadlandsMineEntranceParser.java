package org.terraform.structure.mineshaft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Slab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.noise.FastNoise;

public class BadlandsMineEntranceParser extends SchematicParser {
    static @Nullable FastNoise noise = null;
    private boolean didPlaceLantern = false;

    // Don't cache, as it does not change based on terraformworld.
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
    public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
        double noiseValue = getNoise(block.getX(), block.getY(), block.getZ());

        switch (data.getMaterial()) {
            case RED_CONCRETE: {
                if (noiseValue > 0) {
                    super.applyData(block, Bukkit.createBlockData(Material.DARK_OAK_FENCE));
                }
                else {
                    Slab s = (Slab) Bukkit.createBlockData(Material.DARK_OAK_SLAB);
                    s.setType(Slab.Type.TOP);
                    super.applyData(block, s);
                }
                break;
            }
            case GREEN_CONCRETE: {
                if (noiseValue > 0.5 && BlockUtils.isAir(block.getType())) {
                    for (BlockFace face : BlockUtils.directBlockFaces) {
                        SimpleBlock b = block.getRelative(face);

                        if (b.getType() == Material.OAK_LOG) {
                            MultipleFacing dir = (MultipleFacing) Bukkit.createBlockData(Material.VINE);
                            dir.setFace(face, true);
                            super.applyData(block, dir);
                        }
                    }
                }

                break;
            }
            case DARK_OAK_FENCE: {
                if (willPlaceFence(block)) {
                    block.lsetBlockData(data);
                }
                else if (willPlaceFence(block.getUp()) && !didPlaceLantern) {
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
                        b = b.getUp();
                    }
                    b.lsetType(Material.RED_SAND);
                }
                break;
            }
            case DARK_OAK_SLAB: {
                Slab s = (Slab) data;
                if (s.getType() == Slab.Type.BOTTOM) {
                    block.lsetBlockData(data);
                }
                else {
                    super.applyData(block, data);
                }
                break;
            }
            case OAK_STAIRS: {
                block.lsetBlockData(data);
                break;
            }
            default:
                super.applyData(block, data);
        }
    }

    boolean willPlaceFence(@NotNull SimpleBlock block) {
        return getNoise(block.getX(), block.getY(), block.getZ()) < 0.4;
    }
}
