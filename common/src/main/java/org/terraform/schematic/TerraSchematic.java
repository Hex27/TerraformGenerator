package org.terraform.schematic;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.util.Vector;
import org.terraform.coregen.BlockDataFixerAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.version.Version;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

public class TerraSchematic {
    public SchematicParser parser = new SchematicParser();
    HashMap<Vector, BlockData> data = new HashMap<>();
    SimpleBlock refPoint;
    BlockFace face = BlockFace.NORTH;
    private double VERSION_VALUE;

    //North is always the default blockface.
    //
    public TerraSchematic(SimpleBlock vector) {
        this.refPoint = vector;
    }

    public TerraSchematic(Location loc) {
        this.refPoint = new SimpleBlock(loc);
    }

    public static TerraSchematic load(String internalPath, SimpleBlock refPoint) throws FileNotFoundException {
        TerraSchematic schem = new TerraSchematic(refPoint);
        InputStream is = TerraformGeneratorPlugin.get().getClass().getResourceAsStream("/" + internalPath + ".terra");
        @SuppressWarnings("resource")
        Scanner sc = new Scanner(is);    //file to be scanned

        String line = sc.nextLine(); //First line is the schematic's version.
        schem.VERSION_VALUE = Version.toVersionDouble(line);
        
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            if (line.isEmpty()) continue;
            String[] cont = line.split(":@:");
            String[] v = cont[0].split(",");
            Vector key = new Vector(Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2]));
            BlockData value;
            try {
                value = Bukkit.createBlockData(cont[1]);
            } catch (IllegalArgumentException e) {
                BlockDataFixerAbstract fixer = TerraformGeneratorPlugin.injector.getBlockDataFixer();
                if (fixer != null) {
                    value = Bukkit.createBlockData(fixer.updateSchematic(cont[1]));
                } else {
                    //GG
                    value = null;
                    throw e;
                }
            }
            schem.data.put(key, value);
        }
        sc.close();
        return schem;
    }
    
    public static TerraSchematic load(String internalPath, Location loc) throws FileNotFoundException {
        SimpleBlock block = new SimpleBlock(loc);

        return load(internalPath, block);
    }

    public void registerBlock(Block b) {
        Vector rel = b.getLocation().toVector().subtract(refPoint.toVector());
        //String coords = rel.getBlockX() + "," + rel.getBlockY() + "," + rel.getBlockZ();
        data.put(rel, b.getBlockData());
    }

    public void apply() {
        BlockDataFixerAbstract bdfa = TerraformGeneratorPlugin.injector.getBlockDataFixer();
        ArrayList<Vector> multiFace = new ArrayList<>();
        
//        if(this.VERSION_VALUE > 14.4) {
//        	//Fix weird one block offset.
//        	refPoint = refPoint.getRelative(1,0,1);
//        }
        
        for (Entry<Vector, BlockData> entry : data.entrySet()) {
            Vector pos = entry.getKey().clone();
            BlockData bd = entry.getValue();
            if (face == BlockFace.WEST) {
                int x = pos.getBlockX();
                pos.setX(pos.getZ());
                pos.setZ(x * -1);
            } else if (face == BlockFace.SOUTH) {
                pos.setX(pos.getX() * -1);
                pos.setZ(pos.getZ() * -1);
            } else if (face == BlockFace.EAST) {
                int x = pos.getBlockX();
                pos.setX(pos.getZ() * -1);
                pos.setZ(x);
            }

            if (face != BlockFace.NORTH) {
                if (bd instanceof Orientable) {
                    Orientable o = (Orientable) bd;
                    if (face == BlockFace.EAST || face == BlockFace.WEST) {
                        if (o.getAxis() == Axis.X) {
                            o.setAxis(Axis.Z);
                        } else if (o.getAxis() == Axis.Z) {
                            o.setAxis(Axis.X);
                        }
                    }
                } else if (bd instanceof Rotatable) {
                    Rotatable r = (Rotatable) bd;
                    if (face == BlockFace.SOUTH) {
                        r.setRotation(r.getRotation().getOppositeFace());
                    } else if (face == BlockFace.EAST) {
                        r.setRotation(BlockUtils.getAdjacentFaces(r.getRotation())[0]);
                    } else if (face == BlockFace.WEST) {
                        r.setRotation(BlockUtils.getAdjacentFaces(r.getRotation())[1]);
                    }
                } else if (bd instanceof Directional) {
                    Directional r = (Directional) bd;
                    if (BlockUtils.isDirectBlockFace(r.getFacing()))
                        if (face == BlockFace.SOUTH) {
                            r.setFacing(r.getFacing().getOppositeFace());
                        } else if (face == BlockFace.WEST) {
                            r.setFacing(BlockUtils.getAdjacentFaces(r.getFacing())[1]);
                        } else if (face == BlockFace.EAST) {
                            r.setFacing(BlockUtils.getAdjacentFaces(r.getFacing())[0]);
                        }
                } else if (bd instanceof MultipleFacing) {
                    multiFace.add(pos);
                }

                //Apply version-specific updates
                if (bdfa != null)
                    bdfa.correctFacing(pos, null, bd, face);
            }
            
//            if(pos.getBlockX() == 0 && pos.getBlockZ() == 0) {
//            	Bukkit.getLogger().info("DEBUG SCHEMATIC: " + refPoint.getRelative(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()).toVector().toString());
//            }
            parser.applyData(refPoint.getRelative(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()), bd);
        }

        //Multiple-facing blocks are just gonna be painful.
        for (Vector pos : multiFace) {
            SimpleBlock b = refPoint.getRelative(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());

            BlockUtils.correctSurroundingMultifacingData(b);
        }
        if (bdfa != null && face != BlockFace.NORTH) {
            for (Vector pos : bdfa.flush()) {
                SimpleBlock b = refPoint.getRelative(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
                bdfa.correctFacing(pos, b, null, face);
            }
        }
    }

    public void export(String path) throws IOException {
        File fout = new File(path);
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        
        //Append version header
        bw.write(Version.DOUBLE+"");
        bw.newLine();
        
        for (Entry<Vector, BlockData> entry : data.entrySet()) {
            String v = entry.getKey().getBlockX() + "," + entry.getKey().getBlockY() + ',' + entry.getKey().getBlockZ();
            bw.write(v + ":@:" + entry.getValue().getAsString());
            bw.newLine();
        }

        bw.close();
    }

    /**
     * @return the face
     */
    public BlockFace getFace() {
        return face;
    }

    /**
     * @param face the face to set
     */
    public void setFace(BlockFace face) {
        this.face = face;
    }

	public double getVersionValue() {
		return VERSION_VALUE;
	}

}
