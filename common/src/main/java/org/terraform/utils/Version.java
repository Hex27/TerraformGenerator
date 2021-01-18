package org.terraform.utils;

import org.bukkit.Bukkit;

public class Version {
    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    private static final double DOUBLE = toVersionDouble(VERSION);

    @Deprecated
    public static boolean isAtLeast(String version) {
        return DOUBLE >= toVersionDouble(version);
    }

    public static boolean isAtLeast(double version) {
        return DOUBLE >= version;
    }

    public static String getVersionPackage() {
        return VERSION;
    }

    @Deprecated
    private static double toVersionDouble(String version) {
        return Double.parseDouble(version.replace("1_", "").replace("_", ".").replace("R", "").replace("v", ""));
    }
}