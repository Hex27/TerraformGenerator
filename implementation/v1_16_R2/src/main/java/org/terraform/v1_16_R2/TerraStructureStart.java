package org.terraform.v1_16_R2;

import net.minecraft.server.v1_16_R2.BiomeBase;
import net.minecraft.server.v1_16_R2.ChunkGenerator;
import net.minecraft.server.v1_16_R2.DefinedStructureManager;
import net.minecraft.server.v1_16_R2.EnumDirection;
import net.minecraft.server.v1_16_R2.IRegistryCustom;
import net.minecraft.server.v1_16_R2.StructureAbstract;
import net.minecraft.server.v1_16_R2.StructureBoundingBox;
import net.minecraft.server.v1_16_R2.StructureGenerator;
import net.minecraft.server.v1_16_R2.WorldGenFeatureConfiguration;
import net.minecraft.server.v1_16_R2.WorldGenMonumentPieces;

import java.util.Random;

@SuppressWarnings("rawtypes")
public class TerraStructureStart extends StructureAbstract {

    private final String tag;

    public TerraStructureStart(String tag, StructureGenerator<?> var0, int var1, int var2,
                               StructureBoundingBox var3, int var4, long var5) {
        super(var0, var1, var2, var3, var4, var5);
        this.tag = tag;
    }

    @Override //isValid
    public boolean e() {
        return true;
    }

    public void setStructureBounds(StructureBoundingBox c) {
        this.c = c;
    }

    public void setStructureBounds(int x0, int y0, int z0, int x1, int y1, int z1) {
        this.c = new StructureBoundingBox(x0, y0, z0, x1, y1, z1);
        this.b.add(new WorldGenMonumentPieces
                .WorldGenMonumentPiece1(new Random(), x0, z0,
                EnumDirection.NORTH));
    }

    @Override
    public void a(IRegistryCustom arg0, ChunkGenerator arg1, DefinedStructureManager arg2, int arg3, int arg4,
                  BiomeBase arg5, WorldGenFeatureConfiguration arg6) {
        // TODO Auto-generated method stub
    }
}
