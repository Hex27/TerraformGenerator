package org.terraform.structure.caves;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.util.Vector;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class GenericLargeCavePopulator extends RoomPopulatorAbstract {

    public GenericLargeCavePopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    public static void stalagmite(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, int baseRadius, int height) {

        //Vector one to two;
        Vector base = new Vector(x, y, z);
        Vector base2 = new Vector(x, y + height, z);
        Vector v = base2.subtract(base);
        v.clone().multiply(1 / v.length());
        SimpleBlock one = new SimpleBlock(data, x, y, z);
        double radius = baseRadius;
        for (int i = 0; i <= height; i++) {
            Vector seg = v.clone().multiply((float) i / ((float) height));
            SimpleBlock segment = one.getRelative(seg);

            BlockUtils.replaceSphere((int) (tw.getSeed() * 12), (float) radius, 2, (float) radius, segment, false, false, BlockUtils.stoneOrSlate(y));
            radius = ((double) baseRadius) * (1 - ((double) i) / ((double) height));
        }
    }

    public static void stalactite(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, int baseRadius, int height) {

        //Vector one to two;
        Vector base = new Vector(x, y, z);
        Vector base2 = new Vector(x, y - height, z);
        Vector v = base2.subtract(base);
        v.clone().multiply(1 / v.length());
        SimpleBlock one = new SimpleBlock(data, x, y, z);
        double radius = baseRadius;
        for (int i = 0; i <= height; i++) {
            Vector seg = v.clone().multiply((float) i / ((float) height));
            SimpleBlock segment = one.getRelative(seg);

            BlockUtils.replaceSphere((int) (tw.getSeed() * 12), (float) radius, 2, (float) radius, segment, false, false, Material.STONE);
            radius = ((double) baseRadius) * (1 - ((double) i) / ((double) height));
        }
    }

    protected void populateFloor(SimpleBlock floor, int waterLevel){}
    protected void populateCeil(SimpleBlock ceil){}
    protected void populateCeilFloorPair(SimpleBlock ceil, SimpleBlock floor, int height){
        //Stalactites
        if(GenUtils.chance(rand, 1, 200))
        {
            int r = 2;
            int h = GenUtils.randInt(rand, height/4, (int) ((3f / 2f) * (height/2f)));
            stalactite(ceil.getPopData().getTerraformWorld(), rand, ceil.getPopData(), ceil.getX(), ceil.getY(), ceil.getZ(), r, h);
        }

        //Stalagmites
        if(GenUtils.chance(rand, 1, 200))
        {
            int r = 2;
            int h = GenUtils.randInt(rand, height/4, (int) ((3f / 2f) * (height/2f)));
            stalagmite(ceil.getPopData().getTerraformWorld(), rand, ceil.getPopData(), floor.getX(), floor.getY(), floor.getZ(), r, h);
        }

        //Sea pickles
        if(BlockUtils.isWet(floor.getUp())
                && GenUtils.chance(rand, 4, 100))
        {
            SeaPickle sp = (SeaPickle) Bukkit.createBlockData(Material.SEA_PICKLE);
            sp.setPickles(GenUtils.randInt(1, 2));
            floor.getUp().setBlockData(sp);
        }
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        if(!(room instanceof LargeCaveRoomPiece caveRoom))
            throw new NotImplementedException("room for LargeCavePopulator was not a LargeCaveRoomPiece");

        caveRoom.ceilFloorPairs.forEach((l, pair)->{
            if(pair[0] != null) populateCeil(new SimpleBlock(data, pair[0]));
            if(pair[1] != null) populateFloor(new SimpleBlock(data, pair[1]), caveRoom.waterLevel);
            if(pair[0] != null && pair[1] != null)
            {
                SimpleBlock ceil = new SimpleBlock(data, pair[0]);
                SimpleBlock floor = new SimpleBlock(data, pair[1]);
                int height = ceil.getY()-floor.getY();

                populateCeilFloorPair(ceil,floor,height);
            }
        });
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}
