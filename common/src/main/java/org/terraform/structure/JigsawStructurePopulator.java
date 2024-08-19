package org.terraform.structure;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;

/**
 * Idea: Split RoomPopulatorAbstract into a way to reason about chunk divisions.
 * Paths can be "split" as they already are discretely placed piece by piece.
 * Rooms, if no overflows out of the 3x3 are detected, can just be placed
 * in the new block populator.
 * <br>
 * The large rooms that exceed 3x3 chunks MUST use the old block populator and
 * they WILL go last no matter what
 */
public abstract class JigsawStructurePopulator extends SingleMegaChunkStructurePopulator {

    /**
     * @return the JigsawState for the structure. This will be used in
     * StructurePopulator to generate structures.
     */
    public abstract JigsawState calculateRoomPopulators(TerraformWorld tw, MegaChunk mc);


    /**
     * Override this and do nothing for compatibility's sake
     */
    public void populate(TerraformWorld world, PopulatorDataAbstract data)
    {

    }


}
