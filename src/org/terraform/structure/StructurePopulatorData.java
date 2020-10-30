package org.terraform.structure;

import org.terraform.coregen.PopulatorDataAbstract;

public abstract class StructurePopulatorData extends PopulatorDataAbstract {
//	
//	HashMap<SimpleLocation, BlockData> changes = new HashMap<>();
//	
//	public void applyChances(PopulatorDataAbstract exp){
//		Iterator<Entry<SimpleLocation,BlockData>> it = changes.entrySet().iterator();
//		while(it.hasNext()){
//			Entry<SimpleLocation,BlockData> change = it.next();
//			if(exp.setBlockData(change.getKey().getX(), 
//					change.getKey().getY(), 
//					change.getKey().getZ(), change.getValue())){
//				it.remove();
//			}
//		}
//	}
//
//	@Override
//	public Material getType(int x, int y, int z) {
//		SimpleLocation loc = new SimpleLocation(x,y,z);
//		if(changes.containsKey(loc)) return changes.get(loc).getMaterial();
//		return Material.VOID_AIR;
//	}
//
//	@Override
//	public BlockData getBlockData(int x, int y, int z) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public boolean setType(int x, int y, int z, Material type) {
//		// TODO Auto-generated method stub
//		
//		return true; //Structure populator doesn't care.
//	}
//
//	@Override
//	public boolean setBlockData(int x, int y, int z, BlockData data) {
//		// TODO Auto-generated method stub
//		
//		return true;
//	}
//
//	@Override
//	public Biome getBiome(int rawX, int rawY, int rawZ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void addEntity(int rawX, int rawY, int rawZ, EntityType type) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public int getChunkX() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public int getChunkZ() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void lootTableChest(int x, int y, int z, TerraLootTable table) {
//		// TODO Auto-generated method stub
//		
//	}

}
