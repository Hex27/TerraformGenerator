package org.terraform.structure.ancientcity;

import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

public class AncientCityRuinsPlatform extends AncientCityAbstractRoomPopulator {

    public AncientCityRuinsPlatform(TerraformWorld tw,
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

        FastNoise ruinsNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.STRUCTURE_ANCIENTCITY_RUINS, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() * 11));
            n.SetNoiseType(FastNoise.NoiseType.Simplex);
            n.SetFrequency(0.09f);

            return n;
        });

        int totalPillars = 0;
        for (Entry<Wall, Integer> entry : this.effectiveRoom.getFourWalls(data, 2).entrySet()) {
            Wall w = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {

                w.LPillar(
                        Math.round(Math.abs(5 * ruinsNoise.GetNoise(totalPillars, 0))),
                        AncientCityUtils.deepslateBricks
                );

                w = w.getLeft();
                totalPillars++;
            }
        }

        super.sculkUp(tw, data, this.effectiveRoom);
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() < 20 || room.getWidthZ() < 20;
    }
}
