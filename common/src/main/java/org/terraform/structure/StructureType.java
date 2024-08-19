package org.terraform.structure;

public enum StructureType {
    VILLAGE, // Any village related structures.
    MEGA_DUNGEON, // Strongholds, Pyramids, Monuments, Mansions etc. No overlap allowed.
    LARGE_CAVE, // Overlap generally allowed
    SMALL, // Shipwrecks, Witch huts, Desert Wells. Overlap generally allowed.
    LARGE_MISC // Mineshafts fall under here for now.

    /*
      Structures of the same type cannot overlap.
      Structure type will also be used in the Structure Registry to determine whether
      certain things should spawn in the same megachunk.
     */
}
