package org.terraform.utils;

import org.bukkit.Bukkit;

public class Version {
	
	public static String getVersionPackage(){
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}
	
	public static boolean isAtLeast(String version){
		return toVersionDouble(getVersionPackage()) 
				>= toVersionDouble(version);
	}
	
	private static double toVersionDouble(String version){
		return Double.parseDouble(version.replace("1_","").replace("_",".").replace("R","").replace("v",""));
	}

}
