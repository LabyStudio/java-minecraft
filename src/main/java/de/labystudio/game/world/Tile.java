package de.labystudio.game.world;

import de.labystudio.game.render.Tesselator;
import de.labystudio.game.util.EnumBlockFace;

public class Tile {
    public static Tile rock = new Tile(0);
    public static Tile grass = new Tile(1);
    private int tex = 0;

    private Tile(int tex) {
        this.tex = tex;
    }

    public void render(Tesselator t, World world, int layer, int x, int y, int z) {
        float u0 = this.tex / 16.0F;
        float u1 = u0 + 0.0624375F;
        float v0 = 0.0F;
        float v1 = v0 + 0.0624375F;
        float c1 = 1.0F;
        float c2 = 0.8F;
        float c3 = 0.6F;

        float x0 = x + 0.0F;
        float x1 = x + 1.0F;
        float y0 = y + 0.0F;
        float y1 = y + 1.0F;
        float z0 = z + 0.0F;
        float z1 = z + 1.0F;
        if (!world.isSolidBlockAt(x, y - 1, z)) {
            float br = world.getBrightness(x, y - 1, z) * c1;
            if (((br == c1 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.color(br, br, br);
                t.tex(u0, v1);
                t.vertex(x0, y0, z1);
                t.tex(u0, v0);
                t.vertex(x0, y0, z0);
                t.tex(u1, v0);
                t.vertex(x1, y0, z0);

                t.tex(u1, v0);
                t.vertex(x1, y0, z0);
                t.tex(u1, v1);
                t.vertex(x1, y0, z1);
                t.tex(u0, v1);
                t.vertex(x0, y0, z1);
            }
        }
        if (!world.isSolidBlockAt(x, y + 1, z)) {
            float br = world.getBrightness(x, y, z) * c1;
            if (((br == c1 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.color(br, br, br);
                t.tex(u1, v1);
                t.vertex(x1, y1, z1);
                t.tex(u1, v0);
                t.vertex(x1, y1, z0);
                t.tex(u0, v0);
                t.vertex(x0, y1, z0);

                t.tex(u0, v0);
                t.vertex(x0, y1, z0);
                t.tex(u0, v1);
                t.vertex(x0, y1, z1);
                t.tex(u1, v1);
                t.vertex(x1, y1, z1);
            }
        }
        if (!world.isSolidBlockAt(x, y, z - 1)) {
            float br = world.getBrightness(x, y, z - 1) * c2;
            if (((br == c2 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.color(br, br, br);
                t.tex(u1, v0);
                t.vertex(x0, y1, z0);
                t.tex(u0, v0);
                t.vertex(x1, y1, z0);
                t.tex(u0, v1);
                t.vertex(x1, y0, z0);

                t.tex(u0, v1);
                t.vertex(x1, y0, z0);
                t.tex(u1, v1);
                t.vertex(x0, y0, z0);
                t.tex(u1, v0);
                t.vertex(x0, y1, z0);
            }
        }
        if (!world.isSolidBlockAt(x, y, z + 1)) {
            float br = world.getBrightness(x, y, z + 1) * c2;
            if (((br == c2 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.color(br, br, br);
                t.tex(u0, v0);
                t.vertex(x0, y1, z1);
                t.tex(u0, v1);
                t.vertex(x0, y0, z1);
                t.tex(u1, v1);
                t.vertex(x1, y0, z1);

                t.tex(u1, v1);
                t.vertex(x1, y0, z1);
                t.tex(u1, v0);
                t.vertex(x1, y1, z1);
                t.tex(u0, v0);
                t.vertex(x0, y1, z1);
            }
        }
        if (!world.isSolidBlockAt(x - 1, y, z)) {
            float br = world.getBrightness(x - 1, y, z) * c3;
            if (((br == c3 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.color(br, br, br);
                t.tex(u1, v0);
                t.vertex(x0, y1, z1);
                t.tex(u0, v0);
                t.vertex(x0, y1, z0);
                t.tex(u0, v1);
                t.vertex(x0, y0, z0);

                t.tex(u0, v1);
                t.vertex(x0, y0, z0);
                t.tex(u1, v1);
                t.vertex(x0, y0, z1);
                t.tex(u1, v0);
                t.vertex(x0, y1, z1);
            }
        }
        if (!world.isSolidBlockAt(x + 1, y, z)) {
            float br = world.getBrightness(x + 1, y, z) * c3;
            if (((br == c3 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.color(br, br, br);
                t.tex(u0, v1);
                t.vertex(x1, y0, z1);
                t.tex(u1, v1);
                t.vertex(x1, y0, z0);
                t.tex(u1, v0);
                t.vertex(x1, y1, z0);

                t.tex(u1, v0);
                t.vertex(x1, y1, z0);
                t.tex(u0, v0);
                t.vertex(x1, y1, z1);
                t.tex(u0, v1);
                t.vertex(x1, y0, z1);
            }
        }
    }

    public void renderFace(Tesselator tesselator, int x, int y, int z, EnumBlockFace face) {
        float x0 = x + 0.0F;
        float x1 = x + 1.0F;
        float y0 = y + 0.0F;
        float y1 = y + 1.0F;
        float z0 = z + 0.0F;
        float z1 = z + 1.0F;
        if (face == EnumBlockFace.BOTTOM) {
            tesselator.vertex(x0, y0, z1);
            tesselator.vertex(x0, y0, z0);
            tesselator.vertex(x1, y0, z0);

            tesselator.vertex(x1, y0, z0);
            tesselator.vertex(x1, y0, z1);
            tesselator.vertex(x0, y0, z1);
        }
        if (face == EnumBlockFace.TOP) {
            tesselator.vertex(x1, y1, z1);
            tesselator.vertex(x1, y1, z0);
            tesselator.vertex(x0, y1, z0);

            tesselator.vertex(x0, y1, z0);
            tesselator.vertex(x0, y1, z1);
            tesselator.vertex(x1, y1, z1);
        }
        if (face == EnumBlockFace.EAST) {
            tesselator.vertex(x0, y1, z0);
            tesselator.vertex(x1, y1, z0);
            tesselator.vertex(x1, y0, z0);

            tesselator.vertex(x1, y0, z0);
            tesselator.vertex(x0, y0, z0);
            tesselator.vertex(x0, y1, z0);
        }
        if (face == EnumBlockFace.WEST) {
            tesselator.vertex(x0, y1, z1);
            tesselator.vertex(x0, y0, z1);
            tesselator.vertex(x1, y0, z1);

            tesselator.vertex(x1, y0, z1);
            tesselator.vertex(x1, y1, z1);
            tesselator.vertex(x0, y1, z1);
        }
        if (face == EnumBlockFace.NORTH) {
            tesselator.vertex(x0, y1, z1);
            tesselator.vertex(x0, y1, z0);
            tesselator.vertex(x0, y0, z0);

            tesselator.vertex(x0, y0, z0);
            tesselator.vertex(x0, y0, z1);
            tesselator.vertex(x0, y1, z1);
        }
        if (face == EnumBlockFace.SOUTH) {
            tesselator.vertex(x1, y0, z1);
            tesselator.vertex(x1, y0, z0);
            tesselator.vertex(x1, y1, z0);

            tesselator.vertex(x1, y1, z0);
            tesselator.vertex(x1, y1, z1);
            tesselator.vertex(x1, y0, z1);
        }
    }
}
