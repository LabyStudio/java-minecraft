package de.labystudio.game.world.chunk;

import de.labystudio.game.world.World;

public class ChunkLayers {

    private Chunk[] layers;

    private final int x;
    private final int z;

    public ChunkLayers(World world, int x, int z) {
        this.x = x;
        this.z = z;

        this.layers = new Chunk[16];
        for (int y = 0; y < 16; y++) {
            this.layers[y] = new Chunk(world, x, y, z);
        }
    }

    public Chunk getLayer(int y) {
        return this.layers[y];
    }

    public Chunk[] getLayers() {
        return this.layers;
    }

    public void setLayers(Chunk[] layers) {
        this.layers = layers;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isEmpty() {
        for (Chunk chunk : this.layers) {
            if (!chunk.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void setDirty() {
        for (Chunk chunk : this.layers) {
            chunk.setDirty();
        }
    }
}
