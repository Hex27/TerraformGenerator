package org.terraform.carving;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;

public abstract class Carver {

	public abstract void carve(TerraformWorld tw, PopulatorDataAbstract data, Random random);
}
