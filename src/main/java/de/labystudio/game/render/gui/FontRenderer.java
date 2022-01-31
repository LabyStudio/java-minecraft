package de.labystudio.game.render.gui;

import de.labystudio.game.render.Tessellator;
import de.labystudio.game.util.TextureManager;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FontRenderer {

    private static final int BITMAP_SIZE = 16;
    private static final int FIELD_SIZE = 8;

    private static final String COLOR_CODE_INDEX_LOOKUP = "0123456789abcdef";

    private final GuiRenderer gui;
    private final Tessellator tessellator = Tessellator.instance;

    private final int[] charWidths = new int[256];
    private final int fontTextureId;

    public FontRenderer(GuiRenderer gui, String name) throws IOException {
        this.gui = gui;

        BufferedImage bitMap = ImageIO.read(TextureManager.class.getResourceAsStream(name));

        // Calculate character width
        for (int i = 0; i < 128; i++) {
            this.charWidths[i] = this.calculateCharacterWidthAt(bitMap, i % BITMAP_SIZE, i / BITMAP_SIZE) + 2;
        }

        // Load texture
        this.fontTextureId = TextureManager.loadTexture(name, GL11.GL_NEAREST);
    }

    private int calculateCharacterWidthAt(BufferedImage bitMap, int indexX, int indexY) {
        // We scan the bitmap field from right to left
        for (int x = indexX * FIELD_SIZE + FIELD_SIZE - 1; x >= indexX * FIELD_SIZE; x--) {

            // Scan this column from top to bottom
            for (int y = indexY * FIELD_SIZE; y < indexY * FIELD_SIZE + FIELD_SIZE; y++) {

                // Return width if there is a white pixel
                if ((bitMap.getRGB(x, y) & 0xFF) != 0) {
                    return x - indexX * FIELD_SIZE;
                }
            }
        }

        // Empty field width (Could be a space character)
        return 2;
    }

    public void drawString(String string, int x, int y) {
        this.drawString(string, x, y, -1);
    }

    public void drawString(String string, int x, int y, int color) {
        this.drawStringRaw(string, x + 1, y + 1, color, true);
        this.drawStringRaw(string, x, y, color, false);
    }

    public void drawStringWithoutShadow(String string, int x, int y, int color) {
        this.drawStringRaw(string, x, y, color, false);
    }

    private void drawStringRaw(String string, int x, int y, int color, boolean isShadow) {
        // Setup texture
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.fontTextureId);

        // Start rendering
        this.tessellator.startDrawingQuads();
        this.setColor(color, isShadow);

        char[] chars = string.toCharArray();

        // For each character
        for (int i = 0; i < chars.length; i++) {
            char character = chars[i];

            // Handle color codes if character is &
            if (character == '&' && i != chars.length - 1) {
                // Get the next character
                char nextCharacter = chars[i + 1];

                // Change color of string
                this.setColor(this.getColorOfCharacter(nextCharacter), isShadow);

                // Skip the color code for rendering
                i += 1;
                continue;
            }

            // Get character offset in bitmap
            int textureOffsetX = chars[i] % BITMAP_SIZE * FIELD_SIZE;
            int textureOffsetY = chars[i] / BITMAP_SIZE * FIELD_SIZE;

            // Draw character
            this.gui.drawTexturedModalRect(this.tessellator, x, y,
                    textureOffsetX, textureOffsetY,
                    FIELD_SIZE, FIELD_SIZE,
                    FIELD_SIZE, FIELD_SIZE,
                    128, 128);

            // Increase drawing cursor
            x += this.charWidths[character];
        }

        // Finish drawing
        this.tessellator.draw();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public int getColorOfCharacter(char character) {
        int index = COLOR_CODE_INDEX_LOOKUP.indexOf(character);
        int brightness = (index & 0x8) * 8;

        // Convert index to RGB
        int b = (index & 0x1) * 191 + brightness;
        int g = ((index & 0x2) >> 1) * 191 + brightness;
        int r = ((index & 0x4) >> 2) * 191 + brightness;

        return r << 16 | g << 8 | b;
    }

    private void setColor(int color, boolean isShadow) {
        this.tessellator.setColorOpaque_I(isShadow ? (color & 0xFCFCFC) >> 2 : color);
    }

    public int getStringWidth(String string) {
        char[] chars = string.toCharArray();
        int length = 0;

        // For each character
        for (int i = 0; i < chars.length; i++) {

            // Check for color code
            if (chars[i] == '&') {
                // Skip the next character
                i++;
            } else {
                // Add the width of the character
                length += this.charWidths[chars[i]];
            }
        }

        return length;
    }

}
