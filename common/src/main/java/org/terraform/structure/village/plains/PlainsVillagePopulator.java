package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.village.VillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.WoodUtils;
import org.terraform.utils.WoodUtils.WoodType;

import java.util.Locale;
import java.util.Random;

public class PlainsVillagePopulator extends VillagePopulator {
    public @NotNull Material woodSlab = Material.OAK_SLAB;
    public @NotNull Material woodPlank = Material.OAK_PLANKS;
    public @NotNull Material woodLog = Material.OAK_LOG;
    public @NotNull Material woodStrippedLog = Material.STRIPPED_OAK_LOG;
    public @NotNull Material woodFence = Material.OAK_FENCE;
    public @NotNull Material woodButton = Material.OAK_BUTTON;
    public @NotNull Material woodTrapdoor = Material.OAK_TRAPDOOR;
    public @NotNull Material woodDoor = Material.OAK_DOOR;
    public @NotNull Material woodStairs = Material.OAK_STAIRS;
    public @NotNull Material woodLeaves = Material.OAK_LEAVES;
    public @NotNull Material woodPressurePlate = Material.OAK_PRESSURE_PLATE;
    public @NotNull String wood = "oak_";

    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = mc.getCenterBiomeSectionBlockCoords(); // getCoordsFromMegaChunk(tw, mc);
        int x = coords[0];// data.getChunkX()*16 + random.nextInt(16);
        int z = coords[1];// data.getChunkZ()*16 + random.nextInt(16);
        // Height set to 50 as plains village will settle its own height.
        spawnPlainsVillage(tw, this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()), data, x, 50, z);
    }

    /**
     *
     */
    private void ensureFarmHouseEntrance(@NotNull Random rand,
                                         @NotNull DirectionalCubeRoom room,
                                         @NotNull PopulatorDataAbstract data)
    {
        int frontSpaceGuarantee = 11;
        Wall w = new Wall(
                new SimpleBlock(data, room.getX(), room.getY(), room.getZ()).getGround(),
                room.getDirection()
        ).getUp(4);


        int elevation = GenUtils.randInt(rand, 2, 4);
        int max = 30;
        while (max > 0 && !isFrontSpaceClear(w, frontSpaceGuarantee)) {
            switch (rand.nextInt(2 + 1)) {
                case 0 -> // Move the house forward
                        w = w.getFront().getGround().getRelative(0, elevation, 0);
                case 1 -> // Turn the house
                        w = new Wall(w.get(), BlockUtils.getTurnBlockFace(rand, w.getDirection()));
                case 2 -> elevation += 2; // elevate more
            }
            max--;
        }

        // If the block above is wet, find another area.
        Wall temp = w.getGround().getUp(elevation);
        while (BlockUtils.isWet(temp) || BlockUtils.isWet(temp.getDown())) {
            temp = temp.getUp();
            elevation++;
        }

        if (max == 0) { // Maybe it's a mountain or stuck in the middle of eroded plains
            TerraformGeneratorPlugin.logger.info("Village at " + w.get().toVector() + " may have a weird spawn.");
        }

        room.setX(w.getX());
        room.setY(w.getY());
        room.setZ(w.getZ());
        room.setDirection(w.getDirection());
        ((PlainsVillageTownhallPopulator) room.getPop()).setElevation(elevation);
    }

    private boolean isFrontSpaceClear(@NotNull Wall w, int space) {
        for (int i = 0; i < space; i++) {
            if (w.getFront(i).isSolid()) {
                return false;
            }
        }
        return true;
    }

    public void spawnPlainsVillage(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data,
                                   int x,
                                   int y,
                                   int z)
    {


        BlockFace pathStart = BlockUtils.getDirectBlockFace(random);
        TerraformGeneratorPlugin.logger.info("Spawning plains village at " + x + "," + y + "," + z);
        DirectionalCubeRoom townHall = new DirectionalCubeRoom(pathStart, 24, 24, 24, x, y, z);
        PlainsVillageTownhallPopulator townHallPop = new PlainsVillageTownhallPopulator(tw, random, false, false);
        townHall.setRoomPopulator(townHallPop);

        ensureFarmHouseEntrance(random, townHall, data);
        pathStart = townHall.getDirection();

        BiomeBank biome = tw.getBiomeBank(townHall.getX(), townHall.getZ());
        woodSlab = WoodUtils.getWoodForBiome(biome, WoodType.SLAB);
        woodPlank = WoodUtils.getWoodForBiome(biome, WoodType.PLANKS);
        woodLog = WoodUtils.getWoodForBiome(biome, WoodType.LOG);
        woodStairs = WoodUtils.getWoodForBiome(biome, WoodType.STAIRS);
        woodFence = WoodUtils.getWoodForBiome(biome, WoodType.FENCE);
        woodStrippedLog = WoodUtils.getWoodForBiome(biome, WoodType.STRIPPED_LOG);
        woodButton = WoodUtils.getWoodForBiome(biome, WoodType.BUTTON);
        woodTrapdoor = WoodUtils.getWoodForBiome(biome, WoodType.TRAPDOOR);
        woodPressurePlate = WoodUtils.getWoodForBiome(biome, WoodType.PRESSURE_PLATE);
        woodDoor = WoodUtils.getWoodForBiome(biome, WoodType.DOOR);
        woodLeaves = WoodUtils.getWoodForBiome(biome, WoodType.LEAVES);
        wood = woodLeaves.toString().toLowerCase(Locale.ENGLISH).replace("leaves", "");

        // Re-get x and z because they change after ensureFarmHouseEntrance.
        // 13 because that's the width of the townhall.
        PlainsPathRecursiveSpawner spawner = new PlainsPathRecursiveSpawner(new SimpleBlock(
                data,
                townHall.getX() + pathStart.getModX() * 13,
                y,
                townHall.getZ() + pathStart.getModZ() * 13
        ), 100, BlockUtils.getAdjacentFaces(pathStart));
        spawner.forceRegisterRoom(townHall);
        spawner.setVillageDensity(0.7);
        spawner.setPathPop(new PlainsVillagePathPopulator(tw, spawner.getRooms().values(), random));
        spawner.registerRoomPopulator(new PlainsVillageStandardHousePopulator(this, random, false, false));
        spawner.registerRoomPopulator(new PlainsVillageTemplePopulator(this, random, false, true));
        spawner.registerRoomPopulator(new PlainsVillageForgePopulator(this, random, false, true));
        spawner.registerRoomPopulator(new PlainsVillageCropFarmPopulator(this, random, false, false));
        spawner.registerRoomPopulator(new PlainsVillageAnimalPenPopulator(this, random, false, false));
        spawner.registerRoomPopulator(new PlainsVillageWellPopulator(this, random, false, false));
        spawner.registerRoomPopulator(new PlainsVillageFountainPopulator(this, random, false, true));
        spawner.registerRoomPopulator(new PlainsVillagePondPopulator(random, false, false));

        spawner.generate(random);
        spawner.build(random);
    }

}
