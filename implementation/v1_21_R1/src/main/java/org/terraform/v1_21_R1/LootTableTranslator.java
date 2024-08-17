package org.terraform.v1_21_R1;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import org.terraform.coregen.TerraLootTable;

import java.util.HashMap;

public class LootTableTranslator {

    //I am indeed formally trained to properly structure my code
    //I have however elected to completely ignore said training here
    public static final HashMap<TerraLootTable, ResourceKey<LootTable>> translationMap = new HashMap<>(){{
        put(TerraLootTable.EMPTY, LootTables.a);
        put(TerraLootTable.SPAWN_BONUS_CHEST, LootTables.b);
        put(TerraLootTable.END_CITY_TREASURE, LootTables.c);
        put(TerraLootTable.SIMPLE_DUNGEON, LootTables.d);
        put(TerraLootTable.VILLAGE_WEAPONSMITH, LootTables.e);
        put(TerraLootTable.VILLAGE_TOOLSMITH, LootTables.f);
        put(TerraLootTable.VILLAGE_ARMORER, LootTables.g);
        put(TerraLootTable.VILLAGE_CARTOGRAPHER, LootTables.h);
        put(TerraLootTable.VILLAGE_MASON, LootTables.i);
        put(TerraLootTable.VILLAGE_SHEPHERD, LootTables.j);
        put(TerraLootTable.VILLAGE_BUTCHER, LootTables.k);
        put(TerraLootTable.VILLAGE_FLETCHER, LootTables.l);
        put(TerraLootTable.VILLAGE_FISHER, LootTables.m);
        put(TerraLootTable.VILLAGE_TANNERY, LootTables.n);
        put(TerraLootTable.VILLAGE_TEMPLE, LootTables.o);
        put(TerraLootTable.VILLAGE_DESERT_HOUSE, LootTables.p);
        put(TerraLootTable.VILLAGE_PLAINS_HOUSE, LootTables.q);
        put(TerraLootTable.VILLAGE_TAIGA_HOUSE, LootTables.r);
        put(TerraLootTable.VILLAGE_SNOWY_HOUSE, LootTables.s);
        put(TerraLootTable.VILLAGE_SAVANNA_HOUSE, LootTables.t);
        put(TerraLootTable.ABANDONED_MINESHAFT, LootTables.u);
        put(TerraLootTable.NETHER_BRIDGE, LootTables.v);
        put(TerraLootTable.STRONGHOLD_LIBRARY, LootTables.w);
        put(TerraLootTable.STRONGHOLD_CROSSING, LootTables.x);
        put(TerraLootTable.STRONGHOLD_CORRIDOR, LootTables.y);
        put(TerraLootTable.DESERT_PYRAMID, LootTables.z);
        put(TerraLootTable.JUNGLE_TEMPLE, LootTables.A);
        put(TerraLootTable.JUNGLE_TEMPLE_DISPENSER, LootTables.B);
        put(TerraLootTable.IGLOO_CHEST, LootTables.C);
        put(TerraLootTable.WOODLAND_MANSION, LootTables.D);
        put(TerraLootTable.UNDERWATER_RUIN_SMALL, LootTables.E);
        put(TerraLootTable.UNDERWATER_RUIN_BIG, LootTables.F);
        put(TerraLootTable.BURIED_TREASURE, LootTables.G);
        put(TerraLootTable.SHIPWRECK_MAP, LootTables.H);
        put(TerraLootTable.SHIPWRECK_SUPPLY, LootTables.I);
        put(TerraLootTable.SHIPWRECK_TREASURE, LootTables.J);
        put(TerraLootTable.PILLAGER_OUTPOST, LootTables.K);
        put(TerraLootTable.BASTION_TREASURE, LootTables.L);
        put(TerraLootTable.BASTION_OTHER, LootTables.M);
        put(TerraLootTable.BASTION_BRIDGE, LootTables.N);
        put(TerraLootTable.BASTION_HOGLIN_STABLE, LootTables.O);
        put(TerraLootTable.ANCIENT_CITY, LootTables.P);
        put(TerraLootTable.ANCIENT_CITY_ICE_BOX, LootTables.Q);
        put(TerraLootTable.RUINED_PORTAL, LootTables.R);
        put(TerraLootTable.TRIAL_CHAMBERS_REWARD, LootTables.S);
        put(TerraLootTable.TRIAL_CHAMBERS_REWARD_COMMON, LootTables.T);
        put(TerraLootTable.TRIAL_CHAMBERS_REWARD_RARE, LootTables.U);
        put(TerraLootTable.TRIAL_CHAMBERS_REWARD_UNIQUE, LootTables.V);
        put(TerraLootTable.TRIAL_CHAMBERS_REWARD_OMINOUS, LootTables.W);
        put(TerraLootTable.TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON, LootTables.X);
        put(TerraLootTable.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE, LootTables.Y);
        put(TerraLootTable.TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE, LootTables.Z);
        put(TerraLootTable.TRIAL_CHAMBERS_SUPPLY, LootTables.aa);
        put(TerraLootTable.TRIAL_CHAMBERS_CORRIDOR, LootTables.ab);
        put(TerraLootTable.TRIAL_CHAMBERS_INTERSECTION, LootTables.ac);
        put(TerraLootTable.TRIAL_CHAMBERS_INTERSECTION_BARREL, LootTables.ad);
        put(TerraLootTable.TRIAL_CHAMBERS_ENTRANCE, LootTables.ae);
        put(TerraLootTable.TRIAL_CHAMBERS_CORRIDOR_DISPENSER, LootTables.af);
        put(TerraLootTable.TRIAL_CHAMBERS_CHAMBER_DISPENSER, LootTables.ag);
        put(TerraLootTable.TRIAL_CHAMBERS_WATER_DISPENSER, LootTables.ah);
        put(TerraLootTable.TRIAL_CHAMBERS_CORRIDOR_POT, LootTables.ai);
        put(TerraLootTable.EQUIPMENT_TRIAL_CHAMBER, LootTables.aj);
        put(TerraLootTable.EQUIPMENT_TRIAL_CHAMBER_RANGED, LootTables.ak);
        put(TerraLootTable.EQUIPMENT_TRIAL_CHAMBER_MELEE, LootTables.al);
        put(TerraLootTable.SHEEP_WHITE, LootTables.am);
        put(TerraLootTable.SHEEP_ORANGE, LootTables.an);
        put(TerraLootTable.SHEEP_MAGENTA, LootTables.ao);
        put(TerraLootTable.SHEEP_LIGHT_BLUE, LootTables.ap);
        put(TerraLootTable.SHEEP_YELLOW, LootTables.aq);
        put(TerraLootTable.SHEEP_LIME, LootTables.ar);
        put(TerraLootTable.SHEEP_PINK, LootTables.as);
        put(TerraLootTable.SHEEP_GRAY, LootTables.at);
        put(TerraLootTable.SHEEP_LIGHT_GRAY, LootTables.au);
        put(TerraLootTable.SHEEP_CYAN, LootTables.av);
        put(TerraLootTable.SHEEP_PURPLE, LootTables.aw);
        put(TerraLootTable.SHEEP_BLUE, LootTables.ax);
        put(TerraLootTable.SHEEP_BROWN, LootTables.ay);
        put(TerraLootTable.SHEEP_GREEN, LootTables.az);
        put(TerraLootTable.SHEEP_RED, LootTables.aA);
        put(TerraLootTable.SHEEP_BLACK, LootTables.aB);
        put(TerraLootTable.FISHING, LootTables.aC);
        put(TerraLootTable.FISHING_JUNK, LootTables.aD);
        put(TerraLootTable.FISHING_TREASURE, LootTables.aE);
        put(TerraLootTable.FISHING_FISH, LootTables.aF);
        put(TerraLootTable.CAT_MORNING_GIFT, LootTables.aG);
        put(TerraLootTable.ARMORER_GIFT, LootTables.aH);
        put(TerraLootTable.BUTCHER_GIFT, LootTables.aI);
        put(TerraLootTable.CARTOGRAPHER_GIFT, LootTables.aJ);
        put(TerraLootTable.CLERIC_GIFT, LootTables.aK);
        put(TerraLootTable.FARMER_GIFT, LootTables.aL);
        put(TerraLootTable.FISHERMAN_GIFT, LootTables.aM);
        put(TerraLootTable.FLETCHER_GIFT, LootTables.aN);
        put(TerraLootTable.LEATHERWORKER_GIFT, LootTables.aO);
        put(TerraLootTable.LIBRARIAN_GIFT, LootTables.aP);
        put(TerraLootTable.MASON_GIFT, LootTables.aQ);
        put(TerraLootTable.SHEPHERD_GIFT, LootTables.aR);
        put(TerraLootTable.TOOLSMITH_GIFT, LootTables.aS);
        put(TerraLootTable.WEAPONSMITH_GIFT, LootTables.aT);
        put(TerraLootTable.SNIFFER_DIGGING, LootTables.aU);
        put(TerraLootTable.PANDA_SNEEZE, LootTables.aV);
        put(TerraLootTable.PIGLIN_BARTERING, LootTables.aW);
        put(TerraLootTable.SPAWNER_TRIAL_CHAMBER_KEY, LootTables.aX);
        put(TerraLootTable.SPAWNER_TRIAL_CHAMBER_CONSUMABLES, LootTables.aY);
        put(TerraLootTable.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, LootTables.aZ);
        put(TerraLootTable.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, LootTables.ba);
        put(TerraLootTable.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS, LootTables.bb);
        put(TerraLootTable.BOGGED_SHEAR, LootTables.bc);
        put(TerraLootTable.DESERT_WELL_ARCHAEOLOGY, LootTables.bd);
        put(TerraLootTable.DESERT_PYRAMID_ARCHAEOLOGY, LootTables.be);
        put(TerraLootTable.TRAIL_RUINS_ARCHAEOLOGY_COMMON, LootTables.bf);
        put(TerraLootTable.TRAIL_RUINS_ARCHAEOLOGY_RARE, LootTables.bg);
        put(TerraLootTable.OCEAN_RUIN_WARM_ARCHAEOLOGY, LootTables.bh);
        put(TerraLootTable.OCEAN_RUIN_COLD_ARCHAEOLOGY, LootTables.bi);
    }};

}
