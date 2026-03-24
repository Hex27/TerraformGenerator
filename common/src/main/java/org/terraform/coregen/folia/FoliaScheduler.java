package org.terraform.coregen.folia;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.terraform.main.TerraformGeneratorPlugin;

public class FoliaScheduler implements AbstractScheduler {
    @Override
    public void execSyncRegion(@NotNull World world,
                               int chunkX,
                               int chunkZ,
                               @NotNull Runnable run)
    {
        Bukkit.getRegionScheduler().execute(TerraformGeneratorPlugin.get(),
            world, chunkX, chunkZ, run);
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
