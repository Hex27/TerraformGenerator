package org.terraform.main;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.ConfigLoader;
import org.terraform.main.config.TConfigOption;
import org.terraform.reflection.Post14PrivateFieldHandler;
import org.terraform.reflection.Pre14PrivateFieldHandler;
import org.terraform.reflection.PrivateFieldHandler;
import org.terraform.schematic.SchematicListener;
import org.terraform.structure.StructureRegistry;
import org.terraform.tree.SaplingOverrider;
import org.terraform.utils.version.Version;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TerraformGeneratorPlugin extends JavaPlugin implements Listener {

	public static TLogger logger;
    public static final Set<String> INJECTED_WORLDS = new HashSet<>();
    public static final PrivateFieldHandler privateFieldHandler;
    public static NMSInjectorAbstract injector;
    private static TerraformGeneratorPlugin instance;
    
    private ConfigLoader config;
    private LanguageManager lang;

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

	@Override
    public void onEnable() {
        super.onEnable();
        instance = this;
		config = new ConfigLoader(this);
		lang = new LanguageManager(this);
        TConfigOption.loadValues(config);
        LangOpt.init(this);
        logger = new TLogger();
        TerraformGenerator.updateSeaLevelFromConfig();
        new TerraformCommandManager(this, "terraform", "terra");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new SchematicListener(), this);
        String version = Version.getVersionPackage();
        logger.stdout("Detected version: " + version);
        try {
			injector = (NMSInjectorAbstract) Class.forName("org.terraform." + version + ".NMSInjector").getDeclaredConstructor().newInstance();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.stdout("&cNo support for this version has been made yet!");
        } catch (InstantiationException | IllegalAccessException 
        		| IllegalArgumentException | InvocationTargetException 
        		| NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            logger.stdout("&cSomething went wrong initiating the injector!");

        }
        
        injector.startupTasks();

        if (TConfigOption.MISC_SAPLING_CUSTOM_TREES_ENABLED.getBoolean()) {
            Bukkit.getPluginManager().registerEvents(new SaplingOverrider(), this);
        }

        StructureRegistry.init();
    }

    
    @Override
    public void onDisable() {
    	//This is already done in NativeGeneratorPatcherPopulator World Unload Event.
    	//NativeGeneratorPatcherPopulator.flushChanges();
    }
    
    /**
     * Legacy thing. Consider removal.
     * @param event
     * @deprecated
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (event.getWorld().getGenerator() instanceof TerraformGenerator) {
            logger.stdout(event.getWorld().getName() + " loaded.");
            if (!TerraformGenerator.preWorldInitGen.isEmpty()) {
                if (!TConfigOption.DEVSTUFF_ATTEMPT_FIXING_PREMATURE.getBoolean()) {
                    logger.stdout("&cIgnoring "
                            + TerraformGenerator.preWorldInitGen.size()
                            + " pre-maturely generated chunks."
                            + " You may see a patch of plain land.");
                    return;
                }
                logger.stdout("&6Trying to decorate "
                        + TerraformGenerator.preWorldInitGen.size()
                        + " pre-maturely generated chunks.");
                int fixed = 0;
                TerraformWorld tw = TerraformWorld.get(event.getWorld());
                for (SimpleChunkLocation sc : TerraformGenerator.preWorldInitGen) {
                    if (!sc.getWorld().equals(event.getWorld().getName())) continue;
                    logger.stdout("Populating " + sc);
                    PopulatorDataPostGen data = new PopulatorDataPostGen(sc.toChunk());
                    new TerraformPopulator(tw).populate(tw, new Random(), data);
                    fixed++;
                }
                logger.stdout("&aSuccessfully finished fixing " + fixed + " pre-mature chunks!");

            }
        }
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        if (event.getWorld().getGenerator() instanceof TerraformGenerator) {
            logger.stdout("Detected world: " + event.getWorld().getName() + ", commencing injection... ");
            if (injector.attemptInject(event.getWorld())) {
                INJECTED_WORLDS.add(event.getWorld().getName());
                TerraformWorld tw = TerraformWorld.get(event.getWorld());
                tw.minY = injector.getMinY();
                tw.maxY = injector.getMaxY();
                
                logger.stdout("&aInjection success! Proceeding with generation.");

            } else {
                logger.stdout("&cInjection failed.");
            }
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new TerraformGenerator();
    }

	public ConfigLoader getConfigLoader() {
		return config;
	}

	public LanguageManager getLang() {
		// TODO Auto-generated method stub
		return lang;
	}

}
