package de.labystudio.game.util;

public class HitResult {
    public int x;
    public int y;
    public int z;
    public EnumBlockFace face;

    public HitResult(int x, int y, int z, EnumBlockFace face) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
    }
}
