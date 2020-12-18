package de.labystudio.game.render;

public class Gui {

    public static int zLevel = 0;

    public static void drawTexturedModalRect(int left, int top, int offsetX, int offsetY, int width, int height) {
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
