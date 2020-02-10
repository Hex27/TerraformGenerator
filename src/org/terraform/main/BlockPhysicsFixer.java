package org.terraform.main;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http2.Http2Stream;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.terraform.coregen.TerraformGenerator;

public class BlockPhysicsFixer implements Listener{
	
	@EventHandler
	public void onDirtTill(PlayerInteractEvent event){
		if(event.getClickedBlock() != null)
			event.getClickedBlock().setMetadata("terra-player-block", new FixedMetadataValue(TerraformGeneratorPlugin.get(),""));
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		event.getBlock().setMetadata("terra-player-block", new FixedMetadataValue(TerraformGeneratorPlugin.get(),""));
	}
	
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event){
		if(event.getBlock().getWorld().getGenerator() instanceof TerraformGenerator){
			if(event.getChangedType() == Material.FARMLAND||
					event.getBlock().getType() == Material.FARMLAND){
				if(!event.getBlock().hasMetadata("terra-player-block")
						&& !event.getBlock().hasMetadata("terra-gen")){
					event.setCancelled(true);
				}
//				int x = event.getBlock().getX();
//				int y = event.getBlock().getY();
//				int z = event.getBlock().getZ();
//				boolean isTerra = event.getBlock().hasMetadata("terra-gen");
				//TerraformGeneratorPlugin.logger.info(event.getBlock().getType() + " -> " + event.getChangedType().toString() + ": Physics change at :" + x + "," + y + "," + z + " (" + isTerra + ")");
				
			}
		}
	}
}
