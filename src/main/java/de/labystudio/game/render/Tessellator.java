package de.labystudio.game.render;

import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Tessellator {

    public static final Tessellator instance = new Tessellator(0x200000);

    private static final boolean convertQuadsToTriangles = true;
    private static final boolean tryVBO = false;

    private final ByteBuffer byteBuffer;
    private final IntBuffer intBuffer;
    private final FloatBuffer floatBuffer;

    private final int[] rawBuffer;
    private int vertexCount;

    private double textureU;
    private double textureV;

    private int color;
    private boolean hasColor;
    private boolean hasTexture;
    private boolean hasNormals;

    private int rawBufferIndex;
    private int addedVertices;
    private boolean isColorDisabled;
    private int drawMode;

    private double xOffset;
    private double yOffset;
    private double zOffset;

    private int normal;
    private boolean isDrawing;
    private final boolean useVBO = tryVBO && GLContext.getCapabilities().GL_ARB_vertex_buffer_object;
    private IntBuffer vertexBuffers;
    private int vboIndex;
    private final int vboCount = 10;
    private final int bufferSize;

    private Tessellator(int i) {
        this.bufferSize = i;
        this.byteBuffer = GLAllocation.createDirectByteBuffer(i * 4);
        this.rawBuffer = new int[i];

        this.intBuffer = byteBuffer.asIntBuffer();
        this.floatBuffer = byteBuffer.asFloatBuffer();

        if (this.useVBO) {
            this.vertexBuffers = GLAllocation.createDirectIntBuffer(this.vboCount);
            ARBVertexBufferObject.glGenBuffersARB(this.vertexBuffers);
        }
    }

    public void draw() {
        if (!this.isDrawing) {
            throw new IllegalStateException("Not tesselating!");
        }
        this.isDrawing = false;
        if (vertexCount > 0) {
            intBuffer.clear();

            intBuffer.put(rawBuffer, 0, rawBufferIndex);
            byteBuffer.position(0);
            byteBuffer.limit(rawBufferIndex * 4);

            if (useVBO) {
                vboIndex = (vboIndex + 1) % vboCount;
                ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vertexBuffers.get(vboIndex));
                ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, byteBuffer, 35040);
            }
            if (hasTexture) {
                if (useVBO) {
                    GL11.glTexCoordPointer(2, GL11.GL_FLOAT, GL11.GL_PIXEL_MODE_BIT, 12L);
                } else {
                    floatBuffer.position(3);
                    GL11.glTexCoordPointer(2, GL11.GL_PIXEL_MODE_BIT, floatBuffer);
                }
                GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            }
            if (hasColor) {
                if (useVBO) {
                    GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, GL11.GL_PIXEL_MODE_BIT, 20L);
                } else {
                    byteBuffer.position(20);
                    GL11.glColorPointer(4, true, GL11.GL_PIXEL_MODE_BIT, byteBuffer);
                }
                GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
            }
            if (hasNormals) {
                if (useVBO) {
                    GL11.glNormalPointer(GL11.GL_BYTE, GL11.GL_PIXEL_MODE_BIT, 24L);
                } else {
                    byteBuffer.position(24);
                    GL11.glNormalPointer(GL11.GL_PIXEL_MODE_BIT, byteBuffer);
                }
                GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
            }
            if (useVBO) {
                GL11.glVertexPointer(GL11.GL_LINE_STRIP, GL11.GL_FLOAT, GL11.GL_PIXEL_MODE_BIT, 0L);
            } else {
                floatBuffer.position(0);
                GL11.glVertexPointer(GL11.GL_LINE_STRIP, GL11.GL_PIXEL_MODE_BIT, floatBuffer);
            }
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            if (drawMode == 7 && convertQuadsToTriangles) {
                GL11.glDrawArrays(4, GL11.GL_POINTS, vertexCount);
            } else {
                GL11.glDrawArrays(drawMode, GL11.GL_POINTS, vertexCount);
            }
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            if (hasTexture) {
                GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            }
            if (hasColor) {
                GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
            }
            if (hasNormals) {
                GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
            }
        }

        reset();
    }

    private void reset() {
        this.vertexCount = 0;
        this.byteBuffer.clear();
        this.rawBufferIndex = 0;
        this.addedVertices = 0;
    }

    public void startDrawingQuads() {
        startDrawing(7);
    }

    public void startDrawing(int i) {
        if (this.isDrawing) {
            throw new IllegalStateException("Already tessellating!");
        } else {
            this.isDrawing = true;

            reset();

            this.drawMode = i;
            this.hasNormals = false;
            this.hasColor = false;
            this.hasTexture = false;
            this.isColorDisabled = false;
        }
    }

    public void setTextureUV(double u, double v) {
        this.hasTexture = true;
        this.textureU = u;
        this.textureV = v;
    }

    public void setColorOpaque_F(float r, float g, float b) {
        setColorOpaque((int) (r * 255F), (int) (g * 255F), (int) (b * 255F));
    }

    public void setColorRGBA_F(float r, float g, float b, float a) {
        setColorRGBA((int) (r * 255F), (int) (g * 255F), (int) (b * 255F), (int) (a * 255F));
    }

    public void setColorRGB_F(float r, float g, float b) {
        setColorRGBA((int) (r * 255F), (int) (g * 255F), (int) (b * 255F), 255);
    }

    public void setColorOpaque(int r, int g, int b) {
        setColorRGBA(r, g, b, 255);
    }

    public void setColorRGBA(int r, int g, int b, int a) {
        if (this.isColorDisabled) {
            return;
        }

        this.hasColor = true;
        this.color = ensureColorRange(a) << 24
                | ensureColorRange(b) << 16
                | ensureColorRange(g) << 8
                | ensureColorRange(r);
    }

    public void addVertexWithUV(double x, double y, double z, double u, double v) {
        setTextureUV(u, v);
        addVertex(x, y, z);
    }

    public void addVertex(double x, double y, double z) {
        this.addedVertices++;

        if (this.drawMode == 7 && convertQuadsToTriangles && this.addedVertices % 4 == 0) {
            for (int i = 0; i < 2; i++) {
                int j = 8 * (3 - i);

                if (this.hasTexture) {
                    rawBuffer[rawBufferIndex + 3] = rawBuffer[(rawBufferIndex - j) + 3];
                    rawBuffer[rawBufferIndex + 4] = rawBuffer[(rawBufferIndex - j) + 4];
                }

                if (this.hasColor) {
                    rawBuffer[rawBufferIndex + 5] = rawBuffer[(rawBufferIndex - j) + 5];
                }

                rawBuffer[rawBufferIndex] = rawBuffer[(rawBufferIndex - j)];
                rawBuffer[rawBufferIndex + 1] = rawBuffer[(rawBufferIndex - j) + 1];
                rawBuffer[rawBufferIndex + 2] = rawBuffer[(rawBufferIndex - j) + 2];

                vertexCount++;
                rawBufferIndex += 8;
            }
        }

        if (this.hasTexture) {
            rawBuffer[rawBufferIndex + 3] = Float.floatToRawIntBits((float) textureU);
            rawBuffer[rawBufferIndex + 4] = Float.floatToRawIntBits((float) textureV);
        }

        if (this.hasColor) {
            rawBuffer[rawBufferIndex + 5] = color;
        }

        if (this.hasNormals) {
            rawBuffer[rawBufferIndex + 6] = normal;
        }

        rawBuffer[rawBufferIndex] = Float.floatToRawIntBits((float) (x + xOffset));
        rawBuffer[rawBufferIndex + 1] = Float.floatToRawIntBits((float) (y + yOffset));
        rawBuffer[rawBufferIndex + 2] = Float.floatToRawIntBits((float) (z + zOffset));

        rawBufferIndex += 8;
        vertexCount++;

        if (this.vertexCount % 4 == 0 && rawBufferIndex >= bufferSize - 32) {
            draw();
            isDrawing = true;
        }
    }

    public void setColorOpaque_I(int rgb) {
        int r = rgb >> 16 & 0xff;
        int g = rgb >> 8 & 0xff;
        int b = rgb & 0xff;

        setColorOpaque(r, g, b);
    }

    public void setColorRGBA_I(int rgb, int alpha) {
        int k = rgb >> 16 & 0xff;
        int l = rgb >> 8 & 0xff;
        int i1 = rgb & 0xff;

        setColorRGBA(k, l, i1, alpha);
    }

    public void disableColor() {
        isColorDisabled = true;
    }

    public void setNormal(float x, float y, float z) {
        this.hasNormals = true;

        byte xByte = (byte) (int) (x * 128F);
        byte yByte = (byte) (int) (y * 127F);
        byte zByte = (byte) (int) (z * 127F);

        normal = xByte | yByte << 8 | zByte << 16;
    }

    public void setTranslationD(double xOffset, double yOffset, double zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public void setTranslationF(float xOffset, float yOffset, float zOffset) {
        this.xOffset += xOffset;
        this.yOffset += yOffset;
        this.zOffset += zOffset;
    }

    private int ensureColorRange(int value) {
        return Math.min(Math.max(value, 0), 255);
    }
}
