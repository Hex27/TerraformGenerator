package org.terraform.coregen.folia;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface AbstractScheduler {

    void execAsyncRegion(@NotNull World world, int chunkX, int chunkZ, @NotNull Runnable run);

    void execSyncGlobal(@NotNull Runnable run);

    void execAsync(@NotNull Runnable run);
}
