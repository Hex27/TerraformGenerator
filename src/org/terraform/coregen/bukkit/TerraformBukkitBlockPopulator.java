package org.terraform.coregen.bukkit;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.terraform.data.TerraformWorld;

/**
 * This class is used to control the order in which the bukkit populator is used
 * to perform various actions.
 * @author Leonard
 *
 */
public class TerraformBukkitBlockPopulator extends BlockPopulator{
	
	protected final TerraformWorld tw;
	private TerraformStructurePopulator structurePopulator;
	private NativeGeneratorPatcherPopulator nativePatcherPopulator;

    public TerraformBukkitBlockPopulator(TerraformWorld tw) {
        this.tw = tw;
        this.nativePatcherPopulator = new NativeGeneratorPatcherPopulator();
        this.structurePopulator = new TerraformStructurePopulator(tw);
    }
    
	@Override
	 public void populate(World world, Random random, Chunk chunk) {
		//Run the fixer first
		this.nativePatcherPopulator.populate(world, random, chunk);
		
		//Populate structures next
		this.structurePopulator.populate(world, random, chunk);
	}

}
