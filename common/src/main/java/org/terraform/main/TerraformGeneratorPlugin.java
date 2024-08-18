package org.terraform.main;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.ChunkCacheLoader;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
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
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.bstats.TerraformGeneratorMetricsHandler;
import org.terraform.utils.version.Version;
import org.terraform.watchdog.TfgWatchdogSuppressant;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TerraformGeneratorPlugin extends JavaPlugin implements Listener {

	public static TLogger logger;
    public static final Set<String> INJECTED_WORLDS = new HashSet<>();
    public static final @NotNull PrivateFieldHandler privateFieldHandler;
    public static @Nullable NMSInjectorAbstract injector;
    private static TerraformGeneratorPlugin instance;
    public static TfgWatchdogSuppressant watchdogSuppressant;
    
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
		GenUtils.initGenUtils();
		BlockUtils.initBlockUtils();
        instance = this;
		config = new ConfigLoader(this);
		lang = new LanguageManager(this);
        TConfigOption.loadValues(config);

        //Initiate the height map flat radius value
        HeightMap.spawnFlatRadiusSquared = TConfigOption.HEIGHT_MAP_SPAWN_FLAT_RADIUS.getInt();
        if(HeightMap.spawnFlatRadiusSquared > 0) HeightMap.spawnFlatRadiusSquared *= HeightMap.spawnFlatRadiusSquared;

        BiomeBank.initSinglesConfig(); //Initiates single biome modes.
        
        //Initialize chunk cache based on config size
        TerraformGenerator.CHUNK_CACHE = 
        		CacheBuilder.newBuilder()
        		.maximumSize(TConfigOption.DEVSTUFF_CHUNKCACHE_SIZE.getInt()).build(new ChunkCacheLoader());
        
        //Initialize biome query cache based on config size
        GenUtils.biomeQueryCache = CacheBuilder.newBuilder()
                .maximumSize(TConfigOption.DEVSTUFF_CHUNKBIOMES_SIZE.getInt())
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull EnumSet<BiomeBank> load(@NotNull ChunkCache key) {
                        EnumSet<BiomeBank> banks = EnumSet.noneOf(BiomeBank.class);
                        int gridX = key.chunkX * 16;
                        int gridZ = key.chunkZ * 16;
                        for(int x = gridX; x < gridX + 16; x++) {
                            for(int z = gridZ; z < gridZ + 16; z++) {
                                BiomeBank bank = key.tw.getBiomeBank(x, z);
                                if(!banks.contains(bank)) banks.add(bank);
                            }
                        }
                        return banks;
                    }
                });
        
        LangOpt.init(this);
        logger = new TLogger();
        watchdogSuppressant = new TfgWatchdogSuppressant();
        new TerraformGeneratorMetricsHandler(this); //bStats
        
        TerraformGenerator.updateSeaLevelFromConfig();
        new TerraformCommandManager(this, "terraform", "terra");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new SchematicListener(), this);
        String version = Version.getVersionPackage();
        logger.stdout("Detected version: " + version + ", number: " + Version.DOUBLE);
        try {
			injector = Version.SupportedVersion.getInjector();
            if(injector == null) throw new ClassNotFoundException();
        } catch (ClassNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
            logger.stdout("&cNo support for this version has been made yet!");
        } catch (InstantiationException | IllegalAccessException 
        		| IllegalArgumentException | InvocationTargetException 
        		| NoSuchMethodException | SecurityException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
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
     * @deprecated
     */
    @Deprecated
    @EventHandler
    public void onWorldLoad(@NotNull WorldLoadEvent event) {
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

    @SuppressWarnings("unused")
    @EventHandler
    public void onWorldInit(@NotNull WorldInitEvent event) {
        if (event.getWorld().getGenerator() instanceof TerraformGenerator) {
            logger.stdout("Detected world: " + event.getWorld().getName() + ", commencing injection... ");
            TerraformWorld tw = TerraformWorld.forceOverrideSeed(event.getWorld());
            if (injector.attemptInject(event.getWorld())) {
                INJECTED_WORLDS.add(event.getWorld().getName());
                tw.minY = injector.getMinY();
                tw.maxY = injector.getMaxY();
                
                logger.stdout("&aInjection success! Proceeding with generation.");

            } else {
                logger.stdout("&cInjection failed.");
            }
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
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
