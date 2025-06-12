package org.terraform.data;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * class represent chunk location in simple form
 *
 * @author wysohn
 */
public record SimpleChunkLocation(String world, int x, int z) implements Cloneable {
    public SimpleChunkLocation(TerraformWorld tw, int x, int z){
        this(tw.getName(),x,z);
    }
    public SimpleChunkLocation(String world, int x, int y, int z) {
        this(world, x>>4, z>>4);
    }

    public SimpleChunkLocation(@NotNull Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static @NotNull SimpleChunkLocation of(@NotNull Block block) {
        return new SimpleChunkLocation(block.getWorld().getName(), block.getX() >> 4, block.getZ() >> 4);
    }

    // world , x, z
    public static SimpleChunkLocation chunkStrToLoc(@Nullable String chunk) {
        if (chunk == null) {
            return null;
        }
        String[] split = StringUtils.split(StringUtils.deleteWhitespace(chunk), ',');

        String world = split[0];
        int x = Integer.parseInt(split[1]);
        int z = Integer.parseInt(split[2]);

        return new SimpleChunkLocation(world, x, z);
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public @NotNull SimpleChunkLocation getRelative(int nx, int nz) {
        return new SimpleChunkLocation(world, nx + x, nz + z);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public @NotNull SimpleChunkLocation clone() {
        return new SimpleChunkLocation(world, x, z);
    }

    @Override
    public @NotNull String toString() {
        return world + ", " + x + ", " + z;
    }

    public @NotNull Chunk toChunk() {
        return Bukkit.getWorld(world).getChunkAt(x, z);
    }

}
