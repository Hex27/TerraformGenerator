package org.terraform.coregen.populatordata;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beehive;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.NativeGeneratorPatcherPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;

import java.util.Random;

public class PopulatorDataSpigotAPI extends PopulatorDataAbstract
        implements IPopulatorDataBeehiveEditor, IPopulatorDataMinecartSpawner
{
    public final LimitedRegion lr;
    private final TerraformWorld tw;
    private final int chunkX, chunkZ;

    public PopulatorDataSpigotAPI(LimitedRegion lr, TerraformWorld tw, int chunkX, int chunkZ) {
        this.lr = lr;
        this.tw = tw;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public Material getType(int x, int y, int z) {
        if (!lr.isInRegion(x, y, z)) {
            // yes i fucking know this is bad
            return y > TerraformGenerator.seaLevel ? Material.AIR : Material.WATER;
        }
        return lr.getType(x, y, z);
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        if (!lr.isInRegion(x, y, z)) {
            return y > TerraformGenerator.seaLevel
                   ? Bukkit.createBlockData(Material.AIR)
                   : Bukkit.createBlockData(Material.WATER);
        }

        return lr.getBlockData(x, y, z);
    }

    @Override
    public void setType(int x, int y, int z, @NotNull Material type) {
        if (!lr.isInRegion(x, y, z)) {
            NativeGeneratorPatcherPopulator.pushChange(tw.getName(), x, y, z, Bukkit.createBlockData(type));
            return;
        }
        lr.setType(x, y, z, type);
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull BlockData data) {
        if (!lr.isInRegion(x, y, z)) {
            NativeGeneratorPatcherPopulator.pushChange(tw.getName(), x, y, z, data);
            return;
        }
        lr.setBlockData(x, y, z, data);
    }

    @Override
    public Biome getBiome(int rawX, int rawZ) {
        return lr.getBiome(rawX, 50, rawZ);
    }

    @Override
    public void addEntity(int rawX, int rawY, int rawZ, @NotNull EntityType type) {
        lr.spawnEntity(new Location(tw.getWorld(), rawX, rawY, rawZ), type);
    }

    @Override
    public int getChunkX() {
        return chunkX;
    }

    @Override
    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, @NotNull EntityType type) {
        if (!TConfig.areAnimalsEnabled()) {
            return;
        }

        setType(rawX, rawY, rawZ, Material.SPAWNER);
        try {
            // This will give class cast exception sometimes. I'm not sure why.
            // Additionally, if rawX/rawZ is outside the region, this will correctly
            // throw an error
            CreatureSpawner spawner = (CreatureSpawner) lr.getBlockState(rawX, rawY, rawZ);
            spawner.setSpawnedType(type);
            spawner.update(true, false);
        }
        catch (ClassCastException e) {
            TerraformGeneratorPlugin.logger.info("Failed to set spawner at " + rawX + "," + rawY + "," + rawZ);
        }
    }

    @Override
    public void lootTableChest(int x, int y, int z, @NotNull TerraLootTable table) {
        BlockState s = lr.getBlockState(x, y, z);
        if (s instanceof Lootable t) {
            t.setLootTable(table.bukkit());
            s.update(true, false);
        }
    }

    @Override
    public TerraformWorld getTerraformWorld() {
        return tw;
    }

    @Override
    public void setBeehiveWithBee(int rawX, int rawY, int rawZ) {
        if (!TConfig.areAnimalsEnabled()) return;

        if (!lr.isInRegion(rawX, rawY, rawZ)) {
            return; // just forget it
        }

        setType(rawX, rawY, rawZ, Material.BEE_NEST);

        // I guess the above can fail sometimes. I don't know why.
        // Catch and throw because that's fucking stupid
        try {
            Beehive bukkitBeehive = (Beehive) lr.getBlockState(rawX, rawY, rawZ);
            TerraformGeneratorPlugin.injector.storeBee(bukkitBeehive);
        }
        catch (ClassCastException e) {
            TerraformGeneratorPlugin.logger.info("Failed to set beehive at " + rawX + "," + rawY + "," + rawZ);
        }

    }

    @Override
    public void spawnMinecartWithChest(int x, int y, int z, @NotNull TerraLootTable table, Random random) {

        StorageMinecart e = (StorageMinecart) lr.spawnEntity(
                new Location(tw.getWorld(), x + 0.5f, y + 0.5f, z + 0.5f),
                EntityType.MINECART_CHEST
        );
        e.setLootTable(table.bukkit());
    }
}
