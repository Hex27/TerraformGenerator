package org.terraform.structure.portal;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.utils.BlockUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

public class PortalPopulator extends MultiMegaChunkStructurePopulator {
    public static void spawnPortal(TerraformWorld world, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        try {
            int i = random.nextInt(3);
            switch (i) {
                case 0:
                    spawnBasalt1(world, random, data, x, y, z); return;
                case 1:
                    spawnBigBastionRemnant1(world, random, data, x, y, z); return;
                case 2:
                    spawnBastionRemnant1(world, random, data, x, y, z); return;
            }

            TerraformGeneratorPlugin.logger.info("Spawning ruined portal at " + x + ", " + y + ", " + z);
        } catch (Throwable e) {
            TerraformGeneratorPlugin.logger.error("Something went wrong trying to place ruined portal at " + x + ", " + y + ", " + z);
            e.printStackTrace();
        }
    }

    public static void spawnBasalt1(TerraformWorld world, Random random, PopulatorDataAbstract data, int x, int y, int z) throws FileNotFoundException {
            TerraSchematic portal = TerraSchematic.load("portal-basalt-1", new Location(world.getWorld(), x, y, z));
            portal.parser = new BasaltPortalSchematicParser(data, random);
            portal.setFace(BlockUtils.getDirectBlockFace(random));
            portal.apply();
    }

    public static void spawnBigBastionRemnant1(TerraformWorld world, Random random, PopulatorDataAbstract data, int x, int y, int z) throws FileNotFoundException {
        TerraSchematic portal = TerraSchematic.load("portal-bastion-remnant-big-1", new Location(world.getWorld(), x, y, z));
        portal.parser = new BastionRemnantPortalBigSchematicParser(data, random);
        portal.setFace(BlockFace.NORTH);
        portal.apply();
    }

    public static void spawnBastionRemnant1(TerraformWorld world, Random random, PopulatorDataAbstract data, int x, int y, int z) throws FileNotFoundException {
        TerraSchematic portal = TerraSchematic.load("portal-bastion-remnant-1", new Location(world.getWorld(), x, y, z));
        portal.parser = new BastionRemnantPortalSchematicParser(data, random);
        portal.setFace(BlockUtils.getDirectBlockFace(random));
        portal.apply();
    }

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, ArrayList<BiomeBank> biomes) {
        return true;
    }

    @Override
    public int[][] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
        return new int[0][];
    }

    @Override
    public int[] getNearestFeature(TerraformWorld world, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                for (int[] loc : getCoordsFromMegaChunk(world, mc)) {
                    double distSqr = Math.pow(loc[0] - rawX, 2) + Math.pow(loc[1] - rawZ, 2);
                    if (distSqr < minDistanceSquared) {
                        minDistanceSquared = distSqr;
                        min = loc;
                    }
                }
            }
        }
        return min;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void populate(TerraformWorld world, PopulatorDataAbstract data) {
        Random random = this.getHashedRandom(world, data.getChunkX(), data.getChunkZ());
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        for (int[] coords : getCoordsFromMegaChunk(world, mc)) {
            int x = coords[0];
            int z = coords[1];
            spawnPortal(world, random, data, x, HeightMap.getBlockHeight(world, x, z), z);
        }
    }

    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(509812412, chunkX, chunkZ);
    }
}
