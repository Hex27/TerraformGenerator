package org.terraform.data;

public class MegaChunkKey {

    private final TerraformWorld tw;

    private final MegaChunk mc;
    public MegaChunkKey(TerraformWorld tw, MegaChunk mc) {
        this.tw = tw;
        this.mc = mc;
    }
    public TerraformWorld getTw() {
        return tw;
    }

    public MegaChunk getMc() {
        return mc;
    }

    @Override
    public int hashCode() {
        return ((tw.hashCode() + 28661*mc.getX())*34171 + mc.getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MegaChunkKey other)) return false;
        return this.tw.hashCode() == other.tw.hashCode()
                && mc.getX() == other.mc.getX()
                && mc.getZ() == other.mc.getZ();
    }
}
