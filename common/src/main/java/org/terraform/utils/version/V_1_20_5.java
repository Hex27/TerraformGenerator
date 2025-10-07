package org.terraform.utils.version;

import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.EntityType;

public class V_1_20_5 {
    public static final PatternType RHOMBUS = !Version.VERSION.isAtLeast(Version.v1_20_5)
                                              ? PatternType.valueOf("RHOMBUS_MIDDLE")
                                              : PatternType.valueOf("RHOMBUS");
    public static final PatternType CIRCLE = !Version.VERSION.isAtLeast(Version.v1_20_5)
                                             ? PatternType.valueOf("CIRCLE_MIDDLE")
                                             : PatternType.valueOf("CIRCLE");

    public static final EntityType ARMADILLO = !Version.VERSION.isAtLeast(Version.v1_20_5)
                                               ? EntityType.PIG
                                               : EntityType.valueOf("ARMADILLO");
}
