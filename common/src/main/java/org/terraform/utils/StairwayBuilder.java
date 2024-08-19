package org.terraform.utils;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.ArrayList;
import java.util.Random;

/**
 * Used to generate 1-block wide stairways from a starting point
 */
public class StairwayBuilder {

    private final Material[] stairTypes;
    private boolean carveAirSpace = true;
    private BlockFace stairDirection = BlockFace.DOWN;
    private Material[] downTypes;
    private boolean stopAtWater = false;
    private int stopAtY = Short.MIN_VALUE;
    private boolean angled = false;
    private int maxExtensionForward = 10;
    private boolean upwardsCarveUntilNotSolid = true;
    private boolean upwardsCarveUntilSolid = false;

    public StairwayBuilder(Material @NotNull ... stairTypes) {
        this.stairTypes = stairTypes;

        // Infer downTypes
        ArrayList<Material> downTypes = new ArrayList<>();
        for (Material mat : stairTypes) {
            Material toAdd = Material.matchMaterial(mat.toString().replace("_STAIRS", ""));
            if (toAdd != null) {
                downTypes.add(toAdd);
            }
        }
        this.downTypes = new Material[downTypes.size()];

        for (int i = 0; i < downTypes.size(); i++) {
            this.downTypes[i] = downTypes.get(i);
        }

    }

    public @NotNull StairwayBuilder setStopAtY(int y) {
        this.stopAtY = y;
        return this;
    }

    public @NotNull StairwayBuilder setCarveAirSpace(boolean carveAirSpace) {
        this.carveAirSpace = carveAirSpace;
        return this;
    }

    public @NotNull StairwayBuilder setUpwardsCarveUntilSolid(boolean carve) {
        this.upwardsCarveUntilSolid = carve;
        return this;
    }

    public @NotNull StairwayBuilder setUpwardsCarveUntilNotSolid(boolean carve) {
        this.upwardsCarveUntilNotSolid = carve;
        return this;
    }

    public @NotNull StairwayBuilder setDownTypes(Material... mat) {
        this.downTypes = mat;
        return this;
    }

    public @NotNull StairwayBuilder build(@NotNull Wall start) {
        if (stairDirection == BlockFace.DOWN) { // Stairway extends downwards
            int threshold = 5;
            BlockFace extensionDir = start.getDirection();
            while (continueCondition(start)) {

                if (threshold == 0) {
                    start.setType(downTypes);
                    start.getDown().downUntilSolid(new Random(), downTypes);
                    extensionDir = BlockUtils.getTurnBlockFace(new Random(), extensionDir);
                    start = start.getRelative(extensionDir);
                }

                Material stairType = stairTypes[new Random().nextInt(stairTypes.length)];
                if (stairType.toString().endsWith("STAIRS")) {
                    new StairBuilder(stairType).setFacing(extensionDir.getOppositeFace()).apply(start);
                }
                else {
                    start.setType(stairType);
                }

                start.getDown().downUntilSolid(new Random(), downTypes);

                if (angled) {
                    threshold--;
                }
                start = start.getRelative(extensionDir).getDown();
            }

            // If it is on water, build a pathway forward.
            // Hope that there's something there.
            // Stop on oak slabs specifically too, because that's the path type
            if (stopAtWater && start.get().getType() != Material.OAK_SLAB && BlockUtils.isWet(start.get())) {
                for (int i = 0; i < maxExtensionForward; i++) {
                    if (start.isSolid()) {
                        break;
                    }

                    start.downUntilSolid(new Random(), downTypes);
                    start = start.getFront();
                }
            }
        }
        else if (stairDirection == BlockFace.UP) { // Stairway extends upwards

            int threshold = 5;
            BlockFace extensionDir = start.getDirection();
            while (continueCondition(start)) {

                if (threshold == 0) {
                    start = start.getDown();
                    if (carveAirSpace) {
                        start.getUp().Pillar(3, new Random(), Material.AIR);
                    }
                    start.setType(downTypes);
                    start.getDown().downUntilSolid(new Random(), downTypes);
                    extensionDir = BlockUtils.getTurnBlockFace(new Random(), extensionDir);
                    start = start.getRelative(extensionDir).getUp();
                }

                Material stairType = stairTypes[new Random().nextInt(stairTypes.length)];
                if (stairType.toString().endsWith("STAIRS")) {
                    new StairBuilder(stairTypes).setFacing(extensionDir).apply(start);
                }
                else {
                    start.setType(stairType);
                }

                start.getDown().downUntilSolid(new Random(), downTypes);

                // This space is required for movement
                if (carveAirSpace) {
                    start.getUp().Pillar(3, new Random(), Material.AIR);
                    start.getUp(2).getRelative(extensionDir).setType(Material.AIR);
                }

                if (angled) {
                    threshold--;
                }
                start = start.getRelative(extensionDir).getUp();
            }

        }
        else {
            TerraformGeneratorPlugin.logger.error(
                    "StairwayBuilder was told to spawn stairway with non up/down stair direction!");
        }


        return this;
    }

    private boolean continueCondition(@NotNull Wall target) {

        if (this.stairDirection == BlockFace.DOWN) {
            if (stopAtY != Short.MIN_VALUE) {
                if (target.getY() == stopAtY) {
                    return false;
                }
            }

            if (stopAtWater && BlockUtils.isWet(target.get())) {
                return false;
            }

            return !target.isSolid();
        }
        else {
            if (stopAtY != Short.MIN_VALUE) {
                if (target.getY() == stopAtY + 1) {
                    return false;
                }
            }

            // Continue carving upwards until the area isn't solid anymore.
            if (upwardsCarveUntilNotSolid) {
                return target.isSolid();
            }

            // Continue carving upwards until the area is solid anymore.
            if (upwardsCarveUntilSolid) {
                return !target.isSolid();
            }

            return true;
        }
    }

    /**
     * Only used when stopAtWater is true.
     * Refers to the maximum length of the pathway generated when
     * the stairway hits water.
     */
    public @NotNull StairwayBuilder setMaxExtensionForward(int extension) {
        this.maxExtensionForward = extension;
        return this;
    }

    public @NotNull StairwayBuilder setStairwayDirection(BlockFace stairDirection) {
        this.stairDirection = stairDirection;
        return this;
    }

    public @NotNull StairwayBuilder setStopAtWater(boolean stopAtWater) {
        this.stopAtWater = stopAtWater;
        return this;
    }

    public @NotNull StairwayBuilder setAngled(boolean angled) {
        this.angled = angled;
        return this;
    }

}
