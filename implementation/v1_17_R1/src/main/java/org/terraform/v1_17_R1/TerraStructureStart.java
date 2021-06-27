package org.terraform.v1_17_R1;

import java.lang.reflect.Field;
import java.util.Random;

import net.minecraft.core.EnumDirection;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.WorldGenMonumentPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructureManager;

@SuppressWarnings("rawtypes")
public class TerraStructureStart extends StructureStart {

    @SuppressWarnings("unchecked")
	public TerraStructureStart(StructureGenerator var0, ChunkCoordIntPair var1, int var2, long var3) {
        super(var0, var1, var2, var3);
    }

	@Override
	public void a(IRegistryCustom arg0, ChunkGenerator arg1, DefinedStructureManager arg2, ChunkCoordIntPair arg3,
			BiomeBase arg4, WorldGenFeatureConfiguration arg5, LevelHeightAccessor arg6) {
		// TODO Auto-generated method stub
		
	}

//    @Override //isValid
//    public boolean e() {
//        return true;
//    }
//
//    @SuppressWarnings("unchecked")
//	public void setStructureBounds(StructureBoundingBox c) {
//        this.c = c;
//    }
//
    @SuppressWarnings("unchecked")
	public void setStructureBounds(int x0, int y0, int z0, int x1, int y1, int z1) {
        //i is private, because fuck you.
    	//this.i = new StructureBoundingBox(x0, y0, z0, x1, y1, z1);
    	try {
        	Field i = StructureStart.class.getDeclaredField("i");
        	i.setAccessible(true);
        	i.set(this, new StructureBoundingBox(x0, y0, z0, x1, y1, z1));
        }
    	catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) 
    	{
    		e.printStackTrace();
    	}
    	
    	this.c.add(new WorldGenMonumentPieces
                .WorldGenMonumentPiece1(new Random(), x0, z0,
                EnumDirection.a));
    }
//
//    @Override
//    public void a(IRegistryCustom arg0, ChunkGenerator arg1, DefinedStructureManager arg2, int arg3, int arg4,
//                  BiomeBase arg5, WorldGenFeatureConfiguration arg6) {
//        // TODO Auto-generated method stub
//    }
}
