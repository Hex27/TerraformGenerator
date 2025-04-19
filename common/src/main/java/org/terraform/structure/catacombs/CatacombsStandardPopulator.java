package org.terraform.structure.catacombs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataSpigotAPI;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.SphereBuilder.SphereType;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;

public class CatacombsStandardPopulator extends RoomPopulatorAbstract {

    public CatacombsStandardPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int[] lowerCorner = room.getLowerCorner(0);
        int[] upperCorner = room.getUpperCorner(0);
        float maxTotalDiff = room.getWidthX() / 2f + room.getWidthZ() / 2f;
        // Flooring and ceiling decor
        int y = room.getY();

        // Flooring

        new SphereBuilder(
                this.rand,
                room.getCenterSimpleBlock(data),
                CatacombsPathPopulator.pathMaterial
        ).setRX(room.getWidthX() / 2f)
         .setRZ(room.getWidthZ() / 2f)
         .setRY(room.getWidthX() / 3f)
         .setHardReplace(true)
         .setSphereType(SphereType.LOWER_SEMISPHERE)
         .build();

        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                //            	// Flooring
                if (rand.nextInt(5) < 4) {
                    data.setType(x, y, z, GenUtils.randChoice(CatacombsPathPopulator.pathMaterial));
                }
                else if (!data.getType(x, y, z).isSolid()) {
                    data.setType(
                            x,
                            y,
                            z,
                            GenUtils.randChoice(Material.STONE, Material.ANDESITE, Material.CRACKED_STONE_BRICKS)
                    );
                }

                // Ceiling is a fuzzed dome.
                SimpleBlock ceiling = new SimpleBlock(data, x, y + 1, z).findCeiling(room.getHeight() + 1);
                if (ceiling != null) {
                    float maxDownExtend = room.getHeight() - 4;
                    int xDiffFromCent = Math.abs(x - room.getX()) + rand.nextInt(2);
                    int zDiffFromCent = Math.abs(z - room.getZ()) + rand.nextInt(2);
                    int extend = Math.round(((xDiffFromCent + zDiffFromCent) / (maxTotalDiff)) * maxDownExtend);
                    ceiling.getDown()
                           .downLPillar(new Random(),
                                   extend,
                                   Material.STONE,
                                   Material.ANDESITE,
                                   Material.CRACKED_STONE_BRICKS
                           );

                    // Cobwebs
                    if (TConfig.areDecorationsEnabled() && rand.nextInt(10) == 0) {
                        ceiling.getDown(extend + 1)
                               .getRelative(BlockUtils.getDirectBlockFace(rand))
                               .lsetType(Material.COBWEB);
                    }
                }
            }
        }

        // Walling
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {

                // Texture walls
                w.getRear().ReplacePillar(room.getHeight(), Material.STONE, Material.ANDESITE);
                if (rand.nextInt(8) == 4) {
                    Wall target = w.getUp(rand.nextInt(room.getHeight()) + 1).getRear();
                    if (target.isSolid()) {
                        target.ReplacePillar(1, Material.POLISHED_ANDESITE);
                        target.getRight().ReplacePillar(1, Material.POLISHED_ANDESITE);
                    }
                }

                // Candles on room entrances
                if ((w.getUp(2).getLeft().getRear().isAir() || w.getUp(2).getRight().getRear().isAir()) && w.getUp(2)
                                                                                                            .getRear()
                                                                                                            .isSolid())
                {
                    new StairBuilder(
                            Material.STONE_BRICK_STAIRS,
                            Material.MOSSY_STONE_BRICK_STAIRS,
                            Material.COBBLESTONE_STAIRS
                    ).setHalf(Half.TOP).setFacing(w.getDirection().getOppositeFace()).apply(w.getUp(2));

                    BlockUtils.placeCandle(w.getUp(3), GenUtils.randInt(1, 4), lightCandles());
                }

                w = w.getLeft();
            }
        }

        // Skeleton Warriors
        for (int i = 0; i < 1 + rand.nextInt(3); i++) {
            int[] coords = room.randomCoords(rand, 1);
            SimpleBlock target = new SimpleBlock(data, coords[0], room.getY() + 1, coords[2]);
            target = target.getAtY(room.getY() + 1);

            if (data instanceof PopulatorDataSpigotAPI) {
                Skeleton e = (Skeleton) ((PopulatorDataSpigotAPI) data).lr
                        .spawnEntity(new Location(
                                        Objects.requireNonNull(data.getTerraformWorld()).getWorld(),
                                        target.getX()+0.5, target.getY()+0.3, target.getZ()+0.5),
                                EntityType.SKELETON);

                Objects.requireNonNull(e.getEquipment()).setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                e.setPersistent(true);
            }
        }
    }

    // This has to be isolated into another method because
    // the chains interfere with lPillar by being solid.
    // Child classes are responsible for calling this.
    protected void spawnHangingChains(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        if (!TConfig.areDecorationsEnabled()) {
            return;
        }

        for (int i = 3; i <= 10; i++) {
            int[] coords = room.randomCoords(rand, 1);
            SimpleBlock target = new SimpleBlock(data, coords[0], room.getY() + 1, coords[2]);
            target = target.findCeiling(room.getHeight());
            if (target == null || target.getY() - room.getY() < 4) {
                continue;
            }

            target.getDown().downLPillar(new Random(), GenUtils.randInt(3, 5), Material.CHAIN);
        }
    }

    protected boolean lightCandles() {
        return true;
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }
}
