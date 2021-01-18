package org.terraform.main;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.reflection.Post14PrivateFieldHandler;
import org.terraform.reflection.Pre14PrivateFieldHandler;
import org.terraform.reflection.PrivateFieldHandler;
import org.terraform.schematic.SchematicListener;
import org.terraform.structure.StructureRegistry;
import org.terraform.tree.SaplingOverrider;
import org.terraform.utils.Version;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TerraformGeneratorPlugin extends DrycellPlugin implements Listener {

    public static final Set<String> INJECTED_WORLDS = new HashSet<>();
    public static final PrivateFieldHandler privateFieldHandler;
    public static NMSInjectorAbstract injector;
    private static TerraformGeneratorPlugin instance;

    static {
        PrivateFieldHandler handler;
        try {
            Field.class.getDeclaredField("modifiers");
            handler = new Pre14PrivateFieldHandler();
        } catch (NoSuchFieldException | SecurityException ex) {
            handler = new Post14PrivateFieldHandler();
        }
        privateFieldHandler = handler;
    }

    public static TerraformGeneratorPlugin get() {
        return instance;
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        logger = new TLogger(this);
        TConfigOption.loadValues(this.getDCConfig());
        LangOpt.init(this);
        TerraformGenerator.updateSeaLevelFromConfig();
        TerraformGenerator.updateMinMountainLevelFromConfig();
        new TerraformCommandManager(this, "terraform", "terra");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new SchematicListener(), this);
        String version = Version.getVersionPackage();
        logger.info("Detected version: " + version);
        try {
            injector = (NMSInjectorAbstract) Class.forName("org.terraform." + version + ".NMSInjector").newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("&cNo support for this version has been made yet!");
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            logger.error("&cSomething went wrong initiating the injector!");

        }

        if (TConfigOption.MISC_SAPLING_CUSTOM_TREES_ENABLED.getBoolean()) {
            Bukkit.getPluginManager().registerEvents(new SaplingOverrider(), this);
        }

        StructureRegistry.init();
    }

    /**
     * Legacy thing. Consider removal.
     * @param event
     * @deprecated
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (event.getWorld().getGenerator() instanceof TerraformGenerator) {
            logger.info(event.getWorld().getName() + " loaded.");
            if (!TerraformGenerator.preWorldInitGen.isEmpty()) {
                if (!TConfigOption.DEVSTUFF_ATTEMPT_FIXING_PREMATURE.getBoolean()) {
                    logger.info("&cIgnoring "
                            + TerraformGenerator.preWorldInitGen.size()
                            + " pre-maturely generated chunks."
                            + " You may see a patch of plain land.");
                    return;
                }
                logger.info("&6Trying to decorate "
                        + TerraformGenerator.preWorldInitGen.size()
                        + " pre-maturely generated chunks.");
                int fixed = 0;
                TerraformWorld tw = TerraformWorld.get(event.getWorld());
                for (SimpleChunkLocation sc : TerraformGenerator.preWorldInitGen) {
                    if (!sc.getWorld().equals(event.getWorld().getName())) continue;
                    logger.debug("Populating " + sc);
                    PopulatorDataPostGen data = new PopulatorDataPostGen(sc.toChunk());
                    new TerraformPopulator(tw).populate(tw, new Random(), data);
                    fixed++;
                }
                logger.info("&aSuccessfully finished fixing " + fixed + " pre-mature chunks!");

            }
        }
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        if (event.getWorld().getGenerator() instanceof TerraformGenerator) {
            logger.info("Detected world: " + event.getWorld().getName() + ", commencing injection... ");
            if (injector.attemptInject(event.getWorld())) {
                INJECTED_WORLDS.add(event.getWorld().getName());
                logger.info("&aInjection success! Proceeding with generation.");

            } else {
                logger.error("&cInjection failed.");
            }
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new TerraformGenerator();
    }

}
