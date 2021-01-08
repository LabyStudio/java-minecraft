package de.labystudio.game.render.world;

import de.labystudio.game.render.Tessellator;
import de.labystudio.game.util.BoundingBox;
import de.labystudio.game.util.EnumBlockFace;
import de.labystudio.game.world.WorldRenderer;
import de.labystudio.game.world.block.Block;
import org.lwjgl.opengl.GL11;

public class BlockRenderer {

    public static final boolean CLASSIC_LIGHTNING = false;

    private final Tessellator tessellator = Tessellator.instance;

    public void renderBlock(IWorldAccess world, Block block, int x, int y, int z) {
        BoundingBox boundingBox = block.getBoundingBox(world, x, y, z);

        // Render faces
        for (EnumBlockFace face : EnumBlockFace.values()) {
            if (block.shouldRenderFace(world, x, y, z, face)) {
                renderFace(world, block, boundingBox, face, x, y, z);
            }
        }
    }

    public void renderFace(IWorldAccess world, Block block, BoundingBox boundingBox, EnumBlockFace face, int x, int y, int z) {

        // Vertex mappings
        double minX = x + boundingBox.minX;
        double minY = y + boundingBox.minY;
        double minZ = z + boundingBox.minZ;
        double maxX = x + boundingBox.maxX;
        double maxY = y + boundingBox.maxY;
        double maxZ = z + boundingBox.maxZ;

        // UV Mapping
        int textureIndex = block.getTextureForFace(face);
        float minU = (textureIndex % 16) / 16.0F;
        float maxU = minU + (16 / 256F);
        float minV = (float) (textureIndex / 16);
        float maxV = minV + (16 / 256F);

        // Classic lightning
        if (CLASSIC_LIGHTNING) {
            float brightness = 0.9F / 15.0F * world.getLightAt((int) minX + face.x, (int) minY + face.y, (int) minZ + face.z) + 0.1F;
            float color = brightness * face.getShading();
            tessellator.setColorRGB_F(color, color, color);
        }

        if (face == EnumBlockFace.BOTTOM) {
            addBlockCorner(world, face, minX, minY, maxZ, minU, maxV);
            addBlockCorner(world, face, minX, minY, minZ, minU, minV);
            addBlockCorner(world, face, maxX, minY, minZ, maxU, minV);
            addBlockCorner(world, face, maxX, minY, maxZ, maxU, maxV);
        }
        if (face == EnumBlockFace.TOP) {
            addBlockCorner(world, face, maxX, maxY, maxZ, maxU, maxV);
            addBlockCorner(world, face, maxX, maxY, minZ, maxU, minV);
            addBlockCorner(world, face, minX, maxY, minZ, minU, minV);
            addBlockCorner(world, face, minX, maxY, maxZ, minU, maxV);
        }
        if (face == EnumBlockFace.EAST) {
            addBlockCorner(world, face, minX, maxY, minZ, maxU, minV);
            addBlockCorner(world, face, maxX, maxY, minZ, minU, minV);
            addBlockCorner(world, face, maxX, minY, minZ, minU, maxV);
            addBlockCorner(world, face, minX, minY, minZ, maxU, maxV);
        }
        if (face == EnumBlockFace.WEST) {
            addBlockCorner(world, face, minX, maxY, maxZ, minU, minV);
            addBlockCorner(world, face, minX, minY, maxZ, minU, maxV);
            addBlockCorner(world, face, maxX, minY, maxZ, maxU, maxV);
            addBlockCorner(world, face, maxX, maxY, maxZ, maxU, minV);
        }
        if (face == EnumBlockFace.NORTH) {
            addBlockCorner(world, face, minX, maxY, maxZ, maxU, minV);
            addBlockCorner(world, face, minX, maxY, minZ, minU, minV);
            addBlockCorner(world, face, minX, minY, minZ, minU, maxV);
            addBlockCorner(world, face, minX, minY, maxZ, maxU, maxV);
        }
        if (face == EnumBlockFace.SOUTH) {
            addBlockCorner(world, face, maxX, minY, maxZ, minU, maxV);
            addBlockCorner(world, face, maxX, minY, minZ, maxU, maxV);
            addBlockCorner(world, face, maxX, maxY, minZ, maxU, minV);
            addBlockCorner(world, face, maxX, maxY, maxZ, minU, minV);
        }
    }


    private void addBlockCorner(IWorldAccess world, EnumBlockFace face, double x, double y, double z, float u, float v) {
        // Smooth lightning
        if (!CLASSIC_LIGHTNING) {
            setAverageColor(world, face, (int) x, (int) y, (int) z);
        }

        this.tessellator.addVertexWithUV(x, y, z, u, v);
    }

    private void setAverageColor(IWorldAccess world, EnumBlockFace face, int x, int y, int z) {
        // Get the average light level of all 4 blocks at this corner
        int lightLevelAtThisCorner = getAverageLightLevelAt(world, x, y, z);

        // Convert light level from [0 - 15] to [0.1 - 1.0]
        float brightness = 0.9F / 15.0F * lightLevelAtThisCorner + 0.1F;
        float color = brightness * face.getShading();

        // Set color with shading
        this.tessellator.setColorRGB_F(color, color, color);
    }

    private int getAverageLightLevelAt(IWorldAccess world, int x, int y, int z) {
        int totalLightLevel = 0;
        int totalBlocks = 0;

        // For all blocks around this corner
        for (int offsetX = -1; offsetX <= 0; offsetX++) {
            for (int offsetY = -1; offsetY <= 0; offsetY++) {
                for (int offsetZ = -1; offsetZ <= 0; offsetZ++) {
                    short typeId = world.getBlockAt(x + offsetX, y + offsetY, z + offsetZ);

                    // Does it contain air?
                    if (typeId == 0 || Block.getById(typeId).isTransparent()) {

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

    public void renderSingleBlock(WorldRenderer worldRenderer, IWorldAccess world, Block block, int x, int y, int z) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, worldRenderer.textureId);

        this.tessellator.startDrawing(7);
        worldRenderer.getBlockRenderer().renderBlock(world, Block.LEAVE, x, y, z);
        this.tessellator.draw();
    }

    public void drawBoundingBox(double minX, double minY, double minZ,
                                double maxX, double maxY, double maxZ) {

        // Bottom
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glEnd();

        // Ceiling
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glEnd();
    }
}
