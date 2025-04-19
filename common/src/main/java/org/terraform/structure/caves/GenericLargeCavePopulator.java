package org.terraform.structure.caves;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.SeaPickle;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.StalactiteBuilder;

import java.util.Random;

public class GenericLargeCavePopulator extends RoomPopulatorAbstract {

    public GenericLargeCavePopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    protected void populateFloor(SimpleBlock floor, int waterLevel) {
    }

    protected void populateCeilFloorPair(@NotNull SimpleBlock ceil, @NotNull SimpleBlock floor, int height) {
        // Stalactites
        if (GenUtils.chance(rand, 1, 200)) {
            int r = 2;
            int h = GenUtils.randInt(rand, height / 4, (int) ((3f / 2f) * (height / 2f)));
            new StalactiteBuilder(BlockUtils.stoneOrSlateWall(ceil.getY())).setSolidBlockType(BlockUtils.stoneOrSlate(
                    ceil.getY())).makeSpike(ceil, r, h, false);
        }

        // Stalagmites
        if (GenUtils.chance(rand, 1, 200)) {
            int r = 2;
            int h = GenUtils.randInt(rand, height / 4, (int) ((3f / 2f) * (height / 2f)));
            new StalactiteBuilder(BlockUtils.stoneOrSlateWall(floor.getY())).setSolidBlockType(BlockUtils.stoneOrSlate(
                    floor.getY())).makeSpike(floor, r, h, true);
        }

        // Sea pickles
        if (BlockUtils.isWet(floor.getUp()) && GenUtils.chance(rand, 4, 100)) {
            SeaPickle sp = (SeaPickle) Bukkit.createBlockData(Material.SEA_PICKLE);
            sp.setPickles(GenUtils.randInt(1, 2));
            floor.getUp().setBlockData(sp);
        }
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, CubeRoom room) {
        if (!(room instanceof LargeCaveRoomPiece caveRoom)) {
            throw new NotImplementedException("room for LargeCavePopulator was not a LargeCaveRoomPiece");
        }
        assert data.getChunkX() == room.getX() >> 4;
        assert data.getChunkZ() == room.getZ() >> 4;

        caveRoom.ceilFloorPairs.forEach((l, pair) -> {
            if (pair.z() != LargeCaveRoomCarver.FLOOR_CEIL_NULL) {
                populateFloor(new SimpleBlock(data, l.x(), pair.z(), l.z()), caveRoom.waterLevel);
            }
            if (pair.x() != LargeCaveRoomCarver.FLOOR_CEIL_NULL
                && pair.z() != LargeCaveRoomCarver.FLOOR_CEIL_NULL) {
                SimpleBlock ceil = new SimpleBlock(data, l.x(), pair.x(), l.z());
                SimpleBlock floor = new SimpleBlock(data, l.x(), pair.z(), l.z());
                int height = ceil.getY() - floor.getY();

                populateCeilFloorPair(ceil, floor, height);
            }
        });
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}
