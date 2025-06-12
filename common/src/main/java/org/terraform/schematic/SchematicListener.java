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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SchematicListener implements Listener {
    public static final ConcurrentHashMap<UUID, TerraRegion> rgs = new ConcurrentHashMap<>();
    private static final String WAND_NAME = ChatColor.AQUA + "Terra Wand";

    public static @NotNull ItemStack getWand() {
        ItemStack wand = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        assert meta != null;
        meta.setDisplayName(WAND_NAME);
        meta.setLore(List.of(ChatColor.RED + "-=[Developer's Tool]=-"));
        wand.setItemMeta(meta);

        return wand;
    }

    @EventHandler
    public void onBlockClick(@NotNull PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getPlayer().getEquipment().getItemInMainHand();
        if (!item.hasItemMeta()) {
            return;
        }
        if (!item.getItemMeta().getDisplayName().equals(WAND_NAME)) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        TerraRegion terraRg = rgs.computeIfAbsent(player.getUniqueId(), k -> new TerraRegion());

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            terraRg.setOne(event.getClickedBlock());
            player.sendMessage(ChatColor.GREEN + "Position one set.");
        }
        else {
            terraRg.setTwo(event.getClickedBlock());
            player.sendMessage(ChatColor.GREEN + "Position two set.");
        }
    }
}
