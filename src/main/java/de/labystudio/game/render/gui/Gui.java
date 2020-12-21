package de.labystudio.game.render.gui;

import de.labystudio.game.GameWindow;
import de.labystudio.game.render.Tessellator;
import de.labystudio.game.util.Textures;
import org.lwjgl.opengl.GL11;

public class Gui {

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
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(left, top + height, zLevel, (float) (offsetX) * f, (float) (offsetY + height) * f1);
        tessellator.addVertexWithUV(left + width, top + height, zLevel, (float) (offsetX + width) * f, (float) (offsetY + height) * f1);
        tessellator.addVertexWithUV(left + width, top, zLevel, (float) (offsetX + width) * f, (float) (offsetY) * f1);
        tessellator.addVertexWithUV(left, top, zLevel, (float) (offsetX) * f, (float) (offsetY) * f1);
        tessellator.draw();
    }

}
