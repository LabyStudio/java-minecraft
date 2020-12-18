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

    private ByteBuffer byteBuffer;
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
        vertexCount = 0;
        byteBuffer.clear();
        rawBufferIndex = 0;
        addedVertices = 0;
    }

    public void startDrawingQuads() {
        startDrawing(7);
    }

    public void startDrawing(int i) {
        if (isDrawing) {
            throw new IllegalStateException("Already tesselating!");
        } else {
            isDrawing = true;
            reset();
            drawMode = i;
            hasNormals = false;
            hasColor = false;
            hasTexture = false;
            isColorDisabled = false;
        }
    }

    public void setTextureUV(double d, double d1) {
        hasTexture = true;
        textureU = d;
        textureV = d1;
    }

    public void setColorOpaque_F(float f, float f1, float f2) {
        setColorOpaque((int) (f * 255F), (int) (f1 * 255F), (int) (f2 * 255F));
    }

    public void setColorRGBA_F(float f, float f1, float f2, float f3) {
        setColorRGBA((int) (f * 255F), (int) (f1 * 255F), (int) (f2 * 255F), (int) (f3 * 255F));
    }

    public void setColorRGB_F(float f, float f1, float f2) {
        setColorRGBA((int) (f * 255F), (int) (f1 * 255F), (int) (f2 * 255F), 255);
    }

    public void setColorOpaque(int i, int j, int k) {
        setColorRGBA(i, j, k, 255);
    }

    public void setColorRGBA(int i, int j, int k, int l) {
        if (isColorDisabled) {
            return;
        }
        if (i > 255) {
            i = 255;
        }
        if (j > 255) {
            j = 255;
        }
        if (k > 255) {
            k = 255;
        }
        if (l > 255) {
            l = 255;
        }
        if (i < 0) {
            i = 0;
        }
        if (j < 0) {
            j = 0;
        }
        if (k < 0) {
            k = 0;
        }
        if (l < 0) {
            l = 0;
        }
        hasColor = true;
        color = l << 24 | k << 16 | j << 8 | i;
    }

    public void addVertexWithUV(double d, double d1, double d2, double d3, double d4) {
        setTextureUV(d3, d4);
        addVertex(d, d1, d2);
    }

    public void addVertex(double d, double d1, double d2) {
        addedVertices++;
        if (drawMode == 7 && convertQuadsToTriangles && addedVertices % 4 == 0) {
            for (int i = 0; i < 2; i++) {
                int j = 8 * (3 - i);
                if (hasTexture) {
                    rawBuffer[rawBufferIndex + 3] = rawBuffer[(rawBufferIndex - j) + 3];
                    rawBuffer[rawBufferIndex + 4] = rawBuffer[(rawBufferIndex - j) + 4];
                }
                if (hasColor) {
                    rawBuffer[rawBufferIndex + 5] = rawBuffer[(rawBufferIndex - j) + 5];
                }
                rawBuffer[rawBufferIndex] = rawBuffer[(rawBufferIndex - j)];
                rawBuffer[rawBufferIndex + 1] = rawBuffer[(rawBufferIndex - j) + 1];
                rawBuffer[rawBufferIndex + 2] = rawBuffer[(rawBufferIndex - j) + 2];
                vertexCount++;
                rawBufferIndex += 8;
            }

        }
        if (hasTexture) {
            rawBuffer[rawBufferIndex + 3] = Float.floatToRawIntBits((float) textureU);
            rawBuffer[rawBufferIndex + 4] = Float.floatToRawIntBits((float) textureV);
        }
        if (hasColor) {
            rawBuffer[rawBufferIndex + 5] = color;
        }
        if (hasNormals) {
            rawBuffer[rawBufferIndex + 6] = normal;
        }
        rawBuffer[rawBufferIndex] = Float.floatToRawIntBits((float) (d + xOffset));
        rawBuffer[rawBufferIndex + 1] = Float.floatToRawIntBits((float) (d1 + yOffset));
        rawBuffer[rawBufferIndex + 2] = Float.floatToRawIntBits((float) (d2 + zOffset));
        rawBufferIndex += 8;
        vertexCount++;
        if (vertexCount % 4 == 0 && rawBufferIndex >= bufferSize - 32) {
            draw();
            isDrawing = true;
        }
    }

    public void setColorOpaque_I(int i) {
        int j = i >> 16 & 0xff;
        int k = i >> 8 & 0xff;
        int l = i & 0xff;
        setColorOpaque(j, k, l);
    }

    public void setColorRGBA_I(int i, int j) {
        int k = i >> 16 & 0xff;
        int l = i >> 8 & 0xff;
        int i1 = i & 0xff;
        setColorRGBA(k, l, i1, j);
    }

    public void disableColor() {
        isColorDisabled = true;
    }

    public void setNormal(float f, float f1, float f2) {
        if (!isDrawing) {
            System.out.println("But..");
        }
        hasNormals = true;
        byte byte0 = (byte) (int) (f * 128F);
        byte byte1 = (byte) (int) (f1 * 127F);
        byte byte2 = (byte) (int) (f2 * 127F);
        normal = byte0 | byte1 << 8 | byte2 << 16;
    }

    public void setTranslationD(double d, double d1, double d2) {
        xOffset = d;
        yOffset = d1;
        zOffset = d2;
    }

    public void setTranslationF(float f, float f1, float f2) {
        xOffset += f;
        yOffset += f1;
        zOffset += f2;
    }

}
