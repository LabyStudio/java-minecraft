package de.labystudio.game.world.chunk;

import de.labystudio.game.world.World;
import de.labystudio.game.world.Block;
import de.labystudio.game.world.WorldRenderer;
import org.lwjgl.opengl.GL11;

public class Chunk {
    public static final int SIZE = 16;

    public final World world;

    private final byte[] blocks = new byte[(SIZE * SIZE + SIZE) * SIZE + SIZE];
    private final byte[] lightDepths = new byte[(SIZE * SIZE + SIZE) * SIZE + SIZE];

    public int x;
    public int y;
    public int z;

    private boolean dirty = true;
    private int lists = -1;

    public Chunk(World world, int x, int y, int z) {
        this.world = world;

        this.x = x;
        this.y = y;
        this.z = z;

        this.lists = GL11.glGenLists(2);
    }

    private void rebuild(WorldRenderer renderer, int layer) {
        // No longer dirty
        this.dirty = false;

        // Create GPU memory list storage
        GL11.glNewList(this.lists + layer, GL11.GL_COMPILE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderer.textureId);

        // Start rendering
        renderer.tesselator.init();

        // Render blocks
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    short typeId = getBlockAt(x, y, z);

                    if (typeId != 0) {
                        int absoluteX = this.x * SIZE + x;
                        int absoluteY = this.y * SIZE + y;
                        int absoluteZ = this.z * SIZE + z;

                        Block.getById(typeId).render(renderer.tesselator, this.world, layer, absoluteX, absoluteY, absoluteZ);
                    }
                }
            }
        }

        // Stop rendering
        renderer.tesselator.flush();

        // End storafe
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEndList();
    }

    public boolean isSolidBlockAt(int x, int y, int z) {
        return getBlockAt(x, y, z) != 0;
    }

    public byte getBlockAt(int x, int y, int z) {
        int index = y << 8 | z << 4 | x;
        return this.blocks[index];
    }

    public void setBlockAt(int x, int y, int z, int type) {
        int index = y << 8 | z << 4 | x;
        this.blocks[index] = (byte) type;
    }

    public void render(WorldRenderer renderer, int layer) {
        if (this.dirty && this.world.rebuiltThisFrame) {
            this.world.updates += 1;
            this.world.rebuiltThisFrame = false;
            this.dirty = false;

            rebuild(renderer, 0);
            rebuild(renderer, 1);
        }
        GL11.glCallList(this.lists + layer);
    }

    public void setDirty() {
        this.dirty = true;
    }

    public void calcLightDepths(int minX, int minZ, int maxX, int maxZ) {
        for (int x = minX; x < minX + maxX; x++) {
            for (int z = minZ; z < minZ + maxZ; z++) {
                int y = World.TOTAL_HEIGHT - 1;

                while (y > 0 && !this.world.isSolidBlockAt(x, y, z)) {
                    y--;
                }

                int index = z << 4 | x;
                int oldDepth = this.lightDepths[index];
                this.lightDepths[index] = (byte) y;

                if (oldDepth != y) {
                    int minY = Math.min(oldDepth, y);
                    int maxY = Math.max(oldDepth, y);

                    this.world.lightColumnChanged(x, z, minY, maxY);
                }
            }
        }
    }

    public float getBrightnessAt(int x, int y, int z) {
        float dark = 0.8F;
        float light = 1.0F;

        if ((x < 0) || (y < 0) || (z < 0) || (x >= SIZE) || (y >= SIZE) || (z >= SIZE)) {
            return light;
        }

        int index = z << 4 | x;
        if (y < this.lightDepths[index]) {
            return dark;
        }

        return light;
    }
}
