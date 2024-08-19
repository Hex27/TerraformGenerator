package org.terraform.coregen.bukkit;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.terraform.data.TerraformWorld;

import java.util.Random;

/**
 * This class is used to control the order in which the bukkit populator is used
 * to perform various actions.
 *
 * @author Hex_27
 */
public class TerraformBukkitBlockPopulator extends BlockPopulator {

    protected final TerraformWorld tw;
    private final @NotNull TerraformStructurePopulator structurePopulator;
    private final @NotNull NativeGeneratorPatcherPopulator nativePatcherPopulator;
    private final @NotNull PhysicsUpdaterPopulator physicsUpdaterPopulator;
    private final @NotNull TerraformAnimalPopulator animalPopulator;

    public TerraformBukkitBlockPopulator(TerraformWorld tw) {
        this.tw = tw;
        this.nativePatcherPopulator = new NativeGeneratorPatcherPopulator();
        this.structurePopulator = new TerraformStructurePopulator(tw);
        this.physicsUpdaterPopulator = new PhysicsUpdaterPopulator();
        this.animalPopulator = new TerraformAnimalPopulator(tw);
        // Bukkit.getPluginManager().registerEvents(this,TerraformGeneratorPlugin.get());
    }

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk) {
        // Run the fixers first
        this.nativePatcherPopulator.populate(world, random, chunk);
        this.physicsUpdaterPopulator.populate(world, random, chunk);

        // Populate structures next
        this.structurePopulator.populate(world, random, chunk);

        // Populate animals last
        this.animalPopulator.populate(world, random, chunk);
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo,
                         @NotNull Random random,
                         int chunkX,
                         int chunkZ,
                         @NotNull LimitedRegion lr)
    {
        this.structurePopulator.populate(worldInfo, random, chunkX, chunkZ, lr);
    }

    public @NotNull TerraformStructurePopulator getStructurePopulator() {
        return structurePopulator;
    }

}
