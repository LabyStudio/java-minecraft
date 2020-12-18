package de.labystudio.game.world.chunk;

import de.labystudio.game.world.World;
import de.labystudio.game.world.Tile;
import de.labystudio.game.world.WorldRenderer;
import org.lwjgl.opengl.GL11;

public class Chunk {
    public static final int SIZE = 16;

    public final World world;

    private byte[] blocks = new byte[(SIZE * SIZE + SIZE) * SIZE + SIZE];
    // private int[] lightDepths;

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
        GL11.glNewList(this.lists + layer, 4864);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(3553, renderer.textureId);

        // Start rendering
        renderer.tesselator.init();

        // Render blocks
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                for (int z = 0; z < SIZE; z++) {
                    if (isSolidBlockAt(x, y, z)) {
                        int absoluteX = this.x * SIZE + x;
                        int absoluteY = this.y * SIZE + y;
                        int absoluteZ = this.z * SIZE + z;

                        Tile.grass.render(renderer.tesselator, this.world, layer, absoluteX, absoluteY, absoluteZ);
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
        if (this.dirty && this.world.rebuiltThisFrame != 2) {
            this.world.updates += 2;
            this.world.rebuiltThisFrame += 2;
            this.dirty = false;

            rebuild(renderer, 0);
            rebuild(renderer, 1);
        }
        GL11.glCallList(this.lists + layer);
    }

    public void setDirty() {
        this.dirty = true;
    }
}
