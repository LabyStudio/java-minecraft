package de.labystudio.game.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

public class Textures {
    private static final HashMap<String, Integer> idMap = new HashMap();
    private static int lastId = -9999999;

    public static int loadTexture(String resourceName, int mode) {
        try {
            if (idMap.containsKey(resourceName)) {
                return idMap.get(resourceName);
            }
            IntBuffer ib = BufferUtils.createIntBuffer(1);

            GL11.glGenTextures(ib);
            int id = ib.get(0);

            bind(id);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mode);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mode);


            BufferedImage img = ImageIO.read(Textures.class.getResourceAsStream(resourceName));
            int w = img.getWidth();
            int h = img.getHeight();

            ByteBuffer pixels = BufferUtils.createByteBuffer(w * h * 4);
            int[] rawPixels = new int[w * h];
            img.getRGB(0, 0, w, h, rawPixels, 0, w);
            for (int i = 0; i < rawPixels.length; i++) {
                int a = rawPixels[i] >> 24 & 0xFF;
                int r = rawPixels[i] >> 16 & 0xFF;
                int g = rawPixels[i] >> 8 & 0xFF;
                int b = rawPixels[i] & 0xFF;

                rawPixels[i] = (a << 24 | b << 16 | g << 8 | r);
            }
            pixels.asIntBuffer().put(rawPixels);
            GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);

            return id;
        } catch (IOException e) {
            throw new RuntimeException("!!");
        }
    }

    public static void bind(int id) {
        if (id != lastId) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
            lastId = id;
        }
    }
}
