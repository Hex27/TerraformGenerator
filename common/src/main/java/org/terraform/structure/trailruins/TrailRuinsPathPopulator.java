package org.terraform.structure.trailruins;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.ArrayList;
import java.util.Random;

public class TrailRuinsPathPopulator extends PathPopulatorAbstract {
    private final Random rand;

    public TrailRuinsPathPopulator(Random rand) {
        this.rand = rand;
    }

    @Override
    public void populate(PathPopulatorData ppd) {
        for(int i = -1; i <= 1; i++) {
            Wall core = new Wall(ppd.base, ppd.dir).getLeft(i);
            if(core.getDown(2).isSolid()) {
                core.setType(Material.STONE, Material.COBBLESTONE, Material.AIR);
                core.getDown().setType(Material.STONE, Material.COBBLESTONE);
            }
        }
    }
}
