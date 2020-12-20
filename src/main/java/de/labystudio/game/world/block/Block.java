package de.labystudio.game.world.block;

import de.labystudio.game.render.Tessellator;
import de.labystudio.game.util.EnumBlockFace;
import de.labystudio.game.world.World;

import java.util.HashMap;
import java.util.Map;

public abstract class Block {

    private static final Map<Short, Block> blocks = new HashMap<>();

    public static Block STONE = new BlockStone(1, 0);
    public static Block GRASS = new BlockGrass(2, 1);

    protected final int id;
    protected final int textureSlotId;

    protected Block(int id) {
        this(id, id);
    }

    protected Block(int id, int textureSlotId) {
        this.id = id;
        this.textureSlotId = textureSlotId;
        blocks.put((short) id, this);
    }

    public static Block getById(short typeId) {
        return blocks.get(typeId);
    }

    public int getId() {
        return id;
    }

    protected int getTextureForFace(EnumBlockFace face) {
        return this.textureSlotId;
    }

    public void render(Tessellator tessellator, World world, int x, int y, int z) {
        float color1 = 1.0F;
        float color2 = 0.8F;
        float color3 = 0.6F;

        for (EnumBlockFace face : EnumBlockFace.values()) {
            if (!world.isSolidBlockAt(x + face.x, y + face.y, z + face.z)) {
                float brightness = 1.0F / 15.0F * world.getLightAt(x + face.x, y + face.y, z + face.z);
                renderFace(tessellator, face, x, y, z, color1 * brightness, color2 * brightness, color3 * brightness);
            }
        }
    }

    public void renderFace(Tessellator tessellator,  EnumBlockFace face, int x, int y, int z, float color1, float color2, float color3) {
        // Vertex mappings
        float minX = x + 0.0F;
        float maxX = x + 1.0F;
        float minY = y + 0.0F;
        float maxY = y + 1.0F;
        float minZ = z + 0.0F;
        float maxZ = z + 1.0F;

        // UV Mapping
        int textureIndex = getTextureForFace(face);
        float minU = (textureIndex % 16) / 16.0F;
        float maxU = minU + (16 / 256F);
        float minV = (float) (textureIndex / 16);
        float maxV = minV + (16 / 256F);

        if (face == EnumBlockFace.BOTTOM) {
            tessellator.setColorRGB_F(color1, color1, color1);
            tessellator.addVertexWithUV(minX, minY, maxZ, minU, maxV);
            tessellator.addVertexWithUV(minX, minY, minZ, minU, minV);
            tessellator.addVertexWithUV(maxX, minY, minZ, maxU, minV);

            tessellator.addVertexWithUV(maxX, minY, minZ, maxU, minV);
            tessellator.addVertexWithUV(maxX, minY, maxZ, maxU, maxV);
            tessellator.addVertexWithUV(minX, minY, maxZ, minU, maxV);
        }
        if (face == EnumBlockFace.TOP) {
            tessellator.setColorRGB_F(color1, color1, color1);
            tessellator.addVertexWithUV(maxX, maxY, maxZ, maxU, maxV);
            tessellator.addVertexWithUV(maxX, maxY, minZ, maxU, minV);
            tessellator.addVertexWithUV(minX, maxY, minZ, minU, minV);

            tessellator.addVertexWithUV(minX, maxY, minZ, minU, minV);
            tessellator.addVertexWithUV(minX, maxY, maxZ, minU, maxV);
            tessellator.addVertexWithUV(maxX, maxY, maxZ, maxU, maxV);
        }
        if (face == EnumBlockFace.EAST) {
            tessellator.setColorRGB_F(color2, color2, color2);
            tessellator.addVertexWithUV(minX, maxY, minZ, maxU, minV);
            tessellator.addVertexWithUV(maxX, maxY, minZ, minU, minV);
            tessellator.addVertexWithUV(maxX, minY, minZ, minU, maxV);

            tessellator.addVertexWithUV(maxX, minY, minZ, minU, maxV);
            tessellator.addVertexWithUV(minX, minY, minZ, maxU, maxV);
            tessellator.addVertexWithUV(minX, maxY, minZ, maxU, minV);
        }
        if (face == EnumBlockFace.WEST) {
            tessellator.setColorRGB_F(color2, color2, color2);
            tessellator.addVertexWithUV(minX, maxY, maxZ, minU, minV);
            tessellator.addVertexWithUV(minX, minY, maxZ, minU, maxV);
            tessellator.addVertexWithUV(maxX, minY, maxZ, maxU, maxV);

            tessellator.addVertexWithUV(maxX, minY, maxZ, maxU, maxV);
            tessellator.addVertexWithUV(maxX, maxY, maxZ, maxU, minV);
            tessellator.addVertexWithUV(minX, maxY, maxZ, minU, minV);
        }
        if (face == EnumBlockFace.NORTH) {
            tessellator.setColorRGB_F(color3, color3, color3);
            tessellator.addVertexWithUV(minX, maxY, maxZ, maxU, minV);
            tessellator.addVertexWithUV(minX, maxY, minZ, minU, minV);
            tessellator.addVertexWithUV(minX, minY, minZ, minU, maxV);

            tessellator.addVertexWithUV(minX, minY, minZ, minU, maxV);
            tessellator.addVertexWithUV(minX, minY, maxZ, maxU, maxV);
            tessellator.addVertexWithUV(minX, maxY, maxZ, maxU, minV);
        }
        if (face == EnumBlockFace.SOUTH) {
            tessellator.setColorRGB_F(color3, color3, color3);
            tessellator.addVertexWithUV(maxX, minY, maxZ, minU, maxV);
            tessellator.addVertexWithUV(maxX, minY, minZ, maxU, maxV);
            tessellator.addVertexWithUV(maxX, maxY, minZ, maxU, minV);

            tessellator.addVertexWithUV(maxX, maxY, minZ, maxU, minV);
            tessellator.addVertexWithUV(maxX, maxY, maxZ, minU, minV);
            tessellator.addVertexWithUV(maxX, minY, maxZ, minU, maxV);
        }
    }
}
