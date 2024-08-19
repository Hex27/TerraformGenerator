package org.terraform.structure.room.jigsaw;

import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.*;

/**
 * This class has nothing to do with the new system
 */
public class JigsawBuilder {
    protected final int widthX;
    protected final int widthZ;
    protected final int @NotNull [] lowerBounds = new int[2];
    protected final int @NotNull [] upperBounds = new int[2];
    protected final int maxDepth = 5; // The amount of pieces before an end piece is forced.
    protected final SimpleBlock core;
    protected final @NotNull Stack<JigsawStructurePiece> traverseStack = new Stack<>();
    protected final @NotNull HashMap<SimpleLocation, JigsawStructurePiece> pieces = new HashMap<>();
    protected final @NotNull ArrayList<JigsawStructurePiece> overlapperPieces = new ArrayList<>();
    protected int chanceToAddNewPiece = 60;
    protected int minimumPieces = 0;
    protected int pieceWidth = 5;
    protected @Nullable JigsawStructurePiece center;
    protected Wall entranceBlock;
    protected JigsawStructurePiece[] pieceRegistry;
    protected BlockFace entranceDir;
    protected boolean hasPlacedEntrance = false;
    int traversalIndex = 0;

    public JigsawBuilder(int widthX, int widthZ, @NotNull PopulatorDataAbstract data, int x, int y, int z) {
        this.widthX = widthX;
        this.widthZ = widthZ;
        this.core = new SimpleBlock(data, x, y, z);
        // this.pieceRegistry = pieceRegistry;
        this.lowerBounds[0] = x - widthX / 2;
        this.lowerBounds[1] = z - widthZ / 2;
        this.upperBounds[0] = x + widthX / 2;
        this.upperBounds[1] = z + widthZ / 2;
    }

    public void forceEntranceDirection(BlockFace face) {
        entranceDir = face;
    }

    public BlockFace getEntranceDirection() {
        return entranceDir;
    }

    public @Nullable JigsawStructurePiece getFirstPiece(@NotNull Random random) {
        return getPiece(pieceRegistry, JigsawType.STANDARD, random).getInstance(random, 0);
    }

    public void generate(@NotNull Random random) {

        center = getFirstPiece(random);
        center.getRoom().setX(core.getX());
        center.getRoom().setY(core.getY());
        center.getRoom().setZ(core.getZ());
        pieces.put(new SimpleLocation(core.getX(), core.getY(), core.getZ()), center);
        traverseStack.push(center);
        while (!areAllPiecesCovered()) {
            if (!traverseAndPopulatePieces(random)) {
                break;
            }
        }

        // MAKE SURE NO CELL HAS 4 WALLS. Remove all 4 walls and
        // replace it with a room.
        ArrayList<SimpleLocation> problemCells = new ArrayList<>();
        HashMap<SimpleLocation, Integer> map = new HashMap<>();
        for (JigsawStructurePiece piece : overlapperPieces) {
            if (map.containsKey(piece.getRoom().getSimpleLocation())) {
                map.put(piece.getRoom().getSimpleLocation(), map.get(piece.getRoom().getSimpleLocation()) + 1);
            }
            else {
                map.put(piece.getRoom().getSimpleLocation(), 1);
            }
            if (map.get(piece.getRoom().getSimpleLocation()) >= 4) {
                // 4 wall thing spotted. That's bad. Try to replace it.
                problemCells.add(piece.getRoom().getSimpleLocation());
                TerraformGeneratorPlugin.logger.info("Found problem piece. Attempting to replace with room.");
            }
        }
        if (!problemCells.isEmpty()) {
            overlapperPieces.removeIf(piece -> problemCells.contains(piece.getRoom().getSimpleLocation()));

            for (SimpleLocation loc : problemCells) {

                JigsawStructurePiece toAdd = getPiece(pieceRegistry, JigsawType.STANDARD, random).getInstance(
                        random,
                        0
                );

                toAdd.getRoom().setX(loc.getX());
                toAdd.getRoom().setY(loc.getY());
                toAdd.getRoom().setZ(loc.getZ());

                toAdd.setPopulated(BlockFace.NORTH);
                toAdd.setPopulated(BlockFace.SOUTH);
                toAdd.setPopulated(BlockFace.EAST);
                toAdd.setPopulated(BlockFace.WEST);
                pieces.put(loc, toAdd);
                TerraformGeneratorPlugin.logger.info("Patched problem piece with new room.");
            }
        }
    }

    public boolean traverseAndPopulatePieces(@NotNull Random random) {
        if (traverseStack.isEmpty()) {
            TerraformGeneratorPlugin.logger.info("Jigsaw stack size empty!");
            return false;
        }
        JigsawStructurePiece current = traverseStack.peek();
        // TerraformGeneratorPlugin.logger.info("Traversal Index " + traversalIndex + ", on: " + current.toString());
        traversalIndex++;
        if (traversalIndex > 200) {
            TerraformGeneratorPlugin.logger.error("Infinite loop detected! Breaking.");
            return false;
        }
        if (current.hasUnpopulatedDirections()) {
            BlockFace dir = current.getNextUnpopulatedBlockFace();
            JigsawStructurePiece toAdd;
            int toAddX = current.getRoom().getX() + pieceWidth * dir.getModX();
            int toAddY = current.getRoom().getY() + pieceWidth * dir.getModY();
            int toAddZ = current.getRoom().getZ() + pieceWidth * dir.getModZ();
            SimpleLocation newLoc = new SimpleLocation(toAddX, toAddY, toAddZ);
            if (!pieces.containsKey(newLoc)) {
                if (dir == BlockFace.UP) {
                    // Place an upper connector piece
                    toAdd = getRelativePiece(current, JigsawType.UPPERCONNECTOR, random).getInstance(
                            random,
                            current.getDepth() + 1
                    );
                    toAdd.setRotation(BlockUtils.getDirectBlockFace(random));
                    toAdd.setElevation(current.getElevation() + 1);
                }
                else {
                    // Depth is too high (prevent recursion)
                    if (current.getDepth() >= maxDepth) { // Place an end
                        toAdd = getRelativePiece(current, JigsawType.END, random).getInstance(
                                random,
                                current.getDepth() + 1
                        );
                        toAdd.setRotation(dir);
                    }
                    else {
                        if (toAddX - pieceWidth / 2 < lowerBounds[0]
                            || toAddX + pieceWidth / 2 > upperBounds[0]
                            || toAddZ - pieceWidth / 2 < lowerBounds[1]
                            || toAddZ + pieceWidth / 2 > upperBounds[1])
                        {
                            // If outside of bounding box, just force an end piece.
                            toAdd = getRelativePiece(current, JigsawType.END, random).getInstance(
                                    random,
                                    current.getDepth() + 1
                            );
                            toAdd.setRotation(dir);
                        }
                        else if (pieces.size() > minimumPieces && !GenUtils.chance(random, chanceToAddNewPiece, 100)) {
                            // Failed chance to add. Force wall.
                            toAdd = getRelativePiece(current, JigsawType.END, random).getInstance(
                                    random,
                                    current.getDepth() + 1
                            );
                            toAdd.setRotation(dir);
                        }
                        else {
                            // Place a standard piece
                            toAdd = getRelativePiece(current, JigsawType.STANDARD, random).getInstance(
                                    random,
                                    current.getDepth() + 1
                            );
                        }
                    }
                }
                toAdd.getRoom().setX(toAddX);
                toAdd.getRoom().setY(toAddY);
                toAdd.getRoom().setZ(toAddZ);

                current.setPopulated(dir);
                toAdd.setPopulated(dir.getOppositeFace());


                // TerraformGeneratorPlugin.logger.info("New location: " + new SimpleLocation(toAddX, toAddY, toAddZ));


                if (toAdd.getType() == JigsawType.END) {
                    overlapperPieces.add(toAdd);
                }

                if (toAdd.getType() != JigsawType.END) {
                    pieces.put(newLoc, toAdd);
                    traverseStack.push(toAdd);
                }
            }
            else { // The location exists in the map already
                JigsawStructurePiece other = pieces.get(newLoc);
                current.setPopulated(dir);
                other.setPopulated(dir.getOppositeFace());
            }

        }
        else {
            traverseStack.pop(); // Go to the previous piece to continue populating
        }
        return true;
    }

    public boolean areAllPiecesCovered() {
        for (JigsawStructurePiece piece : pieces.values()) {
            // TerraformGeneratorPlugin.logger.info("Checking " + piece.toString());
            if (piece.hasUnpopulatedDirections()) {
                return false;
            }
        }
        return true;
    }

    public SimpleBlock getCore() {
        return core;
    }

    public @NotNull HashMap<SimpleLocation, JigsawStructurePiece> getPieces() {
        return pieces;
    }

    public void build(@NotNull Random random) {
        for (JigsawStructurePiece piece : pieces.values()) {

            // Force room to be air first
            piece.getRoom().purgeRoomContents(core.getPopData(), 0);

            // Build room
            piece.build(core.getPopData(), random);
        }

        ArrayList<JigsawStructurePiece> toRemove = new ArrayList<>();
        JigsawStructurePiece entrance = null;
        // Overlapper pieces are stuff like walls and entrances.
        Collections.shuffle(overlapperPieces);
        for (JigsawStructurePiece piece : overlapperPieces) {
            // Don't place overlapper objects where rooms have been placed.
            SimpleLocation pieceLoc = new SimpleLocation(
                    piece.getRoom().getX(),
                    piece.getRoom().getY(),
                    piece.getRoom().getZ()
            );
            if (pieces.containsKey(pieceLoc)) {
                toRemove.add(piece);
                continue;
            }
            if (!hasPlacedEntrance) {
                if (entranceDir == null // Random block face direction
                    || (piece.getRotation() == entranceDir))  // Forced entrance direction
                {
                    // This check is to ensure that entrances aren't walled up in 4 sides.
                    if (canPlaceEntrance(pieceLoc)) {
                        // Place an entrance if none was placed before.
                        hasPlacedEntrance = true;
                        entrance = getPiece(pieceRegistry, JigsawType.ENTRANCE, random).getInstance(
                                random,
                                piece.getDepth()
                        );
                        entrance.getRoom().setX(piece.getRoom().getX());
                        entrance.getRoom().setY(piece.getRoom().getY());
                        entrance.getRoom().setZ(piece.getRoom().getZ());
                        entrance.setRotation(piece.getRotation());
                        entranceBlock = new Wall(new SimpleBlock(core.getPopData(),
                                piece.getRoom().getX(),
                                piece.getRoom().getY(),
                                piece.getRoom().getZ()
                        ), piece.getRotation());
                        piece = entrance;
                    }
                }
            }
            JigsawStructurePiece host = getAdjacentPiece(pieceLoc, piece.getRotation().getOppositeFace());
            if (host != null) {
                host.getWalledFaces().add(piece.getRotation());
            }
            // TerraformGeneratorPlugin.logger.info("Populating at " + piece.getClass().getSimpleName() + "::" + piece.getRoom().getX() + "," + piece.getRoom().getZ() + "," + piece.getRotation());
            piece.build(core.getPopData(), random);
        }

        // Remove pieces that weren't placed and replace the unused wall with the entrance.
        Iterator<JigsawStructurePiece> it = overlapperPieces.iterator();
        while (it.hasNext()) {
            JigsawStructurePiece piece = it.next();
            if (toRemove.contains(piece)) {
                it.remove();
            }
            else if (piece.getRoom().getSimpleLocation().equals(entrance.getRoom().getSimpleLocation())
                     && piece.getRotation() == entrance.getRotation())
            {
                it.remove();
            }
        }
        overlapperPieces.add(entrance);
    }

    public boolean canPlaceEntrance(SimpleLocation pieceLoc) {
        return this.countOverlappingPiecesAtLocation(pieceLoc) != 4;
    }

    public int countOverlappingPiecesAtLocation(SimpleLocation loc) {
        int count = 0;
        for (JigsawStructurePiece wall : overlapperPieces) {
            if (wall.getRoom().getSimpleLocation().equals(loc)) {
                count++;
            }
        }
        return count;
    }

    public JigsawStructurePiece getAdjacentPiece(@NotNull SimpleLocation loc, @NotNull BlockFace face) {
        SimpleLocation other = new SimpleLocation(loc.getX() + face.getModX() * pieceWidth,
                loc.getY() + face.getModY() * pieceWidth,
                loc.getZ() + face.getModZ() * pieceWidth
        );
        return pieces.get(other);
    }

    public @Nullable JigsawStructurePiece getAdjacentWall(@NotNull SimpleLocation loc, @NotNull BlockFace face) {
        SimpleLocation other = new SimpleLocation(loc.getX() + face.getModX() * pieceWidth,
                loc.getY() + face.getModY() * pieceWidth,
                loc.getZ() + face.getModZ() * pieceWidth
        );
        for (JigsawStructurePiece wall : overlapperPieces) {
            if (wall.getRotation() == face && wall.getRoom().getSimpleLocation().equals(other)) {
                return wall;
            }
        }
        return null;
    }

    public @Nullable JigsawStructurePiece getPiece(JigsawStructurePiece[] registry,
                                                   JigsawType type,
                                                   @NotNull Random rand)
    {
        ArrayList<JigsawStructurePiece> validPieces = new ArrayList<>();
        for (JigsawStructurePiece piece : pieceRegistry) {
            boolean dontPlace = false;
            if (piece.getType() == type) {
                if (piece.isUnique()) {
                    for (JigsawStructurePiece present : this.pieces.values()) {

                        if (present.getClass().equals(piece.getClass())) {
                            dontPlace = true;
                            break;
                        }
                    }
                }
                if (!dontPlace) {
                    validPieces.add(piece);
                }
            }
        }
        if (validPieces.isEmpty()) {
            TerraformGeneratorPlugin.logger.error("Tried to query jigsaw type that doesn't exist: " + type);
            return null;
        }

        return validPieces.get(rand.nextInt(validPieces.size()));
    }

    public @Nullable JigsawStructurePiece getRelativePiece(@Nullable JigsawStructurePiece current,
                                                           JigsawType type,
                                                           @NotNull Random random)
    {
        if (current == null || (current.getAllowedPieces() != null && current.getAllowedPieces().length == 0)) {
            return getPiece(pieceRegistry, type, random);
        }
        else {
            return getPiece(current.getAllowedPieces(), type, random);
        }
    }

    public int getPieceWidth() {
        return pieceWidth;
    }

    public Wall getEntranceBlock() {
        return entranceBlock;
    }

    public @NotNull ArrayList<JigsawStructurePiece> getOverlapperPieces() {
        return overlapperPieces;
    }
}
