package org.terraform.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;

import java.util.Random;
import java.util.Stack;

public class SphereCommand extends DCCommand {

    public SphereCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Spawns a randomised simplex sphere";
    }

    @Override
    public boolean canConsoleExec() {
        return false;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {

        return sender.isOp();
    }

    @Override
    public void execute(CommandSender sender, Stack<String> args)
            throws InvalidArgumentException {

        Player p = (Player) sender;
        PopulatorDataPostGen data = new PopulatorDataPostGen(p.getLocation().getChunk());
        Random r = new Random();
        int seed = r.nextInt(9999);
        float trueRadius = 4;
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);
        SimpleBlock block = new SimpleBlock(data, p.getLocation().getBlock());

        for(float x = -trueRadius; x <= trueRadius; x++) {
            for(float y = -trueRadius; y <= trueRadius; y++) {
                for(float z = -trueRadius; z <= trueRadius; z++) {
                    double radiusSquared = Math.pow(trueRadius + noise.GetNoise(x, y, z) * 2, 2);
                    SimpleBlock rel = block.getRelative(Math.round(x), Math.round(y), Math.round(z));
                    if(rel.distanceSquared(block) <= radiusSquared) {
                        //replaced = true;
                        rel.setType(Material.STONE);
                    }
                }
            }
        }

    }
}
