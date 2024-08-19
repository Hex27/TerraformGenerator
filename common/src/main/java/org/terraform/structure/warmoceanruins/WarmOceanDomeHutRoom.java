package org.terraform.structure.warmoceanruins;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CylinderBuilder;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.blockdata.ChestBuilder;

import java.util.Random;

public class WarmOceanDomeHutRoom extends WarmOceanBaseRoom {
    public WarmOceanDomeHutRoom(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }


    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);

        Material decorator = new Material[] {
                Material.POLISHED_DIORITE, Material.POLISHED_GRANITE, Material.POLISHED_ANDESITE
        }[rand.nextInt(3)];

        // This is just a cylinder with an ellipse on it
        SimpleBlock centre = room.getCenterSimpleBlock(data);
        float radius = Math.min(room.getWidthX(), room.getWidthZ()) / 3f;
        int cylSize = GenUtils.randInt(rand, 3, 5);

        // Carves the actual dome, with sandstone flooring
        new CylinderBuilder(
                rand,
                centre.getDown(),
                Material.SMOOTH_SANDSTONE,
                Material.SANDSTONE
        ).setStartFromZero(true).setRadius(radius).setRY(cylSize + 1).setNoiseMagnitude(0).setHardReplace(true).build();
        new CylinderBuilder(rand, centre, Material.WATER).setStartFromZero(true)
                                                         .setRadius(radius - 1)
                                                         .setRY(cylSize)
                                                         .setNoiseMagnitude(0)
                                                         .setHardReplace(true)
                                                         .build();
        new SphereBuilder(rand, centre.getUp(cylSize - 1), Material.SMOOTH_SANDSTONE, Material.SANDSTONE).setRadius(
                radius).setSphereType(SphereBuilder.SphereType.UPPER_SEMISPHERE).setSmooth(true).build();
        new SphereBuilder(rand, centre.getUp(cylSize - 1), Material.WATER).setRadius(radius - 1)
                                                                          .setSphereType(SphereBuilder.SphereType.UPPER_SEMISPHERE)
                                                                          .setSmooth(true)
                                                                          .setHardReplace(true)
                                                                          .build();

        // Place chest
        BlockFace entrance = BlockUtils.getDirectBlockFace(rand);
        new ChestBuilder(Material.CHEST).setFacing(entrance)
                                        .setLootTable(TerraLootTable.UNDERWATER_RUIN_SMALL)
                                        .apply(centre.getRelative(entrance.getOppositeFace(), ((int) radius) - 2));

        // Carve door
        centre.getRelative(entrance, (int) radius).physicsSetType(Material.WATER, true);
        centre.getUp().getRelative(entrance, (int) radius).physicsSetType(Material.WATER, true);

        // Holes
        for (int i = 0; i < GenUtils.randInt(rand, 2, 4); i++) {
            BlockUtils.replaceWaterSphere(i * room.getX() * room.getZ(), GenUtils.randInt(2, 4), centre.getRelative(
                    GenUtils.getSign(rand) * rand.nextInt((int) radius),
                    3 + rand.nextInt((int) radius),
                    GenUtils.getSign(rand) * rand.nextInt((int) radius)
            ));
        }

        // Decorate stuff
        centre.getUp((int) (cylSize + radius - 1)).setType(decorator);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (face == entrance) {
                continue;
            }

            centre.getUp(cylSize / 2).getRelative(face, (int) radius).setType(decorator);
        }

        // Drowned
        centre.addEntity(EntityType.DROWNED);
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() < 25;
    }
}
