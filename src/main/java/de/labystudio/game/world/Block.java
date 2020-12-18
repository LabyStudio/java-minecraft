package de.labystudio.game.world;

import de.labystudio.game.render.Tessellator;
import de.labystudio.game.util.EnumBlockFace;

import java.util.HashMap;
import java.util.Map;

public class Block {

    private static final Map<Short, Block> blocks = new HashMap<>();

    public static Block STONE = new Block((short) 1);
    public static Block GRASS = new Block((short) 2);

    private int id = 0;

    public static Block getById(short typeId) {
        return blocks.get(typeId);
    }

    private Block(short id) {
        this.id = id;
        blocks.put(id, this);
    }

    public int getId() {
        return id;
    }

    public void render(World world, int layer, int x, int y, int z) {
        Tessellator t = Tessellator.instance;

        float u0 = (this.id - 1) / 16.0F;
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
            float br = world.getBrightnessAtBlock(x, y - 1, z) * c1;
            if (((br == c1 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.setColorRGB_F(br, br, br);
                t.setTextureUV(u0, v1);
                t.addVertex(x0, y0, z1);
                t.setTextureUV(u0, v0);
                t.addVertex(x0, y0, z0);
                t.setTextureUV(u1, v0);
                t.addVertex(x1, y0, z0);

                t.setTextureUV(u1, v0);
                t.addVertex(x1, y0, z0);
                t.setTextureUV(u1, v1);
                t.addVertex(x1, y0, z1);
                t.setTextureUV(u0, v1);
                t.addVertex(x0, y0, z1);
            }
        }
        if (!world.isSolidBlockAt(x, y + 1, z)) {
            float br = world.getBrightnessAtBlock(x, y, z) * c1;
            if (((br == c1 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.setColorRGB_F(br, br, br);
                t.setTextureUV(u1, v1);
                t.addVertex(x1, y1, z1);
                t.setTextureUV(u1, v0);
                t.addVertex(x1, y1, z0);
                t.setTextureUV(u0, v0);
                t.addVertex(x0, y1, z0);

                t.setTextureUV(u0, v0);
                t.addVertex(x0, y1, z0);
                t.setTextureUV(u0, v1);
                t.addVertex(x0, y1, z1);
                t.setTextureUV(u1, v1);
                t.addVertex(x1, y1, z1);
            }
        }
        if (!world.isSolidBlockAt(x, y, z - 1)) {
            float br = world.getBrightnessAtBlock(x, y, z - 1) * c2;
            if (((br == c2 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.setColorRGB_F(br, br, br);
                t.setTextureUV(u1, v0);
                t.addVertex(x0, y1, z0);
                t.setTextureUV(u0, v0);
                t.addVertex(x1, y1, z0);
                t.setTextureUV(u0, v1);
                t.addVertex(x1, y0, z0);

                t.setTextureUV(u0, v1);
                t.addVertex(x1, y0, z0);
                t.setTextureUV(u1, v1);
                t.addVertex(x0, y0, z0);
                t.setTextureUV(u1, v0);
                t.addVertex(x0, y1, z0);
            }
        }
        if (!world.isSolidBlockAt(x, y, z + 1)) {
            float br = world.getBrightnessAtBlock(x, y, z + 1) * c2;
            if (((br == c2 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.setColorRGB_F(br, br, br);
                t.setTextureUV(u0, v0);
                t.addVertex(x0, y1, z1);
                t.setTextureUV(u0, v1);
                t.addVertex(x0, y0, z1);
                t.setTextureUV(u1, v1);
                t.addVertex(x1, y0, z1);

                t.setTextureUV(u1, v1);
                t.addVertex(x1, y0, z1);
                t.setTextureUV(u1, v0);
                t.addVertex(x1, y1, z1);
                t.setTextureUV(u0, v0);
                t.addVertex(x0, y1, z1);
            }
        }
        if (!world.isSolidBlockAt(x - 1, y, z)) {
            float br = world.getBrightnessAtBlock(x - 1, y, z) * c3;
            if (((br == c3 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.setColorRGB_F(br, br, br);
                t.setTextureUV(u1, v0);
                t.addVertex(x0, y1, z1);
                t.setTextureUV(u0, v0);
                t.addVertex(x0, y1, z0);
                t.setTextureUV(u0, v1);
                t.addVertex(x0, y0, z0);

                t.setTextureUV(u0, v1);
                t.addVertex(x0, y0, z0);
                t.setTextureUV(u1, v1);
                t.addVertex(x0, y0, z1);
                t.setTextureUV(u1, v0);
                t.addVertex(x0, y1, z1);
            }
        }
        if (!world.isSolidBlockAt(x + 1, y, z)) {
            float br = world.getBrightnessAtBlock(x + 1, y, z) * c3;
            if (((br == c3 ? 1 : 0) ^ (layer == 1 ? 1 : 0)) != 0) {
                t.setColorRGB_F(br, br, br);
                t.setTextureUV(u0, v1);
                t.addVertex(x1, y0, z1);
                t.setTextureUV(u1, v1);
                t.addVertex(x1, y0, z0);
                t.setTextureUV(u1, v0);
                t.addVertex(x1, y1, z0);

                t.setTextureUV(u1, v0);
                t.addVertex(x1, y1, z0);
                t.setTextureUV(u0, v0);
                t.addVertex(x1, y1, z1);
                t.setTextureUV(u0, v1);
                t.addVertex(x1, y0, z1);
            }
        }
    }

    public void renderFace(int x, int y, int z, EnumBlockFace face) {
        Tessellator t = Tessellator.instance;

        float x0 = x + 0.0F;
        float x1 = x + 1.0F;
        float y0 = y + 0.0F;
        float y1 = y + 1.0F;
        float z0 = z + 0.0F;
        float z1 = z + 1.0F;
        if (face == EnumBlockFace.BOTTOM) {
            t.addVertex(x0, y0, z1);
            t.addVertex(x0, y0, z0);
            t.addVertex(x1, y0, z0);

            t.addVertex(x1, y0, z0);
            t.addVertex(x1, y0, z1);
            t.addVertex(x0, y0, z1);
        }
        if (face == EnumBlockFace.TOP) {
            t.addVertex(x1, y1, z1);
            t.addVertex(x1, y1, z0);
            t.addVertex(x0, y1, z0);

            t.addVertex(x0, y1, z0);
            t.addVertex(x0, y1, z1);
            t.addVertex(x1, y1, z1);
        }
        if (face == EnumBlockFace.EAST) {
            t.addVertex(x0, y1, z0);
            t.addVertex(x1, y1, z0);
            t.addVertex(x1, y0, z0);

            t.addVertex(x1, y0, z0);
            t.addVertex(x0, y0, z0);
            t.addVertex(x0, y1, z0);
        }
        if (face == EnumBlockFace.WEST) {
            t.addVertex(x0, y1, z1);
            t.addVertex(x0, y0, z1);
            t.addVertex(x1, y0, z1);

            t.addVertex(x1, y0, z1);
            t.addVertex(x1, y1, z1);
            t.addVertex(x0, y1, z1);
        }
        if (face == EnumBlockFace.NORTH) {
            t.addVertex(x0, y1, z1);
            t.addVertex(x0, y1, z0);
            t.addVertex(x0, y0, z0);

            t.addVertex(x0, y0, z0);
            t.addVertex(x0, y0, z1);
            t.addVertex(x0, y1, z1);
        }
        if (face == EnumBlockFace.SOUTH) {
            t.addVertex(x1, y0, z1);
            t.addVertex(x1, y0, z0);
            t.addVertex(x1, y1, z0);

            t.addVertex(x1, y1, z0);
            t.addVertex(x1, y1, z1);
            t.addVertex(x1, y0, z1);
        }
    }
}
