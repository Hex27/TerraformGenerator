package org.terraform.biome.cavepopulators;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.MultipleFacingBuilder;
import org.terraform.utils.version.V_1_19;

import java.util.Random;

public class DeepDarkClusterCavePopulator extends AbstractCaveClusterPopulator {

    // private boolean isForLargeCave;
    public DeepDarkClusterCavePopulator(float radius) {
        super(radius);
        // this.isForLargeCave = isForLargeCave;
    }

    public static void oneUnit(TerraformWorld tw, @NotNull Random random, @NotNull SimpleBlock origin) {
        if (origin.isSolid()) {
            return;
        }
        new DeepDarkClusterCavePopulator(0f).oneUnit(
                tw,
                random,
                origin.findStonelikeCeiling(50),
                origin.findStonelikeFloor(50),
                false
        );
        origin.setType(Material.GLASS);
    }

    @Override
    public void oneUnit(TerraformWorld tw,
                        @NotNull Random random,
                        @Nullable SimpleBlock ceil,
                        @Nullable SimpleBlock floor,
                        boolean boundary)
    {
        if (ceil == null || floor == null) {
            return;
        }

        // Already processed
        if (ceil.getType() == V_1_19.SCULK || floor.getType() == V_1_19.SCULK) {
            return;
        }
        // =========================
        // Upper decorations
        // =========================
        // int caveHeight = ceil.getY() - floor.getY();

        // Don't decorate wet areas
        if (!BlockUtils.isWet(ceil.getDown())) {
            // Don't touch slabbed floors or stalagmites
            if (Tag.SLABS.isTagged(floor.getType()) || Tag.WALLS.isTagged(floor.getType())) {
                return;
            }

            // Ceiling is ALWAYS sculk/veins
            if (BlockUtils.isStoneLike(ceil.getType())) {
                ceil.setType(V_1_19.SCULK);
            }
            else {
                MultipleFacing sculkVein = (MultipleFacing) Bukkit.createBlockData(V_1_19.SCULK_VEIN);
                sculkVein.setFace(BlockFace.UP, true);
                ceil.getDown().setBlockData(sculkVein);
            }
        }

        // =========================
        // Lower decorations
        // =========================

        // If floor is submerged, then don't touch it.
        if (BlockUtils.isWet(floor.getUp())) {
            return;
        }

        // Ground is sculk
        // Ceiling is ALWAYS sculk/veins
        if (BlockUtils.isStoneLike(ceil.getType())) {
            floor.setType(V_1_19.SCULK);
        }
        else {
            MultipleFacing sculkVein = (MultipleFacing) Bukkit.createBlockData(V_1_19.SCULK_VEIN);
            sculkVein.setFace(BlockFace.DOWN, true);
            floor.getUp().setBlockData(sculkVein);
        }


        if (GenUtils.chance(random, 1, 20)) { // Sculk Catalysts
            floor.getUp().setType(V_1_19.SCULK_CATALYST);
        }
        else if (GenUtils.chance(random, 1, 17)) { // Sculk Sensors
            floor.getUp().setType(V_1_19.SCULK_SENSOR);
        }
        else if (GenUtils.chance(random, 1, 25))
        // Sculk Shrieker
        {
            floor.getUp().setType(V_1_19.SCULK_SHRIEKER);
        }


        // =========================
        // Attempt to replace close-by walls with sculk.
        // =========================

        SimpleBlock target = floor;
        while (target.getY() != ceil.getY()) {
            for (BlockFace face : BlockUtils.directBlockFaces) {
                SimpleBlock rel = target.getRelative(face);
                if (BlockUtils.isStoneLike(rel.getType())) {
                    rel.setType(V_1_19.SCULK);
                    if (BlockUtils.isAir(target.getType()) && GenUtils.chance(random, 1, 5)) {
                        new MultipleFacingBuilder(V_1_19.SCULK_VEIN).setFace(face, true).apply(target);
                    }
                }
            }
            target = target.getUp();
        }

        // =========================
        // Biome Setter
        // =========================
        if (TerraformGeneratorPlugin.injector.getICAData(ceil.getPopData()) instanceof PopulatorDataICABiomeWriterAbstract data) {
            while (floor.getY() < ceil.getY()) {
                data.setBiome(floor.getX(), floor.getY(), floor.getZ(), V_1_19.DEEP_DARK);
                floor = floor.getUp();
            }
        }
    }


}
