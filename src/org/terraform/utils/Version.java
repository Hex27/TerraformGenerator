package org.terraform.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

public class Version {
    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    private static final double DOUBLE = Double.parseDouble(StringUtils.remove(StringUtils.remove(VERSION, "1_"), "R").replace('_', '.'));

    public static boolean isAtLeast(double version) {
        return DOUBLE >= version;
    }

    public static String getVersionPackage() {
        return VERSION;
    }
}
