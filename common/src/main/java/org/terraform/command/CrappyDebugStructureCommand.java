package org.terraform.command;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.ancientcity.AncientCityPopulator;

import java.util.Random;
import java.util.Stack;

public class CrappyDebugStructureCommand extends TerraCommand {

    public CrappyDebugStructureCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Debug spawning command not meant for server use ever.";
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
//		PopulatorDataAbstract gen = 
//				new org.terraform.coregen.v1_15_R1.PopulatorData(
//						((org.bukkit.craftbukkit.v1_15_R1.CraftChunk)p.getChunk()).getHandle(),
//						null,p.getLocation().getChunk().getX(),p.getLocation().getChunk().getZ());
        PopulatorDataPostGen data = new PopulatorDataPostGen(p.getLocation().getChunk());
        int x = p.getLocation().getBlockX() + 1000;
        int y = 70;
        int z = p.getLocation().getBlockZ();
        TerraformWorld tw = TerraformWorld.get(p.getWorld());
        
        new AncientCityPopulator().spawnAncientCity(tw, new Random(), data, x, y, z);
        p.teleport(new Location(p.getWorld(), x,y + 40,z));
        
    }

}
