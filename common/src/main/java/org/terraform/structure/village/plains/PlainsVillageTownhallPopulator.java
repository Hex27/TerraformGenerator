package org.terraform.structure.village.plains;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bell;
import org.bukkit.block.data.type.Bell.Attachment;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.structure.villagehouse.farmhouse.FarmhouseSchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.WoodUtils;
import org.terraform.utils.WoodUtils.WoodType;
import org.terraform.utils.version.OneOneSevenBlockHandler;

import java.util.Random;

public class PlainsVillageTownhallPopulator extends RoomPopulatorAbstract {

    private final TerraformWorld tw;
    private int elevation;

    public PlainsVillageTownhallPopulator(TerraformWorld tw, Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        this.tw = tw;
        this.elevation = GenUtils.randInt(this.rand, 2, 4);
    }
    
    public void setElevation(int elevation) {
    	this.elevation = elevation;
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        int x = room.getX();
        int z = room.getZ();
        int y = GenUtils.getHighestGround(data, x, z);
        try {
            BiomeBank biome = tw.getBiomeBank(x, z);
            y += elevation;
            TerraSchematic farmHouse = TerraSchematic.load("farmhouse", new Location(tw.getWorld(), x, y, z));
            farmHouse.parser = new FarmhouseSchematicParser(biome, this.rand, data);
            BlockFace face = BlockUtils.getDirectBlockFace(this.rand);
            if (room instanceof DirectionalCubeRoom) {
                face = ((DirectionalCubeRoom) room).getDirection();
            }

            farmHouse.setFace(face);
            farmHouse.apply();

            TerraformGeneratorPlugin.logger.info("Spawning farmhouse at " + x + "," + y + "," + z + " with rotation of " + farmHouse.getFace());

            data.addEntity(x, y + 1, z, EntityType.VILLAGER); //Two villagers
            data.addEntity(x, y + 1, z, EntityType.VILLAGER);
            data.addEntity(x, y + 1, z, EntityType.CAT); //And a cat.

            //Spawn a base on the house to sit on
            for (int nx = -17 / 2 - 1; nx <= 17 / 2 + 1; nx++) {
                for (int nz = -17 / 2 - 1; nz <= 17 / 2 + 1; nz++) {
                    if (data.getType(x + nx, y - 1, z + nz).toString().contains("PLANKS") ||
                            data.getType(x + nx, y - 1, z + nz).toString().contains("STONE_BRICKS"))
                        BlockUtils.setDownUntilSolid(x + nx, y - 2, z + nz, data, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
                    else if (data.getType(x + nx, y - 1, z + nz).toString().contains("LOG"))
                        BlockUtils.setDownUntilSolid(x + nx, y - 2, z + nz, data, data.getType(x + nx, y - 1, z + nz));
                }
            }

            //Spawn a stairway from the house.
            Wall w = new Wall(new SimpleBlock(data, x, y - 1, z), farmHouse.getFace()).getRight();
            for (int i = 0; i < 7; i++)
                w = w.getFront();
            //while(w.getType() != Material.DIRT){
            while (!w.getType().isSolid() ||
                    w.getType().toString().contains("PLANKS")) {
                Stairs stairs = (Stairs) Bukkit.createBlockData(GenUtils.randMaterial(this.rand, Material.COBBLESTONE_STAIRS, Material.COBBLESTONE_STAIRS,
                        Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS));
                stairs.setFacing(w.getDirection().getOppositeFace());
                w.getRight().setBlockData(stairs);
                w.setBlockData(stairs);
                w.getLeft().setBlockData(stairs);

                w.getLeft(2).getRelative(0, 1, 0).downUntilSolid(this.rand, WoodUtils.getWoodForBiome(biome, WoodType.LOG));
                w.getLeft(2).getRelative(0, 2, 0).setType(GenUtils.randMaterial(this.rand, Material.COBBLESTONE_WALL, Material.COBBLESTONE_WALL, Material.COBBLESTONE_WALL,
                        Material.MOSSY_COBBLESTONE_WALL));

                w.getRight(2).getRelative(0, 1, 0).downUntilSolid(this.rand, WoodUtils.getWoodForBiome(biome, WoodType.LOG));
                w.getRight(2).getRelative(0, 2, 0).setType(GenUtils.randMaterial(this.rand, Material.COBBLESTONE_WALL, Material.COBBLESTONE_WALL,
                        Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));

                w = w.getFront().getRelative(0, -1, 0);
            }

            //Place a bell. The townhall acts as the village center.
            Bell bell = (Bell) Bukkit.createBlockData(Material.BELL);
            bell.setAttachment(Attachment.SINGLE_WALL);
            bell.setFacing(w.getDirection().getOppositeFace());
            w.getLeft(2).getRelative(0, 2, 0).setBlockData(bell);

            Wall entrance = w.getGround();
            int maxDepth = 5;

            //Connect front to the nearest path.
            while (entrance.getType() != OneOneSevenBlockHandler.DIRT_PATH() && maxDepth > 0) {
                if (BlockUtils.isDirtLike(entrance.getType()))
                    entrance.setType(OneOneSevenBlockHandler.DIRT_PATH());

                Wall leftPath = entrance.getLeft().getGround();
                Wall rightPath = entrance.getRight().getGround();
                if (BlockUtils.isDirtLike(leftPath.getType()))
                    leftPath.setType(OneOneSevenBlockHandler.DIRT_PATH());
                if (BlockUtils.isDirtLike(rightPath.getType()))
                    rightPath.setType(OneOneSevenBlockHandler.DIRT_PATH());


                entrance = entrance.getFront().getGround();
                maxDepth--;
            }

        } catch (Throwable e) {
            TerraformGeneratorPlugin.logger.error("Something went wrong trying to place farmhouse at " + x + "," + y + "," + z + "!");
            e.printStackTrace();
        }

    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return room.isHuge();
    }
}
