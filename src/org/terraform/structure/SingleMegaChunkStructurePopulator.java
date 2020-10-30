package org.terraform.structure;

import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;

public abstract class SingleMegaChunkStructurePopulator extends StructurePopulator {

    public abstract int[] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc);

}
