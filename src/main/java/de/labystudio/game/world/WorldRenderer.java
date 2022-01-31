package de.labystudio.game.world;

import de.labystudio.game.render.Frustum;
import de.labystudio.game.render.GLAllocation;
import de.labystudio.game.render.world.BlockRenderer;
import de.labystudio.game.util.EnumWorldBlockLayer;
import de.labystudio.game.util.TextureManager;
import de.labystudio.game.world.chunk.Chunk;
import de.labystudio.game.world.chunk.ChunkSection;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldRenderer {

    public static final int RENDER_DISTANCE = 8;

    private final FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(16);
    public final int textureId = TextureManager.loadTexture("/terrain.png", GL11.GL_NEAREST);

    private final World world;

    private final BlockRenderer blockRenderer = new BlockRenderer();
    private final Frustum frustum = new Frustum();
    private final List<ChunkSection> chunkSectionUpdateQueue = new ArrayList<>();

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

    public void setupFog(boolean inWater) {
        if (inWater) {
            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
            GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F); // Fog distance
            GL11.glFog(GL11.GL_FOG_COLOR, this.putColor(0.2F, 0.2F, 0.4F, 1.0F));
        } else {
            int viewDistance = WorldRenderer.RENDER_DISTANCE * ChunkSection.SIZE;

            GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
            GL11.glFogf(GL11.GL_FOG_START, viewDistance / 4.0F); // Fog start
            GL11.glFogf(GL11.GL_FOG_END, viewDistance); // Fog end
            GL11.glFog(GL11.GL_FOG_COLOR, this.putColor(0.6222222F - 0.05F, 0.5F + 0.1F, 1.0F, 1.0F));
        }
    }

    public void onTick() {

    }

    public void render(int cameraChunkX, int cameraChunkZ, EnumWorldBlockLayer renderLayer) {
        this.frustum.calculateFrustum();

        for (Chunk chunk : this.world.chunks.values()) {
            int distanceX = Math.abs(cameraChunkX - chunk.getX());
            int distanceZ = Math.abs(cameraChunkZ - chunk.getZ());

            // Is in camera view
            if (distanceX < RENDER_DISTANCE && distanceZ < RENDER_DISTANCE && this.frustum.cubeInFrustum(chunk)) {

                // For all chunk sections
                for (ChunkSection chunkSection : chunk.getSections()) {
                    // Render chunk section
                    chunkSection.render(renderLayer);

                    // Queue for rebuild
                    if (chunkSection.isQueuedForRebuild() && !this.chunkSectionUpdateQueue.contains(chunkSection)) {
                        this.chunkSectionUpdateQueue.add(chunkSection);
                    }
                }
            }
        }

        // Sort update queue, chunk sections that are closer to the camera get a higher priority
        Collections.sort(this.chunkSectionUpdateQueue, (section1, section2) -> {
            int distance1 = (int) (Math.pow(section1.x - cameraChunkX, 2) + Math.pow(section1.z - cameraChunkZ, 2));
            int distance2 = (int) (Math.pow(section2.x - cameraChunkX, 2) + Math.pow(section2.z - cameraChunkZ, 2));
            return Integer.compare(distance1, distance2);
        });

        // Rebuild one chunk per frame
        if (!this.chunkSectionUpdateQueue.isEmpty()) {
            ChunkSection chunkSection = this.chunkSectionUpdateQueue.remove(0);
            if (chunkSection != null) {
                chunkSection.rebuild(this);
            }
        }
    }

    private FloatBuffer putColor(float r, float g, float b, float a) {
        this.colorBuffer.clear();
        this.colorBuffer.put(r).put(g).put(b).put(a);
        this.colorBuffer.flip();
        return this.colorBuffer;
    }

    public BlockRenderer getBlockRenderer() {
        return this.blockRenderer;
    }
}
