package de.labystudio.game.world.chunk;

import de.labystudio.game.util.Textures;
import de.labystudio.game.util.AABB;

import de.labystudio.game.world.World;
import de.labystudio.game.render.Tesselator;
import de.labystudio.game.world.Tile;
import org.lwjgl.opengl.GL11;

public class Chunk {
    private static int texture = Textures.loadTexture("/terrain.png", 9728);
    private static Tesselator tesselator = new Tesselator();

    public AABB aabb;
    public final World world;

    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;

    private boolean dirty = true;
    private int lists = -1;
    public static int rebuiltThisFrame = 0;
    public static int updates = 0;

    public Chunk(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.world = world;

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        this.aabb = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        this.lists = GL11.glGenLists(2);
    }

    private void rebuild(int layer) {
        if (rebuiltThisFrame == 2) {
            return;
        }
        this.dirty = false;

        updates += 1;
        rebuiltThisFrame += 1;

        GL11.glNewList(this.lists + layer, 4864);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(3553, texture);

        tesselator.init();

        int tiles = 0;
        for (int x = this.minX; x < this.maxX; x++) {
            for (int y = this.minY; y < this.maxY; y++) {
                for (int z = this.minZ; z < this.maxZ; z++) {
                    if (this.world.isTile(x, y, z)) {
                        int type = y == this.world.depth * 2 / 3 ? 0 : 1;

                        tiles++;

                        if (type == 0) {
                            Tile.rock.render(tesselator, this.world, layer, x, y, z);
                        } else {
                            Tile.grass.render(tesselator, this.world, layer, x, y, z);
                        }
                    }
                }
            }
        }

        tesselator.flush();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEndList();
    }

    public void render(int layer) {
        if (this.dirty) {
            rebuild(0);
            rebuild(1);
        }
        GL11.glCallList(this.lists + layer);
    }

    public void setDirty() {
        this.dirty = true;
    }
}
