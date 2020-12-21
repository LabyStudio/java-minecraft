package de.labystudio.game.render.gui;

import de.labystudio.game.GameWindow;
import de.labystudio.game.render.Tessellator;
import de.labystudio.game.util.Textures;
import org.lwjgl.opengl.GL11;

public class GuiRenderer {

    private int textureId;
    private int zLevel = 0;

    private int scaleFactor;
    private int width;
    private int height;

    public void loadTextures() {
        this.textureId = Textures.loadTexture("/icons.png", GL11.GL_NEAREST);
    }

    public void init(GameWindow gameWindow) {
        this.width = gameWindow.displayWidth;
        this.height = gameWindow.displayHeight;
        for (this.scaleFactor = 1; this.width / (this.scaleFactor + 1) >= 320 && this.height / (this.scaleFactor + 1) >= 240; this.scaleFactor++) {
        }
        this.width = this.width / this.scaleFactor;
        this.height = this.height / this.scaleFactor;
    }

    public void setupCamera() {
        GL11.glClear(256);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, this.width, this.height, 0.0D, 1000D, 3000D);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000F);
    }

    public void renderCrosshair() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
        drawTexturedModalRect(this.width / 2 - 7, this.height / 2 - 7, 0, 0, 16, 16);

        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawTexturedModalRect(int left, int top, int offsetX, int offsetY, int width, int height) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        drawTexturedModalRect(tessellator, left, top, offsetX, offsetY, width, height, width, height, 256, 256);
        tessellator.draw();
    }

    public void drawTexturedModalRect(Tessellator tessellator, int x, int y,
                                      int u, int v, int uWidth, int vHeight,
                                      int width, int height,
                                      float bitMapWidth, float bitmapHeight) {

        float factorX = 1.0F / bitMapWidth;
        float factorY = 1.0F / bitmapHeight;

        tessellator.addVertexWithUV(x, y + height, zLevel, u * factorX, (v + vHeight) * factorY);
        tessellator.addVertexWithUV(x + width, y + height, zLevel, (u + uWidth) * factorX, (v + vHeight) * factorY);
        tessellator.addVertexWithUV(x + width, y, zLevel, (u + uWidth) * factorX, v * factorY);
        tessellator.addVertexWithUV(x, y, zLevel, u * factorX, v * factorY);
    }
}
