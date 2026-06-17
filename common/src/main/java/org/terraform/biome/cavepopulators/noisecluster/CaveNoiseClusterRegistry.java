package org.terraform.biome.cavepopulators.noisecluster;

import org.terraform.main.config.TConfig;
import org.terraform.utils.version.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CaveNoiseClusterRegistry {
    public static Collection<AbstractNoiseClusterPopulator> initiateNoiseClusterPopulators(){
        ArrayList<AbstractNoiseClusterPopulator> populators = new ArrayList<>();
        if(Version.VERSION.isAtLeast(Version.v26_2))
            populators.add(new SulfurNoiseClusterPopulator());
        return populators;
    }
}
