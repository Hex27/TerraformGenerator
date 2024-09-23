package org.terraform.main.config;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public enum TConfigOption {
    // -=[HEIGHTMAP]=-
    HEIGHT_MAP_CORE_FREQUENCY("heightmap.core-frequency", 0.003f),
    HEIGHT_MAP_RIVER_FREQUENCY("heightmap.river-frequency", 0.005f),
    HEIGHT_MAP_LAND_HEIGHT_AMPLIFIER("heightmap.land-height-amplifier", 1f),
    HEIGHT_MAP_SEA_LEVEL("heightmap.sea-level", 62),
    HEIGHT_MAP_BEDROCK_HEIGHT("heightmap.bedrock-height", 5),
    HEIGHT_MAP_BEDROCK_DENSITY("heightmap.bedrock-density", 70),
    HEIGHT_MAP_SPAWN_FLAT_RADIUS("heightmap.spawn-flat-radius", -1),

    // -=[CLIMATES]=-
    CLIMATE_HUMIDVEGETATION_MINTEMP("climate.humid-vegetation.minimum-temperature", -0.35d),
    CLIMATE_HUMIDVEGETATION_MAXTEMP("climate.humid-vegetation.maximum-temperature", 4.0d),
    CLIMATE_HUMIDVEGETATION_MINMOIST("climate.humid-vegetation.minimum-moisture", 0d),
    CLIMATE_HUMIDVEGETATION_MAXMOIST("climate.humid-vegetation.maximum-moisture", 4.0d),

    CLIMATE_DRYVEGETATION_MINTEMP("climate.dry-vegetation.minimum-temperature", -0.35d),
    CLIMATE_DRYVEGETATION_MAXTEMP("climate.dry-vegetation.maximum-temperature", 4.0d),
    CLIMATE_DRYVEGETATION_MINMOIST("climate.dry-vegetation.minimum-moisture", -4.0d),
    CLIMATE_DRYVEGETATION_MAXMOIST("climate.dry-vegetation.maximum-moisture", 0d),

    CLIMATE_HOTBARREN_MINTEMP("climate.hot-barren.minimum-temperature", 1d),
    CLIMATE_HOTBARREN_MAXTEMP("climate.hot-barren.maximum-temperature", 4.0d),
    CLIMATE_HOTBARREN_MINMOIST("climate.hot-barren.minimum-moisture", -4.0d),
    CLIMATE_HOTBARREN_MAXMOIST("climate.hot-barren.maximum-moisture", -1.0d),

    CLIMATE_COLD_MINTEMP("climate.cold.minimum-temperature", -4.0d),
    CLIMATE_COLD_MAXTEMP("climate.cold.maximum-temperature", -0.5d),
    CLIMATE_COLD_MINMOIST("climate.cold.minimum-moisture", -4.0d),
    CLIMATE_COLD_MAXMOIST("climate.cold.maximum-moisture", 4.0d),

    CLIMATE_SNOWY_MINTEMP("climate.snowy.minimum-temperature", -4.0d),
    CLIMATE_SNOWY_MAXTEMP("climate.snowy.maximum-temperature", -2.7d),
    CLIMATE_SNOWY_MINMOIST("climate.snowy.minimum-moisture", -4.0d),
    CLIMATE_SNOWY_MAXMOIST("climate.snowy.maximum-moisture", 4.0d),

    // -=[BIOMES]=-
    // Biome globals
    BIOME_TEMPERATURE_FREQUENCY("biome.temperature-frequency", 0.03f),
    BIOME_MOISTURE_FREQUENCY("biome.moisture-frequency", 0.03f),
    BIOME_MOUNTAINOUS_THRESHOLD("biome.mountainous-threshold", 18.5f),
    BIOME_HIGH_MOUNTAINOUS_THRESHOLD("biome.highmountains-threshold", 27.5f),
    BIOME_MOUNTAINOUS_FREQUENCY("biome.mountainous-frequency", 0.30f),
    BIOME_OCEANIC_FREQUENCY("biome.oceanic-frequency", 0.11f),
    BIOME_OCEANIC_THRESHOLD("biome.oceanic-threshold", 22f),
    BIOME_CAVE_CRYSTALLINECLUSTER_SEPARATION("biome.cave.crystalline-cluster.separation", 86),
    BIOME_CAVE_CRYSTALLINECLUSTER_MAXPERTUB("biome.cave.crystalline-cluster.separation-maxpertub", 0.37),
    BIOME_CAVE_CRYSTALLINECLUSTER_MINSIZE("biome.cave.crystalline-cluster.minsize", 7),
    BIOME_CAVE_CRYSTALLINECLUSTER_MAXSIZE("biome.cave.crystalline-cluster.maxsize", 11),
    BIOME_CAVE_DRIPSTONECLUSTER_SEPARATION("biome.cave.dripstone-cluster.separation", 64),
    BIOME_CAVE_DRIPSTONECLUSTER_MAXPERTUB("biome.cave.dripstone-cluster.separation-maxpertub", 0.35),
    BIOME_CAVE_DRIPSTONECLUSTER_MINSIZE("biome.cave.dripstone-cluster.minsize", 5),
    BIOME_CAVE_DRIPSTONECLUSTER_MAXSIZE("biome.cave.dripstone-cluster.maxsize", 11),
    BIOME_CAVE_LUSHCLUSTER_SEPARATION("biome.cave.lush-cluster.separation", 64),
    BIOME_CAVE_LUSHCLUSTER_MAXPERTUB("biome.cave.lush-cluster.separation-maxpertub", 0.35),
    BIOME_CAVE_LUSHCLUSTER_MINSIZE("biome.cave.lush-cluster.minsize", 10),
    BIOME_CAVE_LUSHCLUSTER_MAXSIZE("biome.cave.lush-cluster.maxsize", 15),
    BIOME_DEEP_OCEANIC_THRESHOLD("biome.deep-oceanic-threshold", 27f),
    BIOME_DITHER("biome.dithering", 0.04d),
    BIOME_SECTION_BITSHIFTS("biome.biomesection-bitshifts", 7),
    BIOME_CLAY_DEPOSIT_SIZE("biome.clay-deposit-radius", 3f),
    BIOME_CLAY_DEPOSIT_CHANCE_OUT_OF_THOUSAND("biome.clay-deposit-chance-out-of-thousand", 3),
    BIOME_SINGLE_TERRESTRIAL_TYPE("biome.single.land", "disabled"),
    BIOME_SINGLE_OCEAN_TYPE("biome.single.ocean", "disabled"),
    BIOME_SINGLE_DEEPOCEAN_TYPE("biome.single.deepocean", "disabled"),
    BIOME_SINGLE_MOUNTAIN_TYPE("biome.single.mountain", "disabled"),
    BIOME_SINGLE_HIGHMOUNTAIN_TYPE("biome.single.highmountain", "disabled"),
    // BIOME_SINGLE_RIVER_TYPE("biome.single.river", "disabled"),
    // BIOME_SINGLE_BEACH_TYPE("biome.single.beach", "disabled"),
    BIOME_DEFAULT_FLAT("biome.defaults.flat", "PLAINS"),
    BIOME_DEFAULT_OCEANIC("biome.defaults.oceanic", "OCEAN"),
    BIOME_DEFAULT_DEEPOCEANIC("biome.defaults.deepoceanic", "DEEP_OCEAN"),
    BIOME_DEFAULT_MOUNTAINOUS("biome.defaults.mountainous", "ROCKY_MOUNTAINS"),
    BIOME_DEFAULT_HIGHMOUNTAINOUS("biome.defaults.highmountainous", "JAGGED_PEAKS"),
    BIOME_DEFAULT_RIVER("biome.defaults.river", "RIVER"),
    BIOME_DEFAULT_BEACH("biome.defaults.beach", "SANDY_BEACH"),

    // Biome specific
    BIOME_ROCKY_MOUNTAINS_WEIGHT("biome.rockymountains.weight", 1),
    BIOME_FORESTED_MOUNTAINS_WEIGHT("biome.forestedmountains.weight", 1),
    BIOME_BADLANDS_MOUNTAINS_WEIGHT("biome.badlandsmountains.weight", 1),
    BIOME_SNOWY_MOUNTAINS_WEIGHT("biome.snowymountains.weight", 1),
    BIOME_BIRCH_MOUNTAINS_WEIGHT("biome.birchmountains.weight", 1),
    BIOME_PAINTED_HILLS_WEIGHT("biome.paintedhills.weight", 1),
    BIOME_SHATTERED_SAVANNA_WEIGHT("biome.shatteredsavanna.weight", 4),
    BIOME_DESERT_MOUNTAINS_WEIGHT("biome.desertmountains.weight", 0),
    BIOME_DESERT_MOUNTAINS_YELLOW_CONCRETE("biome.desertmountains.place-yellow-concrete", true),
    BIOME_DESERT_MOUNTAINS_YELLOW_CONCRETE_POWDER("biome.desertmountains.place-yellow-concrete-powder", true),
    BIOME_JAGGED_PEAKS_WEIGHT("biome.jaggedpeaks.weight", 1),
    BIOME_OCEAN_WEIGHT("biome.ocean.weight", 6),
    BIOME_BLACK_OCEAN_WEIGHT("biome.blackocean.weight", 1),
    BIOME_COLD_OCEAN_WEIGHT("biome.coldocean.weight", 6),
    BIOME_FROZEN_OCEAN_WEIGHT("biome.frozenocean.weight", 6),
    BIOME_WARM_OCEAN_WEIGHT("biome.warmocean.weight", 6),
    BIOME_HUMID_OCEAN_WEIGHT("biome.humidocean.weight", 6),
    BIOME_DRY_OCEAN_WEIGHT("biome.dryocean.weight", 6),
    BIOME_CORALREEF_OCEAN_WEIGHT("biome.coralreefocean.weight", 5),
    BIOME_DEEP_OCEAN_WEIGHT("biome.deepocean.weight", 7),
    BIOME_DEEP_COLD_OCEAN_WEIGHT("biome.deepcoldocean.weight", 7),
    BIOME_DEEP_BLACK_OCEAN_WEIGHT("biome.deepblackocean.weight", 0),
    BIOME_DEEP_FROZEN_OCEAN_WEIGHT("biome.deepfrozenocean.weight", 7),
    BIOME_DEEP_WARM_OCEAN_WEIGHT("biome.deepwarmocean.weight", 7),
    BIOME_DEEP_HUMID_OCEAN_WEIGHT("biome.deephumidocean.weight", 7),
    BIOME_DEEP_DRY_OCEAN_WEIGHT("biome.deepdryocean.weight", 7),
    BIOME_DEEP_LUKEWARM_OCEAN_WEIGHT("biome.deeplukewarmocean.weight", 7),
    BIOME_MUSHROOM_ISLAND_WEIGHT("biome.mushroomisland.weight", 1),
    BIOME_PLAINS_WEIGHT("biome.plains.weight", 10),
    BIOME_ELEVATED_PLAINS_WEIGHT("biome.elevatedplains.weight", 8),
    BIOME_PETRIFIEDCLIFFS_WEIGHT("biome.petrifiedcliffs.weight", 6),
    BIOME_ARCHED_CLIFFS_WEIGHT("biome.archedcliffs.weight", 6),
    BIOME_GORGE_WEIGHT("biome.gorge.weight", 6),
    BIOME_ERODED_PLAINS_WEIGHT("biome.erodedplains.weight", 6),
    BIOME_SAVANNA_WEIGHT("biome.savanna.weight", 6),
    BIOME_MUDDYBOG_WEIGHT("biome.muddybog.weight", 2),
    BIOME_FOREST_WEIGHT("biome.forest.weight", 8),
    BIOME_DESERT_WEIGHT("biome.desert.weight", 6),
    BIOME_JUNGLE_WEIGHT("biome.jungle.weight", 5),
    BIOME_JUNGLE_STATUE_CHANCE("biome.jungle.statue-chance-out-of-1000", 4),
    BIOME_BAMBOO_FOREST_WEIGHT("biome.bambooforest.weight", 2),
    BIOME_BADLANDS_WEIGHT("biome.badlands.weight", 1),
    BIOME_BADLANDS_PLATEAU_HEIGHT("biome.badlands.plateaus.height", 15),
    BIOME_BADLANDS_PLATEAU_SAND_RADIUS("biome.badlands.plateaus.sand-radius", 7),
    BIOME_BADLANDS_PLATEAU_THRESHOLD("biome.badlands.plateaus.threshold", 0.23),
    BIOME_BADLANDS_PLATEAU_FREQUENCY("biome.badlands.plateaus.frequency", 0.01),
    BIOME_BADLANDS_PLATEAU_COMMONNESS("biome.badlands.plateaus.commonness", 0.18),
    BIOME_TAIGA_WEIGHT("biome.taiga.weight", 6),
    BIOME_CHERRYGROVE_WEIGHT("biome.cherrygrove.weight", 3),
    BIOME_SCARLETFOREST_WEIGHT("biome.scarletforest.weight", 3),
    BIOME_SNOWY_TAIGA_WEIGHT("biome.snowytaiga.weight", 6),
    BIOME_SNOWY_WASTELAND_WEIGHT("biome.snowywasteland.weight", 4),
    BIOME_ICE_SPIKES_WEIGHT("biome.icespikes.weight", 2),
    BIOME_DARK_FOREST_WEIGHT("biome.darkforest.weight", 5),
    BIOME_DARK_FOREST_SPAWN_HEADS("biome.darkforest.spawn-heads", true),
    BIOME_SWAMP_WEIGHT("biome.swamp.weight", 5),
    BIOME_MANGROVE_WEIGHT("biome.mangrove.weight", 5),
    BIOME_OASIS_COMMONNESS("biome.oasis.commonness", 1.0),
    BIOME_OASIS_FREQUENCY("biome.oasis.frequency", 0.012f),


    // -=[TREES]=-
    TREES_JUNGLE_BIG_ENABLED("trees.big-jungle-trees.enabled", true),
    TREES_TAIGA_BIG_ENABLED("trees.big-taiga-trees.enabled", true),
    TREES_FOREST_BIG_ENABLED("trees.big-forest-trees.enabled", true),
    TREES_SAVANNA_BIG_ENABLED("trees.big-savanna-trees.enabled", true),
    TREES_BIRCH_BIG_ENABLED("trees.big-birch-trees.enabled", true),
    TREES_SCARLET_BIG_ENABLED("trees.big-scarlet-trees.enabled", true),
    TREES_SNOWY_TAIGA_BIG_ENABLED("trees.big-snowy-taiga-trees.enabled", true),
    TREES_DARK_FOREST_BIG_ENABLED("trees.big-dark-forest-trees.enabled", true),

    // -=[MISC]=-
    // MISC_SMOOTH_DESIGN("misc.smooth-design",false),
    MISC_SAPLING_CUSTOM_TREES_ENABLED("misc.custom-small-trees-from-saplings.enabled", true),
    MISC_SAPLING_CUSTOM_TREES_BIGTREES_JUNGLE("misc.custom-small-trees-from-saplings.big-jungle-tree", true),
    MISC_SAPLING_CUSTOM_TREES_BIGTREES_SPRUCE("misc.custom-small-trees-from-saplings.big-spruce-tree", true),
    MISC_TREES_FORCE_LOGS("misc.trees.only-use-logs-no-wood", false),
    MISC_TREES_GRADIENT_LIMIT("misc.trees.ground-gradient-limit", 1.3d),
    MISC_USE_SLABS_TO_SMOOTH("misc.use-slabs-to-smooth-terrain", true),

    // -=[DEVSTUFF]=-
    DEVSTUFF_SUPPRESS_WATCHDOG("dev-stuff.suppress-watchdog", true),
    DEVSTUFF_CHUNKCACHE_SIZE("dev-stuff.chunk-cache-size", 6000),
    DEVSTUFF_CHUNKBIOMES_SIZE("dev-stuff.biomecache-size", 3000),
    // DEVSTUFF_EXPERIMENTAL_STRUCTURE_PLACEMENT("dev-stuff.experimental-structure-placement", false),
    DEVSTUFF_DEBUG_MODE("dev-stuff.debug-mode", false),
    DEVSTUFF_VANILLA_MUSHROOMS("dev-stuff.force-only-vanilla-mushrooms", false),
    DEVSTUFF_FLUSH_PATCHER_CACHE_FREQUENCY("dev-stuff.patcher-cache-max-size", 100),
    DEVSTUFF_EXTENDED_COMMANDS("dev-stuff.extended-commands", false),
    DEVSTUFF_SUPPRESS_CONSOLE_LOGS("dev-stuff.suppress-terraform-console-logs", true),
    DEVSTUFF_ATTEMPT_FIXING_PREMATURE("dev-stuff.attempt-fixing-premature-generations", true),
    DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS("dev-stuff.vanilla-structure-locate-timeoutmillis", 5000),
    DEVSTUFF_VANILLA_LOCATE_DISABLE("dev-stuff.vanilla-disable-locate", false),

    // -=[CAVES]=-
    CAVES_ALLOW_FLOODED_CAVES("caves.allow-flooded-caves", false),
    // CAVES_ALLOW_FLOODED_RAVINES("caves.allow-flooded-ravines",true),

    // -=[STRUCTURES]=-
    STRUCTURES_MEGACHUNK_NUMBIOMESECTIONS("structures.technical.megachunk.numbiomesections", 4),
    STRUCTURES_MEGACHUNK_MAXSTRUCTURES("structures.technical.megachunk.max-structures-per-megachunk", 3),
    STRUCTURES_MANSION_ENABLED("structures.mansion.enabled", true),
    STRUCTURES_MANSION_SIZE("structures.mansion.size", 80),
    STRUCTURES_MANSION_MINDISTANCE("structures.mansion.min-distance-blocks", 5000),
    STRUCTURES_MANSION_SPAWNAGGRESSION("structures.mansion.pillager-spawn-aggression", 1),
    STRUCTURES_MANSION_SPAWNRATIO("structures.mansion.spawnratio", 0.3),
    STRUCTURES_MANSION_CHUNK_EXCLUSION_ZONE("structures.mansion.chunk-exclusion-zone", 4),
    STRUCTURES_STRONGHOLD_ENABLED("structures.stronghold.enabled", true),
    STRUCTURES_STRONGHOLD_FAILSAFE_Y("structures.stronghold.failsafe-y", -16),
    STRUCTURES_STRONGHOLD_MIN_Y("structures.stronghold.min-y", 0),
    STRUCTURES_STRONGHOLD_MAX_Y("structures.stronghold.max-y", 25),
    STRUCTURES_MONUMENT_ENABLED("structures.monument.enabled", true),
    STRUCTURES_MONUMENT_SPAWNRATIO("structures.monument.spawn-ratio", 0.3),
    STRUCTURES_PYRAMID_ENABLED("structures.pyramid.enabled", true),
    STRUCTURES_PYRAMID_SPAWNRATIO("structures.pyramid.spawn-ratio", 0.3),
    STRUCTURES_PYRAMID_SPAWN_ELDER_GUARDIAN("structures.pyramid.spawn-elder-guardian", true),
    STRUCTURES_PYRAMID_SUSPICIOUS_SAND_COUNT_PER_ANTECHAMBER("structures.pyramid.suspicious-sand-per-antechamber", 4),
    STRUCTURES_VILLAGEHOUSE_SPAWNRATIO("structures.villagehouse.spawnratio", 0.8),
    STRUCTURES_MOUNTAINHOUSE_ENABLED("structures.mountainhouse.enabled", true),
    STRUCTURES_FARMHOUSE_ENABLED("structures.farmhouse.enabled", true),
    STRUCTURES_ANIMALFARM_ENABLED("structures.animalfarm.enabled", true),
    STRUCTURES_TRAILRUINS_SPAWNRATIO("structures.trailruins.spawnratio", 0.5),
    STRUCTURES_TRAILRUINS_ENABLED("structures.trailruins.enabled", true),
    STRUCTURES_TRIALCHAMBER_SPAWNRATIO("structures.trialchamber.spawnratio", 0.3),
    STRUCTURES_TRIALCHAMBER_ENABLED("structures.trialchamber.enabled", true),
    STRUCTURES_WARMOCEANRUINS_SPAWNRATIO("structures.warmoceanruins.spawnratio", 1.0),
    STRUCTURES_WARMOCEANRUINS_ENABLED("structures.warmoceanruins.enabled", true),
    STRUCTURES_VILLAGE_SPAWNRATIO("structures.village.spawnratio", 1.0),
    STRUCTURES_PLAINSVILLAGE_ENABLED("structures.plainsvillage.enabled", true),
    STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE("structures.plainsvillage.height-tolerance", 10),
    STRUCTURES_VILLAGE_CHUNK_EXCLUSION_ZONE("structures.village.chunk-exclusion-zone", 4),
    STRUCTURES_SWAMPHUT_ENABLED("structures.swamphut.enabled", true),
    STRUCTURES_SWAMPHUT_SPAWNRATIO("structures.swamphut.spawnratio", 0.4),
    STRUCTURES_SWAMPHUT_COUNT_PER_MEGACHUNK("structures.swamphut.count-per-megachunk", 1),
    STRUCTURES_DESERTWELL_ENABLED("structures.desertwell.enabled", true),
    STRUCTURES_DESERTWELL_SPAWNRATIO("structures.desertwell.spawnratio", 0.3),
    STRUCTURES_DESERTWELL_COUNT_PER_MEGACHUNK("structures.desertwell.count-per-megachunk", 2),
    STRUCTURES_DUNGEONS_SPAWNRATIO("structures.small-dungeon.spawnratio", 0.4),
    STRUCTURES_DUNGEONS_COUNT_PER_MEGACHUNK("structures.small-dungeon.count-per-megachunk", 3),
    STRUCTURES_UNDERGROUNDDUNGEON_ENABLED("structures.underground-dungeon.enabled", true),
    STRUCTURES_DROWNEDDUNGEON_ENABLED("structures.drowned-dungeon.enabled", true),
    STRUCTURES_DROWNEDDUNGEON_MIN_DEPTH("structures.drowned-dungeon.min-chunk-y", 52),
    STRUCTURES_DROWNEDDUNGEON_CHANCE("structures.drowned-dungeon.chance-out-of-1000", 200),
    STRUCTURES_SHIPWRECK_SPAWNRATIO("structures.shipwreck.spawnratio", 0.6),
    STRUCTURES_SHIPWRECK_ENABLED("structures.shipwreck.enabled", true),
    STRUCTURES_SHIPWRECK_COUNT_PER_MEGACHUNK("structures.shipwreck.count-per-megachunk", 2),
    STRUCTURES_RUINEDPORTAL_SPAWNRATIO("structures.ruinedportal.spawnratio", 0.4),
    STRUCTURES_RUINEDPORTAL_ENABLED("structures.ruinedportal.enabled", true),
    STRUCTURES_RUINEDPORTAL_COUNT_PER_MEGACHUNK("structures.ruinedportal.count-per-megachunk", 1),
    STRUCTURES_IGLOO_SPAWNRATIO("structures.igloo.spawnratio", 0.8),
    STRUCTURES_IGLOO_ENABLED("structures.igloo.enabled", true),
    STRUCTURES_IGLOO_COUNT_PER_MEGACHUNK("structures.igloo.count-per-megachunk", 1),
    STRUCTURES_BURIEDTREASURE_SPAWNRATIO("structures.buriedtreasure.spawnratio", 0.3),
    STRUCTURES_BURIEDTREASURE_ENABLED("structures.buriedtreasure.enabled", true),
    STRUCTURES_BURIEDTREASURE_COUNT_PER_MEGACHUNK("structures.buriedtreasure.count-per-megachunk", 2),
    STRUCTURES_MINESHAFT_ENABLED("structures.mineshaft.enabled", true),
    STRUCTURES_MINESHAFT_SPAWNRATIO("structures.mineshaft.spawnratio", 0.8),
    STRUCTURES_MINESHAFT_MIN_Y("structures.mineshaft.min-y", -10),
    STRUCTURES_MINESHAFT_MAX_Y("structures.mineshaft.max-y", 30),
    STRUCTURES_CATACOMBS_ENABLED("structures.catacombs.enabled", true),
    STRUCTURES_CATACOMBS_SPAWNRATIO("structures.catacombs.spawnratio", 0.6),
    STRUCTURES_CATACOMBS_SIZEROLLCHANCE("structures.catacombs.sizerollchance", 0.40),
    STRUCTURES_CATACOMBS_MAX_LEVELS("structures.catacombs.max-levels", 5),
    STRUCTURES_CATACOMBS_MIN_Y("structures.catacombs.min-y", 0),
    STRUCTURES_CATACOMBS_MAX_Y("structures.catacombs.max-y", 30),
    STRUCTURES_ANCIENTCITY_ENABLED("structures.ancientcity.enabled", true),
    STRUCTURES_ANCIENTCITY_SPAWNRATIO("structures.ancientcity.spawnratio", 0.5),
    STRUCTURES_ANCIENTCITY_MIN_Y("structures.ancientcity.min-y", -20),
    STRUCTURES_ANCIENTCITY_MAX_Y("structures.ancientcity.max-y", -10),
    STRUCTURES_LARGECAVE_ENABLED("structures.largecave.enabled", true),
    STRUCTURES_LARGECAVE_SPAWNRATIO("structures.largecave.spawnratio", 0.8),
    STRUCTURES_BADLANDS_MINE_ENABLED("structures.badlands-mine.enabled", true),
    STRUCTURES_BADLANDS_MINE_DISTANCE("structures.badlands-mine.distance-between-mines", 8),
    STRUCTURES_BADLANDS_MINE_DEPTH("structures.badlands-mine.depth", 25),
    STRUCTURES_OUTPOST_ENABLED("structures.outpost.enabled", true),
    STRUCTURES_OUTPOST_SPAWNRATIO("structures.outpost.spawnratio", 0.8),

    // -=[ANIMALS]=-
    // BEES
    ANIMALS_BEE_HIVEFREQUENCY("animals.bee.hive-frequency", 0.02),
    // PIG
    ANIMALS_PIG_MINHERDSIZE("animals.pig.min-herd-size", 3),
    ANIMALS_PIG_MAXHERDSIZE("animals.pig.max-herd-size", 4),
    ANIMALS_PIG_CHANCE("animals.pig.chance", 2),
    // COW
    ANIMALS_COW_MINHERDSIZE("animals.cow.min-herd-size", 4),
    ANIMALS_COW_MAXHERDSIZE("animals.cow.max-herd-size", 12),
    ANIMALS_COW_CHANCE("animals.cow.chance", 2),
    // MOOSHROOM
    ANIMALS_MOOSHROOM_MINHERDSIZE("animals.mooshroom.min-herd-size", 2),
    ANIMALS_MOOSHROOM_MAXHERDSIZE("animals.mooshroom.max-herd-size", 6),
    ANIMALS_MOOSHROOM_CHANCE("animals.mooshroom.chance", 10),
    // SHEEP
    ANIMALS_SHEEP_MINHERDSIZE("animals.sheep.min-herd-size", 2),
    ANIMALS_SHEEP_MAXHERDSIZE("animals.sheep.max-herd-size", 8),
    ANIMALS_SHEEP_CHANCE("animals.sheep.chance", 2),
    // CHICKEN
    ANIMALS_CHICKEN_MINHERDSIZE("animals.chicken.min-herd-size", 1),
    ANIMALS_CHICKEN_MAXHERDSIZE("animals.chicken.max-herd-size", 3),
    ANIMALS_CHICKEN_CHANCE("animals.chicken.chance", 2),
    // ARMADILLO
    ANIMALS_ARMADILLO_MINHERDSIZE("animals.armadillo.min-herd-size", 1),
    ANIMALS_ARMADILLO_MAXHERDSIZE("animals.armadillo.max-herd-size", 3),
    ANIMALS_ARMADILLO_CHANCE("animals.armadillo.chance", 2),
    // HORSE
    ANIMALS_HORSE_MINHERDSIZE("animals.horse.min-herd-size", 2),
    ANIMALS_HORSE_MAXHERDSIZE("animals.horse.max-herd-size", 6),
    ANIMALS_HORSE_CHANCE("animals.horse.chance", 1),
    // DONKEY
    ANIMALS_DONKEY_MINHERDSIZE("animals.donkey.min-herd-size", 2),
    ANIMALS_DONKEY_MAXHERDSIZE("animals.donkey.max-herd-size", 6),
    ANIMALS_DONKEY_CHANCE("animals.donkey.chance", 1),
    // GOAT
    ANIMALS_GOAT_MINHERDSIZE("animals.goat.min-herd-size", 3),
    ANIMALS_GOAT_MAXHERDSIZE("animals.goat.max-herd-size", 4),
    ANIMALS_GOAT_CHANCE("animals.goat.chance", 1),
    // RABBIT
    ANIMALS_RABBIT_MINHERDSIZE("animals.rabbit.min-herd-size", 2),
    ANIMALS_RABBIT_MAXHERDSIZE("animals.rabbit.max-herd-size", 3),
    ANIMALS_RABBIT_CHANCE("animals.rabbit.chance", 1),
    // POLAR_BEAR
    ANIMALS_POLAR_BEAR_MINHERDSIZE("animals.polarbear.min-herd-size", 2),
    ANIMALS_POLAR_BEAR_MAXHERDSIZE("animals.polarbear.max-herd-size", 3),
    ANIMALS_POLAR_BEAR_CHANCE("animals.polarbear.chance", 1),
    // PANDA
    ANIMALS_PANDA_MINHERDSIZE("animals.panda.min-herd-size", 2),
    ANIMALS_PANDA_MAXHERDSIZE("animals.panda.max-herd-size", 3),
    ANIMALS_PANDA_CHANCE("animals.panda.chance", 1),
    // FOX
    ANIMALS_FOX_MINHERDSIZE("animals.fox.min-herd-size", 1),
    ANIMALS_FOX_MAXHERDSIZE("animals.fox.max-herd-size", 2),
    ANIMALS_FOX_CHANCE("animals.fox.chance", 1),
    // LLAMMA
    ANIMALS_LLAMA_MINHERDSIZE("animals.llama.min-herd-size", 4),
    ANIMALS_LLAMA_MAXHERDSIZE("animals.llama.max-herd-size", 6),
    ANIMALS_LLAMA_CHANCE("animals.llama.chance", 2),
    // PARROT
    ANIMALS_PARROT_MINHERDSIZE("animals.parrot.min-herd-size", 1),
    ANIMALS_PARROT_MAXHERDSIZE("animals.parrot.max-herd-size", 2),
    ANIMALS_PARROT_CHANCE("animals.parrot.chance", 2),
    // OCELOT
    ANIMALS_OCELOT_MINHERDSIZE("animals.ocelot.min-herd-size", 1),
    ANIMALS_OCELOT_MAXHERDSIZE("animals.ocelot.max-herd-size", 3),
    ANIMALS_OCELOT_CHANCE("animals.ocelot.chance", 1),
    // WOLF
    ANIMALS_WOLF_MINHERDSIZE("animals.wolf.min-herd-size", 1),
    ANIMALS_WOLF_MAXHERDSIZE("animals.wolf.max-herd-size", 4),
    ANIMALS_WOLF_CHANCE("animals.wolf.chance", 2),
    // TURTLE
    ANIMALS_TURTLE_MINHERDSIZE("animals.turtle.min-herd-size", 2),
    ANIMALS_TURTLE_MAXHERDSIZE("animals.turtle.max-herd-size", 5),
    ANIMALS_TURTLE_CHANCE("animals.turtle.chance", 1),
    // DOLPHIN
    ANIMALS_DOLPHIN_MINHERDSIZE("animals.dolphin.min-herd-size", 1),
    ANIMALS_DOLPHIN_MAXHERDSIZE("animals.dolphin.max-herd-size", 3),
    ANIMALS_DOLPHIN_CHANCE("animals.dolphin.chance", 1),
    // COD
    ANIMALS_COD_MINHERDSIZE("animals.cod.min-herd-size", 3),
    ANIMALS_COD_MAXHERDSIZE("animals.cod.max-herd-size", 6),
    ANIMALS_COD_CHANCE("animals.cod.chance", 2),
    // SQUID
    ANIMALS_SQUID_MINHERDSIZE("animals.squid.min-herd-size", 1),
    ANIMALS_SQUID_MAXHERDSIZE("animals.squid.max-herd-size", 4),
    ANIMALS_SQUID_CHANCE("animals.squid.chance", 2),
    // SALMON
    ANIMALS_SALMON_MINHERDSIZE("animals.salmon.min-herd-size", 4),
    ANIMALS_SALMON_MAXHERDSIZE("animals.salmon.max-herd-size", 4),
    ANIMALS_SALMON_CHANCE("animals.salmon.chance", 2),
    // PUFFERFISH
    ANIMALS_PUFFERFISH_MINHERDSIZE("animals.pufferfish.min-herd-size", 1),
    ANIMALS_PUFFERFISH_MAXHERDSIZE("animals.pufferfish.max-herd-size", 3),
    ANIMALS_PUFFERFISH_CHANCE("animals.pufferfish.chance", 1),
    // TROPICALFISH
    ANIMALS_TROPICALFISH_MINHERDSIZE("animals.tropical-fish.min-herd-size", 4),
    ANIMALS_TROPICALFISH_MAXHERDSIZE("animals.tropical-fish.max-herd-size", 8),
    ANIMALS_TROPICALFISH_CHANCE("animals.tropical-fish.chance", 2),

    // -=[ORES]=-
    // AMETHYST
    ORE_AMETHYST_CHANCE("ore.amethyst.chance-per-chunk", 0.05),
    ORE_AMETHYST_GEODE_SIZE("ore.amethyst.geode-size", 7),
    ORE_AMETHYST_MIN_DEPTH("ore.amethyst.min-depth", 70),
    ORE_AMETHYST_MIN_DEPTH_BELOW_SURFACE("ore.amethyst.min-depth-below-surface", 15),

    // COAL
    ORE_COAL_CHANCE("ore.coal.chance-per-chunk", 50),
    ORE_COAL_VEINSIZE("ore.coal.max-vein-size", 25),
    ORE_COAL_MAXVEINNUMBER("ore.coal.max-vein-count", 115),
    ORE_COAL_COMMONSPAWNHEIGHT("ore.coal.common-spawn-height", 96),
    ORE_COAL_MAXSPAWNHEIGHT("ore.coal.max-spawn-height", 256),
    ORE_COAL_MINSPAWNHEIGHT("ore.coal.min-spawn-height", 5),

    // IRON
    ORE_IRON_CHANCE("ore.iron.chance-per-chunk", 50),
    ORE_IRON_VEINSIZE("ore.iron.max-vein-size", 10),
    ORE_IRON_MAXVEINNUMBER("ore.iron.max-vein-count", 73),
    ORE_IRON_COMMONSPAWNHEIGHT("ore.iron.common-spawn-height", 16),
    ORE_IRON_MAXSPAWNHEIGHT("ore.iron.max-spawn-height", 256),
    ORE_IRON_MINSPAWNHEIGHT("ore.iron.min-spawn-height", -64),

    // GOLD
    ORE_GOLD_CHANCE("ore.gold.chance-per-chunk", 40),
    ORE_GOLD_VEINSIZE("ore.gold.max-vein-size", 10),
    ORE_GOLD_MAXVEINNUMBER("ore.gold.max-vein-count", 15),
    ORE_GOLD_COMMONSPAWNHEIGHT("ore.gold.common-spawn-height", -16),
    ORE_GOLD_MAXSPAWNHEIGHT("ore.gold.max-spawn-height", 32),
    ORE_GOLD_MINSPAWNHEIGHT("ore.gold.min-spawn-height", -64),

    // GOLD
    ORE_BADLANDSGOLD_CHANCE("ore.badlandsgold.chance-per-chunk", 40),
    ORE_BADLANDSGOLD_VEINSIZE("ore.badlandsgold.max-vein-size", 10),
    ORE_BADLANDSGOLD_MAXVEINNUMBER("ore.badlandsgold.max-vein-count", 15),
    ORE_BADLANDSGOLD_COMMONSPAWNHEIGHT("ore.badlandsgold.common-spawn-height", 32),
    ORE_BADLANDSGOLD_MAXSPAWNHEIGHT("ore.badlandsgold.max-spawn-height", 256),
    ORE_BADLANDSGOLD_MINSPAWNHEIGHT("ore.badlandsgold.min-spawn-height", -64),

    // DIAMOND
    ORE_DIAMOND_CHANCE("ore.diamond.chance-per-chunk", 30),
    ORE_DIAMOND_VEINSIZE("ore.diamond.max-vein-size", 7),
    ORE_DIAMOND_MAXVEINNUMBER("ore.diamond.max-vein-count", 16),
    ORE_DIAMOND_COMMONSPAWNHEIGHT("ore.diamond.common-spawn-height", -64),
    ORE_DIAMOND_MAXSPAWNHEIGHT("ore.diamond.max-spawn-height", 16),
    ORE_DIAMOND_MINSPAWNHEIGHT("ore.diamond.min-spawn-height", -64),

    // EMERALD
    ORE_EMERALD_CHANCE("ore.emerald.chance-per-chunk", 30),
    ORE_EMERALD_VEINSIZE("ore.emerald.max-vein-size", 7),
    ORE_EMERALD_MAXVEINNUMBER("ore.emerald.max-vein-count", 3),
    ORE_EMERALD_COMMONSPAWNHEIGHT("ore.emerald.common-spawn-height", 12),
    ORE_EMERALD_MAXSPAWNHEIGHT("ore.emerald.max-spawn-height", 256),
    ORE_EMERALD_MINSPAWNHEIGHT("ore.emerald.min-spawn-height", -16),

    // LAPIS
    ORE_LAPIS_CHANCE("ore.lapis.chance-per-chunk", 30),
    ORE_LAPIS_VEINSIZE("ore.lapis.max-vein-size", 6),
    ORE_LAPIS_MAXVEINNUMBER("ore.lapis.max-vein-count", 40),
    ORE_LAPIS_COMMONSPAWNHEIGHT("ore.lapis.common-spawn-height", 0),
    ORE_LAPIS_MAXSPAWNHEIGHT("ore.lapis.max-spawn-height", 64),
    ORE_LAPIS_MINSPAWNHEIGHT("ore.lapis.min-spawn-height", -64),

    // REDSTONE
    ORE_REDSTONE_CHANCE("ore.redstone.chance-per-chunk", 40),
    ORE_REDSTONE_VEINSIZE("ore.redstone.max-vein-size", 10),
    ORE_REDSTONE_MAXVEINNUMBER("ore.redstone.max-vein-count", 15),
    ORE_REDSTONE_COMMONSPAWNHEIGHT("ore.redstone.common-spawn-height", -32),
    ORE_REDSTONE_MAXSPAWNHEIGHT("ore.redstone.max-spawn-height", 16),
    ORE_REDSTONE_MINSPAWNHEIGHT("ore.redstone.min-spawn-height", -64),

    // COPPER
    ORE_COPPER_CHANCE("ore.copper.chance-per-chunk", 40),
    ORE_COPPER_VEINSIZE("ore.copper.max-vein-size", 10),
    ORE_COPPER_MAXVEINNUMBER("ore.copper.max-vein-count", 45),
    ORE_COPPER_COMMONSPAWNHEIGHT("ore.copper.common-spawn-height", 48),
    ORE_COPPER_MAXSPAWNHEIGHT("ore.copper.max-spawn-height", 104),
    ORE_COPPER_MINSPAWNHEIGHT("ore.copper.min-spawn-height", -16),

    // GRAVEL
    ORE_GRAVEL_CHANCE("ore.gravel.chance-per-chunk", 75),
    ORE_GRAVEL_VEINSIZE("ore.gravel.max-vein-size", 45),
    ORE_GRAVEL_MAXVEINNUMBER("ore.gravel.max-vein-count", 100),
    ORE_GRAVEL_COMMONSPAWNHEIGHT("ore.gravel.common-spawn-height", 255),
    ORE_GRAVEL_MAXSPAWNHEIGHT("ore.gravel.max-spawn-height", 300),
    ORE_GRAVEL_MINSPAWNHEIGHT("ore.gravel.min-spawn-height", -64),

    // ANDESITE
    ORE_ANDESITE_CHANCE("ore.andesite.chance-per-chunk", 80),
    ORE_ANDESITE_VEINSIZE("ore.andesite.max-vein-size", 45),
    ORE_ANDESITE_MAXVEINNUMBER("ore.andesite.max-vein-count", 110),
    ORE_ANDESITE_COMMONSPAWNHEIGHT("ore.andesite.common-spawn-height", 255),
    ORE_ANDESITE_MAXSPAWNHEIGHT("ore.andesite.max-spawn-height", 300),
    ORE_ANDESITE_MINSPAWNHEIGHT("ore.andesite.min-spawn-height", -64),

    // DIORITE
    ORE_DIORITE_CHANCE("ore.diorite.chance-per-chunk", 80),
    ORE_DIORITE_VEINSIZE("ore.diorite.max-vein-size", 45),
    ORE_DIORITE_MAXVEINNUMBER("ore.diorite.max-vein-count", 110),
    ORE_DIORITE_COMMONSPAWNHEIGHT("ore.diorite.common-spawn-height", 255),
    ORE_DIORITE_MAXSPAWNHEIGHT("ore.diorite.max-spawn-height", 300),
    ORE_DIORITE_MINSPAWNHEIGHT("ore.diorite.min-spawn-height", -64),

    // GRANITE
    ORE_GRANITE_CHANCE("ore.granite.chance-per-chunk", 80),
    ORE_GRANITE_VEINSIZE("ore.granite.max-vein-size", 45),
    ORE_GRANITE_MAXVEINNUMBER("ore.granite.max-vein-count", 110),
    ORE_GRANITE_COMMONSPAWNHEIGHT("ore.granite.common-spawn-height", 255),
    ORE_GRANITE_MAXSPAWNHEIGHT("ore.granite.max-spawn-height", 300),
    ORE_GRANITE_MINSPAWNHEIGHT("ore.granite.min-spawn-height", -64),

    // TUFF
    ORE_TUFF_CHANCE("ore.tuff.chance-per-chunk", 40),
    ORE_TUFF_VEINSIZE("ore.tuff.max-vein-size", 30),
    ORE_TUFF_MAXVEINNUMBER("ore.tuff.max-vein-count", 50),
    ORE_TUFF_COMMONSPAWNHEIGHT("ore.tuff.common-spawn-height", -64),
    ORE_TUFF_MAXSPAWNHEIGHT("ore.tuff.max-spawn-height", 10),
    ORE_TUFF_MINSPAWNHEIGHT("ore.tuff.min-spawn-height", -63),

    // DEEPSLATE
    ORE_DEEPSLATE_CHANCE("ore.deepslate.chance-per-chunk", 80),
    ORE_DEEPSLATE_VEINSIZE("ore.deepslate.max-vein-size", 45),
    ORE_DEEPSLATE_MAXVEINNUMBER("ore.deepslate.max-vein-count", 50),
    ORE_DEEPSLATE_COMMONSPAWNHEIGHT("ore.deepslate.common-spawn-height", 5),
    ORE_DEEPSLATE_MAXSPAWNHEIGHT("ore.deepslate.max-spawn-height", 15),
    ORE_DEEPSLATE_MINSPAWNHEIGHT("ore.deepslate.min-spawn-height", 0),

    FEATURE_CAVES_ENABLED("feature_toggle.caves", true),
    FEATURE_ANIMALS_ENABLED("feature_toggle.animals", true),
    FEATURE_ORES_ENABLED("feature_toggle.ores", true),
    FEATURE_TREES_ENABLED("feature_toggle.trees", true),
    FEATURE_TALL_MUSHROOMS_ENABLED("feature_toggle.tall_mushrooms", true),
    FEATURE_PLANTS_ENABLED("feature_toggle.plants", true),
    FEATURE_STRUCTURES_ENABLED("feature_toggle.structures", true),
    FEATURE_DECORATIONS_ENABLED("feature_toggle.decorations", true),

    ;
    private final String path;
    private Object value;
    private final Function<Object, Object> map;

    TConfigOption(String path, Object defaultValue) {
        this.path = path;
        this.value = defaultValue;
        this.map = o -> o;
    }

    TConfigOption(String path, Object defaultValue, Function<Object, Object> map) {
        this.path = path;
        this.value = defaultValue;
        this.map = map;
    }

    public static void loadValues(@NotNull ConfigLoader conf) {
        for (TConfigOption option : TConfigOption.values()) {
            conf.reg(option.path, option.map.apply(option.value));
        }
        conf.load();
        for (TConfigOption option : TConfigOption.values()) {
            option.value = option.map.apply(conf.get(option.path));
        }
    }

    public @NotNull String getString() {
        return ChatColor.translateAlternateColorCodes('&', (String) value);
    }

    public @NotNull String parse(String @NotNull ... placeholders) {
        String parsed = this.getString();

        String placeholder = "";

        for (int i = 0; i < placeholders.length; i++) {
            if (i % 2 == 0) {
                placeholder = placeholders[i];
            }
            else {
                parsed = parsed.replaceAll(placeholder, placeholders[i]);
            }
        }
        return parsed;
    }

    public int getInt() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return (int) value;
    }

    public boolean getBoolean() {
        return (Boolean) value;
    }

    public double getDouble() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return (double) value;
    }

    public float getFloat() {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return (float) value;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList() {
        return (List<String>) value;
    }

    public String @NotNull [] getStringArray() {
        String[] arr = new String[getStringList().size()];
        int i = 0;
        for (String item : getStringList()) {
            arr[i] = item;
            i++;
        }
        return arr;
    }

    public <T> T get(@NotNull Class<T> clazz) {
        return clazz.cast(value);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean areCavesEnabled() {
        return TConfigOption.FEATURE_CAVES_ENABLED.getBoolean();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean areTreesEnabled() {
        return TConfigOption.FEATURE_TREES_ENABLED.getBoolean();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean areTallMushroomsEnabled() {
        return TConfigOption.FEATURE_TALL_MUSHROOMS_ENABLED.getBoolean();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean arePlantsEnabled() {
        return TConfigOption.FEATURE_PLANTS_ENABLED.getBoolean();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean areStructuresEnabled() {
        return TConfigOption.FEATURE_STRUCTURES_ENABLED.getBoolean();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean areAnimalsEnabled() {
        return TConfigOption.FEATURE_ANIMALS_ENABLED.getBoolean();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean areDecorationsEnabled() {
        return TConfigOption.FEATURE_DECORATIONS_ENABLED.getBoolean();
    }

}
