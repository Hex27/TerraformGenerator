package org.terraform.schematic;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TerraRegion {
    private Block one, two;

    public @NotNull ArrayList<Block> getBlocks() {
        ArrayList<Block> blocks = new ArrayList<>();

        int topBlockX = (Math.max(one.getX(), two.getX()));
        int bottomBlockX = (Math.min(one.getX(), two.getX()));

        int topBlockY = (Math.max(one.getY(), two.getY()));
        int bottomBlockY = (Math.min(one.getY(), two.getY()));

        int topBlockZ = (Math.max(one.getZ(), two.getZ()));
        int bottomBlockZ = (Math.min(one.getZ(), two.getZ()));

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = one.getWorld().getBlockAt(x, y, z);

                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    public boolean isComplete() {
        return one != null && two != null;
    }

    /**
     * @return the one
     */
    public Block getOne() {
        return one;
    }

    /**
     * @param one the one to set
     */
    public void setOne(Block one) {
        this.one = one;
    }

    /**
     * @return the two
     */
    public Block getTwo() {
        return two;
    }

    /**
     * @param two the two to set
     */
    public void setTwo(Block two) {
        this.two = two;
    }
}
