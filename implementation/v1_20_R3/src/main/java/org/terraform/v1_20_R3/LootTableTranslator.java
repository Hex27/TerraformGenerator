package org.terraform.v1_20_R3;

import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.storage.loot.LootTables;
import org.terraform.coregen.TerraLootTable;

import java.util.HashMap;

public class LootTableTranslator {

    // I am indeed formally trained to properly structure my code
    // I have however elected to completely ignore said training here
    public static final HashMap<TerraLootTable, MinecraftKey> translationMap = new HashMap<>() {{
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
        put(TerraLootTable.TRIAL_CHAMBERS_SUPPLY, LootTables.T);
        put(TerraLootTable.TRIAL_CHAMBERS_CORRIDOR, LootTables.U);
        put(TerraLootTable.TRIAL_CHAMBERS_INTERSECTION, LootTables.V);
        put(TerraLootTable.TRIAL_CHAMBERS_INTERSECTION_BARREL, LootTables.W);
        put(TerraLootTable.TRIAL_CHAMBERS_ENTRANCE, LootTables.X);
        put(TerraLootTable.TRIAL_CHAMBERS_CORRIDOR_DISPENSER, LootTables.Y);
        put(TerraLootTable.TRIAL_CHAMBERS_CHAMBER_DISPENSER, LootTables.Z);
        put(TerraLootTable.TRIAL_CHAMBERS_WATER_DISPENSER, LootTables.aa);
        put(TerraLootTable.TRIAL_CHAMBERS_CORRIDOR_POT, LootTables.ab);
        put(TerraLootTable.SHEEP_WHITE, LootTables.ac);
        put(TerraLootTable.SHEEP_ORANGE, LootTables.ad);
        put(TerraLootTable.SHEEP_MAGENTA, LootTables.ae);
        put(TerraLootTable.SHEEP_LIGHT_BLUE, LootTables.af);
        put(TerraLootTable.SHEEP_YELLOW, LootTables.ag);
        put(TerraLootTable.SHEEP_LIME, LootTables.ah);
        put(TerraLootTable.SHEEP_PINK, LootTables.ai);
        put(TerraLootTable.SHEEP_GRAY, LootTables.aj);
        put(TerraLootTable.SHEEP_LIGHT_GRAY, LootTables.ak);
        put(TerraLootTable.SHEEP_CYAN, LootTables.al);
        put(TerraLootTable.SHEEP_PURPLE, LootTables.am);
        put(TerraLootTable.SHEEP_BLUE, LootTables.an);
        put(TerraLootTable.SHEEP_BROWN, LootTables.ao);
        put(TerraLootTable.SHEEP_GREEN, LootTables.ap);
        put(TerraLootTable.SHEEP_RED, LootTables.aq);
        put(TerraLootTable.SHEEP_BLACK, LootTables.ar);
        put(TerraLootTable.FISHING, LootTables.as);
        put(TerraLootTable.FISHING_JUNK, LootTables.at);
        put(TerraLootTable.FISHING_TREASURE, LootTables.au);
        put(TerraLootTable.FISHING_FISH, LootTables.av);
        put(TerraLootTable.CAT_MORNING_GIFT, LootTables.aw);
        put(TerraLootTable.ARMORER_GIFT, LootTables.ax);
        put(TerraLootTable.BUTCHER_GIFT, LootTables.ay);
        put(TerraLootTable.CARTOGRAPHER_GIFT, LootTables.az);
        put(TerraLootTable.CLERIC_GIFT, LootTables.aA);
        put(TerraLootTable.FARMER_GIFT, LootTables.aB);
        put(TerraLootTable.FISHERMAN_GIFT, LootTables.aC);
        put(TerraLootTable.FLETCHER_GIFT, LootTables.aD);
        put(TerraLootTable.LEATHERWORKER_GIFT, LootTables.aE);
        put(TerraLootTable.LIBRARIAN_GIFT, LootTables.aF);
        put(TerraLootTable.MASON_GIFT, LootTables.aG);
        put(TerraLootTable.SHEPHERD_GIFT, LootTables.aH);
        put(TerraLootTable.TOOLSMITH_GIFT, LootTables.aI);
        put(TerraLootTable.WEAPONSMITH_GIFT, LootTables.aJ);
        put(TerraLootTable.SNIFFER_DIGGING, LootTables.aK);
        put(TerraLootTable.PIGLIN_BARTERING, LootTables.aL);
        put(TerraLootTable.SPAWNER_TRIAL_CHAMBER_KEY, LootTables.aM);
        put(TerraLootTable.SPAWNER_TRIAL_CHAMBER_CONSUMABLES, LootTables.aN);
        put(TerraLootTable.DESERT_WELL_ARCHAEOLOGY, LootTables.aO);
        put(TerraLootTable.DESERT_PYRAMID_ARCHAEOLOGY, LootTables.aP);
        put(TerraLootTable.TRAIL_RUINS_ARCHAEOLOGY_COMMON, LootTables.aQ);
        put(TerraLootTable.TRAIL_RUINS_ARCHAEOLOGY_RARE, LootTables.aR);
        put(TerraLootTable.OCEAN_RUIN_WARM_ARCHAEOLOGY, LootTables.aS);
        put(TerraLootTable.OCEAN_RUIN_COLD_ARCHAEOLOGY, LootTables.aT);
    }};

}
