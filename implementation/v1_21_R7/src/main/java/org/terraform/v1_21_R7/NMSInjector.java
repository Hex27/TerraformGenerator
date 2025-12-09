package org.terraform.v1_21_R7;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Beehive;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.generator.CraftLimitedRegion;
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
import org.terraform.utils.version.TerraformFieldHandler;
import org.terraform.utils.version.TerraformMethodHandler;

import java.lang.reflect.InvocationTargetException;

public class NMSInjector extends NMSInjectorAbstract {

    // private boolean heightInjectSuccess = true;

    private static @Nullable TerraformMethodHandler getTileEntity = null;

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
        try {
            CraftWorld cw = (CraftWorld) world;
            ServerLevel ws = cw.getHandle();

            // Force world to correct height
            TerraformWorld.get(world).minY = -64;
            TerraformWorld.get(world).maxY = 320;

            // n is getChunkSource, g is getGenerator()
            ChunkGenerator delegate = ws.getChunkSource().getGenerator();

            TerraformGeneratorPlugin.logger.info("NMSChunkGenerator Delegate is of type " + delegate.getClass()
                                                                                                    .getSimpleName());

            // String worldname,
            // int seed,
            // WorldChunkManager worldchunkmanager,
            // WorldChunkManager worldchunkmanager1,
            // StructureSettings structuresettings,
            // long i
            NMSChunkGenerator bpg = new NMSChunkGenerator(world.getName(), (int) world.getSeed(), delegate);

            // Inject TerraformGenerator NMS chunk generator into ChunkMap AND worldgencontext
            ChunkMap pcm = ws.getChunkSource().chunkMap; // getChunkProvider().ChunkMap
            // worldGenContext stores chunkGenerator, not pcm
            // Q is worldGenContext
            var wgc = new TerraformFieldHandler(pcm.getClass(), "worldGenContext","N");
            WorldGenContext worldGenContext = (WorldGenContext) wgc.field.get(pcm);
            // b is chunkGenerator
            wgc.field.set(pcm,
                    new WorldGenContext(worldGenContext.level(),
                            bpg,
                            worldGenContext.structureManager(),
                            worldGenContext.lightEngine(),
                            worldGenContext.mainThreadExecutor(),
                            worldGenContext.unsavedListener()
                    )
            );
            TerraformGeneratorPlugin.logger.info("Post injection: getChunkSource().getChunkGenerator() is of type "
                                                 + ws.getChunkSource().getGenerator().getClass().getSimpleName());
        }
        catch (Throwable e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
            return false;
        }

        return true;
    }

    @Override
    public @NotNull PopulatorDataICAAbstract getICAData(@NotNull Chunk chunk) {
        // ChunKStatus.FULL
        ChunkAccess ica = ((CraftChunk) chunk).getHandle(ChunkStatus.FULL);
        CraftWorld cw = (CraftWorld) chunk.getWorld();
        ServerLevel ws = cw.getHandle();

        TerraformWorld tw = TerraformWorld.get(chunk.getWorld());
        // return new PopulatorData(new RegionLimitedWorldAccess(ws, list), null, chunk.getX(), chunk.getZ());
        return new PopulatorDataICA(new PopulatorDataPostGen(chunk), tw, ws, ica, chunk.getX(), chunk.getZ());
    }

    @Override
    public PopulatorDataICAAbstract getICAData(PopulatorDataAbstract data) {
        // This is for the damn bees
        if (data instanceof PopulatorDataSpigotAPI pdata) {
            WorldGenLevel gas = ((CraftLimitedRegion) pdata.lr).getHandle();
            ServerLevel ws = gas.getMinecraftWorld();
            TerraformWorld tw = TerraformWorld.get(ws.getWorld().getName(), ws.getSeed()); // H is getSeed()
            return new PopulatorDataICA(
                    data,
                    tw,
                    ws,
                    gas.getChunk(data.getChunkX(), data.getChunkZ()),
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
                getTileEntity = new TerraformMethodHandler(CraftBlockEntityState.class,
                        new String[]{"getTileEntity","getBlockEntity"});
            }
            BeehiveBlockEntity teb = (BeehiveBlockEntity) getTileEntity.method.invoke(hive);
            //
            //            NBTTagCompound nbttagcompound = new NBTTagCompound();
            //            nbttagcompound.a("id", "minecraft:bee");
            // BeehiveBlockEntity.storeBee
            // BeehiveBlockEntity.Occupant.create(1)
            // Copied from world\level\levelgen\feature\treedecorators\WorldGenFeatureTreeBeehive.java
            teb.storeBee(BeehiveBlockEntity.Occupant.create(GenUtils.RANDOMIZER.nextInt(599)));

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
