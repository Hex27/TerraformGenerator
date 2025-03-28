package org.terraform.v1_18_R2;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityLootable;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.storage.loot.LootTables;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.NativeGeneratorPatcherPopulator;
import org.terraform.coregen.populatordata.IPopulatorDataBaseHeightAccess;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;

import java.util.HashMap;

public class PopulatorData extends PopulatorDataAbstract implements IPopulatorDataBaseHeightAccess {
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
                if (type == EntityType.ENDER_SIGNAL) {
                    continue;
                }
                if (type == EntityType.UNKNOWN) {
                    continue;
                }
                try {
                    EntityTypes<?> et = (EntityTypes<?>) EntityTypes.class.getDeclaredField(EntityTypeMapper.getObfsNameFromBukkitEntityType(
                            type)).get(null);
                    entityTypesDict.put(type, et);
                }
                catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
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
    public void addEntity(int rawX, int rawY, int rawZ, EntityType type) {
        if (Math.abs((rawX >> 4) - chunkX) > 1 || Math.abs((rawZ >> 4) - chunkZ) > 1) {
            TerraformGeneratorPlugin.logger.info("Failed to spawn " + type + " as it was out of bounds.");
            return;
        }
        try {
            EntityTypes<?> et = entityTypesDict.get(type);
            Entity e = et.a(rlwa.getMinecraftWorld());
            // o is setPosRaw
            e.o((double) rawX + 0.5D, rawY, (double) rawZ + 0.5D);
            if (e instanceof EntityInsentient) {

                ((EntityInsentient) e).a(
                        rlwa,
                        rlwa.d_(new BlockPosition(rawX + 0.5D, rawY, rawZ + 0.5D)),
                        EnumMobSpawn.n,
                        (GroupDataEntity) null,
                        (NBTTagCompound) null
                );
            }
            // b is addFreshEntity
            rlwa.b(e);
        }
        catch (IllegalArgumentException | SecurityException e1) {
            TerraformGeneratorPlugin.logger.stackTrace(e1);
        }

        // rlwa.spawnEntity(new Location(gen.getTerraformWorld().getWorld(), rawX, rawY, rawZ), type);
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, @NotNull EntityType type) {
        if (!TConfig.areAnimalsEnabled()) {
            return;
        }

        BlockPosition pos = new BlockPosition(rawX, rawY, rawZ);

        setType(rawX, rawY, rawZ, Material.SPAWNER);
        TileEntity tileentity = rlwa.c_(pos);

        if (tileentity instanceof TileEntityMobSpawner) {
            try {
                // Fetch from ENTITY_TYPE_REGISTRY (m)'s map
                @SuppressWarnings("deprecation")
                // W is ENTITY_TYPE
                EntityTypes<?> nmsEntity = IRegistry.W.a(new MinecraftKey(type.getName()));
                ((TileEntityMobSpawner) tileentity).d().a(nmsEntity);
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
    public void lootTableChest(int x, int y, int z, @NotNull TerraLootTable table) {
        BlockPosition pos = new BlockPosition(x, y, z);
        TileEntityLootable.a(rlwa, gen.getTerraformWorld().getHashedRand(x, y, z), pos, getLootTable(table));
    }

    private @Nullable MinecraftKey getLootTable(@NotNull TerraLootTable table) {
        return switch (table) {
            case EMPTY -> LootTables.a;
            case SPAWN_BONUS_CHEST -> LootTables.b;
            case END_CITY_TREASURE -> LootTables.c;
            case SIMPLE_DUNGEON -> LootTables.d;
            case VILLAGE_WEAPONSMITH -> LootTables.e;
            case VILLAGE_TOOLSMITH -> LootTables.f;
            case VILLAGE_ARMORER -> LootTables.g;
            case VILLAGE_CARTOGRAPHER -> LootTables.h;
            case VILLAGE_MASON -> LootTables.i;
            case VILLAGE_SHEPHERD -> LootTables.j;
            case VILLAGE_BUTCHER -> LootTables.k;
            case VILLAGE_FLETCHER -> LootTables.l;
            case VILLAGE_FISHER -> LootTables.m;
            case VILLAGE_TANNERY -> LootTables.n;
            case VILLAGE_TEMPLE -> LootTables.o;
            case VILLAGE_DESERT_HOUSE -> LootTables.p;
            case VILLAGE_PLAINS_HOUSE -> LootTables.q;
            case VILLAGE_TAIGA_HOUSE -> LootTables.r;
            case VILLAGE_SNOWY_HOUSE -> LootTables.s;
            case VILLAGE_SAVANNA_HOUSE -> LootTables.t;
            case ABANDONED_MINESHAFT -> LootTables.u;
            case NETHER_BRIDGE -> LootTables.v;
            case STRONGHOLD_LIBRARY -> LootTables.w;
            case STRONGHOLD_CROSSING -> LootTables.x;
            case STRONGHOLD_CORRIDOR -> LootTables.y;
            case DESERT_PYRAMID -> LootTables.z;
            case JUNGLE_TEMPLE -> LootTables.A;
            case JUNGLE_TEMPLE_DISPENSER -> LootTables.B;
            case IGLOO_CHEST -> LootTables.C;
            case WOODLAND_MANSION -> LootTables.D;
            case UNDERWATER_RUIN_SMALL -> LootTables.E;
            case UNDERWATER_RUIN_BIG -> LootTables.F;
            case BURIED_TREASURE -> LootTables.G;
            case SHIPWRECK_MAP -> LootTables.H;
            case SHIPWRECK_SUPPLY -> LootTables.I;
            case SHIPWRECK_TREASURE -> LootTables.J;
            case PILLAGER_OUTPOST -> LootTables.K;
            case RUINED_PORTAL -> LootTables.P;
            default ->
                //        case SHEEP_WHITE:
                //            return LootTables.Q;
                //        case SHEEP_ORANGE:
                //            return LootTables.R;
                //        case SHEEP_MAGENTA:
                //            return LootTables.S;
                //        case SHEEP_LIGHT_BLUE:
                //            return LootTables.T;
                //        case SHEEP_YELLOW:
                //            return LootTables.U;
                //        case SHEEP_LIME:
                //            return LootTables.V;
                //        case SHEEP_PINK:
                //            return LootTables.W;
                //        case SHEEP_GRAY:
                //            return LootTables.X;
                //        case SHEEP_LIGHT_GRAY:
                //            return LootTables.Y;
                //        case SHEEP_CYAN:
                //            return LootTables.Z;
                //        case SHEEP_PURPLE:
                //            return LootTables.aa;
                //        case SHEEP_BLUE:
                //            return LootTables.ab;
                //        case SHEEP_BROWN:
                //            return LootTables.ac;
                //        case SHEEP_GREEN:
                //            return LootTables.ad;
                //        case SHEEP_RED:
                //            return LootTables.ae;
                //        case SHEEP_BLACK:
                //            return LootTables.af;
                //        case FISHING:
                //            return LootTables.ag;
                //        case FISHING_JUNK:
                //            return LootTables.ah;
                //        case FISHING_TREASURE:
                //            return LootTables.ai;
                //        case FISHING_FISH:
                //            return LootTables.aj;
                //        case CAT_MORNING_GIFT:
                //            return LootTables.ak;
                //        case ARMORER_GIFT:
                //            return LootTables.al;
                //        case BUTCHER_GIFT:
                //            return LootTables.am;
                //        case CARTOGRAPHER_GIFT:
                //            return LootTables.an;
                //        case CLERIC_GIFT:
                //            return LootTables.ao;
                //        case FARMER_GIFT:
                //            return LootTables.ap;
                //        case FISHERMAN_GIFT:
                //            return LootTables.aq;
                //        case FLETCHER_GIFT:
                //            return LootTables.ar;
                //        case LEATHERWORKER_GIFT:
                //            return LootTables.as;
                //        case LIBRARIAN_GIFT:
                //            return LootTables.at;
                //        case MASON_GIFT:
                //            return LootTables.au;
                //        case SHEPHERD_GIFT:
                //            return LootTables.av;
                //        case TOOLSMITH_GIFT:
                //            return LootTables.aw;
                //        case WEAPONSMITH_GIFT:
                //            return LootTables.ax;
                //        case PIGLIN_BARTERING:
                //            return LootTables.ay
                    null;
        };
    }

    @Override
    public @NotNull TerraformWorld getTerraformWorld() {
        return gen.getTerraformWorld();
    }

    @Override
    public int getBaseHeight(int rawX, int rawZ) {
        return gen.a(rawX, rawZ, HeightMap.Type.a, this.rlwa);
    }
}
