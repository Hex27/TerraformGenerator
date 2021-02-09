package org.terraform.structure.mineshaft;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
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
import org.terraform.structure.room.PathGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;

import java.util.ArrayList;
import java.util.Random;

public class BadlandsMinePopulator extends MultiMegaChunkStructurePopulator {
    static int sandRadius = TConfigOption.BIOME_BADLANDS_PLATEAU_SAND_RADIUS.getInt();
    static int mineDistance = TConfigOption.BIOME_BADLANDS_MINE_DISTANCE.getInt();

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, ArrayList<BiomeBank> biomes) {
        if (!biomes.contains(BiomeBank.BADLANDS)) return false;

        for (Vector2f pos : GenUtils.randomObjectPositions(tw, chunkX >> 4, chunkZ >> 4, mineDistance, mineDistance * 0.3f)) {
            if ((int) pos.x == chunkX && (int) pos.y == chunkZ) {
                SimpleBlock s = getSpawnPosition(tw, chunkX, chunkZ);
                return s != null && getSpawnDirection(tw, s.getX(), s.getZ()) != null;
            }
        }

        return false;
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
        SimpleBlock entrance = getSpawnPosition(tw, data.getChunkX(), data.getChunkZ());
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

    SimpleBlock getSpawnPosition(TerraformWorld tw, int chunkX, int chunkZ) {
        for (int x = chunkX << 4; x < chunkX << 4 + 15; x += 3) {
            for (int z = chunkZ << 4; z < chunkZ << 4 + 15; z += 3) {
                if (BadlandsHandler.mineCanSpawn(tw, x, z))
                    return new SimpleBlock(new Location(tw.getWorld(),x, HeightMap.getBlockHeight(tw, x, z), z));
            }
        }

        return null;
    }

    public static BlockFace getSpawnDirection(TerraformWorld tw, int x, int z) {
        SimpleBlock base = new SimpleBlock(new Location(tw.getWorld(), x, 0, z));

        for (BlockFace face : BlockUtils.directBlockFaces) {
            SimpleBlock right = base.getRelative(BlockUtils.getRight(face), 3);
            SimpleBlock left = base.getRelative(BlockUtils.getLeft(face), 3);

            SimpleBlock b = right.getRelative(face, 20);
            if (!BadlandsHandler.containsPlateau(tw, b.getX(), b.getZ()))
                continue;

            b = right.getRelative(face.getOppositeFace(), sandRadius + 2);
            if (BadlandsHandler.containsPlateau(tw, b.getX(), b.getZ()))
                continue;

            b = left.getRelative(face, 20);
            if (!BadlandsHandler.containsPlateau(tw, b.getX(), b.getZ()))
                continue;

            b = left.getRelative(face.getOppositeFace(), sandRadius + 2);
            if (BadlandsHandler.containsPlateau(tw, b.getX(), b.getZ()))
                continue;
            
            if (!BadlandsHandler.containsPlateau(tw, base.getX() + face.getModX() * 10,
                    base.getZ() + face.getModZ() * 10))
                continue;

            return face;
        }

        return null;
    }

    void spawnEntrance(SimpleBlock entrance, BlockFace direction) {
        entrance = entrance.getRelative(direction.getModX(), -1, direction.getModZ());

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
