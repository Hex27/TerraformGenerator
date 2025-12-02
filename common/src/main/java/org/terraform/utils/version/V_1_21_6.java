package org.terraform.utils.version;

import org.bukkit.Material;

public class V_1_21_6 {
    public static Material IRON_CHAIN = Version.VERSION.isAtLeast(Version.v1_21_9) ?
                                  Material.valueOf("IRON_CHAIN") : Material.CHAIN;

}
