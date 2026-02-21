package org.terraform.v1_21_R6;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.generator.CraftLimitedRegion;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.RandomSourceWrapper;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.NativeGeneratorPatcherPopulator;
import org.terraform.coregen.populatordata.IPopulatorDataBaseHeightAccess;
import org.terraform.coregen.populatordata.IPopulatorDataBeehiveEditor;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

public class PopulatorData extends PopulatorDataAbstract
        implements IPopulatorDataBaseHeightAccess, IPopulatorDataBeehiveEditor
{
    private static final HashMap<org.bukkit.entity.EntityType, EntityType<?>> EntityTypeDict = new HashMap<>();
    final WorldGenLevel rlwa;
    final ChunkAccess ica;
    private final int chunkX;
    private final int chunkZ;
    private final NMSChunkGenerator gen;
    private int radius = 1;

    public PopulatorData(WorldGenLevel rlwa, ChunkAccess ica, NMSChunkGenerator gen, int chunkX, int chunkZ) {
        this.rlwa = rlwa;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.gen = gen;
        this.ica = ica;

        if (EntityTypeDict.isEmpty()) {
            for (org.bukkit.entity.EntityType type : org.bukkit.entity.EntityType.values()) {
                if (type == org.bukkit.entity.EntityType.UNKNOWN) {
                    continue;
                }
                try {
                    // EntityType.byString
                    Optional<EntityType<?>> et = EntityType.byString("minecraft:" + type.toString()
                                                                                   .toLowerCase(Locale.ENGLISH));
                    et.ifPresent(EntityType -> EntityTypeDict.put(type, EntityType));
                }
                catch (IllegalArgumentException e) {
                    TerraformGeneratorPlugin.logger.stackTrace(e);
                }
            }
        }
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public @NotNull Material getType(int x, int y, int z) {
        try {
            // return rlwa.getType(x, y, z);
            return CraftMagicNumbers.getMaterial(rlwa.getBlockState(new BlockPos(x, y, z)).getBlock());
        }
        catch (Exception e) {
            Bukkit.getLogger()
                  .info("Error chunk: "
                        + chunkX
                        + ","
                        + chunkZ
                        + "--- Block Coords: "
                        + 16 * chunkX
                        + ","
                        + 16 * chunkZ
                        + " for coords "
                        + x
                        + ","
                        + y
                        + ","
                        + z);
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
        return null;
    }

    public BlockData getBlockData(int x, int y, int z) {
        // return rlwa.getBlockData(x,y,z);
        return CraftBlockData.fromData(rlwa.getBlockState(new BlockPos(x, y, z)));
    }

    @Override
    public void setType(int x, int y, int z, @NotNull Material type) {
        if (Math.abs((x >> 4) - chunkX) > radius || Math.abs((z >> 4) - chunkZ) > radius) {
            if (radius > 0) {
                NativeGeneratorPatcherPopulator.pushChange(rlwa.getMinecraftWorld().getWorld().getName(),
                        x,
                        y,
                        z,
                        Bukkit.createBlockData(type)
                );
            }
            else {
                TerraformGeneratorPlugin.logger.stackTrace(new Exception(
                        "Tried to call adjacent chunk with populator radius 0: ("
                        + x
                        + ","
                        + y
                        + ","
                        + z
                        + ") for chunk ("
                        + chunkX
                        + ","
                        + chunkZ
                        + ")"));
            }
        }
        else {
            rlwa.setBlock(new BlockPos(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), 0);
        }
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull BlockData data) {
        if (Math.abs((x >> 4) - chunkX) > radius || Math.abs((z >> 4) - chunkZ) > radius) {
            if (radius > 0) {
                NativeGeneratorPatcherPopulator.pushChange(rlwa.getMinecraftWorld().getWorld().getName(),
                        x,
                        y,
                        z,
                        data
                );
            }
            else {
                TerraformGeneratorPlugin.logger.stackTrace(new Exception(
                        "Tried to call adjacent chunk with populator radius 0: ("
                        + x
                        + ","
                        + y
                        + ","
                        + z
                        + ") for chunk ("
                        + chunkX
                        + ","
                        + chunkZ
                        + ")"));
            }
        }
        else {
            rlwa.setBlock(new BlockPos(x, y, z), ((CraftBlockData) data).getState(), 0);
        }
    }

    // wtf
    public Biome getBiome(int rawX, int rawZ) {
        TerraformWorld tw = gen.getTerraformWorld();
        return tw.getBiomeBank(rawX, rawZ).getHandler().getBiome();
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
    public void addEntity(int rawX, int rawY, int rawZ, @NotNull org.bukkit.entity.EntityType type) {
        if (Math.abs((rawX >> 4) - chunkX) > 1 || Math.abs((rawZ >> 4) - chunkZ) > 1) {
            TerraformGeneratorPlugin.logger.info("Failed to spawn " + type + " as it was out of bounds.");
            return;
        }

        // Use this method for thread safety.
        CraftLimitedRegion clr = new CraftLimitedRegion(rlwa, ica.getPos());
        net.minecraft.world.entity.Entity e = clr.createEntity(new Location(gen.getTerraformWorld().getWorld(),
                rawX,
                rawY,
                rawZ), type.getEntityClass(), true);
        // Mob.setPersistenceRequired()
        if (e instanceof Mob) {
            ((Mob) e).setPersistenceRequired();
        }
        rlwa.addFreshEntity(e); //used to be b
        // TerraformGeneratorPlugin.logger.info("Spawned " + e.getType() + " at " + rawX + " " + rawY + " " + rawZ);
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, org.bukkit.entity.EntityType type) {
        if (!TConfig.c.FEATURE_SPAWNERS_ENABLED) {
            return;
        }

        BlockPos pos = new BlockPos(rawX, rawY, rawZ);

        setType(rawX, rawY, rawZ, Material.SPAWNER);
        Optional<SpawnerBlockEntity> opt = rlwa.getBlockEntity(pos, BlockEntityType.MOB_SPAWNER);

        if (opt.isPresent()) {
            SpawnerBlockEntity tileentity = opt.get();
            try {
                // Refer to WorldGenDungeons
                //                SpawnerBlockEntity SpawnerBlockEntity = (SpawnerBlockEntity) tileentity;
                //
                //                SpawnerBlockEntity.setEntityId(this.randomEntityId(randomsource), randomsource);

                // Fetch from ENTITY_TYPE (Q)'s map
                // q is ENTITY_TYPE
                EntityType<?> nmsEntity = EntityTypeDict.get(type);
                if (nmsEntity == null) {
                    TerraformGeneratorPlugin.logger.error(type + " was not present in the EntityTypeDict.");
                }
                tileentity.setEntityId(nmsEntity, new RandomSourceWrapper(new Random()));
            }
            catch (IllegalArgumentException | SecurityException e) {
                TerraformGeneratorPlugin.logger.stackTrace(e);
            }
        }
        else {
            TerraformGeneratorPlugin.logger.error("Failed to fetch mob spawner entity at ("
                                                  + ","
                                                  + rawX
                                                  + ","
                                                  + rawY
                                                  + ","
                                                  + rawZ
                                                  + ")");
        }
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        BlockPos pos = new BlockPos(x, y, z);

        // getBlockEntity
        BlockEntity te = rlwa.getBlockEntity(pos);
        if (te instanceof RandomizableContainerBlockEntity rcb) {
            rcb.setLootTable(LootTableTranslator.translationMap.get(table));
        }
        else if (te instanceof BrushableBlockEntity bbe)
        // BrushableBlockEntity.setLootTable
        {
            bbe.setLootTable(LootTableTranslator.translationMap.get(table),
                    gen.getTerraformWorld().getHashedRand(x, y, z).nextLong()
            );
        }

        // RandomizableContainerBlockEntity.a(rlwa, RandomSource.a(gen.getTerraformWorld().getHashedRand(x, y, z).nextLong()), pos, LootTableTranslator.translationMap.get(table));
    }

    @Override
    public @NotNull TerraformWorld getTerraformWorld() {
        return gen.getTerraformWorld();
    }

    @Override
    public int getBaseHeight(int rawX, int rawZ) {
        // (int i, int j, HeightMap.Type heightmap_type, LevelHeightAccessor levelheightaccessor, RandomState randomstate)
        return 100;
        // return gen.a(rawX, rawZ, HeightMap.Type.a, this.rlwa);
    }

    @Override
    public void setBeehiveWithBee(int rawX, int rawY, int rawZ) {
        BlockPos pos = new BlockPos(rawX, rawY, rawZ);
        // TerraformGeneratorPlugin.logger.info(IRegistry.X.b(EntityType.h).toString());
        setType(rawX, rawY, rawZ, Material.BEE_NEST);

        try {
            // TerraformGeneratorPlugin.logger.error("Failed to set beehive at (" + rawX + "," + rawY + "," + rawZ + ") " + BuiltInRegistries.h.b(EntityTypeDict.get(EntityType.BEE)));
            BeehiveBlockEntity tileentity = (BeehiveBlockEntity) rlwa.getBlockEntity(pos);
            if (tileentity == null) { // retry?
                setType(rawX, rawY, rawZ, Material.BEE_NEST);
                tileentity = (BeehiveBlockEntity) rlwa.getBlockEntity(pos);
            }
            //
            //            NBTTagCompound nbttagcompound = new NBTTagCompound();
            //            nbttagcompound.a("id", "minecraft:bee");
            // BeehiveBlockEntity.storeBee
            // BeehiveBlockEntity.Occupant.create(1)
            tileentity.storeBee(BeehiveBlockEntity.Occupant.create(GenUtils.positiveHashMod(rawX, rawY, rawZ, 599)));
        }
        catch (NullPointerException | IllegalArgumentException | SecurityException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }
}
