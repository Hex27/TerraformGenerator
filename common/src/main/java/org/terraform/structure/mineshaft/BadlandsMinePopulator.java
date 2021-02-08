package org.terraform.structure.mineshaft;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.flat.BadlandsHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.structure.mineshaft.BadlandsMineEntranceParser;
import org.terraform.structure.mineshaft.BadlandsMineshaftPathPopulator;
import org.terraform.structure.room.PathGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;
import org.terraform.utils.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BadlandsMinePopulator extends MultiMegaChunkStructurePopulator {
    static int sandRadius = TConfigOption.BIOME_BADLANDS_PLATEAU_SAND_RADIUS.getInt();
    static int mineDistance = TConfigOption.BIOME_BADLANDS_MINE_DISTANCE.getInt();

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, ArrayList<BiomeBank> biomes) {
        return biomes.contains(BiomeBank.BADLANDS);
    }

    @Override
    public int[] getNearestFeature(TerraformWorld world, int rawX, int rawZ) {
        return new int[0];
    }

    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int[][] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
        return new int[0][];
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        SimpleBlock entrance = getSpawnPosition(tw, data, data.getChunkX(), data.getChunkZ());
        if(entrance == null) return;
        BlockFace inDir = getSpawnDirection(tw, entrance.getX(), entrance.getZ());
        if(inDir == null) return;
        BlockFace outDir = inDir.getOppositeFace();
        entrance = entrance.getRelative(outDir, sandRadius);
        // Gate schematic is facing in wrong direction in the file, whoops
        SimpleBlock gateStart = entrance.getRelative(inDir, 2 + 5);
        Random random = tw.getHashedRand(entrance.getX(), entrance.getY(), entrance.getZ(), 4);
        System.out.println("CARVIIIIIINNNNGGGGGGG " + entrance.getX() + " " + entrance.getY() + " " + entrance.getZ());

        PathGenerator g = new PathGenerator(entrance.getRelative(inDir.getModX() * 2, -1, inDir.getModZ() * 2),
                new Material[] {Material.CAVE_AIR}, new Random(), new int[]{0,0}, new int[]{0,0});
        g.setPopulator(new BadlandsMineshaftPathPopulator(random));
        g.generateStraightPath(null, inDir, 10);

        spawnEntrance(gateStart, outDir);
        patchEntrance(entrance, inDir);
    }

    SimpleBlock getSpawnPosition(TerraformWorld tw, PopulatorDataAbstract data, int chunkX, int chunkZ) {
        for(Vector2f pos : GenUtils.randomObjectPositions(tw, chunkX, chunkZ, mineDistance, mineDistance * 0.17f)) {
            if(((BadlandsHandler) BiomeBank.BADLANDS.getHandler()).mineCanSpawn(tw, (int) pos.x, (int) pos.y))
                return new SimpleBlock(data, (int) pos.x, HeightMap.getBlockHeight(tw, (int) pos.x, (int) pos.y), (int) pos.y);
        }

        return null;
    }

    BlockFace getSpawnDirection(TerraformWorld tw, int x, int z) {
        List<BlockFace> faces = Arrays.asList(BlockUtils.directBlockFaces);
        double highest = -10;
        BlockFace highDir = null;

        for(BlockFace face : BlockUtils.xzPlaneBlockFaces) {
            double n = BadlandsHandler.getPlateauNoise(tw).GetNoise(x + face.getModX(), z + face.getModZ());

            if(n > highest) {
                highest = n;
                highDir = faces.contains(face) ? face : null;
            }
        }

        return highDir;
    }

    void spawnEntrance(SimpleBlock entrance, BlockFace direction) {
        entrance = entrance.getRelative(direction.getModX(), 0, direction.getModZ());

        // Excavate entrance
//        excavate(entrance.getRelative(BlockUtils.getRight(direction), 2),
//                entrance.getRelative(BlockUtils.getLeft(direction), 2).getRelative(direction, sandRadius + 1).getRelative(0, 4, 0));

        // Place support frame
        try {
            SimpleBlock framePos = entrance.getRelative(BlockUtils.getRight(direction), 1).getRelative(direction);
            TerraSchematic entranceSchematic = TerraSchematic.load("badlands-mineshaft/badlands-mine-entrance", framePos);
            entranceSchematic.parser = new BadlandsMineEntranceParser();
            entranceSchematic.setFace(direction);
            entranceSchematic.apply();
        } catch(Exception ignored) {
            TerraformGeneratorPlugin.logger.error("An error occurred reading Badlands Mine Entrance schematic file.");
        }
    }

    void patchEntrance(SimpleBlock entrance, BlockFace direction) {
        BlockFace nextDir = BlockUtils.getRight(direction);
        SimpleBlock base = entrance.getRelative(direction, 3);
        fillWithBlock(entrance.getRelative(nextDir.getModX() * 2, -1, nextDir.getModZ() * 2),
                entrance.getRelative(-nextDir.getModX() * 2, -4, -nextDir.getModZ() * 2).getRelative(direction, 2), Material.RED_SAND);
    }

    void fillWithBlock(SimpleBlock start, SimpleBlock end, Material material) {
        for(int x = Math.min(start.getX(), end.getX()); x <= Math.min(start.getX(), end.getX()) + Math.abs(start.getX() - end.getX()); x++) {
            for(int z = Math.min(start.getZ(), end.getZ()); z <= Math.min(start.getZ(), end.getZ()) + Math.abs(start.getZ() - end.getZ()); z++) {
                for(int y = Math.min(start.getY(), end.getY()); y <= Math.min(start.getY(), end.getY()) + Math.abs(start.getY() - end.getY()); y++) {
                    new SimpleBlock(start.getPopData(), x, y, z).lsetType(material);
                }
            }
        }
    }
}
