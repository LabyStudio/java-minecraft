package de.labystudio.game.render;

import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class GLAllocation {

    private static final List<Integer> displayLists = new ArrayList<>();
    private static final List<Integer> textureNames = new ArrayList<>();

    public static synchronized int generateDisplayLists(int amount) {
        int id = GL11.glGenLists(amount);

        displayLists.add(id);
        displayLists.add(amount);

        return id;
    }

    public static synchronized void generateTextureNames(IntBuffer intbuffer) {
        GL11.glGenTextures(intbuffer);

        for (int i = intbuffer.position(); i < intbuffer.limit(); i++) {
            textureNames.add(intbuffer.get(i));
        }
    }

    public static synchronized void deleteTexturesAndDisplayLists() {
        for (int i = 0; i < displayLists.size(); i += 2) {
            GL11.glDeleteLists(displayLists.get(i), displayLists.get(i + 1));
        }

        IntBuffer intbuffer = createDirectIntBuffer(textureNames.size());
        intbuffer.flip();
        GL11.glDeleteTextures(intbuffer);

        for (Integer textureName : textureNames) {
            intbuffer.put(textureName);
        }

        intbuffer.flip();
        GL11.glDeleteTextures(intbuffer);

        displayLists.clear();
        textureNames.clear();
    }

    public static IntBuffer createDirectIntBuffer(int size) {
        return createDirectByteBuffer(size << 2).asIntBuffer();
    }

    public static FloatBuffer createDirectFloatBuffer(int size) {
        return createDirectByteBuffer(size << 2).asFloatBuffer();
    }

    public static synchronized ByteBuffer createDirectByteBuffer(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

}
