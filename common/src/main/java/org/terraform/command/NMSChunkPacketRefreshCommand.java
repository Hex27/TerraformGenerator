package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.main.TerraformGeneratorPlugin;
import java.util.Stack;

public class NMSChunkPacketRefreshCommand extends TerraCommand {

    public NMSChunkPacketRefreshCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Sets current biome you're on to muddy bog and forces a packet refresh for you for the chunk you're standing on";
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
//        Player p = (Player) sender;
//        
//        net.minecraft.world.level.chunk.Chunk chunk = ((CraftChunk) p.getLocation().getChunk()).getHandle();
//        p.sendMessage("BiomeStorage class: "+chunk.getBiomeIndex().getClass().getName());
//        
//        DedicatedServer dedicatedserver = ((CraftServer) Bukkit.getServer()).getServer();
//        IRegistryWritable<BiomeBase> registrywritable = dedicatedserver.getCustomRegistry().b(IRegistry.aO);
//        
//        ResourceKey<BiomeBase> rkey = ResourceKey.a(IRegistry.aO, new MinecraftKey("terraformgenerator:muddybog"));
//        BiomeBase base = registrywritable.a(rkey);
//        if(base == null) {
//            ResourceKey<BiomeBase> newrkey = ResourceKey.a(IRegistry.aO, new MinecraftKey("terraformgenerator","muddybog"));
//            base = registrywritable.a(newrkey);
//        }
//        
//        if(base != null) {
//        	int x = (int) p.getLocation().getX();
//            int y = (int) p.getLocation().getY();
//            int z = (int) p.getLocation().getZ();
//            
//            for(int i = -5; i <= 5; i++) {
//            	p.sendMessage("Old Biome Color: " + chunk.getBiomeIndex().getBiome(x >> 2, (y+i) >> 2, z >> 2).g());
//                chunk.getBiomeIndex().setBiome(x >> 2, (y+i) >> 2, z >> 2, base);
//            }
//            ((CraftPlayer) p).getHandle().b.sendPacket(new PacketPlayOutMapChunk(((CraftChunk) p.getLocation().getChunk()).getHandle()));
//            p.sendMessage("Finished setting biome.");
//        }
//        else
//        {
//        	p.sendMessage("Base was null.");
//        }
//        
//        
    }

}
