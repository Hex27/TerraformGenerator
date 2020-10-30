package org.terraform.structure.stronghold;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class StrongholdPathPopulator extends PathPopulatorAbstract {
    private final Random rand;

    public StrongholdPathPopulator(Random rand) {
        this.rand = rand;
    }

    private static boolean setIronBars(PathPopulatorData ppd) {
        ArrayList<SimpleBlock> toBeGate = new ArrayList<>();
        toBeGate.add(ppd.base.getRelative(0, 3, 0));
        if (!ppd.base.getRelative(0, 4, 0).getType().isSolid()) return false;
        for (BlockFace face : BlockUtils.getAdjacentFaces(ppd.dir)) {
            int mX = face.getModX();
            int mZ = face.getModZ();
            int maxDepth = 3;
            SimpleBlock rel = ppd.base.getRelative(mX, 3, mZ);
            while (!rel.getType().isSolid()) {
                maxDepth--;
                if (maxDepth <= 0) {
                    //Strange gate.
                    return false;
                }
                toBeGate.add(rel);
                rel = rel.getRelative(mX, 0, mZ);
                //Bukkit.getLogger().info("1st: "+rel.getX() + "," + rel.getY() + "," + rel.getZ());
            }
            rel = rel.getRelative(-mX, -1, -mZ);
            if (!toBeGate.isEmpty())
                while (!rel.getType().isSolid()) {
                    toBeGate.add(rel);
                    rel = rel.getRelative(0, -1, 0);
                    //Bukkit.getLogger().info("2nd: " + rel.getX() + "," + rel.getY() + "," + rel.getZ());
                }
        }

        for (SimpleBlock b : toBeGate) {
            b.setType(Material.IRON_BARS);
            BlockUtils.correctMultifacingData(b);
        }
        return true;
    }

    @Override
    public void populate(PathPopulatorData ppd) {

        //Loot chests
        if (GenUtils.chance(rand, 1, 100)) {
            int i = GenUtils.randInt(rand, 0, 1);
            Wall w = new Wall(ppd.base.getRelative(0, 1, 0), ppd.dir);
            int depth = 0;
            while (!w.get().getType().isSolid() && depth < 10) {
                if (i == 0) w = w.getLeft();
                if (i == 1) w = w.getRight();
                depth++;
            }

            if (i == 1) w = w.getLeft();
            if (i == 0) w = w.getRight();

            SimpleBlock cBlock = w.get();
            cBlock.setType(Material.CHEST);
            org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest) Bukkit.createBlockData(Material.CHEST);
            if (i == 0) chest.setFacing(BlockUtils.getAdjacentFaces(ppd.dir)[1]);
            if (i == 1) chest.setFacing(BlockUtils.getAdjacentFaces(ppd.dir)[0]);
            cBlock.setBlockData(chest);
            cBlock.getPopData().lootTableChest(cBlock.getX(), cBlock.getY(), cBlock.getZ(), TerraLootTable.STRONGHOLD_CORRIDOR);
        }

        //Iron gates sometimes
        if (GenUtils.chance(rand, 1, 25)) {
            setIronBars(ppd);
        }

        //Find the ceiling for easier management later
        SimpleBlock ceil = ppd.base.getRelative(0, 1, 0);
        int depth = 0;
        while (!ceil.getType().isSolid()) {
            ceil = ceil.getRelative(0, 1, 0);
            depth++;
            if (depth > 10) return;
        }

        //Sometimes parts of the ceiling falls down
        if (GenUtils.chance(rand, 1, 25)) {
            for (int i = 0; i < GenUtils.randInt(rand, 1, 5); i++) {
                dropDownBlock(ceil.getRelative(GenUtils.randInt(rand, -1, 1), 0, GenUtils.randInt(rand, -1, 1)));
            }
        }

        //Cobwebs
        if (GenUtils.chance(rand, 1, 25)) {
            SimpleBlock webBase = ceil.getRelative(0, -1, 0);
            webBase.setType(Material.COBWEB);

            for (int i = 0; i < GenUtils.randInt(rand, 0, 3); i++) {
                BlockFace face = CoralGenerator.getRandomBlockFace();
                if (face == BlockFace.UP) face = BlockFace.SELF;
                webBase.getRelative(face).setType(Material.COBWEB);
            }
        }


    }

    private void dropDownBlock(SimpleBlock block) {
        if (block.getType().isSolid()) {
            Material type = block.getType();
            block.setType(Material.CAVE_AIR);
            int depth = 0;
            while (!block.getType().isSolid()) {
                block = block.getRelative(0, -1, 0);
                depth++;
                if (depth > 50) return;
            }

            if (GenUtils.chance(1, 3)) {
                Material egg;
                try {
                    egg = Material.valueOf("INFESTED_" + type);
                } catch (IllegalArgumentException e) {
                    egg = type;
                }
                block.getRelative(0, 1, 0).setType(egg);
            } else {
                block.getRelative(0, 1, 0).setType(type);
            }
        }
    }
}
