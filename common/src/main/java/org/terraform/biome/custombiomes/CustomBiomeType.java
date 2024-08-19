package org.terraform.biome.custombiomes;

import org.jetbrains.annotations.NotNull;
import org.terraform.utils.version.Version;

import java.util.Locale;

public enum CustomBiomeType {
    NONE,
    MUDDY_BOG("b8ad49", "9c8046", "b8ad49", "d9cd62", "ad8445", "ad8445"),
    CHERRY_GROVE("", "69faff", "", "87fffb", "ffa1fc", "acff96"),
    SCARLET_FOREST("", "", "", "", "fc3103", "ff7700"),
    CRYSTALLINE_CLUSTER("e54fff", "c599ff", "e54fff", "", "", ""),
    ;

    private final @NotNull String key;
    private final String fogColor;
    private final String waterColor;
    private final String waterFogColor;
    private final String skyColor;
    private final String grassColor;
    private String foliageColor;
    private float rainFall = 0.8f;
    private boolean isCold = false;

    CustomBiomeType() {
        this.key = "terraformgenerator:" + this.toString().toLowerCase(Locale.ENGLISH);
        this.fogColor = "";
        this.waterColor = "";
        this.waterFogColor = "";
        this.skyColor = "";
        this.foliageColor = "";
        this.grassColor = "";
    }

    CustomBiomeType(String fogColor,
                    String waterColor,
                    String waterFogColor,
                    String skyColor,
                    String foliageColor,
                    String grassColor)
    {
        this.key = "terraformgenerator:" + this.toString().toLowerCase(Locale.ENGLISH);
        this.fogColor = fogColor;
        this.waterColor = waterColor;
        this.waterFogColor = waterFogColor;
        this.skyColor = skyColor;
        this.foliageColor = foliageColor;
        this.grassColor = grassColor;
        this.rainFall = 0.8f;
        this.isCold = false;
        // In 1.20, cherry trees no longer need the pink.
        if (Version.isAtLeast(20) && this.foliageColor.equals("ffa1fc")) {
            this.foliageColor = "acff96";
        }

    }

    public @NotNull String getKey() {
        return key;
    }

    public String getFogColor() {
        return fogColor;
    }

    public String getWaterColor() {
        return waterColor;
    }

    public String getWaterFogColor() {
        return waterFogColor;
    }

    public String getSkyColor() {
        return skyColor;
    }

    public String getFoliageColor() {
        return foliageColor;
    }

    public String getGrassColor() {
        return grassColor;
    }

    public float getRainFall() {
        return rainFall;
    }

    public boolean isCold() {
        return isCold;
    }
}