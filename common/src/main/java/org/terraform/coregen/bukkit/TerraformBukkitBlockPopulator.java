package org.terraform.coregen.bukkit;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.terraform.data.TerraformWorld;

import java.util.Random;

/**
 * This class is used to control the order in which the bukkit populator is used
 * to perform various actions.
 * @author Hex_27
 */
public class TerraformBukkitBlockPopulator extends BlockPopulator{

    protected final TerraformWorld tw;
    private final TerraformStructurePopulator structurePopulator;
    private final NativeGeneratorPatcherPopulator nativePatcherPopulator;
    private final PhysicsUpdaterPopulator physicsUpdaterPopulator;
    private final TerraformAnimalPopulator animalPopulator;

    public TerraformBukkitBlockPopulator(TerraformWorld tw) {
        this.tw = tw;
        this.nativePatcherPopulator = new NativeGeneratorPatcherPopulator();
        this.structurePopulator = new TerraformStructurePopulator(tw);
        this.physicsUpdaterPopulator = new PhysicsUpdaterPopulator();
        this.animalPopulator = new TerraformAnimalPopulator(tw);
        //Bukkit.getPluginManager().registerEvents(this,TerraformGeneratorPlugin.get());
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        //Run the fixers first
        this.nativePatcherPopulator.populate(world, random, chunk);
        this.physicsUpdaterPopulator.populate(world, random, chunk);
        
        //Populate structures next
        this.structurePopulator.populate(world, random, chunk);

        //Populate animals last
        this.animalPopulator.populate(world, random, chunk);
    }
    
}
