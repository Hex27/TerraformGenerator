package org.terraform.structure.ancientcity;

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
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Random;

public class AncientCityLargePillarRoomPopulator extends AncientCityAbstractRoomPopulator {

    public AncientCityLargePillarRoomPopulator(TerraformWorld tw,
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
        // super.populate(data, room);
        this.effectiveRoom = room;

        // Room flooring
        int[] lowerCorner = effectiveRoom.getLowerCorner(0);
        int[] upperCorner = effectiveRoom.getUpperCorner(0);
        int y = effectiveRoom.getY();
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                SimpleBlock b = new SimpleBlock(data, x, y, z);

                // Fuzz the sides to give a sense of ruin
                if (x == lowerCorner[0] || x == upperCorner[0] || z == lowerCorner[1] || z == upperCorner[1]) {
                    if (rand.nextBoolean()) {
                        b.lsetType(AncientCityUtils.deepslateBricks);
                    }
                }
                else {
                    b.lsetType(AncientCityUtils.deepslateBricks);
                }
            }
        }

        FastNoise ruinsNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.STRUCTURE_ANCIENTCITY_RUINS, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() * 11));
            n.SetNoiseType(FastNoise.NoiseType.Simplex);
            n.SetFrequency(0.09f);

            return n;
        });

        try {
            // Place upwards
            SimpleBlock center = room.getCenterSimpleBlock(data).getUp();
            int maxHeight = 70;

            AncientCityPillarSchematicParser lastParser = null;
            while (maxHeight > 0) {
                TerraSchematic schema = TerraSchematic.load("ancient-city/ancient-city-pillar-segment", center);
                lastParser = new AncientCityPillarSchematicParser();
                schema.parser = lastParser;
                schema.setFace(BlockUtils.getDirectBlockFace(rand));
                schema.apply();

                center = center.getUp(3);

                // If around 30% of the pillar fails to place, break out of the loop and
                // place ruined versions of the upper pillar
                // TerraformGeneratorPlugin.logger.info("FR = " + lastParser.calculateFailRate());
                if (lastParser.calculateFailRate() > 0.3f) {
                    break;
                }
                maxHeight--;
            }

            // Make the top of the pillar a bit fuzzed.
            for (SimpleBlock b : lastParser.getTouchedOffsets()) {
                b.getUp()
                 .LPillar((int) (5 * ruinsNoise.GetNoise(b.getX(), b.getZ())), AncientCityUtils.deepslateBricks);
            }

            // Place downwards
            center = room.getCenterSimpleBlock(data).getDown(3);
            maxHeight = 70;

            lastParser = null;
            while (maxHeight > 0) {
                TerraSchematic schema = TerraSchematic.load("ancient-city/ancient-city-pillar-segment", center);
                lastParser = new AncientCityPillarSchematicParser();
                schema.parser = lastParser;
                schema.setFace(BlockUtils.getDirectBlockFace(rand));
                schema.apply();

                center = center.getDown(3);

                // If around 30% of the pillar fails to place, break out of the loop and
                // place ruined versions of the upper pillar
                // TerraformGeneratorPlugin.logger.info("FR = " + lastParser.calculateFailRate());
                if (lastParser.calculateFailRate() > 0.3f
                    || center.getY() <= TerraformGeneratorPlugin.injector.getMinY())
                {
                    break;
                }
                maxHeight--;
            }

            // Make the bottom of the pillar a bit fuzzed.
            for (SimpleBlock b : lastParser.getTouchedOffsets()) {
                b.getDown(3).downLPillar(new Random(),
                        (int) Math.abs(5 * ruinsNoise.GetNoise(b.getX(), b.getZ())),
                        AncientCityUtils.deepslateBricks
                );
            }

        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }

        super.sculkUp(tw, data, room);
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() <= 20 && room.getWidthZ() <= 20;
    }
}
