package org.terraform.structure.village.plains;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.VillageHousePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class PlainsVillagePopulator extends VillageHousePopulator {
    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = getCoordsFromMegaChunk(tw, mc);
        int x = coords[0];//data.getChunkX()*16 + random.nextInt(16);
        int z = coords[1];//data.getChunkZ()*16 + random.nextInt(16);
        int height = GenUtils.getHighestGround(data, x, z);
        spawnPlainsVillage(tw, this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()), data, x, height + 1, z);
    }

    public void spawnPlainsVillage(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        BlockFace pathStart = BlockUtils.getDirectBlockFace(random);
        TerraformGeneratorPlugin.logger.info("Spawning plains village at " + x + "," + y + "," + z);
        DirectionalCubeRoom townHall = new DirectionalCubeRoom(pathStart, 24, 24, 24, x, y, z);
        PlainsVillageTownhallPopulator townHallPop = new PlainsVillageTownhallPopulator(tw, random, false, false);
        townHall.setRoomPopulator(townHallPop);

        PlainsPathRecursiveSpawner spawner = new PlainsPathRecursiveSpawner(
                new SimpleBlock(data, x + pathStart.getModX() * 13, y, z + pathStart.getModZ() * 13),
                100, BlockUtils.getAdjacentFaces(pathStart));
        spawner.forceRegisterRoom(townHall);
        spawner.setVillageDensity(0.7);
        spawner.setPathPop(new PlainsVillagePathPopulator(tw, spawner.getRooms().values(), random));
        spawner.registerRoomPopulator(new PlainsVillageStandardHousePopulator(random, false, false));
        spawner.registerRoomPopulator(new PlainsVillageTemplePopulator(random, false, true));
        spawner.registerRoomPopulator(new PlainsVillageCropFarmPopulator(random, false, false));

        spawner.generate(random);
        spawner.build(random);
    }

}
