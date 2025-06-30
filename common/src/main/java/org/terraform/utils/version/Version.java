package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.NMSInjectorAbstract;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Version {

    public static final String VERSION = Bukkit.getServer().getBukkitVersion().split("-")[0];
    public static final double DOUBLE = toVersionDouble(VERSION);

    private final static Map<Double, String> availableVersions = new HashMap<>();

    static {
        availableVersions.put(18.2, "v1_18_R2");
        availableVersions.put(19.4, "v1_19_R3");
        availableVersions.put(20.0, "v1_20_R1");
        availableVersions.put(20.1, "v1_20_R1");
        availableVersions.put(20.2, "v1_20_R2");
        availableVersions.put(20.3, "v1_20_R3");
        availableVersions.put(20.4, "v1_20_R3");
        availableVersions.put(20.5, "v1_20_R4");
        availableVersions.put(20.6, "v1_20_R4");
        availableVersions.put(21.0, "v1_21_R1");
        availableVersions.put(21.1, "v1_21_R1");
        availableVersions.put(21.2, "v1_21_R2");
        availableVersions.put(21.3, "v1_21_R2");
        availableVersions.put(21.4, "v1_21_R3");
        availableVersions.put(21.5, "v1_21_R4");
        availableVersions.put(21.6, "v1_21_R5");
        availableVersions.put(21.7, "v1_21_R5");
    }

    // Since I keep forgetting, an example version is 19.1 for 1.19.1
    public static boolean isAtLeast(double version) {
        return DOUBLE >= version;
    }

    public static String getVersionPackage() {
        return VERSION;
    }

    /**
     * @param version a string like "1.20.4"
     * @return e.g. substrings "1." away and returns 20.4 for 1.20.4
     */
    public static double toVersionDouble(@NotNull String version) {
        return Double.parseDouble(version.substring(2));
    }

    public static @Nullable NMSInjectorAbstract getInjector() throws
            ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException
    {
        if (!availableVersions.containsKey(Version.DOUBLE)) {
            return null;
        }

        return (NMSInjectorAbstract) Class.forName("org.terraform." + availableVersions.get(Version.DOUBLE) + ".NMSInjector")
                                                  .getDeclaredConstructor()
                                                  .newInstance();
    }
}
