package org.terraform.v1_21_R5;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.IChunkAccess;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_21_R5.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R5.generator.CraftLimitedRegion;
import org.bukkit.craftbukkit.v1_21_R5.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_21_R5.util.RandomSourceWrapper;
import org.bukkit.entity.EntityType;
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
    private static final HashMap<EntityType, EntityTypes<?>> entityTypesDict = new HashMap<>();
    final GeneratorAccessSeed rlwa;
    final IChunkAccess ica;
    private final int chunkX;
    private final int chunkZ;
    private final NMSChunkGenerator gen;
    private int radius = 1;

    public PopulatorData(GeneratorAccessSeed rlwa, IChunkAccess ica, NMSChunkGenerator gen, int chunkX, int chunkZ) {
        this.rlwa = rlwa;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.gen = gen;
        this.ica = ica;

        if (entityTypesDict.isEmpty()) {
            for (EntityType type : EntityType.values()) {
                if (type == EntityType.UNKNOWN) {
                    continue;
                }
                try {
                    // EntityTypes.byString
                    Optional<EntityTypes<?>> et = EntityTypes.a("minecraft:" + type.toString()
                                                                                   .toLowerCase(Locale.ENGLISH));
                    et.ifPresent(entityTypes -> entityTypesDict.put(type, entityTypes));
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
            return CraftMagicNumbers.getMaterial(rlwa.a_(new BlockPosition(x, y, z)).b());
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
        return CraftBlockData.fromData(rlwa.a_(new BlockPosition(x, y, z)));
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
            rlwa.a(new BlockPosition(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), 0);
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
            rlwa.a(new BlockPosition(x, y, z), ((CraftBlockData) data).getState(), 0);
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
    public void addEntity(int rawX, int rawY, int rawZ, @NotNull EntityType type) {
        if (Math.abs((rawX >> 4) - chunkX) > 1 || Math.abs((rawZ >> 4) - chunkZ) > 1) {
            TerraformGeneratorPlugin.logger.info("Failed to spawn " + type + " as it was out of bounds.");
            return;
        }

        // Use this method for thread safety.
        CraftLimitedRegion clr = new CraftLimitedRegion(rlwa, ica.f());
        net.minecraft.world.entity.Entity e = clr.createEntity(new Location(gen.getTerraformWorld().getWorld(),
                rawX,
                rawY,
                rawZ), type.getEntityClass(), true);
        // EntityInsentient.setPersistenceRequired()
        if (e instanceof EntityInsentient) {
            ((EntityInsentient) e).fF();
        }
        rlwa.b(e);
        // TerraformGeneratorPlugin.logger.info("Spawned " + e.getType() + " at " + rawX + " " + rawY + " " + rawZ);
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
        if (!TConfig.areAnimalsEnabled()) {
            return;
        }

        BlockPosition pos = new BlockPosition(rawX, rawY, rawZ);

        setType(rawX, rawY, rawZ, Material.SPAWNER);
        Optional<TileEntityMobSpawner> opt = rlwa.a(pos, TileEntityTypes.j);

        if (opt.isPresent()) {
            TileEntityMobSpawner tileentity = opt.get();
            try {
                // Refer to WorldGenDungeons
                //                TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) tileentity;
                //
                //                tileentitymobspawner.setEntityId(this.randomEntityId(randomsource), randomsource);

                // Fetch from ENTITY_TYPE (Q)'s map
                // q is ENTITY_TYPE
                EntityTypes<?> nmsEntity = entityTypesDict.get(type);
                if (nmsEntity == null) {
                    TerraformGeneratorPlugin.logger.error(type + " was not present in the entityTypesDict.");
                }
                tileentity.a(nmsEntity, new RandomSourceWrapper(new Random()));
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
        BlockPosition pos = new BlockPosition(x, y, z);

        // getBlockEntity
        TileEntity te = rlwa.c_(pos);
        if (te instanceof TileEntityLootable) {
            ((TileEntityLootable) te).a(LootTableTranslator.translationMap.get(table));
        }
        else if (te instanceof BrushableBlockEntity)
        // BrushableBlockEntity.setLootTable
        {
            ((BrushableBlockEntity) te).a(LootTableTranslator.translationMap.get(table),
                    gen.getTerraformWorld().getHashedRand(x, y, z).nextLong()
            );
        }

        // TileEntityLootable.a(rlwa, RandomSource.a(gen.getTerraformWorld().getHashedRand(x, y, z).nextLong()), pos, LootTableTranslator.translationMap.get(table));
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
        BlockPosition pos = new BlockPosition(rawX, rawY, rawZ);
        // TerraformGeneratorPlugin.logger.info(IRegistry.X.b(EntityTypes.h).toString());
        setType(rawX, rawY, rawZ, Material.BEE_NEST);

        try {
            // TerraformGeneratorPlugin.logger.error("Failed to set beehive at (" + rawX + "," + rawY + "," + rawZ + ") " + BuiltInRegistries.h.b(entityTypesDict.get(EntityType.BEE)));
            TileEntityBeehive tileentity = (TileEntityBeehive) rlwa.c_(pos);
            if (tileentity == null) { // retry?
                setType(rawX, rawY, rawZ, Material.BEE_NEST);
                tileentity = (TileEntityBeehive) rlwa.c_(pos);
            }
            //
            //            NBTTagCompound nbttagcompound = new NBTTagCompound();
            //            nbttagcompound.a("id", "minecraft:bee");
            // TileEntityBeehive.storeBee
            // TileEntityBeehive.Occupant.create(1)
            tileentity.a(TileEntityBeehive.c.a(GenUtils.RANDOMIZER.nextInt(599)));
        }
        catch (NullPointerException | IllegalArgumentException | SecurityException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }
}
