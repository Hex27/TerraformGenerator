package org.terraform.coregen.folia;

import org.bukkit.plugin.Plugin;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface AbstractScheduler {

    public void execSyncRegion(@NotNull World world, int chunkX, int chunkZ, @NotNull Runnable run);

    public void execSyncGlobal(@NotNull Runnable run);

    public void execAsync(@NotNull Runnable run);
}
