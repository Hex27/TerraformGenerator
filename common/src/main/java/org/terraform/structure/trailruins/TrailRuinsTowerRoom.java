package org.terraform.structure.trailruins;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.V_1_20;
import org.terraform.utils.version.Version;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class TrailRuinsTowerRoom extends RoomPopulatorAbstract {
    public TrailRuinsTowerRoom(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        try {
            SimpleBlock core = room.getCenterSimpleBlock(data);
            BlockFace direction = BlockUtils.getDirectBlockFace(rand);

            TerraSchematic towerBase = TerraSchematic.load("trailruins/trailruins-base-1", core);
            towerBase.parser = new TrailRuinsTowerParser(core, rand);
            towerBase.setFace(direction);
            towerBase.apply();

            TerraSchematic towerTop = TerraSchematic.load("trailruins/trailruins-top-1", core.getUp(10));
            towerTop.parser = new TrailRuinsTowerParser(core.getUp(10), rand);
            towerTop.setFace(direction);
            towerTop.apply();

        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return false;
    }

    public static class TrailRuinsTowerParser extends SchematicParser {
        @NotNull
        final HashMap<Material, Material> newMapping = new HashMap<>();
        private final @NotNull SimpleBlock destruction;
        private final @NotNull Random random;

        public TrailRuinsTowerParser(@NotNull SimpleBlock core, @NotNull Random rand) {
            this.random = rand;
            this.destruction = core.getRelative(GenUtils.getSign(rand) * rand.nextInt(3),
                    GenUtils.getSign(rand) * rand.nextInt(5),
                    GenUtils.getSign(rand) * rand.nextInt(3)
            );

            // Shuffle terracotta types
            this.newMapping.put(Material.YELLOW_GLAZED_TERRACOTTA, GenUtils.randChoice(BlockUtils.GLAZED_TERRACOTTA));
            this.newMapping.put(Material.LIGHT_BLUE_TERRACOTTA, GenUtils.randChoice(BlockUtils.TERRACOTTA));
            this.newMapping.put(Material.CYAN_GLAZED_TERRACOTTA, GenUtils.randChoice(BlockUtils.GLAZED_TERRACOTTA));
            this.newMapping.put(Material.CYAN_TERRACOTTA, GenUtils.randChoice(BlockUtils.TERRACOTTA));
        }

        @Override
        public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
            // If within destruction zone, don't place
            if (block.distance(destruction) < 3) {
                return;
            }

            if (newMapping.containsKey(data.getMaterial())) {
                if (data instanceof Directional) {
                    BlockFace f = ((Directional) data).getFacing();
                    data = Bukkit.createBlockData(newMapping.get(data.getMaterial()));
                    ((Directional) data).setFacing(f);
                }
                else {
                    data = Bukkit.createBlockData(newMapping.get(data.getMaterial()));
                }

            }
            super.applyData(block, data);
            for (int i = 1; i < 3; i++) {
                if (GenUtils.chance(random, 1, 4) && BlockUtils.isStoneLike(block.getUp(i).getType())) {
                    if (Version.isAtLeast(20) && GenUtils.chance(random, 1, 5)) {
                        block.getUp(i).setType(V_1_20.SUSPICIOUS_GRAVEL);
                        block.getPopData().lootTableChest(
                                block.getX(),
                                block.getY() + i,
                                block.getZ(),
                                GenUtils.chance(random, 1, 3)
                                ? TerraLootTable.TRAIL_RUINS_ARCHAEOLOGY_RARE
                                : TerraLootTable.TRAIL_RUINS_ARCHAEOLOGY_COMMON
                        );
                    }
                    else {
                        block.getUp(i).setType(Material.SAND, Material.GRAVEL);
                    }
                }
                else {
                    break; // Stack up, but break if its no longer stone
                }
            }
        }
    }
}
