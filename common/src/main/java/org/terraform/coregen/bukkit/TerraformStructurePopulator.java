package org.terraform.coregen.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
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
import org.terraform.main.config.TConfig;
import org.terraform.structure.JigsawState;
import org.terraform.structure.JigsawStructurePopulator;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.StructureRegistry;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.room.path.PathState;
import org.terraform.structure.stronghold.StrongholdPopulator;
import org.terraform.utils.datastructs.ConcurrentLRUCache;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class TerraformStructurePopulator extends BlockPopulator {

    private final ConcurrentLRUCache<MegaChunk, JigsawState> jigsawCache;
    private final TerraformWorld tw;

    public TerraformStructurePopulator(TerraformWorld tw) {
        this.tw = tw;
        this.jigsawCache = new ConcurrentLRUCache<>("jigsawCache",20, (mc)->{
            BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();
            SingleMegaChunkStructurePopulator spop = getMegachunkStructure(mc, tw, biome);
            if (spop == null) {
                return null;
            }
            if (!(spop instanceof JigsawStructurePopulator jsp)) {
                return null;
            }
            return jsp.calculateRoomPopulators(tw, mc);
        });
    }

    // NEW BLOCK POPULATOR API
    // Used to generate small paths and small rooms
    @Override
    public void populate(@NotNull WorldInfo worldInfo,
                         @NotNull Random random,
                         int chunkX,
                         int chunkZ,
                         @NotNull LimitedRegion lr)
    {
        // Check if the nearest structure in this megachunk is close enough
        MegaChunk mc = new MegaChunk(chunkX, chunkZ);

        JigsawState state = jigsawCache.get(mc);
        if(state == null) return;
        // Check if the room will be in range
        if (!state.isInRange(chunkX, chunkZ)) {
            return;
        }

        PopulatorDataAbstract data = new PopulatorDataSpigotAPI(lr, tw, chunkX, chunkZ);

        // Carve each path
        ArrayList<HashSet<PathState.PathNode>> seenNodes = new ArrayList<>();
        for(int i = 0; i < state.roomPopulatorStates.size(); i++)
        {
            RoomLayoutGenerator roomLayoutGenerator = state.roomPopulatorStates.get(i);
            HashSet<PathState.PathNode> nodes = new HashSet<>();
            final PathState pathState = roomLayoutGenerator.getOrCalculatePathState(tw);
            pathState.nodes.stream()
               // Filter to only those inside this chunk
               .filter(node -> node.center.getX() >> 4 == chunkX && node.center.getZ() >> 4 == chunkZ)
               .forEach(node -> {
                   pathState.writer.apply(data, tw, node);
                   nodes.add(node);
               });
            seenNodes.add(nodes);
        }

        // Carve each room
        ArrayList<CubeRoom> seenRooms = new ArrayList<>();
        state.roomPopulatorStates.forEach(roomLayoutGenerator -> roomLayoutGenerator.getRooms()
            .stream()
            .filter(room -> room.canLRCarve(chunkX,chunkZ,lr))
            .forEach(room -> {
                seenRooms.add(room);
                if(roomLayoutGenerator.roomCarver != null)
                    roomLayoutGenerator.roomCarver.carveRoom(
                            data,
                            room,
                            roomLayoutGenerator.wallMaterials
                    );
            }));

        // Populate the paths
        seenNodes.forEach((nodes)->nodes.forEach((node) -> {
            // If the path has a direction of up, it is a crossway.
            if (node.populator != null) {
                node.populator.populate(new PathPopulatorData(new Wall(
                        new SimpleBlock(data, node.center),
                        node.connected.size() == 1 ? node.connected.stream().findAny().get() : BlockFace.UP
                ), node.pathRadius));
            }
        }));

        // Populate the rooms
        seenRooms.stream()
                .filter(room -> room.getPop() != null)
                .forEach(room -> room.getPop().populate(data, room));
    }

    // OLDER BLOCK POPULATOR API
    // Used for large structures as they are too big and rely on a guaranteed write.
    // The older api allows guaranteed writes via cascasion. Slow, but guaranteed to work
    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk chunk) {
        // Structuregen will freeze for long periods
        TerraformGeneratorPlugin.watchdogSuppressant.tickWatchdog();
        // Don't attempt generation pre-injection.
        if (!TerraformGeneratorPlugin.INJECTED_WORLDS.contains(world.getName())) {
            return;
        }
        PopulatorDataPostGen data = new PopulatorDataPostGen(chunk);

        // Use IChunkAccess to place blocks instead. Known to cause lighting problems.
        // Since people keep turning this on for fun, then reporting bugs, I'm removing it.
        //        if (TConfigOption.DEVSTUFF_EXPERIMENTAL_STRUCTURE_PLACEMENT)
        //            data = new PopulatorDataRecursiveICA(chunk);

        // Spawn large structures
        MegaChunk mc = new MegaChunk(chunk.getX(), chunk.getZ());
        BiomeBank biome = mc.getCenterBiomeSection(tw).getBiomeBank();

        // Special Case
        if (TConfig.areStructuresEnabled() && new StrongholdPopulator().canSpawn(
                tw,
                data.getChunkX(),
                data.getChunkZ(),
                biome
        ))
        {
            TerraformGeneratorPlugin.logger.info("Generating Stronghold at chunk: "
                                                 + data.getChunkX()
                                                 + ","
                                                 + data.getChunkZ());
            new StrongholdPopulator().populate(tw, data);
        }

        // Only check singlemegachunkstructures if this chunk is a central chunk.
        int[] chunkCoords = mc.getCenterBiomeSectionChunkCoords();
        // TerraformGeneratorPlugin.logger.info("[v] MC(" + mc.getX() + "," + mc.getZ() + ") - " + data.getChunkX() + "," + data.getChunkZ() + " - Center: " + chunkCoords[0] + "," + chunkCoords[1]);
        if (chunkCoords[0] == data.getChunkX() && chunkCoords[1] == data.getChunkZ()) {
            int[] blockCoords = mc.getCenterBiomeSectionBlockCoords();

            // TerraformGeneratorPlugin.logger.info("[!] MC(" + mc.getX() + "," + mc.getZ() + ") - " + data.getChunkX() + "," + data.getChunkZ() + " - Center: " + chunkCoords[0] + "," + chunkCoords[1]);
            for (SingleMegaChunkStructurePopulator spop : StructureRegistry.getLargeStructureForMegaChunk(tw, mc)) {
                if (spop == null) {
                    continue;
                }
                if (!spop.isEnabled()) {
                    continue;
                }
                if (spop instanceof StrongholdPopulator) {
                    continue;
                }
                if (TConfig.areStructuresEnabled() && spop.canSpawn(
                        tw,
                        data.getChunkX(),
                        data.getChunkZ(),
                        biome
                ))
                {
                    TerraformGeneratorPlugin.logger.info("Generating "
                                                         + spop.getClass().getName()
                                                         + " at chunk: "
                                                         + data.getChunkX()
                                                         + ","
                                                         + data.getChunkZ());
                    Bukkit.getPluginManager()
                          .callEvent(new TerraformStructureSpawnEvent(blockCoords[0],
                                  blockCoords[1],
                                  spop.getClass().getName()
                          ));
                    spop.populate(tw, data);
                    break;
                }
            }
        }
    }

    public @Nullable SingleMegaChunkStructurePopulator getMegachunkStructure(@NotNull MegaChunk mc,
                                                                             @NotNull TerraformWorld tw,
                                                                             BiomeBank biome)
    {
        int[] chunkCoords = mc.getCenterBiomeSectionChunkCoords();
        for (SingleMegaChunkStructurePopulator spop : StructureRegistry.getLargeStructureForMegaChunk(tw, mc)) {
            if (spop == null) {
                continue;
            }
            if (!spop.isEnabled()) {
                continue;
            }
            if (spop instanceof StrongholdPopulator) {
                continue;
            }
            // TerraformGeneratorPlugin.logger.info("[v]       MC(" + mc.getX() + "," + mc.getZ() + ") - Checking " + spop.getClass().getName());
            if (TConfig.areStructuresEnabled() && spop.canSpawn(tw, chunkCoords[0], chunkCoords[1], biome)) {
                return spop;
            }
        }
        return null;
    }

}
