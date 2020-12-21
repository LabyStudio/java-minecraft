package de.labystudio.game.render.world;

public interface IWorldAccess {

    short getBlockAt(int x, int y, int z);

    int getLightAt(int x, int y, int z);
}
