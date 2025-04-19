package org.terraform.structure.ancientcity;

import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayoutGenerator;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

public class AncientCityAltarPopulator extends AncientCityAbstractRoomPopulator {

    public AncientCityAltarPopulator(TerraformWorld tw,
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
        // Generates outer walls
        for (Entry<Wall, Integer> entry : this.effectiveRoom.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey();
            Wall center = null;
            boolean shouldPlaceAltar = true;
            for (int i = 0; i < entry.getValue(); i++) {
                if (i == entry.getValue() / 2) {
                    center = w;
                }
                if (containsPaths.contains(w)) {
                    shouldPlaceAltar = false;
                    break;
                }
                w = w.getLeft();
            }
            if (shouldPlaceAltar) {
                String altarFile;
                if (entry.getValue() < 7) {
                    altarFile = "ancient-city/ancient-city-altar-small";
                }
                else if (entry.getValue() < 11) {
                    altarFile = "ancient-city/ancient-city-altar-medium";
                }
                else {
                    altarFile = "ancient-city/ancient-city-altar-large";
                }

                try {
                    TerraSchematic schema = TerraSchematic.load(altarFile, center);
                    schema.parser = new AncientCitySchematicParser();
                    schema.setFace(center.getDirection());
                    schema.apply();
                }
                catch (FileNotFoundException e) {
                    TerraformGeneratorPlugin.logger.stackTrace(e);
                }

                // Misc pillars leading up to the altar
                int pillarSpacing = entry.getValue() / 3;
                for (int i = pillarSpacing; i < Math.min(effectiveRoom.getWidthX(), effectiveRoom.getWidthZ()); i += 3)
                {
                    center.getFront(i)
                          .getLeft(pillarSpacing)
                          .LPillar(rand.nextInt(room.getHeight() / 3), AncientCityUtils.deepslateBricks);
                    center.getFront(i)
                          .getRight(pillarSpacing)
                          .LPillar(rand.nextInt(room.getHeight() / 3), AncientCityUtils.deepslateBricks);
                }

                super.sculkUp(tw, data, this.effectiveRoom);
                return;
            }
        }
        super.sculkUp(tw, data, this.effectiveRoom);
    }


    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() % 2 == 1 || room.getWidthZ() % 2 == 1;
    }
}
