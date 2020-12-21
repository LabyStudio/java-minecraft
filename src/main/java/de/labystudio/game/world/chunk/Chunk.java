package de.labystudio.game.world.chunk;

import de.labystudio.game.world.World;
import de.labystudio.game.world.WorldRenderer;

public class Chunk {

    private World world;
    private ChunkSection[] sections;

    private final int x;
    private final int z;

    public Chunk(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;

        this.sections = new ChunkSection[16];
        for (int y = 0; y < 16; y++) {
            this.sections[y] = new ChunkSection(world, x, y, z);
        }
    }

    public ChunkSection getSection(int y) {
        return this.sections[y];
    }

    public ChunkSection[] getSections() {
        return this.sections;
    }

    public void setSections(ChunkSection[] sections) {
        this.sections = sections;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isEmpty() {
        for (ChunkSection chunkSection : this.sections) {
            if (!chunkSection.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void rebuild(WorldRenderer renderer) {
        for (ChunkSection chunkSection : this.sections) {
            chunkSection.rebuild(renderer);
        }
    }

    public void queueForRebuild() {
        for (ChunkSection chunkSection : this.sections) {
            chunkSection.queueForRebuild();
        }
    }

    public static long getIndex(int x, int z) {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }
}
