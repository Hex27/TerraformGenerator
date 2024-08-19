package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.NMSInjectorAbstract;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

public class Version {

    public enum SupportedVersion {
        V_1_18_R2("v1_18_R2",18.2),
        V_1_19_R3("v1_19_R3",19.4),
        V_1_20_R1("v1_20_R1",20, 20.1),
        V_1_20_R2("v1_20_R2",20.2),
        V_1_20_R3("v1_20_R3",20.3,20.4),
        V_1_20_R4("v1_20_R4",20.5,20.6),
        V_1_21_R1("v1_21_R1",21.0,21.1),
        ;
        private final double[] versionDouble;
        private final String packageName;

        SupportedVersion(String packageName, double... versionDouble) {
            this.versionDouble = versionDouble;
            this.packageName = packageName;
        }

        public static @Nullable NMSInjectorAbstract getInjector() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            for(SupportedVersion sv:SupportedVersion.values())
            {
                for(double versionDouble:sv.versionDouble){
                    if(versionDouble != Version.DOUBLE) continue;
                    return (NMSInjectorAbstract) Class.forName("org.terraform." + sv.packageName+ ".NMSInjector").getDeclaredConstructor().newInstance();
                }
            }
            return null;
        }
    }

    public static final String VERSION = Bukkit.getServer().getBukkitVersion().split("-")[0];
    public static final double DOUBLE = toVersionDouble(VERSION);

    @Deprecated
    public static boolean isAtLeast(@NotNull String version) {
        return DOUBLE >= toVersionDouble(version);
    }

    // Since I keep forgetting, an example version is 19.1 for 1.19.1
    public static boolean isAtLeast(double version) {
        return DOUBLE >= version;
    }

    public static String getVersionPackage() {
        return VERSION;
    }

    /**
     *
     * @param version a string like "1.20.4"
     * @return e.g. substrings "1." away and returns 20.4 for 1.20.4
     */
    public static double toVersionDouble(@NotNull String version) {
        return Double.parseDouble(version.substring(2));
    }
}