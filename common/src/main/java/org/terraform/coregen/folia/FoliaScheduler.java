package org.terraform.coregen.folia;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.terraform.main.TerraformGeneratorPlugin;

/**
 * This is called FoliaScheduler, but Paper also has the region scheduler
 * methods
 */
public class FoliaScheduler implements AbstractScheduler {
    @Override
    public void execAsyncRegion(@NotNull World world,
                                int chunkX,
                                int chunkZ,
                                @NotNull Runnable run)
    {
       world.getChunkAtAsync(chunkX, chunkZ)
         .thenAccept((c)->
             Bukkit.getRegionScheduler().execute(TerraformGeneratorPlugin.get(),
             world, chunkX, chunkZ, run));
    }

    @Override
    public void execSyncGlobal(@NotNull Runnable run) {
        Bukkit.getGlobalRegionScheduler().execute(TerraformGeneratorPlugin.get(), run);
    }

    @Override
    public void execAsync(@NotNull Runnable run) {
        Bukkit.getAsyncScheduler().runNow(TerraformGeneratorPlugin.get(), (task)->{
            run.run();
        });
    }
}
