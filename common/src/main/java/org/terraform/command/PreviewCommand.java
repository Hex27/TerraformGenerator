package org.terraform.command;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;

public class PreviewCommand extends TerraCommand {

    public PreviewCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Shows a preview of a specified generation technique";
    }

    @Override
    public boolean canConsoleExec() {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {

        return sender.isOp();
    }

    @Override
    public void execute(CommandSender sender, Stack<String> args) {
        // int seed = GenUtils.randInt(1, 1000000);
        int maxX = 16 * 10;
        int maxY = TerraformGeneratorPlugin.injector.getMaxY() - TerraformGeneratorPlugin.injector.getMinY();

        TerraformWorld tw = TerraformWorld.get(
                "test-world-" + new Random().nextInt(99999),
                new Random().nextInt(99999)
        );// TerraformWorld.get("test-world", 11111);

        ImageWorldInfo iwi = new ImageWorldInfo(tw.getName(), tw.getSeed());

        BufferedImage img = new BufferedImage(maxX, maxY + maxX, BufferedImage.TYPE_INT_RGB);
        // Delete existing
        File f = new File("terra-preview.png");
        if (f.exists()) {
            f.delete();
        }
        TerraformGenerator generator = new TerraformGenerator();
        // Generate both side and top-down
        for (int x = (-maxX / 2) >> 4; x < (maxX / 2) >> 4; x++) {
            for (int z = (-maxX / 2) >> 4; z < (maxX / 2) >> 4; z++) {
                ImageChunkData icd = new ImageChunkData(img, x, z, maxX, maxY);
                generator.generateNoise(iwi, tw.getHashedRand(1, x, z), x, z, icd);
                generator.generateSurface(iwi, tw.getHashedRand(1, x, z), x, z, icd);
                generator.generateCaves(iwi, tw.getHashedRand(1, x, z), x, z, icd);
            }
        }
        try {
            f = new java.io.File("terra-preview.png");
            ImageIO.write(img, "png", f);
        }
        catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("Done.");
    }

    @SuppressWarnings("unused")
    private Color getClimateColor(@NotNull BiomeBank bank) {
        if (bank.getType() == BiomeType.OCEANIC || bank.getType() == BiomeType.DEEP_OCEANIC) {
            return Color.blue;
        }
        return switch (bank.getClimate()) {
            case HUMID_VEGETATION -> new Color(118, 163, 3);
            case DRY_VEGETATION -> new Color(172, 187, 2);
            case HOT_BARREN -> Color.red;
            case COLD -> new Color(59, 255, 150);
            case SNOWY -> Color.white;
            case TRANSITION -> new Color(59, 255, 59);
        };
    }

    @SuppressWarnings("unused")
    private Color getBiomeColor(@NotNull BiomeBank bank) {
        switch (bank) {
            case SNOWY_WASTELAND:
                return Color.white;
            case SNOWY_TAIGA:
                return new Color(217, 234, 211);
            case ICE_SPIKES:
                return new Color(207, 226, 243);
            case TAIGA:
                return new Color(56, 118, 29);
            case PLAINS:
                return new Color(59, 255, 59);
            case ERODED_PLAINS:
                return new Color(59, 255, 150);
            case DARK_FOREST:
                return new Color(39, 78, 19);
            case SAVANNA:
                return new Color(172, 187, 2);
            case FOREST:
                return new Color(106, 168, 79);
            case JUNGLE:
                return new Color(118, 163, 3);
            case BAMBOO_FOREST:
                return new Color(0, 255, 186);
            case DESERT:
                return Color.yellow;
            case BADLANDS:
                return Color.red;
            default:
                if (bank.getType() == BiomeType.OCEANIC || bank.getType() == BiomeType.DEEP_OCEANIC) {
                    return Color.blue;
                }
                else {
                    return Color.pink;
                }
        }
    }

    @SuppressWarnings("unused")
    private @NotNull Color getHeightColorFromNoise(int noise) {
        if (noise <= 62) { // Sea level
            return new Color(50, 50, 100 + (noise * 2));// Blue
        }
        else if (noise < 62 + 4) { // Beaches?
            return new Color(240, 238, 108);// Yellow
        }
        else if (noise < 92) {
            return new Color(37, (70 + (noise * 2)), 2);// Green
        }
        else { // Mountainous
            return new Color(255, 255, 255);// White
        }
    }

    private static class ImageChunkData implements ChunkGenerator.ChunkData {
        final BufferedImage img;
        final int chunkX, chunkZ, maxX, maxY;
        private final int[][] maxHeights = new int[16][16];

        private ImageChunkData(BufferedImage img, int chunkX, int chunkZ, int maxX, int maxY) {
            this.img = img;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.maxY = maxY;
            this.maxX = maxX;
        }

        // We only care about this one.
        // x,z in [0,15]
        @Override
        public void setBlock(int x, int y, int z, @NotNull Material material) {
            Color col;
            switch (material) {
                case STONE -> col = Color.LIGHT_GRAY;
                case DEEPSLATE -> col = Color.GRAY;
                case WATER -> col = Color.CYAN;
                case CAVE_AIR -> col = Color.RED.darker();
                default -> col = Color.GREEN.darker();
            }

            // Vertical slice at z=0
            if (z == 0 && chunkZ == 0) {
                img.setRGB(
                        (maxX / 2) + x + chunkX * 16,
                        maxY - (y - TerraformGeneratorPlugin.injector.getMinY()) - 1,
                        col.getRGB()
                );
            }
            // Top-Down slice
            if (y >= maxHeights[x][z]) {
                maxHeights[x][z] = y;
                if ((y - TerraformGenerator.seaLevel) % 3 == 0) {
                    if (y > TerraformGenerator.seaLevel) {
                        col = col.brighter();
                    }
                    else {
                        col = col.darker();
                    }
                }
                img.setRGB((maxX / 2) + x + chunkX * 16, maxY + (maxX / 2) + z + chunkZ * 16, col.getRGB());
            }
        }

        @Override
        public void setBlock(int i, int i1, int i2, @NotNull BlockData blockData) {

        }

        @Override
        public int getMinHeight() {
            return 0; // idc
        }

        @Override
        public int getMaxHeight() {
            return 0; // idc
        }

        @NotNull
        @Override
        public Material getType(int i, int i1, int i2) {
            return null;
        }

        @NotNull
        @Override
        public BlockData getBlockData(int i, int i1, int i2) {
            return null;
        }

        // Ignore these.
        @NotNull
        @Override
        public Biome getBiome(int i, int i1, int i2) {
            return null;
        }

        @Override
        public void setBlock(int i, int i1, int i2, @NotNull MaterialData materialData) {

        }

        @Override
        public void setRegion(int i, int i1, int i2, int i3, int i4, int i5, @NotNull Material material) {

        }

        @Override
        public void setRegion(int i, int i1, int i2, int i3, int i4, int i5, @NotNull MaterialData materialData) {

        }

        @Override
        public void setRegion(int i, int i1, int i2, int i3, int i4, int i5, @NotNull BlockData blockData) {

        }

        @NotNull
        @Override
        public MaterialData getTypeAndData(int i, int i1, int i2) {
            return null;
        }

        @Override
        public byte getData(int i, int i1, int i2) {
            return 0;
        }
    }

    private static class ImageWorldInfo implements WorldInfo {
        private final String name;
        private final long seed;

        private ImageWorldInfo(String name, long seed) {
            this.name = name;
            this.seed = seed;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @NotNull
        @Override
        public UUID getUID() {
            return null;
        }

        @NotNull
        @Override
        public World.Environment getEnvironment() {
            return null;
        }

        @Override
        public long getSeed() {
            return seed;
        }

        @Override
        public int getMinHeight() {
            return 0;
        }

        @Override
        public int getMaxHeight() {
            return 0;
        }
    }
}
