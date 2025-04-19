package org.terraform.structure.ancientcity;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.carver.RoomCarver;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.version.V_1_19;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * This class pretends to be a room populator. It is in reality used to
 * carve the Ancient City sculk cave. It will run BFS against a pre-determined
 * equation to decide where to carve the room. The BFS originates on the room's
 * Y and the closest angle towards the center.
 */
public class AncientCityBFSCarver extends RoomCarver {
    //A small pad is needed to allow a bit of leeway for slightly-over areas
    private static final int PADDING = 7;
    private final SimpleLocation generatorCenter;

    public AncientCityBFSCarver(SimpleLocation generatorCenter) {
        this.generatorCenter = generatorCenter;
    }

    @Override
    public void carveRoom(PopulatorDataAbstract data, CubeRoom room, Material... wallMaterial) {
        Random rand = data.getTerraformWorld().getHashedRand(room.getX(), room.getY(), room.getZ());
        PopulatorDataICABiomeWriterAbstract ica = (PopulatorDataICABiomeWriterAbstract) TerraformGeneratorPlugin.injector.getICAData(data);
        assert ica != null;
        FastNoise circleNoise = NoiseCacheHandler.getNoise(data.getTerraformWorld(), NoiseCacheHandler.NoiseCacheEntry.STRUCTURE_ANCIENTCITY_HOLE, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() ^ 14423311));
            n.SetNoiseType(FastNoise.NoiseType.Simplex);
            n.SetFrequency(0.02f);
            return n;
        });
        SimpleBlock chunkCent = room.getCenterSimpleBlock(data);
        SimpleBlock bfsStart = null;
        //It must be ANY block that is within the radius.
        for(int nx = -PADDING; nx < 16+PADDING; nx++)
            for(int nz = -PADDING; nz < 16+PADDING; nz++)
                for(int ny = -40; ny <= 40; ny++){
                    SimpleBlock rel = new SimpleBlock(data,
                            chunkCent.getChunkX()*16 + nx,
                            chunkCent.getY() + ny,
                            chunkCent.getChunkZ()*16 + nz);
                    if(!rollCriteria(circleNoise, rel)){
                        bfsStart = rel;
                        break;
                    }
                }
        if(bfsStart == null) return;
        //Start BFS
        HashSet<SimpleLocation> seen = new HashSet<>();
        Queue<SimpleBlock> bfsQueue = new LinkedList<>();
        bfsQueue.add(bfsStart);
        seen.add(bfsStart.getLoc());

        while(!bfsQueue.isEmpty())
        {
            SimpleBlock target = bfsQueue.remove();
            ica.setBiome(target.getX(), target.getY(), target.getZ(), V_1_19.DEEP_DARK);
            boolean hasCorner = false;
            for(BlockFace face: BlockUtils.sixBlockFaces) {
                SimpleBlock rel = target.getRelative(face);
                if(seen.contains(rel.getLoc()))
                    continue;

                //There is no edge, because it is outside the chunk
                if(rel.getX() < chunkCent.getChunkX()*16-PADDING
                   || rel.getX() > chunkCent.getChunkX()*16+16+PADDING
                   || rel.getZ() < chunkCent.getChunkZ()*16-PADDING
                   || rel.getZ() > chunkCent.getChunkZ()*16+16+PADDING)
                    continue;

                //There is no edge, because it is outside the equation
                // This implies a wall or floor of some kind
                if (rollCriteria(circleNoise, rel)) {
                    hasCorner = true;
                    //Place some decorations if the above block is clear
                    // This places it on the TARGET, not the rel, as intended.
                    //You have to check if target is solid for caves.
                    if (face == BlockFace.DOWN && !target.getUp().isSolid() && target.isSolid()) {
                        if (GenUtils.chance(rand, 1, 230)) {
                            assert V_1_19.SCULK_CATALYST != null;
                            target.getUp().setType(V_1_19.SCULK_CATALYST);
                        }
                        else if (GenUtils.chance(rand, 1, 150)) {
                            assert V_1_19.SCULK_SENSOR != null;
                            target.getUp().setType(V_1_19.SCULK_SENSOR);
                        }
                        else if (GenUtils.chance(rand, 1, 600)) {
                            target.getUp().setBlockData(V_1_19.getActiveSculkShrieker());
                        }
                    }
                    continue;
                }

                //There was an edge
                seen.add(rel.getLoc());
                bfsQueue.add(rel);

            }

            //If a corner is present, this block can be sculk.
            //Additionally, liquids MUST be replaced.
            if(BlockUtils.isWet(target)
               || target.getType() == Material.LAVA
               || (hasCorner && target.isSolid())) {
                assert V_1_19.SCULK != null;
                target.setType(V_1_19.SCULK);
            }
            //If not, carve
            else if(BlockUtils.caveCarveReplace.contains(target.getType()))
                target.setType(Material.CAVE_AIR);
        }
    }

    boolean rollCriteria(FastNoise circleNoise, SimpleBlock rel){
        double equationResult = Math.pow(rel.getX() - generatorCenter.getX(), 2) / Math.pow(AncientCityPopulator.RADIUS, 2)
                                + Math.pow(rel.getZ() - generatorCenter.getZ(), 2) / Math.pow(AncientCityPopulator.RADIUS, 2)
                                + (Math.pow(rel.getY() - generatorCenter.getY() - 20, 2)) / Math.pow(40, 2);
        float noiseVal = circleNoise.GetNoise(rel.getX(), rel.getY(), rel.getZ());
        return equationResult > 1.7 + 0.7 * noiseVal;
    }
}
