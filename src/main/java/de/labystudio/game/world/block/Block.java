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
        for (EnumBlockFace face : EnumBlockFace.values()) {
            if (!world.isSolidBlockAt(x + face.x, y + face.y, z + face.z)) {
                // Render face
                renderFace(tessellator, world, face, x, y, z);
            }
        }
    }

    public int getAverageLightLevelAt(World world, int x, int y, int z) {
        int totalLightLevel = 0;
        int totalBlocks = 0;

        // For all blocks around this corner
        for (int offsetX = -1; offsetX <= 0; offsetX++) {
            for (int offsetY = -1; offsetY <= 0; offsetY++) {
                for (int offsetZ = -1; offsetZ <= 0; offsetZ++) {

                    // Does it contain air?
                    if (!world.isSolidBlockAt(x + offsetX, y + offsetY, z + offsetZ)) {

                        // Sum up the light levels
                        totalLightLevel += world.getLightAt(x + offsetX, y + offsetY, z + offsetZ);
                        totalBlocks++;
                    }
                }
            }
        }

        // Calculate the average light level of all surrounding blocks
        return totalBlocks == 0 ? 0 : totalLightLevel / totalBlocks;
    }

    public void setAverageColor(Tessellator tessellator, World world, EnumBlockFace face, int x, int y, int z) {
        // Get the average light level of all 4 blocks at this corner
        int lightLevelAtThisCorner = getAverageLightLevelAt(world, x, y, z);

        // Convert light level from [0 - 15] to [0.1 - 1.0]
        float brightness = 0.9F / 15.0F * lightLevelAtThisCorner + 0.1F;
        float color = brightness * face.getShading();

        // Set color with shading
        tessellator.setColorRGB_F(color, color, color);
    }

    public void addBlockCorner(Tessellator tessellator, World world, EnumBlockFace face, int x, int y, int z, float u, float v) {
        setAverageColor(tessellator, world, face, x, y, z);
        tessellator.addVertexWithUV(x, y, z, u, v);
    }

    public void renderFace(Tessellator tessellator, World world, EnumBlockFace face, int minX, int minY, int minZ) {
        // Vertex mappings
        int maxX = minX + 1;
        int maxY = minY + 1;
        int maxZ = minZ + 1;

        // UV Mapping
        int textureIndex = getTextureForFace(face);
        float minU = (textureIndex % 16) / 16.0F;
        float maxU = minU + (16 / 256F);
        float minV = (float) (textureIndex / 16);
        float maxV = minV + (16 / 256F);

        if (face == EnumBlockFace.BOTTOM) {
            addBlockCorner(tessellator, world, face, minX, minY, maxZ, minU, maxV);
            addBlockCorner(tessellator, world, face, minX, minY, minZ, minU, minV);
            addBlockCorner(tessellator, world, face, maxX, minY, minZ, maxU, minV);
            addBlockCorner(tessellator, world, face, maxX, minY, maxZ, maxU, maxV);
        }
        if (face == EnumBlockFace.TOP) {
            addBlockCorner(tessellator, world, face, maxX, maxY, maxZ, maxU, maxV);
            addBlockCorner(tessellator, world, face, maxX, maxY, minZ, maxU, minV);
            addBlockCorner(tessellator, world, face, minX, maxY, minZ, minU, minV);
            addBlockCorner(tessellator, world, face, minX, maxY, maxZ, minU, maxV);
        }
        if (face == EnumBlockFace.EAST) {
            addBlockCorner(tessellator, world, face, minX, maxY, minZ, maxU, minV);
            addBlockCorner(tessellator, world, face, maxX, maxY, minZ, minU, minV);
            addBlockCorner(tessellator, world, face, maxX, minY, minZ, minU, maxV);
            addBlockCorner(tessellator, world, face, minX, minY, minZ, maxU, maxV);
        }
        if (face == EnumBlockFace.WEST) {
            addBlockCorner(tessellator, world, face, minX, maxY, maxZ, minU, minV);
            addBlockCorner(tessellator, world, face, minX, minY, maxZ, minU, maxV);
            addBlockCorner(tessellator, world, face, maxX, minY, maxZ, maxU, maxV);
            addBlockCorner(tessellator, world, face, maxX, maxY, maxZ, maxU, minV);
        }
        if (face == EnumBlockFace.NORTH) {
            addBlockCorner(tessellator, world, face, minX, maxY, maxZ, maxU, minV);
            addBlockCorner(tessellator, world, face, minX, maxY, minZ, minU, minV);
            addBlockCorner(tessellator, world, face, minX, minY, minZ, minU, maxV);
            addBlockCorner(tessellator, world, face, minX, minY, maxZ, maxU, maxV);
        }
        if (face == EnumBlockFace.SOUTH) {
            addBlockCorner(tessellator, world, face, maxX, minY, maxZ, minU, maxV);
            addBlockCorner(tessellator, world, face, maxX, minY, minZ, maxU, maxV);
            addBlockCorner(tessellator, world, face, maxX, maxY, minZ, maxU, minV);
            addBlockCorner(tessellator, world, face, maxX, maxY, maxZ, minU, minV);
        }
    }
}
