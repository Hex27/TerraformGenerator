package org.terraform.structure.caves;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.CoordPair;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.carver.RoomCarver;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.datastructs.CompressedCoordBools;

import java.util.ArrayDeque;

public class LargeCaveRoomCarver extends RoomCarver {
    final Material fluid;
    int waterLevel = -64;
    public static final int FLOOR_CEIL_NULL = TerraformGeneratorPlugin.injector.getMinY()-1;

    public LargeCaveRoomCarver(Material fluid) {
        this.fluid = fluid;
    }

    @Override
    public void carveRoom(@NotNull PopulatorDataAbstract data, CubeRoom room, Material... wallMaterial) {
        if (!(room instanceof LargeCaveRoomPiece caveRoom)) {
            throw new NotImplementedException("room for LargeCaveRoomCarver was not a LargeCaveRoomPiece");
        }

        //Nothing to process
        if(caveRoom.startingLoc == null) return;

        //Perform another BFS on this
        CompressedCoordBools explored = new CompressedCoordBools();
        ArrayDeque<SimpleLocation> queue = new ArrayDeque<>();
        explored.set(caveRoom.startingLoc);
        queue.add(caveRoom.startingLoc);
        while (!queue.isEmpty())
        {
            SimpleLocation loc = queue.remove();
            boolean boundary = caveRoom.boundaries.isSet(loc);

            //Populate BFS list
            for(BlockFace face:BlockUtils.sixBlockFaces){
                SimpleLocation neighbour = loc.getRelative(face);
                boolean isNeighbour = caveRoom.toCarve.isSet(neighbour);
                if(isNeighbour && !explored.isSet(neighbour))
                {
                    explored.set(neighbour);
                    queue.add(neighbour);
                }
            }

            //Process loc
            if (!boundary) {
                data.setType(loc.getX(), loc.getY(), loc.getZ(), loc.getY() > waterLevel ? Material.CAVE_AIR : fluid);
            }
            else {
                // Ensure no fluid flows out
                if (loc.getY() <= waterLevel
                    || BlockUtils.isWet(new SimpleBlock(data,loc))
                    || BlockUtils.isWet(new SimpleBlock(data,loc.getUp()))) {
                    data.setType(loc.getX(), loc.getY(), loc.getZ(), BlockUtils.stoneOrSlate(loc.getY()));
                }

                // find the floors and ceilings for the populator.
                // Only add them to the list if the thing is solid
                if (data.getType(loc.getX(), loc.getY(), loc.getZ()).isSolid()) {
                    CoordPair key = new CoordPair(loc.getX(),loc.getZ());
                    if (!caveRoom.toCarve.isSet(loc.getDown())) {
                        CoordPair def =  caveRoom.ceilFloorPairs.getOrDefault(key, new CoordPair(FLOOR_CEIL_NULL,FLOOR_CEIL_NULL));
                        caveRoom.ceilFloorPairs.put(key, new CoordPair(def.x(), loc.getY()));
                    }
                    else if (!caveRoom.toCarve.isSet(loc.getUp())) {
                        CoordPair def =  caveRoom.ceilFloorPairs.getOrDefault(key, new CoordPair(FLOOR_CEIL_NULL,FLOOR_CEIL_NULL));
                        caveRoom.ceilFloorPairs.put(key, new CoordPair(loc.getY(), def.z()));
                    }
                }
            }
        }
    }
}
