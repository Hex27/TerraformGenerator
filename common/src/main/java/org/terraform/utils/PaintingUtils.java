package org.terraform.utils;

import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;

import java.util.Random;

public class PaintingUtils {

    public static void placePainting(@NotNull SimpleBlock target, @NotNull BlockFace facing, @NotNull Art art) {
        if (target.getPopData() instanceof PopulatorDataPostGen) {
            try {
                PopulatorDataPostGen postGen = ((PopulatorDataPostGen) target.getPopData());
                Painting painting = (Painting) postGen.getWorld()
                                                      .spawnEntity(new Location(postGen.getWorld(),
                                                              target.getX(),
                                                              target.getY(),
                                                              target.getZ()
                                                      ), EntityType.PAINTING);
                painting.setFacingDirection(facing);
                painting.setPersistent(true);
                painting.setArt(art);
            }
            catch (IllegalArgumentException e) {
                // TerraformGeneratorPlugin.TerraformGeneratorPlugin.logger.stackTrace(e);
                // Area is occupied by another entity. Fail silently.
            }
        }
    }

    public static @Nullable Art getArtFromDimensions(@NotNull Random rand, int sizeHor, int sizeVert) {
        Art[] candidates = null;
        if (sizeHor == 1 && sizeVert == 1) {
            candidates = new Art[] {
                    Art.ALBAN, Art.AZTEC, Art.AZTEC2, Art.BOMB, Art.KEBAB, Art.PLANT, Art.WASTELAND
            };
        }
        else if (sizeHor == 2 && sizeVert == 1) {
            candidates = new Art[] {
                    Art.COURBET, Art.POOL, Art.SEA, Art.CREEBET, Art.SUNSET
            };
        }
        else if (sizeHor == 1 && sizeVert == 2) {
            candidates = new Art[] {
                    Art.GRAHAM, Art.WANDERER
            };
        }
        else if (sizeHor == 2 && sizeVert == 2) {
            candidates = new Art[] {
                    Art.BUST, Art.MATCH, Art.SKULL_AND_ROSES, Art.STAGE, Art.VOID, Art.WITHER
            };
        }
        else if (sizeHor == 4 && sizeVert == 2) {
            candidates = new Art[] {
                    Art.FIGHTERS
            };
        }
        else if (sizeHor == 4 && sizeVert == 3) {
            candidates = new Art[] {
                    Art.DONKEY_KONG, Art.SKELETON
            };
        }
        else if (sizeHor == 4 && sizeVert == 4) {
            candidates = new Art[] {
                    Art.BURNING_SKULL, Art.PIGSCENE, Art.POINTER
            };
        }
        if (candidates == null) {
            return null;
        }
        return candidates[rand.nextInt(candidates.length)];
    }

}
