package org.terraform.main;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.TickTimer;

public class TerraformGeneratorPlugin extends DrycellPlugin implements Listener{

	private static TerraformGeneratorPlugin i;
	public static NMSInjectorAbstract injector;
	
	public static TerraformGeneratorPlugin get(){ return i;}
	
	@Override
	public void onEnable(){
		super.onEnable();
		i = this;
		new TerraformCommandManager(this, "terraform","terra");
//		new BukkitRunnable(){
//			public void run(){
//				Bukkit.getLogger().info("=====Avg Timings=====");
//				HashMap<String,Long> c = (HashMap<String, Long>) TickTimer.timings.clone();
//				for(String key:c.keySet()){
//					Bukkit.getLogger().info("- " + key + ":" + c.get(key));
//				}
//			}
//		}.runTaskTimerAsynchronously(this, 10*20, 10*20);
		Bukkit.getPluginManager().registerEvents(this, this);
		String version = getVersionPackage();
		logger.info("Detected version: " + version);
		try {
			this.injector = (NMSInjectorAbstract) Class.forName("org.terraform.coregen." + version + ".NMSInjector").newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.error("&cNo support for this version has been made yet!");
		} catch (InstantiationException | IllegalAccessException e){
			e.printStackTrace();
			logger.error("&cSomething went wrong initiating the injector!");

		}
//		if(version.equals("v1_15_R1")){
//			this.injector = new org.terraform.coregen.v1_15_R1.NMSInjector();
//		}else{
//			logger.error("&cNo support for this version has been made yet!");
//		}
	}
	
	private String getVersionPackage(){
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
	}
	
	@EventHandler
	public void onWorldInit(WorldInitEvent event){
		if(event.getWorld().getGenerator() instanceof TerraformGenerator){
			logger.info("Detected world: " + event.getWorld().getName() + ", commencing injection... ");
			if(injector.attemptInject(event.getWorld())){
				logger.info("&aInjection success! Proceeding with generation.");
			}else{
				logger.error("&cInjection failed.");
			}
		}
	}
	
	@Override
	public void onDisable(){
		super.onDisable();
	}
	
	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
	    return new TerraformGenerator();
	}
	
}
