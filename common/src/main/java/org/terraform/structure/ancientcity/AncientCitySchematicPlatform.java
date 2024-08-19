package org.terraform.structure.ancientcity;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Random;

/**
 * This used to be for just towers, but since the formula for paste-schematic, then
 * delete a random corner works well, this will be used for any schematic.
 *
 * @author Leonard
 */
public class AncientCitySchematicPlatform extends AncientCityAbstractRoomPopulator {

    private final String @NotNull [] smallSchematics = new String[] {
            "ancient-city/ancient-city-wood-tower-1",
            "ancient-city/ancient-city-rock-tower-1",
            "ancient-city/ancient-city-lamp",
            };
    private final String @NotNull [] mediumSchematics = new String[] {
            "ancient-city/ancient-city-hot-tub", "ancient-city/ancient-city-warehouse",
            };
    private final String @NotNull [] largeSchematics = new String[] {
            "ancient-city/ancient-city-pantheon",
            };
    public AncientCitySchematicPlatform(TerraformWorld tw,
                                        HashSet<SimpleLocation> occupied,
                                        RoomLayoutGenerator gen,
                                        Random rand,
                                        boolean forceSpawn,
                                        boolean unique)
    {
        super(tw, gen, rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);

        int platformSize = 0; // 0 - small, 1 - medium, 2 - large
        if (this.effectiveRoom.getWidthX() >= 10 && this.effectiveRoom.getWidthZ() >= 10) {
            platformSize = 1;
        }
        if (this.effectiveRoom.getWidthX() >= 16 && this.effectiveRoom.getWidthZ() >= 16) {
            platformSize = 2;
        }

        String chosenSchema = switch (platformSize) {
            case 0 -> {
                TerraformGeneratorPlugin.logger.info("Small Schematic");
                yield smallSchematics[rand.nextInt(smallSchematics.length)];
            }
            case 1 -> {
                TerraformGeneratorPlugin.logger.info("Medium Schematic");
                yield mediumSchematics[rand.nextInt(mediumSchematics.length)];
            }
            case 2 -> {
                TerraformGeneratorPlugin.logger.info("Large Schematic");
                yield largeSchematics[rand.nextInt(largeSchematics.length)];
            }
            default -> null;
        };

        try {
            SimpleBlock center = this.effectiveRoom.getCenterSimpleBlock(data).getUp();
            TerraSchematic schema = TerraSchematic.load(chosenSchema, center);
            schema.parser = new AncientCitySchematicParser();
            schema.setFace(BlockUtils.getDirectBlockFace(rand));
            schema.apply();

            // 12 blocks up, possibly spawn an air sphere to make the thing broken.
            if (GenUtils.chance(rand, 1, 2)) {
                new SphereBuilder(
                        new Random(),
                        center.getRelative(BlockUtils.getXZPlaneBlockFace(rand), 4).getUp(11),
                        Material.CAVE_AIR
                ).setHardReplace(true).setRadius((float) GenUtils.randDouble(rand, 3, 5)).build();
            }
        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
        super.sculkUp(tw, data, this.effectiveRoom);
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}
