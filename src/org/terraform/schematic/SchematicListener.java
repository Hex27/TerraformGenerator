package org.terraform.schematic;

import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.drycell.gui.ItemBuilder;

public class SchematicListener implements Listener{
	
	public static HashMap<UUID,TerraRegion> rgs = new HashMap<>();
	
	@EventHandler
	public void onBlockClick(PlayerInteractEvent event){
		if(event.getPlayer().getEquipment().getItemInMainHand() == null) return;
		if(event.getPlayer().getEquipment().getItemInMainHand().getItemMeta() == null) return;
		if(event.getPlayer().getEquipment().getItemInMainHand().getItemMeta().getDisplayName() == null) return;
	
		if(!event.getPlayer().getEquipment().getItemInMainHand().getItemMeta()
				.getDisplayName().equals(getWand().getItemMeta().getDisplayName())) 
			return;
		if(event.getHand() != EquipmentSlot.HAND) 
			return;
		
		event.setCancelled(true);
		UUID id = event.getPlayer().getUniqueId();
		if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			if(!rgs.containsKey(id)){
				rgs.put(id, new TerraRegion());
			}
			rgs.get(id).setOne(event.getClickedBlock().getLocation());
			event.getPlayer().sendMessage(ChatColor.GREEN + "Position one set.");
		}else if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(!rgs.containsKey(id)){
				rgs.put(id, new TerraRegion());
			}
			rgs.get(id).setTwo(event.getClickedBlock().getLocation());
			event.getPlayer().sendMessage(ChatColor.GREEN + "Position two set.");
		}
	}
	
	public static ItemStack getWand(){
		return new ItemBuilder(Material.GOLDEN_AXE)
		.setName(ChatColor.AQUA + "Terra Wand")
		.addLore(ChatColor.RED + "-=[Developer's Tool]=-")
		.build();
	}
	
	
}
