package de.labystudio.game.world;

import de.labystudio.game.render.Frustum;
import de.labystudio.game.render.GLAllocation;
import de.labystudio.game.util.Textures;
import de.labystudio.game.world.chunk.Chunk;
import de.labystudio.game.world.chunk.ChunkLayers;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class WorldRenderer {

    public static final int RENDER_DISTANCE = 3;

    private final World world;
    public final int textureId = Textures.loadTexture("/terrain.png", GL11.GL_NEAREST);

    private final FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(16);

    public WorldRenderer(World world) {
        this.world = world;

        // Sky color
        GL11.glClearColor(0.6222222F - 0.05F, 0.5F + 0.1F, 1.0F, 0.0F);
        GL11.glClearDepth(1.0D);

        // Render methods
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        // Matrix
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void setupFog() {
        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_VIEWPORT_BIT);
        GL11.glFogf(GL11.GL_FOG_DENSITY, 0.01F); // Fog distance
        GL11.glFog(GL11.GL_FOG_COLOR, putColor(0.3F, 0.3F, 0.3F, 1.0F));
    }

    public void render(int x, int z) {
        Frustum frustum = Frustum.getFrustum();

        for (ChunkLayers chunkLayers : this.world.chunks.values()) {
            int distanceX = Math.abs(x - chunkLayers.getX());
            int distanceZ = Math.abs(z - chunkLayers.getZ());

            if (distanceX < RENDER_DISTANCE && distanceZ < RENDER_DISTANCE && frustum.cubeInFrustum(chunkLayers)) {
                for (Chunk chunk : chunkLayers.getLayers()) {
                    chunk.render(this);
                }
            }
        }
    }

    private FloatBuffer putColor(float r, float g, float b, float a) {
        this.colorBuffer.clear();
        this.colorBuffer.put(r).put(g).put(b).put(a);
        this.colorBuffer.flip();
        return this.colorBuffer;
    }
}
