package org.terraform.biome.cavepopulators;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.version.V_26_2;

import java.util.HashSet;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SulfurCavePopulator extends AbstractCaveClusterPopulator {

    private static final Material[] BLOCKS = new Material[]{
            V_26_2.SULFUR,
            Material.GRANITE, //Boundary block
            V_26_2.CINNABAR
    };

    public SulfurCavePopulator(float radius) {
        super(radius);
    }

    @Override
    public void oneUnit(@NotNull TerraformWorld tw,
                        @NotNull Random random,
                        @NotNull SimpleBlock ceil,
                        @NotNull SimpleBlock floor,
                        boolean boundary)
    {

        // =========================
        // Upper decorations
        // =========================

        int caveHeight = ceil.getY() - floor.getY();

        // This seems to be a legacy check that doesn't need to be done anymore.
        // Check in game if this is an issue.
        //if (Tag.SLABS.isTagged(floor.getType()) || Tag.WALLS.isTagged(floor.getType())) {
        //    floor = floor.getDown();
        //}

        //Cheap out and do 2D noise. We will see if that's noticeable in-game
        var noiseGen = NoiseCacheHandler.getNoise(tw,
                NoiseCacheHandler.NoiseCacheEntry.BIOME_SULFURCAVE_BLOCKNOISE,
            (world)->{
                FastNoise n = new FastNoise((int) (world.getSeed() ^ 718904164));
                n.SetNoiseType(FastNoise.NoiseType.Simplex);
                n.SetFrequency(0.06f);
                return n;
        });
        int blockIdx = clampNoise(noiseGen.GetNoise(floor.getX(),floor.getY(),floor.getZ())); //cinnabar


        // All ceiling is sulfur
        ceil.setType(BLOCKS[blockIdx]);

        // Stalactites
        if (blockIdx == 0 //Only when base block is sulfur
            && GenUtils.chance(random, 1, 7)) {
            int h = Math.clamp(caveHeight / 4,1,4);
            V_26_2.downLPointedSulfurSpike(GenUtils.randInt(1, h), ceil.getDown());
        }

        // =========================
        // Lower decorations
        // =========================

        // Floor is sulfur
        floor.setType(BLOCKS[blockIdx]);

        // Stalagmites
        if (blockIdx == 0 //Only when base block is sulfur
            && GenUtils.chance(random, 1, 7)) {
            int h = Math.clamp(caveHeight / 4,1,4);
            V_26_2.upLPointedSulfurSpike(GenUtils.randInt(1, h), floor.getUp());
        }else if(!boundary)
            bfsWaterPool(floor); //sulfur pools



        //=========================
        // Wall Decorations
        //=========================
        SimpleBlock target = floor.getUp();
        while (target.getY() != ceil.getY()) {
            // Line the walls with the decoration block
            for (BlockFace face : BlockUtils.directBlockFaces) {
                SimpleBlock rel = target.getRelative(face);

                if (BlockUtils.isStoneLike(rel.getType())) {
                    rel.setType(BLOCKS[clampNoise(noiseGen.GetNoise(rel.getX(),rel.getY(),rel.getZ()))]);
                }
            }
            target = target.getUp();
        }

        // =========================
        // Biome Setter
        // =========================
        if (TerraformGeneratorPlugin.injector.getICAData(ceil.getPopData()) instanceof PopulatorDataICABiomeWriterAbstract data) {
            while (floor.getY() < ceil.getY()) {
                data.setBiome(floor.getX(), floor.getY(), floor.getZ(), V_26_2.SULFUR_CAVES);
                floor = floor.getUp();
            }
        }

        // =========================
        // Surface vents
        // =========================
        //Resort to spawning 1 spring per cave, as there's a race condition
        // for spawning springs in adjacent chunks.
        if (floor.getX() == center.getX()
                && floor.getZ() == center.getZ()
                //&& GenUtils.chance(tw.getHashedRand(ceil.getX(),ceil.getY(),ceil.getZ()),
                //1, 500)
        ) {
            SimpleBlock base = ceil.getGround();
            if ( !BlockUtils.isWet(base.getUp())) {
                spawnSurfaceSpring(tw.getRand(base.hashCode()), base);
            }
        }
    }

    /**
     *
     * @return 0 for +ve noise, 1 for noise close to 0, 2 for negative noise
     */
    private int clampNoise(float noise){
        int blockIdx = 1;
        if(noise >= 0.07) blockIdx = 0; //sulfur
        else if(noise <= -0.07) blockIdx = 2; //cinnabar
        return blockIdx;
    }

    /**
     * Uses BFS to replace everything at the same Y with water, up to a
     * hardcoded distance from core's position. Stops when seeing a block which is
     * next to air, or if nothing is below it.
     */
    private void bfsWaterPool(SimpleBlock core){
        if(!core.getType().isSolid()) return;

        HashSet<SimpleBlock> toSetWater = new HashSet<>();
        HashSet<SimpleBlock> seen = new HashSet<>();
        Stack<SimpleBlock> queue = new Stack<>();
        queue.push(core);
        seen.add(core);

        while(!queue.empty()){
            SimpleBlock candidate = queue.pop();

            if(candidate.isSolid()
               && candidate.isInBound() //Exceeding bounds causes water to float
               && candidate.getUp().isAir()
               && candidate.countAdjacentsThatMatchType(BlockUtils.directBlockFaces, BlockUtils.stoneLike) == 4
               && candidate.distanceSquared(core) < 36){ //radius 6 max
                //Add neighbours and process it
                for(var face:BlockUtils.directBlockFaces){
                    SimpleBlock neighbour = candidate.getRelative(face);
                    if(seen.add(neighbour))
                        queue.push(neighbour);
                }

                if(candidate.getDown().isSolid()) {
                    toSetWater.add(candidate);
                    if(GenUtils.chance(1,5))
                        candidate.getDown().setBlockData(V_26_2.WET_POTENT_SULFUR);
                    else
                        candidate.getDown().setType(V_26_2.SULFUR);
                }
            }
        }
        //Don't bother if the pool size is very small
        if(toSetWater.size() < 5) return;

        toSetWater.forEach((c)->c.setType(Material.WATER));
    }

    private void spawnSurfaceSpring(Random rand, SimpleBlock core){
        int lambdaSeed = rand.nextInt(89430234);
        int waterDepth = 1+rand.nextInt(2);

        //There is no point in this being an atomic but i guess you defined
        // this API really badly.
        AtomicBoolean canSpawn = new AtomicBoolean(true);
        AtomicInteger lowestBlock = new AtomicInteger(core.getY());
        AtomicInteger highestBlock = new AtomicInteger(core.getY());

        //Initial placement check to ensure that the surface is mostly non-solids
        // AND to get lowest/highest blocks
        BlockUtils.lambdaCircularPatch(lambdaSeed, 7f, core, (cons)->{
            if(cons.getUp().isSolid()) canSpawn.set(false);
            if(lowestBlock.get() > cons.getY())
                lowestBlock.set(cons.getY());
            if(highestBlock.get() < cons.getY())
                highestBlock.set(cons.getY());
        });
        //yes this happens sometimes
        if(highestBlock.get() - lowestBlock.get() > 13) return;
        if(!canSpawn.get()) return;

        int waterY = lowestBlock.get()-1;

        BlockUtils.lambdaCircularPatch(lambdaSeed,
                7f,
                core, (cons)->{
                    //Setup the actual sulfur spring
                    double dsqr = cons.distanceSquared(core);
                    //leave leeway of 3^2 for the sphere opening
                    if(dsqr <= 9) //Clear the space above the pool and dig down
                    {
                        //clear 2 additional blocks up to get rid of stuff like flowers
                        cons.getAtY(waterY+waterDepth).Pillar(2+cons.getY()-waterY, Material.AIR);
                        cons.getAtY(waterY+1).Pillar(waterDepth, Material.WATER);
                        if(GenUtils.chance(1,5))
                            cons.getAtY(waterY).getDown().setBlockData(V_26_2.WET_POTENT_SULFUR);
                        else
                            cons.getAtY(waterY).getDown().setType(V_26_2.SULFUR);

                    }
                    else if(dsqr <= 18)
                    {
                        int height = cons.getY()-waterY+1+waterDepth+rand.nextInt(3);
                        cons.getAtY(waterY).Pillar(height, V_26_2.SULFUR);
                        if(rand.nextBoolean())
                            V_26_2.upLPointedSulfurSpike(1, cons.getAtY(waterY+height));
                    }
                    else if(dsqr <= 20)
                        cons.getDown(cons.getY()-waterY).Pillar(cons.getY()-waterY+1+waterDepth+rand.nextInt(2), V_26_2.SULFUR);
                    else if(dsqr <= 25) cons.downPillar(cons.getY()-waterY,Material.GRANITE, V_26_2.CINNABAR);
                    else cons.downPillar(cons.getY()-waterY,Material.TUFF);
                });

    }

}
