package org.terraform.structure;

/**
 * Spawns the associated vanilla structure.
 */
public abstract class VanillaStructurePopulator extends SingleMegaChunkStructurePopulator {
    public final String structureRegistryKey;

    public VanillaStructurePopulator(String structureKey)
    {
        structureRegistryKey = structureKey;
    }
}
