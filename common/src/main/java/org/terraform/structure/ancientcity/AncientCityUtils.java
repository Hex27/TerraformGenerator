package org.terraform.structure.ancientcity;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.MultipleFacingBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.version.V_1_19;

import java.util.Random;

public class AncientCityUtils {

    public static final Material[] deepslateBricks = new Material[] {
            Material.DEEPSLATE_BRICKS, Material.CRACKED_DEEPSLATE_BRICKS
    };

    public static final Material[] deepslateTiles = new Material[] {
            Material.DEEPSLATE_TILES, Material.CRACKED_DEEPSLATE_TILES
    };
    private static @Nullable Tag<Material> t = null;

    public static void placeSupportPillar(@NotNull SimpleBlock w) {
        Random dud = new Random();
        // w.getUp().lsetType(Material.GRAY_WOOL);
        w.downUntilSolid(dud, Material.DEEPSLATE_BRICKS);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            w.getRelative(face).downUntilSolid(dud, Material.DEEPSLATE_BRICKS);
        }

        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            int height = w.getRelative(face).downUntilSolid(dud, Material.COBBLED_DEEPSLATE_WALL);
            // w.getRelative(face).getUp().lsetType(Material.GRAY_WOOL);
            w.getRelative(face).getDown(height - 1).CorrectMultipleFacing(height);
        }
    }

    @SuppressWarnings("unchecked")
    public static void spreadSculk(@NotNull FastNoise circleNoise,
                                   @NotNull Random random,
                                   float radius,
                                   @NotNull SimpleBlock center)
    {
        if (t == null) {
            try {
                t = (Tag<Material>) Tag.class.getDeclaredField("SCULK_REPLACEABLE_WORLD_GEN").get(null);
            }
            catch (Exception e) {
                TerraformGeneratorPlugin.logger.stackTrace(e);
            }
        }
        boolean placedShrieker = false;
        boolean placedSensor = false;
        boolean placedCatalyst = false;
        for (float nx = -radius; nx <= radius; nx++) {
            for (float nz = -radius; nz <= radius; nz++) {
                for (float ny = -radius; ny <= radius; ny++) {
                    SimpleBlock rel = center.getRelative(Math.round(nx), Math.round(ny), Math.round(nz));

                    if (!rel.isSolid() || rel.getType() == V_1_19.SCULK_VEIN) {
                        continue;
                    }
                    // double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                    double equationResult = Math.pow(nx, 2) / Math.pow(radius, 2) + Math.pow(nz, 2) / Math.pow(
                            radius,
                            2
                    ) + Math.pow(ny, 2) / Math.pow(radius, 2);
                    float noiseVal = circleNoise.GetNoise(rel.getX(), rel.getY(), rel.getZ());
                    if (equationResult <= 1 + 0.7 * noiseVal) {
                        if (BlockUtils.isExposedToNonSolid(rel) || !rel.getDown().isSolid() || !rel.getUp().isSolid()) {
                            // Inner area of the circle is sculk
                            if (t.isTagged(rel.getType()) && equationResult <= 0.7 * (1 + 0.7 * noiseVal)) {
                                assert V_1_19.SCULK != null;
                                rel.setType(V_1_19.SCULK);

                                // If the above is not solid, place some decorations
                                if (!rel.getUp().isSolid()) {
                                    if (!placedCatalyst && GenUtils.chance(random, 1, 40)) {
                                        placedCatalyst = true;
                                        rel.getUp().setType(V_1_19.SCULK_CATALYST);
                                    }
                                    else if (!placedSensor && GenUtils.chance(random, 1, 20)) {
                                        placedSensor = true;
                                        rel.getUp().setType(V_1_19.SCULK_SENSOR);
                                    }
                                    else if (!placedShrieker && GenUtils.chance(random, 1, 90)) {
                                        placedShrieker = true;
                                        rel.getUp().setBlockData(V_1_19.getActiveSculkShrieker());
                                    }
                                }
                            }
                            else if (rel.getType() != V_1_19.SCULK_SHRIEKER
                                     && rel.getType() != V_1_19.SCULK_SENSOR
                                     && !Tag.STAIRS.isTagged(rel.getType())
                                     && !Tag.SLABS.isTagged(rel.getType()))// Outer area are sculk veins
                            {
                                for (BlockFace face : BlockUtils.sixBlockFaces) {
                                    SimpleBlock adj = rel.getRelative(face);
                                    if (adj.isAir()) {
                                        new MultipleFacingBuilder(V_1_19.SCULK_VEIN).setFace(
                                                face.getOppositeFace(),
                                                true
                                        ).apply(adj);
                                    }
                                    else if (adj.getType() == V_1_19.SCULK_VEIN) {
                                        MultipleFacing mf = (MultipleFacing) adj.getBlockData();
                                        mf.setFace(face.getOppositeFace(), true);
                                        adj.setBlockData(mf);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
