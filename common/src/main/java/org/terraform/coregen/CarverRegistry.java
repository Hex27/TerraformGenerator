package org.terraform.coregen;

import org.terraform.carving.Carver;
import org.terraform.data.TerraformWorld;

import java.util.ArrayList;
import java.util.Random;

public class CarverRegistry {
    private static final ArrayList<Carver> CARVERS = new ArrayList<>();

    public static void doCarving(TerraformWorld tw, PopulatorDataAbstract data, Random random) {
        for(Carver carver : CARVERS) {
            carver.carve(tw, data, random);
        }
    }
}
