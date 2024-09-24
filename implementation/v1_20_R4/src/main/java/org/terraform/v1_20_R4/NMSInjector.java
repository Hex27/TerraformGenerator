package org.terraform.v1_20_R4;

import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.GeneratorAccessSeed;
import net.minecraft.world.level.block.entity.TileEntityBeehive;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.craftbukkit.v1_20_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_20_R4.generator.CraftLimitedRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICAAbstract;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.coregen.populatordata.PopulatorDataSpigotAPI;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSInjector extends NMSInjectorAbstract {

    // private boolean heightInjectSuccess = true;

    private static @Nullable Method getTileEntity = null;

    @Override
    public void startupTasks() {
        // Inject new biomes
        CustomBiomeHandler.init();
    }

    @Override
    public @NotNull BlockDataFixerAbstract getBlockDataFixer() {
        return new BlockDataFixer();
    }

    @Override
    public boolean attemptInject(@NotNull World world) {
        CraftWorld cw = (CraftWorld) world;
        WorldServer ws = cw.getHandle();

        // Force world to correct height
        TerraformWorld.get(world).minY = -64;
        TerraformWorld.get(world).maxY = 320;

        // k is getChunkSource, g is getChunkGenerator()
        ChunkGenerator delegate = ws.l().g();

        TerraformGeneratorPlugin.logger.info("NMSChunkGenerator Delegate is of type " + delegate.getClass()
                                                                                                .getSimpleName());

        // String worldname,
        // int seed,
        // WorldChunkManager worldchunkmanager,
        // WorldChunkManager worldchunkmanager1,
        // StructureSettings structuresettings,
        // long i
        NMSChunkGenerator bpg = new NMSChunkGenerator(world.getName(), (int) world.getSeed(), delegate);

        // Inject TerraformGenerator NMS chunk generator into playerchunkmap AND worldgencontext
        PlayerChunkMap pcm = ws.l().a; // getChunkProvider().PlayerChunkMap
        pcm.t = bpg;
        try {
            // O is worldGenContext
            Field wgc = pcm.getClass().getDeclaredField("O");
            wgc.setAccessible(true);
            WorldGenContext worldGenContext = (WorldGenContext) wgc.get(pcm);
            // b is chunkGenerator
            wgc.set(pcm, new WorldGenContext(worldGenContext.a(), bpg, worldGenContext.c(), worldGenContext.d()));
        }
        catch (Throwable e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
            return false;
        }
        TerraformGeneratorPlugin.logger.info("Post injection: getChunkSource().getChunkGenerator() is of type " + ws.l()
                                                                                                                    .g()
                                                                                                                    .getClass()
                                                                                                                    .getSimpleName());

        return true;
    }

    @Override
    public @NotNull PopulatorDataICAAbstract getICAData(@NotNull Chunk chunk) {
        // ChunKStatus.FULL
        IChunkAccess ica = ((CraftChunk) chunk).getHandle(ChunkStatus.n);
        CraftWorld cw = (CraftWorld) chunk.getWorld();
        WorldServer ws = cw.getHandle();

        TerraformWorld tw = TerraformWorld.get(chunk.getWorld());
        // return new PopulatorData(new RegionLimitedWorldAccess(ws, list), null, chunk.getX(), chunk.getZ());
        return new PopulatorDataICA(new PopulatorDataPostGen(chunk), tw, ws, ica, chunk.getX(), chunk.getZ());
    }

    @Override
    public @Nullable PopulatorDataICAAbstract getICAData(PopulatorDataAbstract data) {
        // This is for the damn bees
        if (data instanceof PopulatorDataSpigotAPI pdata) {
            GeneratorAccessSeed gas = ((CraftLimitedRegion) pdata.lr).getHandle();
            WorldServer ws = gas.getMinecraftWorld();
            TerraformWorld tw = TerraformWorld.get(ws.getWorld().getName(), ws.C()); // C is getSeed()
            return new PopulatorDataICA(
                    data,
                    tw,
                    ws,
                    gas.a(data.getChunkX(), data.getChunkZ()),
                    data.getChunkX(),
                    data.getChunkZ()
            );
        }
        if (data instanceof PopulatorDataPostGen gdata) {
            return getICAData(gdata.getChunk());
        }

        return null;
    }

    @Override
    public void storeBee(Beehive hive) {
        try {
            if (getTileEntity == null) {
                getTileEntity = CraftBlockEntityState.class.getDeclaredMethod("getTileEntity");
                getTileEntity.setAccessible(true);
            }
            TileEntityBeehive teb = (TileEntityBeehive) getTileEntity.invoke(hive);
            //
            //            NBTTagCompound nbttagcompound = new NBTTagCompound();
            //            nbttagcompound.a("id", "minecraft:bee");
            // TileEntityBeehive.storeBee
            // TileEntityBeehive.Occupant.create(1)
            // Copied from world\level\levelgen\feature\treedecorators\WorldGenFeatureTreeBeehive.java
            teb.a(TileEntityBeehive.c.a(GenUtils.RANDOMIZER.nextInt(599)));

        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getMaxY() {
        return 320;
    }

}
