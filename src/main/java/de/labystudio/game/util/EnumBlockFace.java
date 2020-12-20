package de.labystudio.game.util;

public enum EnumBlockFace {
    TOP(0, 1, 0),
    BOTTOM(0, -1, 0),
    NORTH(-1, 0, 0),
    EAST(0, 0, -1),
    SOUTH(1, 0, 0),
    WEST(0, 0, 1);

    public int x;
    public int y;
    public int z;

    EnumBlockFace(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getShading() {
        return isXAxis() ? 0.6F : isYAxis() ? 1.0F : 0.8F;
    }

    public boolean isXAxis() {
        return this.x != 0;
    }

    public boolean isYAxis() {
        return this.y != 0;
    }

    public boolean isZAxis() {
        return this.z != 0;
    }
}
