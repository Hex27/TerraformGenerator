package org.terraform.coregen.folia;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.terraform.main.TerraformGeneratorPlugin;

public class BukkitScheduler implements AbstractScheduler{
    @Override
    public void execSyncRegion(@NotNull World world, int chunkX, int chunkZ, @NotNull Runnable run) {
        Bukkit.getScheduler().runTask(TerraformGeneratorPlugin.get(), run);
    }

    @Override
    public void execSyncGlobal(@NotNull Runnable run) {
        Bukkit.getScheduler().runTask(TerraformGeneratorPlugin.get(), run);
    }

    @Override
    public void execAsync(@NotNull Runnable run) {
        Bukkit.getScheduler().runTaskAsynchronously(TerraformGeneratorPlugin.get(), run);
    }
}
