package org.terraform.utils.version;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.main.TerraformGeneratorPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public enum Version {
    v1_18_2("v1_18_R2",0),
    v1_19_4("v1_19_R3",1),
    v1_20("v1_20_R1",2),
    v1_20_1("v1_20_R1",3),
    v1_20_2("v1_20_R2",4),
    v1_20_3("v1_20_R3",5),
    v1_20_4("v1_20_R3",6),
    v1_20_5("v1_20_R4",7),
    v1_20_6("v1_20_R4",8),
    v1_21("v1_21_R1",9),
    v1_21_1("v1_21_R1",10),
    v1_21_2("v1_21_R2",11),
    v1_21_3("v1_21_R2",12),
    v1_21_4("v1_21_R3",13),
    v1_21_5("v1_21_R4",14),
    v1_21_6("v1_21_R5",15),
    v1_21_7("v1_21_R5",16),
    v1_21_8("v1_21_R5",17),
    v1_21_9("v1_21_R6",18),
    v1_21_10("v1_21_R6",19),
    ;
    final String packName;
    final int priority;
    Version(String packName, int priority){
        this.packName = packName;
        this.priority = priority;
    }

    public String getSchematicHeader(){
        return this.toString().replace("v1_","").replace("_",".");
    }

    public String getPackName(){
        return packName;
    }

    public boolean isAtLeast(Version other){
        return priority >= other.priority;
    }

    public static final Version VERSION = toVersion(Bukkit.getServer().getBukkitVersion().split("-")[0]);

    private final static Map<Double, String> availableVersions = new HashMap<>();

    /**
     * @param version a string like "1.20.4"
     * @return one of the enums. If this fails, failsafe is the
     * latest priority.
     */
    private static Version toVersion(@NotNull String version) {
        try{
            return Version.valueOf("v" + version.replace(".","_"));
        }catch(IllegalArgumentException e){
            TerraformGeneratorPlugin.logger.stdout("Unknown version " + version + ", trying failsafe.");
            Version highest = Version.v1_18_2;
            for(Version v:Version.values())
                if(v.isAtLeast(highest)) highest = v;
            return highest;
        }
    }

    public static @NotNull NMSInjectorAbstract getInjector() throws
            ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException
    {

        String spigotAppend;
        //https://www.spigotmc.org/threads/how-do-i-detect-if-a-server-is-running-paper.499064/
        try {
            // Any other works, just the shortest I could find.
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            spigotAppend = "";
        } catch (ClassNotFoundException ignored) {
            TerraformGeneratorPlugin.logger.info("Spigot detected");
            spigotAppend = "spigot.";
            try{
                Class.forName("org.terraform." + spigotAppend + VERSION.getPackName() + ".NMSInjector")
                     .getDeclaredConstructor()
                     .newInstance();
            }catch(ClassNotFoundException ignoreAgain){
                TerraformGeneratorPlugin.logger.stdout("There was no spigot package for this version. This is fine if you are BELOW 1.21.9.");
                spigotAppend = "";
            }
        }
        return (NMSInjectorAbstract) Class.forName("org.terraform." + spigotAppend + VERSION.getPackName() + ".NMSInjector")
                                                  .getDeclaredConstructor()
                                                  .newInstance();
    }
}
