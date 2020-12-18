package de.labystudio.game.world;

import de.labystudio.game.player.Player;

import de.labystudio.game.render.Frustum;
import de.labystudio.game.render.Tesselator;
import de.labystudio.game.world.chunk.Chunk;

public class WorldRenderer implements WorldListener {
    private static final int CHUNK_SIZE = 16;
    private World world;
    private Chunk[] chunks;
    private int xChunks;
    private int yChunks;
    private int zChunks;

    private Tesselator tesselator = new Tesselator();

    public WorldRenderer(World world) {
        this.world = world;
        world.addListener(this);

        this.xChunks = (world.width / CHUNK_SIZE);
        this.yChunks = (world.depth / CHUNK_SIZE);
        this.zChunks = (world.height / CHUNK_SIZE);

        this.chunks = new Chunk[this.xChunks * this.yChunks * this.zChunks];
        for (int x = 0; x < this.xChunks; x++) {
            for (int y = 0; y < this.yChunks; y++) {
                for (int z = 0; z < this.zChunks; z++) {
                    int x0 = x * CHUNK_SIZE;
                    int y0 = y * CHUNK_SIZE;
                    int z0 = z * CHUNK_SIZE;
                    int x1 = (x + 1) * CHUNK_SIZE;
                    int y1 = (y + 1) * CHUNK_SIZE;
                    int z1 = (z + 1) * CHUNK_SIZE;
                    if (x1 > world.width) {
                        x1 = world.width;
                    }
                    if (y1 > world.depth) {
                        y1 = world.depth;
                    }
                    if (z1 > world.height) {
                        z1 = world.height;
                    }
                    this.chunks[((x + y * this.xChunks) * this.zChunks + z)] = new Chunk(world, x0, y0, z0, x1, y1, z1);
                }
            }
        }
    }

    public void render(Player player, int layer) {
        Chunk.rebuiltThisFrame = 0;
        Frustum frustum = Frustum.getFrustum();
        for (int i = 0; i < this.chunks.length; i++) {
            if (frustum.cubeInFrustum(this.chunks[i].aabb)) {
                this.chunks[i].render(layer);
            }
        }
    }

    public void setDirty(int x0, int y0, int z0, int x1, int y1, int z1) {
        x0 /= 16;
        x1 /= 16;
        y0 /= 16;
        y1 /= 16;
        z0 /= 16;
        z1 /= 16;
        if (x0 < 0) {
            x0 = 0;
        }
        if (y0 < 0) {
            y0 = 0;
        }
        if (z0 < 0) {
            z0 = 0;
        }
        if (x1 >= this.xChunks) {
            x1 = this.xChunks - 1;
        }
        if (y1 >= this.yChunks) {
            y1 = this.yChunks - 1;
        }
        if (z1 >= this.zChunks) {
            z1 = this.zChunks - 1;
        }
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    this.chunks[((x + y * this.xChunks) * this.zChunks + z)].setDirty();
                }
            }
        }
    }

    public void tileChanged(int x, int y, int z) {
        setDirty(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    public void lightColumnChanged(int x, int z, int y0, int y1) {
        setDirty(x - 1, y0 - 1, z - 1, x + 1, y1 + 1, z + 1);
    }

    public void allChanged() {
        setDirty(0, 0, 0, this.world.width, this.world.depth, this.world.height);
    }
}
