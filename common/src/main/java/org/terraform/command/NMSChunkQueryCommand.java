package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.main.TerraformGeneratorPlugin;
import java.util.Stack;

public class NMSChunkQueryCommand extends TerraCommand {

    public NMSChunkQueryCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Queries NMS Chunk information";
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
    public void execute(CommandSender sender, Stack<String> args)
            throws InvalidArgumentException {
    	//Commented out to prevent runtime errors for older versions
//
//        Player p = (Player) sender;
//        
//        net.minecraft.world.level.chunk.Chunk chunk = ((CraftChunk) p.getLocation().getChunk()).getHandle();
//        p.sendMessage("BiomeStorage class: "+chunk.getBiomeIndex().getClass().getName());
//        
//    	int x = (int) p.getLocation().getX();
//        int y = (int) p.getLocation().getY();
//        int z = (int) p.getLocation().getZ();
//        
//        for(int i = -5; i <= 5; i++) {
//        	p.sendMessage("Current Biome Color: " + Integer.toHexString(chunk.getBiomeIndex().getBiome(x >> 2, (y+i) >> 2, z >> 2).g()));
//        }
//        p.sendMessage("Finished query");
//        
//        
    }

}
