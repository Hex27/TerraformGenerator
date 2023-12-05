package org.terraform.coregen.bukkit;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataSpigotAPI;
import org.terraform.coregen.populatordata.PopulatorDataStructurePregen;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.StructurePregenerator;

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

        //For strongholds as they don't use the regular structure system
        this.structurePopulator.populate(world, random, chunk);

        //Populate animals last
        this.animalPopulator.populate(world, random, chunk);
    }

    //After all the other stuff, populate structures last
    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random,
                         int chunkX, int chunkZ,
                         @NotNull LimitedRegion limitedRegion)
    {
        StructurePregenerator.flushChanges(new PopulatorDataSpigotAPI(limitedRegion, tw, chunkX, chunkZ));
    }
}
