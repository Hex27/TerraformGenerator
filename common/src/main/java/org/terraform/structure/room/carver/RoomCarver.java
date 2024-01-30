package org.terraform.structure.room.carver;

import org.bukkit.Material;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.structure.room.CubeRoom;

public abstract class RoomCarver {

    public abstract void carveRoom(PopulatorDataAbstract data, CubeRoom room, Material... wallMaterial);
}
