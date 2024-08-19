package org.terraform.structure.monument;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Locale;
import java.util.Random;

public enum MonumentDesign {

    DARK_PRISMARINE_CORNERS(Material.DARK_PRISMARINE, Material.PRISMARINE_BRICKS),
    PRISMARINE_LANTERNS(Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICKS, Material.PRISMARINE),
    DARK_LIGHTLESS(Material.PRISMARINE, Material.DARK_PRISMARINE, Material.DARK_PRISMARINE, Material.DARK_PRISMARINE);

    final Material[] tileSet;

    MonumentDesign(Material... tileSet) {
        this.tileSet = tileSet;
    }

    public Material[] tileSet() {
        return tileSet;
    }

    public @Nullable Material slab() {
        return switch (this) {
            case DARK_LIGHTLESS -> Material.DARK_PRISMARINE_SLAB;
            case DARK_PRISMARINE_CORNERS ->
                    GenUtils.randChoice(Material.DARK_PRISMARINE_SLAB, Material.PRISMARINE_BRICK_SLAB);
            case PRISMARINE_LANTERNS -> GenUtils.randChoice(Material.PRISMARINE_SLAB, Material.PRISMARINE_BRICK_SLAB);
        };
    }

    public @NotNull Material stairs() {
        return Material.DARK_PRISMARINE_STAIRS;
    }

    public Material mat(@NotNull Random rand) {
        return GenUtils.randChoice(rand, tileSet);
    }

    public void spawnLargeLight(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        try {
            x++;
            z++;
            y++;
            // World w = ((PopulatorDataPostGen) data).getWorld();
            TerraSchematic schema = TerraSchematic.load(
                    this.toString().toLowerCase(Locale.ENGLISH) + "-largelight",
                    new SimpleBlock(data, x, y, z)
            );
            schema.parser = new MonumentSchematicParser();
            schema.setFace(BlockFace.NORTH);
            schema.apply();
        }
        catch (Throwable e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }

    public void upSpire(@NotNull SimpleBlock base, @NotNull Random rand) {
        while (base.isSolid() || base.getUp().isSolid()) {
            base = base.getUp();
            if (base.getY() > TerraformGenerator.seaLevel) {
                return;
            }
        }
        spire(new Wall(base, BlockFace.NORTH), rand);
    }

    public void spire(@NotNull Wall w, @NotNull Random rand) {
        spire(w, rand, 7);
    }

    public void spire(@NotNull Wall w, @NotNull Random rand, int height) {
        switch (this) {
            case DARK_LIGHTLESS:
                for (int i = 0; i < height; i++) {
                    if (i == 0) {
                        w.setType(Material.DARK_PRISMARINE);
                    }
                    else if (i > height - 3) {
                        w.setType(Material.PRISMARINE_WALL);
                    }
                    else {
                        w.setType(GenUtils.randChoice(Material.DARK_PRISMARINE, Material.PRISMARINE_WALL));
                        if (rand.nextBoolean()) {
                            Stairs stairs = (Stairs) Bukkit.createBlockData(Material.DARK_PRISMARINE_STAIRS);
                            stairs.setFacing(BlockUtils.getDirectBlockFace(rand));
                            stairs.setHalf(rand.nextBoolean() ? Half.TOP : Half.BOTTOM);
                            w.setBlockData(stairs);
                        }

                    }
                    w = w.getUp();
                }
                break;
            case DARK_PRISMARINE_CORNERS:
                for (int i = 0; i < height; i++) {
                    if (i == 0) {
                        w.setType(Material.DARK_PRISMARINE);
                    }
                    else if (i == 3) {
                        w.setType(Material.SEA_LANTERN);
                    }
                    else {
                        w.setType(GenUtils.randChoice(Material.DARK_PRISMARINE, Material.PRISMARINE_WALL));
                        if (rand.nextBoolean()) {
                            Stairs stairs = (Stairs) Bukkit.createBlockData(Material.DARK_PRISMARINE_STAIRS);
                            stairs.setFacing(BlockUtils.getDirectBlockFace(rand));
                            stairs.setHalf(rand.nextBoolean() ? Half.TOP : Half.BOTTOM);
                        }

                    }
                    w = w.getUp();
                }
                break;
            case PRISMARINE_LANTERNS:
                for (int i = 0; i < height; i++) {
                    if (i == 0) {
                        w.setType(Material.PRISMARINE_BRICKS);
                    }
                    else if (i > height - 2) {
                        w.setType(Material.PRISMARINE_WALL);
                    }
                    else if (i == height - 2) {
                        w.setType(Material.PRISMARINE_BRICKS);
                    }
                    else {
                        w.setType(Material.PRISMARINE_WALL);
                        if (i == 3) {
                            w.setType(Material.SEA_LANTERN);
                        }
                    }
                    w = w.getUp();
                }
                break;
        }
    }

}
