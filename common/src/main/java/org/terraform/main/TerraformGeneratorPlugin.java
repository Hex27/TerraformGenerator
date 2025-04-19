package org.terraform.main;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.reflection.Post14PrivateFieldHandler;
import org.terraform.reflection.Pre14PrivateFieldHandler;
import org.terraform.reflection.PrivateFieldHandler;
import org.terraform.schematic.SchematicListener;
import org.terraform.structure.StructureRegistry;
import org.terraform.tree.SaplingOverrider;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.bstats.TerraformGeneratorMetricsHandler;
import org.terraform.utils.datastructs.ConcurrentLRUCache;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.version.Version;
import org.terraform.watchdog.TfgWatchdogSuppressant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class TerraformGeneratorPlugin extends JavaPlugin implements Listener {

    public static final Set<String> INJECTED_WORLDS = new HashSet<>();
    public static final @NotNull PrivateFieldHandler privateFieldHandler;
    public static TLogger logger;

    //Injector "can" be null, but the plugin can be assumed to be completely broken
    // in that case. Just crash.
    public static @NotNull NMSInjectorAbstract injector;
    public static TfgWatchdogSuppressant watchdogSuppressant;
    private static TerraformGeneratorPlugin instance;

    static {
        PrivateFieldHandler handler;
        try {
            Field.class.getDeclaredField("modifiers");
            handler = new Pre14PrivateFieldHandler();
        }
        catch (NoSuchFieldException | SecurityException ex) {
            handler = new Post14PrivateFieldHandler();
        }
        privateFieldHandler = handler;
    }

    private LanguageManager lang;

    public static TerraformGeneratorPlugin get() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        GenUtils.initGenUtils();
        BlockUtils.initBlockUtils();
        instance = this;

        try {
            TConfig.init(new File(getDataFolder(), "config.yml"));
        }
        catch (IOException e) {
            getLogger().severe("Failed to load config.yml: " + e.getMessage());
            getPluginLoader().disablePlugin(this);
            return;
        }

        logger = new TLogger();
        lang = new LanguageManager(this, TConfig.c);

        // Initiate the height map flat radius value
        HeightMap.spawnFlatRadiusSquared = TConfig.c.HEIGHT_MAP_SPAWN_FLAT_RADIUS;
        if (HeightMap.spawnFlatRadiusSquared > 0) {
            HeightMap.spawnFlatRadiusSquared *= HeightMap.spawnFlatRadiusSquared;
        }

        BiomeBank.initSinglesConfig(); // Initiates single biome modes.

        // Initialize chunk cache based on config size
        TerraformGenerator.CHUNK_CACHE = new ConcurrentLRUCache<>(
                "CHUNK_CACHE",
                TConfig.c.DEVSTUFF_CHUNKCACHE_SIZE,
                (key)->{
                    key.initInternalCache();
                    return key;
                });

        // Initialize biome query cache based on config size
        GenUtils.biomeQueryCache = new ConcurrentLRUCache<>(
                "biomeQueryCache",
                TConfig.c.DEVSTUFF_CHUNKBIOMES_SIZE,
                (key) -> {
                    EnumSet<BiomeBank> banks = EnumSet.noneOf(BiomeBank.class);
                    int gridX = key.chunkX * 16;
                    int gridZ = key.chunkZ * 16;
                    for (int x = gridX; x < gridX + 16; x++) {
                        for (int z = gridZ; z < gridZ + 16; z++) {
                            BiomeBank bank = key.tw.getBiomeBank(x, z);
                            if (!banks.contains(bank)) {
                                banks.add(bank);
                            }
                        }
                    }
                    return banks;
                }
        );

        LangOpt.init(this);
        watchdogSuppressant = new TfgWatchdogSuppressant();
        new TerraformGeneratorMetricsHandler(this); // bStats

        TerraformGenerator.updateSeaLevelFromConfig();
        new TerraformCommandManager(this, "terraform", "terra");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new SchematicListener(), this);
        String version = Version.getVersionPackage();
        logger.stdout("Detected version: " + version + ", number: " + Version.DOUBLE);
        try {
            injector = Version.getInjector();
            if (injector != null) {
                injector.startupTasks();
            }else throw new ClassNotFoundException(); //injector no longer throws on no version mapping.
        }
        catch (ClassNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
            logger.stdout("&cNo support for this version has been made yet!");
        }
        catch (InstantiationException |
               IllegalAccessException |
               IllegalArgumentException |
               InvocationTargetException |
               NoSuchMethodException |
               SecurityException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
            logger.stdout("&cSomething went wrong initiating the injector!");
        }

        if (TConfig.c.MISC_SAPLING_CUSTOM_TREES_ENABLED) {
            Bukkit.getPluginManager().registerEvents(new SaplingOverrider(), this);
        }

        StructureRegistry.init();
    }


    @Override
    public void onDisable() {
        // This is already done in NativeGeneratorPatcherPopulator World Unload Event.
        // NativeGeneratorPatcherPopulator.flushChanges();
    }

    /**
     * Legacy thing. Consider removal.
     *
     * @deprecated
     */
    @Deprecated
    @EventHandler
    public void onWorldLoad(@NotNull WorldLoadEvent event) {
        if (event.getWorld().getGenerator() instanceof TerraformGenerator) {
            logger.stdout(event.getWorld().getName() + " loaded.");
            if (!TerraformGenerator.preWorldInitGen.isEmpty()) {
                if (!TConfig.c.DEVSTUFF_ATTEMPT_FIXING_PREMATURE) {
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
                    if (!sc.getWorld().equals(event.getWorld().getName())) {
                        continue;
                    }
                    logger.stdout("Populating " + sc);
                    PopulatorDataPostGen data = new PopulatorDataPostGen(sc.toChunk());
                    new TerraformPopulator().populate(tw, data);
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
            if (injector != null && injector.attemptInject(event.getWorld())) {
                INJECTED_WORLDS.add(event.getWorld().getName());
                tw.minY = injector.getMinY();
                tw.maxY = injector.getMaxY();

                logger.stdout("&aInjection success! Proceeding with generation.");

            }
            else {
                logger.stdout("&cInjection failed.");
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event){
        if(INJECTED_WORLDS.contains(event.getWorld().getName())) {
            logger.stdout("Flushing noise cache for world " + event.getWorld().getName());
            NoiseCacheHandler.flushNoiseCaches(TerraformWorld.get(event.getWorld()));
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        return new TerraformGenerator();
    }

    public LanguageManager getLang() {
        // TODO Auto-generated method stub
        return lang;
    }

}
