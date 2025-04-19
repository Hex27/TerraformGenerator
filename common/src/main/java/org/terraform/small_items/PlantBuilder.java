package org.terraform.small_items;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.V_1_21_4;
import org.terraform.utils.version.V_1_21_5;

import java.util.Random;

import static org.terraform.utils.GenUtils.randChoice;

public enum PlantBuilder {
    TALL_GRASS(Material.TALL_GRASS, true),
    DEAD_BUSH(Material.DEAD_BUSH),
    BUSH(V_1_21_5.BUSH),
    SHORT_DRY_GRASS(V_1_21_5.SHORT_DRY_GRASS),
    TALL_DRY_GRASS(V_1_21_5.TALL_DRY_GRASS),
    BROWN_MUSHROOM(Material.BROWN_MUSHROOM),
    RED_MUSHROOM(Material.RED_MUSHROOM),
    GRASS(Material.GRASS),
    SUGAR_CANE(Material.SUGAR_CANE),
    FERN(Material.FERN),
    OAK_LEAVES(Material.OAK_LEAVES),
    LILY_PAD(Material.LILY_PAD),
    FIREFLY_BUSH(V_1_21_5.FIREFLY_BUSH),
    CACTUS(Material.CACTUS, true),
    HANGING_ROOTS(Material.HANGING_ROOTS),
    SPORE_BLOSSOM(Material.SPORE_BLOSSOM),
    AZALEA(Material.AZALEA),
    FLOWERING_AZALEA(Material.FLOWERING_AZALEA),
    MOSS_CARPET(Material.MOSS_CARPET),
    MELON(Material.MELON),
    PUMPKIN(Material.PUMPKIN),
    DARK_OAK_LEAVES(Material.DARK_OAK_LEAVES),
    KELP_PLANT(Material.KELP_PLANT),
    SEAGRASS(Material.SEAGRASS),
    TALL_SEAGRASS(Material.TALL_SEAGRASS, true),


    // Small flowers
    DANDELION(Material.DANDELION),
    POPPY(Material.POPPY),
    WHITE_TULIP(Material.WHITE_TULIP),
    ORANGE_TULIP(Material.ORANGE_TULIP),
    RED_TULIP(Material.RED_TULIP),
    PINK_TULIP(Material.PINK_TULIP),
    BLUE_ORCHID(Material.BLUE_ORCHID),
    ALLIUM(Material.ALLIUM),
    AZURE_BLUET(Material.AZURE_BLUET),
    OXEYE_DAISY(Material.OXEYE_DAISY),
    CORNFLOWER(Material.CORNFLOWER),
    LILY_OF_THE_VALLEY(Material.LILY_OF_THE_VALLEY),
    CLOSED_EYEBLOSSOM(V_1_21_4.CLOSED_EYEBLOSSOM),

    // Tall flowers
    LILAC(Material.LILAC, true),
    ROSE_BUSH(Material.ROSE_BUSH, true),
    PEONY(Material.PEONY, true),
    LARGE_FERN(Material.LARGE_FERN, true),
    SUNFLOWER(Material.SUNFLOWER, true),

    // Potted plants
    POTTED_DANDELION(Material.POTTED_DANDELION),
    POTTED_POPPY(Material.POTTED_POPPY),
    POTTED_WHITE_TULIP(Material.POTTED_WHITE_TULIP),
    POTTED_ORANGE_TULIP(Material.POTTED_ORANGE_TULIP),
    POTTED_RED_TULIP(Material.POTTED_RED_TULIP),
    POTTED_PINK_TULIP(Material.POTTED_PINK_TULIP),
    POTTED_BLUE_ORCHID(Material.POTTED_BLUE_ORCHID),
    POTTED_ALLIUM(Material.POTTED_ALLIUM),
    POTTED_AZURE_BLUET(Material.POTTED_AZURE_BLUET),
    POTTED_OXEYE_DAISY(Material.POTTED_OXEYE_DAISY),
    POTTED_CORNFLOWER(Material.POTTED_CORNFLOWER),
    POTTED_LILY_OF_THE_VALLEY(Material.POTTED_LILY_OF_THE_VALLEY),
    ;

    public final Material material;
    private final boolean isDoublePlant;

    PlantBuilder(final Material material, boolean isDoublePlant) {
        this.material = material;
        this.isDoublePlant = isDoublePlant;
    }

    PlantBuilder(final Material material) {
        this(material, false);
    }

    public static void build(@NotNull SimpleBlock block, @NotNull PlantBuilder... options) {
        randChoice(options).build(block.getPopData(), block.getX(), block.getY(), block.getZ());
    }

    public static void build(@NotNull Random rand,
                             @NotNull PopulatorDataAbstract data,
                             int x,
                             int y,
                             int z,
                             @NotNull PlantBuilder... options)
    {
        randChoice(rand, options).build(data, x, y, z);
    }

    public static void build(@NotNull PopulatorDataAbstract data,
                             int x,
                             int y,
                             int z,
                             @NotNull PlantBuilder... options)
    {
        randChoice(options).build(data, x, y, z);
    }

    public void build(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        if (!TConfig.arePlantsEnabled()) {
            return;
        }

        if (isDoublePlant) {
            BlockUtils.setDoublePlant(data, x, y, z, material);
        }
        else {
            data.lsetType(x, y, z, material);
        }
    }

    public int build(@NotNull Random rand,
                      @NotNull PopulatorDataAbstract data,
                      int x,
                      int y,
                      int z,
                      int minHeight,
                      int maxHeight)
    {
        if (!TConfig.arePlantsEnabled()) {
            return 0;
        }
        if(data.getType(x,y,z) != Material.AIR) return 0;
        return BlockUtils.spawnPillar(rand, data, x, y, z, material, minHeight, maxHeight);
    }

    public void build(@NotNull SimpleBlock block) {
        build(block.getPopData(), block.getX(), block.getY(), block.getZ());
    }

    public int build(@NotNull SimpleBlock block, @NotNull Random rand, int minHeight, int maxHeight) {
        if (!TConfig.arePlantsEnabled()) {
            return 0;
        }

        int height = GenUtils.randInt(rand, minHeight, maxHeight);
        for (int i = 0; i < height; i++) {
            if(!block.getRelative(0, i, 0).lsetType(material))
                break;
        }
        return height;
    }
}
