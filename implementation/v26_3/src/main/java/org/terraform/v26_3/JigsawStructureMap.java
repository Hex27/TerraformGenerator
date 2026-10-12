package org.terraform.v26_3;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.ancientcity.AncientCityPopulator;
import org.terraform.structure.small.camp.AbandonedCampPopulator;
import org.terraform.structure.trialchamber.TrialChamberPopulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JigsawStructureMap {
    public static ThreadLocal<Map<? extends Structure,Class<? extends StructurePopulator>>> JIGSAW_STRUCTURES
            = ThreadLocal.withInitial(()->{
                HashMap<Structure,Class<? extends StructurePopulator>> map = new HashMap<>();
                Registry<Structure> registry = MinecraftServer.getServer().registryAccess().lookup(Registries.STRUCTURE).orElseThrow();

                map.put(registry.getValue(Identifier.parse("ancient_city")), AncientCityPopulator.class);
                map.put(registry.getValue(Identifier.parse("trial_chambers")), TrialChamberPopulator.class);

                registry.stream().forEach(structure -> {
                    if(Objects.requireNonNull(registry.getKey(structure)).getPath().contains("abandoned_camp"))
                        map.put(structure, AbandonedCampPopulator.class);
                });

                return map;
    });
}
