package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class PlainsVillagePondPopulator extends RoomPopulatorAbstract {

    public PlainsVillagePondPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {

        // Check if this area is flat enough to be a pond
        int[] lowerCorner = room.getLowerCorner();
        int[] upperCorner = room.getUpperCorner();

        int lowest = 256;
        int highest = -1;

        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                int ground = GenUtils.getHighestGround(data, x, z);
                if (ground < lowest) {
                    lowest = ground;
                }
                if (ground > highest) {
                    highest = ground;
                }
            }
        }

        // Height difference is too big. Don't bother.
        if (highest - lowest >= 5) {
            return;
        }

        SimpleBlock core = new SimpleBlock(data, room.getX(), 0, room.getZ());
        core = core.getGround();

        // Carve hole in ground
        int depth = GenUtils.randInt(3, 5);
        BlockUtils.replaceLowerSphere(rand.nextInt(12222),
                (room.getWidthX() / 2f) - 1.5f,
                (float) depth,
                (room.getWidthZ() / 2f) - 1.5f,
                core,
                true,
                Material.AIR
        );

        // Try to replace bottom with water

        // Find lowest ground block in the area
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                int ground = GenUtils.getHighestGround(data, x, z);
                if (ground < lowest) {
                    lowest = ground;
                }
                if (ground > highest) {
                    highest = ground;
                }
            }
        }


        ArrayList<SimpleBlock> lakeWaterBlocks = getLakeWaterBlocks(core, lowerCorner, upperCorner, lowest);

        if (lakeWaterBlocks.isEmpty()) {
            return; // Don't bother if the pond is too small;
        }

        int pondSurface = -1; // this variable will now be used to store the water level of the pond.
        for (SimpleBlock s : lakeWaterBlocks) {
            s.setType(Material.WATER);
            if (s.getY() > pondSurface) {
                pondSurface = s.getY();
            }
        }

        boolean placedJobBlock = false;
        // Place side decorations
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                SimpleBlock target = new SimpleBlock(core.getPopData(), x, 0, z).getGround();

                // Decorate side of the pond
                if (target.getUp().getType() == Material.AIR) {

                    // Make sure it's the side of the pond
                    boolean valid = false;
                    for (BlockFace face : BlockUtils.directBlockFaces) {
                        if (target.getRelative(face).getType() == Material.WATER) {
                            valid = true;
                        }
                    }
                    if (!valid) {
                        continue;
                    }

                    target = target.getUp();
                    if (GenUtils.chance(1, 4)) { // Sugar Canes
                        PlantBuilder.SUGAR_CANE.build(target, rand, 2, 5);
                    }
                    else if (GenUtils.chance(1, 4)) { // Leaves
                        PlantBuilder.OAK_LEAVES.build(target);
                    }
                    else if (GenUtils.chance(1, 4)) { // Double Plants
                        PlantBuilder.build(
                                core.getPopData(),
                                target.getX(),
                                target.getY(),
                                target.getZ(),
                                PlantBuilder.LARGE_FERN,
                                PlantBuilder.TALL_GRASS
                        );
                    }
                    else if (!placedJobBlock && TConfig.areDecorationsEnabled() && GenUtils.chance(2, 5)) {
                        target.setType(Material.BARREL);
                        placedJobBlock = true;
                    }
                }
                else if (target.getUp().getType() == Material.WATER) // Decorate pond surface and pond floor
                {
                    target = target.getUp();
                    if (GenUtils.chance(1, 5)) // Lily pads
                    {
                        PlantBuilder.LILY_PAD.build(target.getAtY(pondSurface).getUp());
                    }
                    else if (GenUtils.chance(1, 5)) // Kelp growth
                    {
                        CoralGenerator.generateKelpGrowth(data, x, target.getY(), z);
                    }
                    else if (GenUtils.chance(1, 7)) // sea pickle growth
                    {
                        CoralGenerator.generateSeaPickles(data, x, target.getY(), z);
                    }

                    if (TConfig.areAnimalsEnabled() && GenUtils.chance(1, 20)) { // spawn fish
                        core.getPopData()
                            .addEntity(target.getX(), target.getY(), target.getZ(), EntityType.TROPICAL_FISH);
                    }
                }
            }
        }


    }

    private @NotNull ArrayList<SimpleBlock> getLakeWaterBlocks(@NotNull SimpleBlock core,
                                                               int @NotNull [] lowerCorner,
                                                               int @NotNull [] upperCorner,
                                                               int lowestPoint)
    {
        int layer = 0;
        ArrayList<SimpleBlock> lakeBlocks = new ArrayList<>();
        while (true) {
            boolean layerValid = true;
            // Check if the corners are solid. If they aren't this layer isn't valid.

            for (int x : new int[] {lowerCorner[0], upperCorner[0]}) {
                for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                    if (!core.getPopData().getType(x, lowestPoint + layer, z).isSolid()) {
                        layerValid = false;
                        break;
                    }
                    if (!layerValid) {
                        break;
                    }
                }
            }

            if (layerValid) {
                for (int z : new int[] {lowerCorner[1], upperCorner[1]}) {
                    for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
                        if (!core.getPopData().getType(x, lowestPoint + layer, z).isSolid()) {
                            layerValid = false;
                            break;
                        }
                        if (!layerValid) {
                            break;
                        }
                    }
                }
            }

            if (!layerValid) {
                break;
            }

            // Add all airspace simpleblocks to the lake blocks
            for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
                for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                    if (!core.getPopData().getType(x, lowestPoint + layer, z).isSolid()) {
                        lakeBlocks.add(new SimpleBlock(core.getPopData(), x, lowestPoint + layer, z));
                    }
                }
            }

            layer++;
        }

        return lakeBlocks;
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() <= 10 && (room.getWidthZ() > 5 && room.getWidthX() > 5);
    }
}
