package org.terraform.structure;

import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.room.path.PathState;
import org.terraform.structure.room.path.PathWriter;

public class EmptyPathWriter extends PathWriter {
    @Override
    public void apply(PopulatorDataAbstract popData, TerraformWorld tw, PathState.PathNode node) {
        //Do nothing
    }
}
