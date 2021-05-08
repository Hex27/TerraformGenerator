package org.terraform.utils.version;

import org.bukkit.Bukkit;

public class Version {
    public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    public static final double DOUBLE = toVersionDouble(VERSION);

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

    public static double toVersionDouble(String version) {
    	
        return Double.parseDouble(version.replace("1_", "").replace("_", ".").replace("R", "").replace("v", ""));
    }
}