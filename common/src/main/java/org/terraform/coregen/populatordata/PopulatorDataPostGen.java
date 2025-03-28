package org.terraform.coregen.populatordata;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.NaturalSpawnType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;

import java.util.Random;

public class PopulatorDataPostGen extends PopulatorDataICABiomeWriterAbstract implements IPopulatorDataPhysicsCapable {
    private static int spawnerRetries = 0;
    private final @NotNull World w;
    private final @NotNull Chunk c;

    public PopulatorDataPostGen(@NotNull Chunk c) {
        this.w = c.getWorld();
        this.c = c;
    }

    /**
     * @return the w
     */
    public @NotNull World getWorld() {
        return w;
    }

    /**
     * @return the c
     */
    public @NotNull Chunk getChunk() {
        return c;
    }

    @Override
    public @NotNull Material getType(int x, int y, int z) {
        return w.getBlockAt(x, y, z).getType();
    }

    @Override
    public @Nullable BlockData getBlockData(int x, int y, int z) {
        return w.getBlockAt(x, y, z).getBlockData();
    }

    @Override
    public void setType(int x, int y, int z, @NotNull Material type) {
        boolean isFragile = Tag.DOORS.isTagged(type)
                            || Tag.CARPETS.isTagged(type)
                            || type == Material.FARMLAND
                            || type == Material.WATER;
        Block b = w.getBlockAt(x, y, z);
        b.setType(type, !isFragile);
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull BlockData data) {
        boolean isFragile = Tag.DOORS.isTagged(data.getMaterial())
                            || Tag.CARPETS.isTagged(data.getMaterial())
                            || data.getMaterial() == Material.FARMLAND
                            || data.getMaterial() == Material.WATER;
        Block b = w.getBlockAt(x, y, z);
        b.setBlockData(data.clone(), !isFragile);
    }

    @Override
    public void setType(int x, int y, int z, @NotNull Material type, boolean updatePhysics) {
        Block b = w.getBlockAt(x, y, z);
        b.setType(type, updatePhysics);
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull BlockData data, boolean updatePhysics) {
        Block b = w.getBlockAt(x, y, z);
        b.setBlockData(data.clone(), updatePhysics);
    }

    /**
     * Blockstates are mutable, so just edit them. There is no method to directly set them.
     */
    public @NotNull BlockState getBlockState(int x, int y, int z) {
        Block b = w.getBlockAt(x, y, z);
        return b.getState();
    }

    public void noPhysicsUpdateForce(int x, int y, int z, @NotNull BlockData data) {
        Block b = w.getBlockAt(x, y, z);
        b.setBlockData(data.clone(), false);
    }

    @Override
    public @Nullable Biome getBiome(int rawX, int rawZ) {
        return w.getBlockAt(rawX, TerraformGenerator.seaLevel, rawZ).getBiome();
    }

    @Override
    public void setBiome(int rawX, int rawY, int rawZ, @NotNull Biome biome) {
        w.setBiome(rawX, rawY, rawZ, biome);
    }

    @Override
    public int getChunkX() {
        return c.getX();
    }

    @Override
    public int getChunkZ() {
        return c.getZ();
    }

    @Override
    public void addEntity(int x, int y, int z, @NotNull EntityType type) {
        // Always offset by 0.5 to prevent them spawning in corners.
        // Y is offset by a small bit to prevent falling through weird spawning areas
        Entity e = c.getWorld().spawnEntity(new Location(c.getWorld(), x + 0.5, y + 0.3, z + 0.5), type);
        e.setPersistent(true);
        if (e instanceof LivingEntity) {
            ((LivingEntity) e).setRemoveWhenFarAway(false);
        }
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, @NotNull EntityType type) {
        if (!TConfig.areAnimalsEnabled()) {
            return;
        }

        Block b = w.getBlockAt(rawX, rawY, rawZ);
        b.setType(Material.SPAWNER, false);
        try {
            CreatureSpawner spawner = (CreatureSpawner) b.getState();
            spawner.setSpawnedType(type);
            spawner.update();
        }
        catch (IllegalStateException | ClassCastException e) {
            spawnerRetries++;
            if (spawnerRetries > 10) {
                Bukkit.getLogger().info("Giving up on spawner at " + rawX + "," + rawY + "," + rawZ);
                spawnerRetries = 0;
                return;
            }
            Bukkit.getLogger()
                  .info("Failed to get state for spawner at "
                        + rawX
                        + ","
                        + rawY
                        + ","
                        + rawZ
                        + ", try "
                        + spawnerRetries);
            setSpawner(rawX, rawY, rawZ, type);
        }
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        TerraformGeneratorPlugin.injector.getICAData(w.getBlockAt(x, y, z).getChunk()).lootTableChest(x, y, z, table);
    }

    @Override
    public @NotNull TerraformWorld getTerraformWorld() {
        return TerraformWorld.get(w);
    }

    @Override
    public void setBiome(int rawX, int rawY, int rawZ, CustomBiomeType cbt, Biome fallback) {
        PopulatorDataICAAbstract icad = TerraformGeneratorPlugin.injector.getICAData(w.getBlockAt(rawX, rawY, rawZ)
                                                                                      .getChunk());
        if (icad instanceof PopulatorDataICABiomeWriterAbstract biomeWriter) {
            biomeWriter.setBiome(rawX, rawY, rawZ, cbt, fallback);
        }
    }

    @Override
    public void registerNaturalSpawns(NaturalSpawnType type, int x0, int y0, int z0, int x1, int y1, int z1) {
        PopulatorDataICAAbstract icad = TerraformGeneratorPlugin.injector.getICAData(w.getBlockAt(x0, y0, z0)
                                                                                      .getChunk());
        if (icad instanceof PopulatorDataICABiomeWriterAbstract) {
            icad.registerNaturalSpawns(type, x0, y0, z0, x1, y1, z1);
        }
    }

    @Override
    public void spawnMinecartWithChest(int x, int y, int z, TerraLootTable table, Random random) {
        PopulatorDataICAAbstract icad = TerraformGeneratorPlugin.injector.getICAData(w.getBlockAt(x, y, z).getChunk());
        if (icad instanceof PopulatorDataICABiomeWriterAbstract) {
            icad.spawnMinecartWithChest(x, y, z, table, random);
        }
    }
}
