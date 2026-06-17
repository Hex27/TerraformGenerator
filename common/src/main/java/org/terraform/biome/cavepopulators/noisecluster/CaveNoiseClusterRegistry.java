package org.terraform.biome.cavepopulators.noisecluster;

import org.terraform.main.config.TConfig;

import java.util.Collection;
import java.util.List;

public class CaveNoiseClusterRegistry {
    public static Collection<AbstractNoiseClusterPopulator> initiateNoiseClusterPopulators(){
        return List.of(
                new SulfurNoiseClusterPopulator()
        );
    }
}
