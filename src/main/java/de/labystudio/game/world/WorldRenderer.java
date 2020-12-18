package de.labystudio.game.world;

import de.labystudio.game.render.Frustum;
import de.labystudio.game.render.Tesselator;
import de.labystudio.game.util.Textures;
import de.labystudio.game.world.chunk.Chunk;

public class WorldRenderer implements WorldListener {

    public static final int RENDER_DISTANCE = 3;

    private final World world;

    public final Tesselator tesselator = new Tesselator();
    public final int textureId = Textures.loadTexture("/terrain.png", 9728);

    public WorldRenderer(World world) {
        this.world = world;
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
