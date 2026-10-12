package org.terraform.structure.small.camp;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Campfire;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.*;
import org.terraform.main.config.TConfig;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.WoodUtils;
import org.terraform.utils.blockdata.BarrelBuilder;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.version.V_1_21_6;
import org.terraform.utils.version.V_26_3;
import org.terraform.utils.version.Version;

import java.util.*;

public class AbandonedCampPopulator  extends MultiMegaChunkStructurePopulator {

    private static final Set<BiomeBank> ALLOWED_BIOMES = Set.of(
            BiomeBank.JUNGLE,BiomeBank.SPARSE_JUNGLE,
            BiomeBank.CHERRY_GROVE, BiomeBank.DAPPLED_FOREST,
            BiomeBank.FLOWER_FOREST, BiomeBank.FOREST,
            BiomeBank.MEADOW, BiomeBank.TAIGA,
            BiomeBank.PALE_FOREST,BiomeBank.SAVANNA,
            BiomeBank.SNOWY_TAIGA
    );
    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        Random random = this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ());
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        for (int[] coords : getCoordsFromMegaChunk(tw, mc)) {
            int x = coords[0];
            int z = coords[1];
            if (x >> 4 != data.getChunkX() || z >> 4 != data.getChunkZ()) {
                continue;
            }
            int height = GenUtils.getHighestGround(data, x, z);
            spawnAbandonedCamp(tw, random, data, x, height, z, tw.getBiomeBank(x, z));
        }
    }

    public void spawnAbandonedCamp(TerraformWorld tw,
                          @NotNull Random random,
                          @NotNull PopulatorDataAbstract data,
                          int x,
                          int y,
                          int z,
                          BiomeBank variant){
        Wall core = new Wall(data,x,y,z, BlockUtils.getDirectBlockFace(random));
        spawnTent(tw,random,core,variant);

        //The tent grows at its rear, so feel free to move forward by only a small margin
        core = core.getFront(GenUtils.randInt(random,4,6)).getGround();
        if(core.getY() < TerraformGenerator.seaLevel
            || BlockUtils.isWet(core)) return; //Give up if wet
        spawnFront(tw,random,core,variant);
    }

    public void spawnTent(TerraformWorld tw,
                                   @NotNull Random random,
                                   Wall core,
                                   BiomeBank variant){
        Material fence = WoodUtils.getWoodForBiome(variant, WoodUtils.WoodType.FENCE);
        Material planks = WoodUtils.getWoodForBiome(variant, WoodUtils.WoodType.PLANKS);
        Material stairs = GenUtils.choice(random, V_26_3.WOOL_STAIRS);
        int campHeight = GenUtils.randInt(random, 4,6);
        int campDepth = GenUtils.randInt(random, 6,8);

        //  xx
        // x  x
        //X    x Camp of height 3 will occupy a breadth of 4
        SimpleBlock rotSource = null;
        boolean spawnedChest = false;
        for(int height = campHeight; height>0; height--){
            for(int depth = 0; depth<=campDepth; depth++){
                int delta = 0; //Track the even width of the tent
                for(BlockFace side:BlockUtils.getAdjacentFaces(core.getDirection())){
                    Wall base = core.getRelative(side,delta+(campHeight-height)).getRear(depth);

                    //Force a flat platform of planks against the tent's base area
                    base.lsetType(planks);
                    var stair = base.getUp(height);
                    if(random.nextInt(7) == 0)
                        stair.setType(Material.COBWEB);
                    else
                        new StairBuilder(stairs)
                            .setFacing(side.getOppositeFace())
                            .apply(stair);

                    if(random.nextInt(20) == 0) rotSource = stair;

                    if(height == 3)
                    {

                        //If height is 3 AND this is the front/back, set the fences
                        if(depth == 0 || depth == campDepth){
                            base.getUp().Pillar(2, fence);
                            BlockUtils.correctMultifacingData(base.getUp());
                            BlockUtils.correctMultifacingData(base.getUp(2));
                        }else if(GenUtils.chance(1,10)
                        || (!spawnedChest && depth == campDepth-1)){ //Guarantee chest spawn
                            spawnedChest = true;
                            //otherwise, that's where the chests go
                            new ChestBuilder(Material.CHEST)
                                    .setFacing(side.getOppositeFace())
                                    .setLootTable(TerraLootTable.ABANDONED_CAMP_COMMON_CHEST)
                                    .apply(base.getUp());
                            base.getUp(2).setType(Material.AIR);
                        }
                    }
                    delta += 1;
                }
            }
        }

        //Make a hole at rotSource
        if(rotSource != null){
            new SphereBuilder(random, rotSource, Material.AIR)
                    .setRadius(3)
                    .addToWhitelist(V_26_3.WOOL_STAIRS)
                    .build();
        }
    }

    public void spawnFront(TerraformWorld tw,
                           @NotNull Random random,
                           Wall core,
                           BiomeBank variant)
    {
        List<SimpleBlock> candidateSecretBlocks = new ArrayList<>();
        HashSet<SimpleBlock> cushionPoses = new HashSet<>();
        //patch of soil
        BlockUtils.replaceCircularPatch(random.nextInt(12309), 3, core, Material.DIRT_PATH);

        //Central campfire
        Campfire campfire = (Campfire) Bukkit.createBlockData(Material.CAMPFIRE);
        campfire.setLit(true);
        campfire.setFacing(BlockUtils.getDirectBlockFace(random));
        core.getUp().Pillar(2,Material.AIR);
        core.getUp().setBlockData(campfire);
        core.setType(Material.DIRT);
        candidateSecretBlocks.add(core);

        //Put cushions around the campfire
        if(Version.VERSION.isAtLeast(Version.v26_3))
            for(BlockFace f:BlockUtils.xzPlaneBlockFaces)
                if(random.nextInt(5) == 0) {
                    var ground = core.getRelative(f).getGround();
                    ground.setType(Material.DIRT);
                    ground.getUp().Pillar(2,Material.AIR);
                    cushionPoses.add(ground.getUp());
                    candidateSecretBlocks.add(ground);
                }

        //in outer radius, place random decor
        CoordPair outerLow = core.getRelative(-3,0,-3).xzCoordPair();
        CoordPair outerHigh = core.getRelative(3,0,3).xzCoordPair();
        //Barrel
        for(int i = 0; i <= GenUtils.randInt(random,1,2); i++){
            var outer = GenUtils.randomOuterCoords(random, outerLow, outerHigh);
            var barrelGround = new SimpleBlock(core.getPopData(),outer.x(),core.getY(),outer.z())
                    .getGround();
            new BarrelBuilder()
                    .setLootTable(TerraLootTable.ABANDONED_CAMP_BARREL)
                    .apply(barrelGround.getUp());
            barrelGround.getUp(2).setType(Material.AIR);
            barrelGround.setType(Material.DIRT);
            candidateSecretBlocks.add(barrelGround);
        }

        //Hay
        if(random.nextInt(4) == 0){
            var outer = GenUtils.randomOuterCoords(random, outerLow, outerHigh);
            var locGround = new SimpleBlock(core.getPopData(),outer.x(),core.getY(),outer.z())
                            .getGround();

            new SphereBuilder(random, locGround.getUp(), Material.HAY_BLOCK)
                    .setRX(2)
                    .setRY(3)
                    .setRZ(2)
                    .setHardReplace(false)
                    .build();

            candidateSecretBlocks.add(locGround);
        }

        var secretChest = GenUtils.choice(random,candidateSecretBlocks.toArray(new SimpleBlock[0]));
        new ChestBuilder(V_1_21_6.OXIDIZED_COPPER_CHEST)
                .setFacing(BlockUtils.getDirectBlockFace(random))
                .setLootTable(TerraLootTable.ABANDONED_CAMP_SECRET_CHEST)
                .apply(secretChest);
        if(cushionPoses.remove(secretChest.getUp())){
            secretChest.getPopData().addEntity(
                secretChest.getX()+0.5f,
                secretChest.getY() + 0.875f,
                secretChest.getZ()+0.5f,
                V_26_3.CUSHION
            );
        }

        for(var pos : cushionPoses){
            pos.addEntity(V_26_3.CUSHION);
        }
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 857613244),
                (int) (TConfig.c.STRUCTURES_ABANDONEDCAMP_SPAWNRATIO * 10000),
                10000
        );
    }
    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        if (!isEnabled()) {
            return false;
        }

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[][] allCoords = getCoordsFromMegaChunk(tw, mc);
        for (int[] coords : allCoords) {
            if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
                EnumSet<BiomeBank> biomes = GenUtils.getBiomesInChunk(tw, chunkX, chunkZ);
                for (BiomeBank b : biomes) {
                    if(!ALLOWED_BIOMES.contains(b)) return false;
                }
                return rollSpawnRatio(tw, chunkX, chunkZ);
            }
        }
        return false;
    }

    @Override
    public int[][] getCoordsFromMegaChunk(@NotNull TerraformWorld tw, @NotNull MegaChunk mc) {
        int num = TConfig.c.STRUCTURES_ABANDONEDCAMP_COUNT_PER_MEGACHUNK;
        int[][] coords = new int[num][2];
        for (int i = 0; i < num; i++) {
            coords[i] = mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 16782493 * (1 + i)));
        }
        return coords;
    }

    @Override
    public int[] getNearestFeature(@NotNull TerraformWorld tw, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                for (int[] loc : getCoordsFromMegaChunk(tw, mc)) {
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
        //There was an attempt to backport this further,
        // but i don't wanna deal with the obfuscated code anymore.
        // The alternative is to deal with the loot tables in each implementation folder.
        return Version.VERSION.isAtLeast(Version.v26_1)
               && TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_ABANDONEDCAMP_ENABLED;
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(61287433, chunkX, chunkZ);
    }

    @Override
    public int getChunkBufferDistance() {
        return 1;
    }
}
