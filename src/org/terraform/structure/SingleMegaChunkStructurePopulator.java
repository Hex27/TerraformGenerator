package org.terraform.structure;

import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;

public abstract class SingleMegaChunkStructurePopulator extends StructurePopulator {

    /**
     * @param tw
     * @param mc
     * @return a 2d array of BLOCK COORDS
     */
    public abstract int[] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc);
}
