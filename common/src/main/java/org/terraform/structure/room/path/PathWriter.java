package org.terraform.structure.room.path;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

public abstract class PathWriter {

    /**
     * Contains instructions on how to carve a specific path.
     * I.e. how to draw the walls and handle turns, or if
     * a cave is to be carved, how it would be carved.
     */
    public abstract void apply(PopulatorDataAbstract popData, TerraformWorld tw, PathState.PathNode node);
}
