package de.labystudio.game.util;

public enum EnumBlockFace {
    TOP(0, -1, 0),
    BOTTOM(0, 1, 0),
    NORTH(1, 0, 0),
    EAST(0, 0, 1),
    SOUTH(-1, 0, 0),
    WEST(0, 0, -1);

    public int x;
    public int y;
    public int z;

    EnumBlockFace(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
