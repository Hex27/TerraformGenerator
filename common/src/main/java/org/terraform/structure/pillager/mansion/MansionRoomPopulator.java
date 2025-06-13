package org.terraform.structure.pillager.mansion;

import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.CubeRoom;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;

public abstract class MansionRoomPopulator {

    private final HashMap<BlockFace, MansionInternalWallState> internalWalls;
    private final CubeRoom room;

    public MansionRoomPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
        super();
        this.internalWalls = internalWalls;
        this.room = room;
    }

    public abstract void decorateRoom(PopulatorDataAbstract data, Random random);

    public CubeRoom getRoom() {
        return room;
    }

    public @NotNull MansionRoomPopulator getInstance(CubeRoom room,
                                                     HashMap<BlockFace, MansionInternalWallState> internalWalls)
    {
        MansionRoomPopulator pop;
        try {
            pop = (MansionRoomPopulator) this.getClass().getConstructors()[0].newInstance(room, internalWalls);
            return pop;
        }
        catch (InstantiationException |
               IllegalAccessException |
               IllegalArgumentException |
               InvocationTargetException |
               SecurityException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
            return null; //Error case
        }
    }

    public void decorateWall(Random rand, Wall w) {
    }

    public void decorateWindow(Random rand, Wall w) {
    }

    public void decorateEntrance(Random rand, Wall w) {
    }

    public void decorateExit(Random rand, Wall w) {
    }

    public HashMap<BlockFace, MansionInternalWallState> getInternalWalls() {
        return internalWalls;
    }

    public abstract MansionRoomSize getSize();

    public int[] getSpawnLocation() {
        return new int[] {this.room.getX(), this.room.getY() + 1, this.room.getZ()};
    }
}
