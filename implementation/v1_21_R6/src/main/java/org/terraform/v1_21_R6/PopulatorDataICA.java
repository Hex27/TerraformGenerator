package org.terraform.v1_21_R6;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.NaturalSpawnType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

public class PopulatorDataICA extends PopulatorDataICABiomeWriterAbstract {
    private final PopulatorDataAbstract parent;
    private final ChunkAccess ica;
    private final int chunkX;
    private final int chunkZ;
    private final ServerLevel ws;
    private final TerraformWorld tw;

    public PopulatorDataICA(PopulatorDataAbstract parent,
                            TerraformWorld tw,
                            ServerLevel ws,
                            ChunkAccess ica,
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
        BlockState ibd = ica.a_(new BlockPos(x, y, z)); // getState
        return CraftBlockData.fromData(ibd).getMaterial();
    }

    public BlockData getBlockData(int x, int y, int z) {
        // return parent.getBlockData(x, y, z);
        BlockState ibd = ica.a_(new BlockPos(x, y, z)); // getState
        return CraftBlockData.fromData(ibd);
    }

    @Override
    public void setBiome(int rawX, int rawY, int rawZ, CustomBiomeType cbt, org.bukkit.block.Biome fallback) {
        Registry<Biome> biomeRegistry = CustomBiomeHandler.getBiomeRegistry();
        Holder<Biome> targetBiome;
        if (cbt == CustomBiomeType.NONE) {

            targetBiome = CraftBiome.bukkitToMinecraftHolder(fallback);
        }
        else {
            ResourceKey<Biome> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(cbt);// ResourceKey.a(Registry.aP, new MinecraftKey(cbt.getKey()));
            Optional<Holder.c<Biome>> optHolder = biomeRegistry.lookup(rkey); // lookup
            if (optHolder.isEmpty()) {
                TerraformGeneratorPlugin.logger.error("Custom biome was not found in the vanilla registry!");
                targetBiome = CraftBiome.bukkitToMinecraftHolder(fallback);
            }
            else {
                targetBiome = optHolder.get();
            }
        }

        ica.setBiome(rawX >> 2, rawY >> 2, rawZ >> 2, targetBiome);
    }

    @Override
    public void setBiome(int rawX, int rawY, int rawZ, org.bukkit.block.Biome biome) {
        // TerraformGeneratorPlugin.logger.info("Set " + rawX + "," + rawY + "," + rawZ + " to " + biome);
        ica.setBiome(rawX >> 2, rawY >> 2, rawZ >> 2, CraftBiome.bukkitToMinecraftHolder(biome));
    }

    @Override
    public void setType(int x, int y, int z, @NotNull Material type) {
        // parent.setType(x, y, z, type);
        //ProtoChunk appears to ignore this flag. It's no longer a bool in 1.21.5.
        //Not sure what "3" does.
        //ChunkAccess.setBlockState
        ica.setBlockState(new BlockPos(x, y, z),
                ((CraftBlockData) Bukkit.createBlockData(type)).getState(),
                3);

        // ica.setType(new BlockPos(x, y, z), ((CraftBlockData) Bukkit.createBlockData(type)).getState(), false);
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull BlockData data) {
        // parent.setBlockData(x, y, z, data);
        //see setType
        ica.setType(new BlockPos(x, y, z), ((CraftBlockData) data).getState(), 3);

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
    public void addEntity(int rawX, int rawY, int rawZ, org.bukkit.entity.EntityType type) {
        parent.addEntity(rawX, rawY, rawZ, type);
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, org.bukkit.entity.EntityType type) {
        parent.setSpawner(rawX, rawY, rawZ, type);
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        BlockPos pos = new BlockPos(x, y, z);

        // getBlockEntity
        BlockEntity te = ica.c_(pos);
        if (te instanceof RandomizableContainerBlockEntity) {
            ((RandomizableContainerBlockEntity) te).a(LootTableTranslator.translationMap.get(table));
        }
        else if (te instanceof BrushableBlockEntity)
        // BrushableBlockEntity.setLootTable
        {
            ((BrushableBlockEntity) te).a(LootTableTranslator.translationMap.get(table),
                    tw.getHashedRand(x, y, z).nextLong()
            );
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerNaturalSpawns(@NotNull NaturalSpawnType type, int x0, int y0, int z0, int x1, int y1, int z1) {
        ResourceKey<Structure> structureKey = switch (type) {
            case GUARDIAN -> BuiltinStructures.l; // Ocean Monument
            case PILLAGER -> BuiltinStructures.a; // Pillager Outpost
            case WITCH -> BuiltinStructures.j; // Swamp Hut
        };

        // bg is registryAccess
        // a is lookup
        // bm is STRUCTURE
        Registry<Structure> featureRegistry = MinecraftServer.getServer().bg().a(Registries.bm).orElseThrow();

        Structure structureFeature = featureRegistry.a(structureKey).get().a();

        try {
            // Something's broken about EnumDirection's import. Might be a temporary thing.
            Class<?> enumDirectionClass = Class.forName("net.minecraft.core.EnumDirection");
            Field enumDirectionA = enumDirectionClass.getField("a");
            enumDirectionA.setAccessible(true);
            Class<OceanMonumentPieces.h> oceanMonumentPiecesHClass = OceanMonumentPieces.h.class;
            StructurePiece customBoundPiece = (StructurePiece) oceanMonumentPiecesHClass.getConstructor(
                    RandomSource.class,
                    int.class,
                    int.class,
                    enumDirectionClass
            ).newInstance(RandomSource.a(), x0, z0, enumDirectionA.get(null));

            PiecesContainer container = new PiecesContainer(new ArrayList<>() {{
                add(customBoundPiece);
            }});

            StructureStart start = new StructureStart(structureFeature,
                    new ChunkPos(chunkX, chunkZ),
                    0,
                    container
            );

            Field i = StructureStart.class.getDeclaredField("h"); // boundingBox
            i.setAccessible(true);
            i.set(start, new StructureBoundingBox(x0, y0, z0, x1, y1, z1));

            // ws.a() is getStructureManager
            // a is setStartForStructure
            /*setStartForStructure(
             * SectionPosition sectionposition,
             * Structure structure,
             * StructureStart structurestart,
             * StructureAccess structureaccess)**/
            // ws.a().a(SectionPosition.a(x0,y0,z0), structureFeature, start, ica);

            ica.a(structureFeature, start);
            //    	ws.a().a( // setStartForFeature
            //        		structureFeature,
            //        		start);

            // addReferenceForFeature
            ica.a(structureFeature, new ChunkPos(chunkX, chunkZ).a()); // a is toLong
        }
        catch (NoSuchMethodException |
               InstantiationException |
               InvocationTargetException |
               ClassNotFoundException |
               NoSuchFieldException |
               IllegalArgumentException |
               IllegalAccessException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void spawnMinecartWithChest(int x, int y, int z, TerraLootTable table, @NotNull Random random) {
        //EntityType.CHEST_MINECART.create(generatoraccessseed.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
        MinecartChest MinecartChest = (MinecartChest) EntityType.A.a(
                ws.getMinecraftWorld(), EntitySpawnReason.b);

        //For whatever reason, the mineshaft code does a null check.
        if(MinecartChest != null)
        {
            //setPosition
            MinecartChest.a_(
                    (float) x + 0.5F,
                    (float) y + 0.5F,
                    (float) z + 0.5F
            );
            MinecartChest.a(LootTableTranslator.translationMap.get(table), random.nextLong());
            ws.addFreshEntity(MinecartChest, SpawnReason.CHUNK_GEN);
        }
    }


    @Override
    public @NotNull TerraformWorld getTerraformWorld() {
        return tw;
    }

}
