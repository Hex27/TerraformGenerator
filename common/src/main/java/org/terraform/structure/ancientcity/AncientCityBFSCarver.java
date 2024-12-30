package org.terraform.structure.ancientcity;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;

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
public class AncientCityBFSCarver extends RoomPopulatorAbstract {
    private final float RADIUS = 80;
    public AncientCityBFSCarver(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        PopulatorDataICABiomeWriterAbstract ica = null;

        FastNoise circleNoise = NoiseCacheHandler.getNoise(data.getTerraformWorld(), NoiseCacheHandler.NoiseCacheEntry.BIOME_CAVECLUSTER_CIRCLENOISE, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() * 11));
            n.SetNoiseType(FastNoise.NoiseType.Simplex);
            n.SetFrequency(0.09f);

            return n;
        });
        SimpleBlock center = room.getCenterSimpleBlock(data);
        if (center.getPopData() instanceof PopulatorDataICABiomeWriterAbstract) {
            ica = (PopulatorDataICABiomeWriterAbstract) center.getPopData();
        }

        //Start BFS
        HashSet<SimpleLocation> seen = new HashSet<>();
        Queue<SimpleBlock> bfsQueue = new LinkedList<>();
        bfsQueue.add(center);

        while(!bfsQueue.isEmpty())
        {
            SimpleBlock target = bfsQueue.remove();
            for(BlockFace face: BlockUtils.sixBlockFaces) {
                SimpleBlock rel = target.getRelative(face);
                double equationResult = Math.pow(rel.getX() - center.getX(), 2) / Math.pow(RADIUS, 2)
                                        + Math.pow(rel.getZ() - center.getZ(), 2) / Math.pow(RADIUS, 2)
                                        + Math.pow(rel.getY() - center.getY(), 2) / Math.pow(50, 2);
                float noiseVal = circleNoise.GetNoise(rel.getX(), rel.getY(), rel.getZ());

                //There is no edge, because it is outside the chunk
                if(rel.getChunkX() != center.getChunkX() || rel.getChunkZ() != center.getChunkZ()){

                }
                //There is no edge, because the
                else if (equationResult <= 1 + 0.7 * noiseVal) {

                }
            }
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return false;
    }
}
