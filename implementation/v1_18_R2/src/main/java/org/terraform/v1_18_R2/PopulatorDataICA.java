package org.terraform.v1_18_R2;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.vehicle.EntityMinecartChest;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.entity.TileEntityLootable;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.IStructureAccess;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.storage.loot.LootTables;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.block.data.CraftBlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.NaturalSpawnType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

public class PopulatorDataICA extends PopulatorDataICABiomeWriterAbstract {
    private final PopulatorDataAbstract parent;
    private final IChunkAccess ica;
    private final int chunkX;
    private final int chunkZ;
    private final WorldServer ws;
    private final TerraformWorld tw;

    public PopulatorDataICA(PopulatorDataAbstract parent,
                            TerraformWorld tw,
                            WorldServer ws,
                            IChunkAccess ica,
                            int chunkX,
                            int chunkZ)
    {
        this.ica = ica;
        this.parent = parent;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.ws = ws;
        this.tw = tw;
    }

    public @NotNull Material getType(int x, int y, int z) {
        // return parent.getType(x, y, z);
        IBlockData ibd = ica.a_(new BlockPosition(x, y, z)); // getState
        return CraftBlockData.fromData(ibd).getMaterial();
    }

    public BlockData getBlockData(int x, int y, int z) {
        // return parent.getBlockData(x, y, z);
        IBlockData ibd = ica.a_(new BlockPosition(x, y, z)); // getState
        return CraftBlockData.fromData(ibd);
    }

    @Override
    public void setBiome(int rawX, int rawY, int rawZ, CustomBiomeType cbt, Biome fallback) {
        DedicatedServer dedicatedserver = ((CraftServer) Bukkit.getServer()).getServer();
        IRegistry<BiomeBase> biomeRegistry = dedicatedserver.aU().b(IRegistry.aP);
        Holder<BiomeBase> targetBiome;
        if (cbt == CustomBiomeType.NONE) {

            targetBiome = CraftBlock.biomeToBiomeBase(ica.biomeRegistry, fallback);
        }
        else {
            ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(cbt);// ResourceKey.a(IRegistry.aP, new MinecraftKey(cbt.getKey()));
            targetBiome = biomeRegistry.g(rkey); // getHolderOrThrow
            if (targetBiome == null) {
                TerraformGeneratorPlugin.logger.error("Custom biome was not found in the vanilla registry!");
                targetBiome = CraftBlock.biomeToBiomeBase(ica.biomeRegistry, fallback);
            }
        }

        ica.setBiome(rawX >> 2, rawY >> 2, rawZ >> 2, targetBiome);
    }

    @Override
    public void setBiome(int rawX, int rawY, int rawZ, Biome biome) {
        // TerraformGeneratorPlugin.logger.info("Set " + rawX + "," + rawY + "," + rawZ + " to " + biome);
        ica.setBiome(rawX >> 2, rawY >> 2, rawZ >> 2, CraftBlock.biomeToBiomeBase(ica.biomeRegistry, biome));
    }

    @Override
    public void setType(int x, int y, int z, @NotNull Material type) {
        // parent.setType(x, y, z, type);
        ica.a(new BlockPosition(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), false);

        // ica.setType(new BlockPosition(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), false);
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull BlockData data) {
        // parent.setBlockData(x, y, z, data);
        ica.a(new BlockPosition(x, y, z), ((CraftBlockData) data).getState(), false);

    }

    public Biome getBiome(int rawX, int rawZ) {
        return parent.getBiome(rawX, rawZ);
        // return tw.getBiomeBank(rawX, rawZ).getHandler().getBiome();// BiomeBank.calculateBiome(tw,tw.getTemperature(rawX, rawZ), y).getHandler().getBiome();// Biome.valueOf(ica
        // .getBiome(rawX, rawY, rawZ).l().replace("biome.minecraft.", "").toUpperCase(Locale.ENGLISH));
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
        parent.addEntity(rawX, rawY, rawZ, type);
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
        parent.setSpawner(rawX, rawY, rawZ, type);
    }

    @Override
    public void lootTableChest(int x, int y, int z, @NotNull TerraLootTable table) {
        BlockPosition pos = new BlockPosition(x, y, z);
        TileEntityLootable.a(ica, tw.getHashedRand(x, y, z), pos, getLootTable(table));
    }

    @Override
    public void registerNaturalSpawns(@NotNull NaturalSpawnType type, int x0, int y0, int z0, int x1, int y1, int z1) {
        ResourceKey<StructureFeature<?, ?>> structureKey = switch (type) {
            case GUARDIAN -> BuiltinStructures.l; // Ocean Monument
            case PILLAGER -> BuiltinStructures.a; // Pillager Outpost
            case WITCH -> BuiltinStructures.j; // Swamp Hut
        }; // Monument

        CraftServer craftserver = (CraftServer) Bukkit.getServer();
        DedicatedServer dedicatedserver = craftserver.getServer();
        IRegistry<StructureFeature<?, ?>> featureRegistry = dedicatedserver.aU().b(IRegistry.aL);

        StructureFeature<?, ?> structureFeature = featureRegistry.a(structureKey);

        StructurePiece customBoundPiece = new WorldGenMonumentPieces.WorldGenMonumentPiece1(new Random(),
                x0,
                z0,
                EnumDirection.a
        );

        PiecesContainer container = new PiecesContainer(new ArrayList<>() {{
            add(customBoundPiece);
        }});

        StructureStart start = new StructureStart(structureFeature,
                new ChunkCoordIntPair(chunkX, chunkZ),
                0,
                container
        );

        try {
            Field i = StructureStart.class.getDeclaredField("g");
            i.setAccessible(true);
            i.set(start, new StructureBoundingBox(x0, y0, z0, x1, y1, z1));
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }

        IStructureAccess sa = ica; // IStructureAccess is FeatureAccess
        sa.a( // setStartForFeature
                structureFeature, start);

        // addReferenceForFeature
        sa.a(structureFeature, new ChunkCoordIntPair(chunkX, chunkZ).a()); // a is toLong
    }

    @SuppressWarnings("deprecation")
    @Override
    public void spawnMinecartWithChest(int x, int y, int z, @NotNull TerraLootTable table, @NotNull Random random) {
        EntityMinecartChest entityminecartchest = new EntityMinecartChest(ws.getMinecraftWorld(),
                (float) x + 0.5F,
                (float) y + 0.5F,
                (float) z + 0.5F
        );

        entityminecartchest.a(getLootTable(table), random.nextLong());
        ws.addFreshEntity(entityminecartchest, SpawnReason.CHUNK_GEN);
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
        return tw;
    }
}
