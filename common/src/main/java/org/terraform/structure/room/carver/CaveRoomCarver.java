package org.terraform.structure.room.carver;

import org.bukkit.Material;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

import java.util.Random;

public class CaveRoomCarver extends RoomCarver{

    private float xMultiplier = 1.0f;
    private float yMultiplier = 1.0f;
    private float zMultiplier = 1.0f;
    private float frequency = 0.09f;
    private float largeRoomFrequency = 0.03f;
    public CaveRoomCarver(){}

    public CaveRoomCarver(float xMultiplier, float yMultiplier, float zMultiplier, float frequency, float largeRoomFrequency) {
        this.xMultiplier = xMultiplier;
        this.yMultiplier = yMultiplier;
        this.zMultiplier = zMultiplier;
        this.frequency = frequency;
        this.largeRoomFrequency = largeRoomFrequency;
    }

    @Override
    public void carveRoom(PopulatorDataAbstract data, CubeRoom room, Material... wallMaterial) {
        int heightOffset = room.getHeight() - (2 * room.getHeight() / 3);
        BlockUtils.carveCaveAir(data.getTerraformWorld().getHashedRand(room.getX(), room.getY(), room.getZ()).nextInt(9999291),
                xMultiplier*(room.getWidthX() / 2f),
                yMultiplier*(2 * room.getHeight() / 3f),
                zMultiplier*(room.getWidthZ() / 2f),
                room.largerThanVolume(40000) ? largeRoomFrequency : frequency,
                new SimpleBlock(data, room.getX(), room.getY() + heightOffset, room.getZ()),
                true,
                true, BlockUtils.caveCarveReplace);
    }
}
