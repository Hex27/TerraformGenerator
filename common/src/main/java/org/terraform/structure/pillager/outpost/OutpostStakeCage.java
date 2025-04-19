package org.terraform.structure.pillager.outpost;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.WoodUtils;
import org.terraform.utils.WoodUtils.WoodType;
import org.terraform.utils.version.V_1_19;
import org.terraform.utils.version.Version;

import java.util.Map.Entry;
import java.util.Random;

public class OutpostStakeCage extends RoomPopulatorAbstract {

    private final BiomeBank biome;
    private final Material[] stakeGravel;

    public OutpostStakeCage(Random rand, boolean forceSpawn, boolean unique, BiomeBank biome, Material... stakeGravel) {
        super(rand, forceSpawn, unique);
        this.biome = biome;
        this.stakeGravel = stakeGravel;
    }

    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        Material fenceMat = WoodUtils.getWoodForBiome(biome, WoodType.FENCE);
        Material plankMat = WoodUtils.getWoodForBiome(biome, WoodType.PLANKS);

        int[] lowerCorner = room.getLowerCorner(2);
        int[] upperCorner = room.getUpperCorner(2);
        int highestHeight = 0;
        for (int nx = lowerCorner[0]; nx <= upperCorner[0]; nx++) {
            for (int nz = lowerCorner[1]; nz <= upperCorner[1]; nz++) {
                SimpleBlock target = new SimpleBlock(data, nx, 0, nz);
                int y = target.getGroundOrSeaLevel().getY();
                if (y > highestHeight) {
                    highestHeight = y;
                }
                target.lsetType(plankMat);
                new Wall(target).downUntilSolid(rand, fenceMat);
            }
        }

        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 2).entrySet()) {
            Wall w = entry.getKey().getGroundOrSeaLevel().getUp();
            for (int i = 0; i < entry.getValue(); i++) {
                int baseHeight = 4 + highestHeight - w.getY();
                if (i % 2 == 0) {
                    spawnOneStake(rand, baseHeight, w.get());
                }
                else {
                    int fenceHeight = baseHeight + 2 + rand.nextInt(3);
                    w.RPillar(fenceHeight, rand, fenceMat);
                    w.CorrectMultipleFacing(fenceHeight);
                }
                w = w.getLeft().getGroundOrSeaLevel().getUp();
            }

        }

        if (Version.isAtLeast(19)) {
            // Spawn the mob.
            switch (rand.nextInt(3)) {
                case 0:
                    // Iron Golem
                    data.addEntity(room.getX(),
                            new SimpleBlock(data, room.getX(), room.getY(), room.getZ()).getGroundOrDry().getY() + 1,
                            room.getZ(),
                            EntityType.IRON_GOLEM
                    );
                    break;
                case 1:
                    // Allay (1 to 3)
                    for (int i = 0; i < 1 + rand.nextInt(3); i++) {
                        data.addEntity(
                                room.getX(),
                                new SimpleBlock(data, room.getX(), room.getY(), room.getZ()).getGroundOrDry().getY()
                                + 1,
                                room.getZ(),
                                V_1_19.ALLAY
                        );
                    }

                    // If spawning allays, a roof must be added to the cage.
                    for (int nx = lowerCorner[0]; nx <= upperCorner[0]; nx++) {
                        for (int nz = lowerCorner[1]; nz <= upperCorner[1]; nz++) {
                            int baseHeight = 6 + highestHeight;
                            SimpleBlock target = new SimpleBlock(data, nx, baseHeight, nz);

                            target.setType(plankMat);
                        }
                    }
                    break;
                case 2:
                    // Nothing
                    break;
            }
        }
        else {
            if (rand.nextBoolean()) {
                data.addEntity(
                        room.getX(),
                        new SimpleBlock(data, room.getX(), room.getY(), room.getZ()).getGroundOrDry().getY() + 1,
                        room.getZ(),
                        EntityType.IRON_GOLEM
                );
            }
        }

    }

    public void spawnOneStake(@NotNull Random rand, int baseHeight, @NotNull SimpleBlock base) {
        WoodType type = new WoodType[] {WoodType.LOG, WoodType.STRIPPED_LOG}[rand.nextInt(2)];

        int h = baseHeight + GenUtils.randInt(1, 3);

        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (rand.nextBoolean()) {
                base.getRelative(face).setType(stakeGravel);
            }
        }
        new Wall(base).Pillar(h, rand, WoodUtils.getWoodForBiome(biome, type));
        new Wall(base.getRelative(0, h, 0)).Pillar(
                GenUtils.randInt(2, 3),
                rand,
                WoodUtils.getWoodForBiome(biome, WoodType.FENCE)
        );
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}