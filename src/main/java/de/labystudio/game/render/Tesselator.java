package de.labystudio.game.render;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Tesselator {
    private static final int MAX_VERTICES = 100000;
    private FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(300000);
    private FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(200000);
    private FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(300000);
    private int vertices = 0;
    private float u;
    private float v;
    private float r;
    private float g;
    private float b;
    private boolean hasColor = false;
    private boolean hasTexture = false;

    public void flush() {
        this.vertexBuffer.flip();
        this.texCoordBuffer.flip();
        this.colorBuffer.flip();

        GL11.glVertexPointer(3, 0, this.vertexBuffer);
        if (this.hasTexture) {
            GL11.glTexCoordPointer(2, 0, this.texCoordBuffer);
        }
        if (this.hasColor) {
            GL11.glColorPointer(3, 0, this.colorBuffer);
        }
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        if (this.hasTexture) {
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }
        if (this.hasColor) {
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.vertices); //7

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        if (this.hasTexture) {
            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        }
        if (this.hasColor) {
            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        }
        clear();
    }

    private void clear() {
        this.vertices = 0;

        this.vertexBuffer.clear();
        this.texCoordBuffer.clear();
        this.colorBuffer.clear();
    }

    public void init() {
        clear();
        this.hasColor = false;
        this.hasTexture = false;
    }

    public void tex(float u, float v) {
        this.hasTexture = true;
        this.u = u;
        this.v = v;
    }

    public void color(float r, float g, float b) {
        this.hasColor = true;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void vertex(float x, float y, float z) {
        this.vertexBuffer.put(this.vertices * 3 + 0, x).put(this.vertices * 3 + 1, y).put(this.vertices * 3 + 2, z);
        if (this.hasTexture) {
            this.texCoordBuffer.put(this.vertices * 2 + 0, this.u).put(this.vertices * 2 + 1, this.v);
        }
        if (this.hasColor) {
            this.colorBuffer.put(this.vertices * 3 + 0, this.r).put(this.vertices * 3 + 1, this.g)
                    .put(this.vertices * 3 + 2, this.b);
        }
        this.vertices += 1;
        if (this.vertices == 100000) {
            flush();
        }
    }
}
