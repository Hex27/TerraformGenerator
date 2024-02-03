package org.terraform.coregen.bukkit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jigsaw;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.coregen.populatordata.PopulatorDataSpigotAPI;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.event.TerraformStructureSpawnEvent;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.JigsawState;
import org.terraform.structure.JigsawStructurePopulator;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.StructureRegistry;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.structure.room.path.PathState;
import org.terraform.structure.stronghold.StrongholdPopulator;
import org.terraform.utils.BlockUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class TerraformStructurePopulator extends BlockPopulator {

    private final LoadingCache<MegaChunk, JigsawState> jigsawCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .build(CacheLoader.from((mc)->null));
    private final TerraformWorld tw;

    public TerraformStructurePopulator(TerraformWorld tw) {
        this.tw = tw;
    }

    //NEW BLOCK POPULATOR API
    //Used to generate small paths and small rooms
    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion lr) {
        //Check if the nearest structure in this megachunk is close enough
        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();

        JigsawState state = jigsawCache.getIfPresent(mc);
        if(state == null)
        {
            SingleMegaChunkStructurePopulator spop = getMegachunkStructure(mc, tw, biome);
            if(spop == null) return;
            if(!(spop instanceof JigsawStructurePopulator jsp)) return;
            state = jsp.calculateRoomPopulators(tw, mc);
            TerraformGeneratorPlugin.logger.info("Calculated structure at " + chunkX + "," + chunkZ);
            jigsawCache.put(mc, state);
        }

        PopulatorDataAbstract data = new PopulatorDataSpigotAPI(lr, tw, chunkX, chunkZ);

        //Carve each path
        HashSet<PathState.PathNode> seenNodes = new HashSet<>();
        state.roomPopulatorStates.forEach(roomLayoutGenerator -> {
            final PathState pathState = roomLayoutGenerator.getOrCalculatePathState(tw);
            pathState.nodes.stream()
                    //Filter to only those inside this chunk
                    .filter(node->node.center.getX() >> 4 == chunkX && node.center.getZ() >> 4 == chunkZ)
                    .forEach(node->{
                        pathState.writer.apply(data, tw, node);
                        seenNodes.add(node);
                    });
        });

        //Carve each room
        ArrayList<CubeRoom> seenRooms = new ArrayList<>();
        state.roomPopulatorStates.forEach(roomLayoutGenerator ->
                roomLayoutGenerator.getRooms().stream()
                //No rooms that have bounds beyond LR
                .filter(room-> room.isInRegion(lr))
                .forEach(room->
                {
                    seenRooms.add(room);
                    roomLayoutGenerator.roomCarver.carveRoom(data, room, roomLayoutGenerator.wallMaterials);
                }));

        //Populate the paths
        seenNodes.forEach((node)->{
            //If the path has a direction of up, it is a crossway.
            if(node.populator != null)
                node.populator.populate(new PathPopulatorData(
                        new Wall(new SimpleBlock(data, node.center),
                                node.connected.size() == 1 ? node.connected.stream().findAny().get()
                                        : BlockFace.UP),
                        node.pathWidth));
        });

        //Populate the rooms
        seenRooms.forEach(room-> room.getPop().populate(data, room));
    }

    //OLDER BLOCK POPULATOR API
    //Used for large structures as they are too big and rely on a guaranteed write.
    //The older api allows guaranteed writes via cascasion. Slow, but guaranteed to work
    @Override
    public void populate(World world, @NotNull Random random, @NotNull Chunk chunk) {
        //Structuregen will freeze for long periods
        TerraformGeneratorPlugin.watchdogSuppressant.tickWatchdog();
        //Don't attempt generation pre-injection.
        if(!TerraformGeneratorPlugin.INJECTED_WORLDS.contains(world.getName())) return;
        PopulatorDataPostGen data = new PopulatorDataPostGen(chunk);

        //Use IChunkAccess to place blocks instead. Known to cause lighting problems.
        //Since people keep turning this on for fun, then reporting bugs, I'm removing it. 
//        if (TConfigOption.DEVSTUFF_EXPERIMENTAL_STRUCTURE_PLACEMENT.getBoolean())
//            data = new PopulatorDataRecursiveICA(chunk);

        //Spawn large structures
        MegaChunk mc = new MegaChunk(chunk.getX(), chunk.getZ());
        BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();

        //Special Case
        if(new StrongholdPopulator().canSpawn(tw, data.getChunkX(), data.getChunkZ(), biome)) {
            TerraformGeneratorPlugin.logger.info("Generating Stronghold at chunk: " + data.getChunkX() + "," + data.getChunkZ());
            new StrongholdPopulator().populate(tw, data);
        }

        //Only check singlemegachunkstructures if this chunk is a central chunk.
        int[] chunkCoords = mc.getCenterBiomeSectionChunkCoords();
        //TerraformGeneratorPlugin.logger.info("[v] MC(" + mc.getX() + "," + mc.getZ() + ") - " + data.getChunkX() + "," + data.getChunkZ() + " - Center: " + chunkCoords[0] + "," + chunkCoords[1]);
        if(chunkCoords[0] == data.getChunkX()
                && chunkCoords[1] == data.getChunkZ()) {
            int[] blockCoords = mc.getCenterBiomeSectionBlockCoords();

            //TerraformGeneratorPlugin.logger.info("[!] MC(" + mc.getX() + "," + mc.getZ() + ") - " + data.getChunkX() + "," + data.getChunkZ() + " - Center: " + chunkCoords[0] + "," + chunkCoords[1]);
            for(SingleMegaChunkStructurePopulator spop : StructureRegistry.getLargeStructureForMegaChunk(tw, mc)) {
                if(spop == null) continue;
                if(!spop.isEnabled()) continue;
                if(spop instanceof StrongholdPopulator) continue;
                //if(spop instanceof JigsawStructurePopulator) continue;
                //TerraformGeneratorPlugin.logger.info("[v]       MC(" + mc.getX() + "," + mc.getZ() + ") - Checking " + spop.getClass().getName());
                if(spop.canSpawn(tw, data.getChunkX(), data.getChunkZ(), biome)) {
                    TerraformGeneratorPlugin.logger.info("Generating " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
                    Bukkit.getPluginManager().callEvent(new TerraformStructureSpawnEvent(blockCoords[0], blockCoords[1], spop.getClass().getName()));
                    spop.populate(tw, data);
                    break;
                }
            }
        }
    }

    public @Nullable SingleMegaChunkStructurePopulator getMegachunkStructure(MegaChunk mc, TerraformWorld tw, BiomeBank biome){
        int[] chunkCoords = mc.getCenterBiomeSectionChunkCoords();
        for(SingleMegaChunkStructurePopulator spop : StructureRegistry.getLargeStructureForMegaChunk(tw, mc)) {
            if(spop == null) continue;
            if(!spop.isEnabled()) continue;
            if(spop instanceof StrongholdPopulator) continue;
            //TerraformGeneratorPlugin.logger.info("[v]       MC(" + mc.getX() + "," + mc.getZ() + ") - Checking " + spop.getClass().getName());
            if(spop.canSpawn(tw, chunkCoords[0], chunkCoords[1], biome)) {
                return spop;
            }
        }
        return null;
    }

}
