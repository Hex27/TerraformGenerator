package org.terraform.utils;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;

public class ArmorStandUtils {

	public static void placeArmorStand(SimpleBlock target, BlockFace facing, Random rand) {
		placeArmorStand(target, facing, ArmorStandQuality.rollQuality(rand));
	}
	
	public static void placeArmorStand(SimpleBlock target, BlockFace facing, ArmorStandQuality quality) {
		if(target.getPopData() instanceof PopulatorDataPostGen) {
			PopulatorDataPostGen postGen = ((PopulatorDataPostGen) target.getPopData());
			ArmorStand stand = (ArmorStand) postGen.getWorld().spawnEntity(new Location(postGen.getWorld(),
					target.getX() + 0.5f, target.getY(), target.getZ() + 0.5f), EntityType.ARMOR_STAND);
			stand.setRotation(BlockUtils.yawFromBlockFace(facing), 0);
			quality.apply(stand);
		}
	}
	
	public static enum ArmorStandQuality
	{
		LEATHER(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS),
		IRON(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS),
		GOLD(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS),
		DIAMOND(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS);
		private Material[] pieces;
		ArmorStandQuality(Material... pieces){
			this.pieces = pieces;
		}
		
		public void apply(ArmorStand entity) {
			entity.getEquipment().setHelmet(new ItemStack(pieces[0]));
			entity.getEquipment().setChestplate(new ItemStack(pieces[1]));
			entity.getEquipment().setLeggings(new ItemStack(pieces[2]));
			entity.getEquipment().setBoots(new ItemStack(pieces[3]));
		}
		
		
		public static ArmorStandQuality rollQuality(Random rand) {
			int weight = rand.nextInt(100);
			if(weight > 95) //15%
				return ArmorStandQuality.DIAMOND;
			else if(weight > 60) //35%
				return ArmorStandQuality.IRON;
			else if(weight > 30) //30%
				return ArmorStandQuality.GOLD;
			else //30%
				return ArmorStandQuality.LEATHER;
		}
	}
}
