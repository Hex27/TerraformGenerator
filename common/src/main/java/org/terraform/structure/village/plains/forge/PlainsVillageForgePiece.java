package org.terraform.structure.village.plains.forge;

import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.Nullable;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.structure.village.plains.forge.PlainsVillageForgeWallPiece.PlainsVillageForgeWallType;

public abstract class PlainsVillageForgePiece extends JigsawStructurePiece {

    protected final PlainsVillagePopulator plainsVillagePopulator;
    private @Nullable PlainsVillageForgeWallType wallType = null;

    public PlainsVillageForgePiece(PlainsVillagePopulator plainsVillagePopulator,
                                   int widthX,
                                   int height,
                                   int widthZ,
                                   JigsawType type,
                                   BlockFace[] validDirs)
    {
        super(widthX, height, widthZ, type, validDirs);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    public PlainsVillageForgePiece(PlainsVillagePopulator plainsVillagePopulator,
                                   int widthX,
                                   int height,
                                   int widthZ,
                                   JigsawType type,
                                   boolean unique,
                                   BlockFace... validDirs)
    {
        super(widthX, height, widthZ, type, unique, validDirs);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    public @Nullable PlainsVillageForgeWallType getWallType() {
        return wallType;
    }

    public void setWallType(@Nullable PlainsVillageForgeWallType wallType) {
        this.wallType = wallType;
    }


}
