package org.terraform.small_items;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;

import static org.terraform.utils.GenUtils.randChoice;

public enum DecorationsBuilder {
    LANTERN(Material.LANTERN),
    CRAFTING_TABLE(Material.CRAFTING_TABLE),
    MELON(Material.MELON),
    PUMPKIN(Material.PUMPKIN),
    CAKE(Material.CAKE),
    OAK_PRESSURE_PLATE(Material.OAK_PRESSURE_PLATE),

    CARTOGRAPHY_TABLE(Material.CARTOGRAPHY_TABLE),
    BREWING_STAND(Material.BREWING_STAND),
    NOTE_BLOCK(Material.NOTE_BLOCK),
    FLETCHING_TABLE(Material.FLETCHING_TABLE),
    ENCHANTING_TABLE(Material.ENCHANTING_TABLE),
    ANVIL(Material.ANVIL),
    JUKEBOX(Material.JUKEBOX);

    private final Material material;

    DecorationsBuilder(final Material material) {
        this.material = material;
    }

    /*
        public void build(@NotNull SimpleBlock block, @NotNull Random rand, int minHeight, int maxHeight) {
            int height = GenUtils.randInt(rand, minHeight, maxHeight);
            for(int i = 0; i < height; i++) block.getRelative(0, i, 0).setType(material);
        }
    */
    public static void build(@NotNull SimpleBlock block, @NotNull DecorationsBuilder... options) {
        randChoice(options).build(block.getPopData(), block.getX(), block.getY(), block.getZ());
    }

    /*
        public static void build(@NotNull Random rand, @NotNull PopulatorDataAbstract data, int x, int y, int z, @NotNull DecorationsBuilder... options) {
            randChoice(rand, options).build(data, x, y, z);
        }
    */
    public static void build(@NotNull PopulatorDataAbstract data,
                             int x,
                             int y,
                             int z,
                             @NotNull DecorationsBuilder... options)
    {
        randChoice(options).build(data, x, y, z);
    }

    public void build(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        data.setType(x, y, z, material);
    }

    /*    public void build(@NotNull Random rand, @NotNull PopulatorDataAbstract data, int x, int y, int z, int minHeight, int maxHeight) {
            BlockUtils.spawnPillar(rand, data, x, y, z, material, minHeight, maxHeight);
        }
    */
    public void build(@NotNull SimpleBlock block) {
        build(block.getPopData(), block.getX(), block.getY(), block.getZ());
    }

}
