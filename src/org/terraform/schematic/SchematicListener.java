package org.terraform.schematic;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.drycell.gui.ItemBuilder;

import java.util.HashMap;
import java.util.UUID;

public class SchematicListener implements Listener {
    public static final HashMap<UUID, TerraRegion> rgs = new HashMap<>();
    private static final String WAND_NAME = ChatColor.AQUA + "Terra Wand";

    public static ItemStack getWand() {
        return new ItemBuilder(Material.GOLDEN_AXE)
                .setName(WAND_NAME)
                .addLore(ChatColor.RED + "-=[Developer's Tool]=-")
                .build();
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getPlayer().getEquipment().getItemInMainHand();
        if (!item.hasItemMeta()) return;
        if (!item.getItemMeta().getDisplayName().equals(WAND_NAME)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        TerraRegion terraRg = rgs.computeIfAbsent(player.getUniqueId(), k -> new TerraRegion());

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            terraRg.setOne(event.getClickedBlock());
            player.sendMessage(ChatColor.GREEN + "Position one set.");
        } else {
            terraRg.setTwo(event.getClickedBlock());
            player.sendMessage(ChatColor.GREEN + "Position two set.");
        }
    }
}
