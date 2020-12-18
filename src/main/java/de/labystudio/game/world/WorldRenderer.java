package de.labystudio.game.world;

import de.labystudio.game.render.Frustum;
import de.labystudio.game.util.Textures;
import de.labystudio.game.world.chunk.Chunk;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class WorldRenderer implements WorldListener {

    public static final int RENDER_DISTANCE = 3;

    private final World world;
    public final int textureId = Textures.loadTexture("/terrain.png", GL11.GL_NEAREST);

    public final FloatBuffer fogColor = BufferUtils.createFloatBuffer(4);

    public WorldRenderer(World world) {
        this.world = world;

        int col = 920330;
        float fr = 0.0F;
        float fg = 0.0F;
        float fb = 0.0F;
        this.fogColor.put(new float[]{(col >> 16 & 0xFF) / 255.0F, (col >> 8 & 0xFF) / 255.0F, (col & 0xFF) / 255.0F, 1.0F});
        this.fogColor.flip();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glClearColor(fr, fg, fb, 0.0F);
        GL11.glClearDepth(1.0D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void render(int x, int z, int layer) {
        this.world.rebuiltThisFrame = true;
        Frustum frustum = Frustum.getFrustum();

        for (Chunk[] chunkLayers : this.world.chunks.values()) {
            for (Chunk chunk : chunkLayers) {
                int distanceX = Math.abs(x - chunk.x);
                int distanceZ = Math.abs(z - chunk.z);

                if (distanceX < RENDER_DISTANCE && distanceZ < RENDER_DISTANCE && frustum.cubeInFrustum(chunk)) {
                    chunk.render(this, layer);
                }
            }
        }
    }
}
