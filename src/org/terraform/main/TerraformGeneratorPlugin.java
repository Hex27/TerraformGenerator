package org.terraform.main;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.NMSInjectorAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.coregen.v1_16_R1.BlockDataFixer;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.reflection.Post14PrivateFieldHandler;
import org.terraform.reflection.Pre14PrivateFieldHandler;
import org.terraform.reflection.PrivateFieldHandler;
import org.terraform.schematic.SchematicListener;
import org.terraform.utils.Version;

public class TerraformGeneratorPlugin extends DrycellPlugin implements Listener{

	private static TerraformGeneratorPlugin i;
	public static NMSInjectorAbstract injector;
	public static ArrayList<String> injectedWorlds = new ArrayList<>();
	public static PrivateFieldHandler privateFieldHandler;
	public static TerraformGeneratorPlugin get(){ return i;}
	
	@Override
	public void onEnable(){
		super.onEnable();
		i = this;
		
		try {
			Field.class.getDeclaredField("modifiers");
			privateFieldHandler = new Pre14PrivateFieldHandler();
		} catch (NoSuchFieldException | SecurityException e1) {
			privateFieldHandler = new Post14PrivateFieldHandler();
		}
		
		this.logger = new TLogger(this);
		TConfigOption.loadValues(this.getDCConfig());
		TerraformGenerator.updateSeaLevelFromConfig();
		new TerraformCommandManager(this, "terraform","terra");
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new SchematicListener(), this);
		String version = Version.getVersionPackage();
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
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event){
		if(event.getWorld().getGenerator() instanceof TerraformGenerator){
			if(TerraformGenerator.preWorldInitGen.size() > 0){
				if(!TConfigOption.DEVSTUFF_ATTEMPT_FIXING_PREMATURE.getBoolean()){
					logger.info("&cIgnoring " 
							+ TerraformGenerator.preWorldInitGen.size() 
							+ " pre-maturely generated chunks."
							+ " You may see a patch of plain land.");
					return;
				}
				logger.info("&6Trying to decorate " 
			+ TerraformGenerator.preWorldInitGen.size() 
			+ " pre-maturely generated chunks.");
				int fixed = 0;
				TerraformWorld tw = TerraformWorld.get(event.getWorld());
				for(SimpleChunkLocation sc:TerraformGenerator.preWorldInitGen){
					if(!sc.getWorld().equals(event.getWorld().getName())) continue;
					logger.debug("Populating " + sc.toString());
					PopulatorDataPostGen data = new PopulatorDataPostGen(sc.toChunk());
					new TerraformPopulator(tw).populate(tw, new Random(), data);
					fixed++;
				}
				logger.info("&aSuccessfully finished fixing " + fixed + " pre-mature chunks!");
				
			}
		}
	}
	
	@EventHandler
	public void onWorldInit(WorldInitEvent event){
		if(event.getWorld().getGenerator() instanceof TerraformGenerator){
			logger.info("Detected world: " + event.getWorld().getName() + ", commencing injection... ");
			if(injector.attemptInject(event.getWorld())){
				injectedWorlds.add(event.getWorld().getName());
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
