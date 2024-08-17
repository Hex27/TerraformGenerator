package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.catacombs.CatacombsPopulator;

import java.util.Random;
import java.util.Stack;

public class CatacombsCommand extends TerraCommand {

    public CatacombsCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Spawntest for catacombs";
    }

    @Override
    public boolean canConsoleExec() {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {

        return sender.isOp();
    }

    @Override
    public void execute(CommandSender sender, Stack<String> args) {

        Player p = (Player) sender;
//		PopulatorDataAbstract gen = 
//				new org.terraform.coregen.v1_15_R1.PopulatorData(
//						((org.bukkit.craftbukkit.v1_15_R1.CraftChunk)p.getChunk()).getHandle(),
//						null,p.getLocation().getChunk().getX(),p.getLocation().getChunk().getZ());
        PopulatorDataPostGen data = new PopulatorDataPostGen(p.getLocation().getChunk());
        int x = p.getLocation().getBlockX();
        int y = p.getLocation().getBlockY();
        int z = p.getLocation().getBlockZ();
        TerraformWorld tw = TerraformWorld.get(p.getWorld());

//		RoomLayoutGenerator gen = new RoomLayoutGenerator(tw.getRand(8),50,x,y,z,100);
//		gen.setPathPopulator(new StrongholdPathPopulator(tw.getRand(13)));
//		gen.generate();
//		gen.fill(data, tw, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
//		
        new CatacombsPopulator().spawnCatacombs(tw, new Random(), data, x, y, z);
    }

}
