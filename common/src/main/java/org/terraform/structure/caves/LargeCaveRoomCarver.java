package org.terraform.structure.caves;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Material;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleLocation;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.carver.RoomCarver;
import org.terraform.utils.BlockUtils;

public class LargeCaveRoomCarver extends RoomCarver {
    final Material fluid;
    int waterLevel = -64;

    public LargeCaveRoomCarver(Material fluid) {
        this.fluid = fluid;
    }

    @Override
    public void carveRoom(PopulatorDataAbstract data, CubeRoom room, Material... wallMaterial) {
        if(!(room instanceof LargeCaveRoomPiece caveRoom))
            throw new NotImplementedException("room for LargeCaveRoomCarver was not a LargeCaveRoomPiece");

        caveRoom.toCarve.forEach((loc, boundary)->{
            if(!boundary) {
                data.setType(loc.getX(), loc.getY(), loc.getZ(),
                        loc.getY() > waterLevel ? Material.CAVE_AIR : fluid);
            }
            else
            {
                //Ensure no fluid flows out
                if(loc.getY() <= waterLevel)
                    data.setType(loc.getX(),loc.getY(),loc.getZ(), BlockUtils.stoneOrSlate(loc.getY()));

                //find the floors and ceilings for the populator.
                //Only add them to the list if the thing is solid
                if(data.getType(loc.getX(),loc.getY(),loc.getZ()).isSolid())
                {
                    if(!caveRoom.toCarve.containsKey(loc.getRelative(0,-1,0)))
                        caveRoom.ceilFloorPairs.computeIfAbsent(loc.getAtY(0),
                                newLoc->new SimpleLocation[2])[1] = loc;
                    else if(!caveRoom.toCarve.containsKey(loc.getRelative(0,1,0)))
                        caveRoom.ceilFloorPairs.computeIfAbsent(loc.getAtY(0),
                                newLoc->new SimpleLocation[2])[0] = loc;
                }
            }
        });
    }
}
