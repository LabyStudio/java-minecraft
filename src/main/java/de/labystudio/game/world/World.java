package de.labystudio.game.world;

import de.labystudio.game.render.world.IWorldAccess;
import de.labystudio.game.util.BoundingBox;
import de.labystudio.game.util.EnumBlockFace;
import de.labystudio.game.world.block.Block;
import de.labystudio.game.world.chunk.Chunk;
import de.labystudio.game.world.chunk.ChunkSection;
import de.labystudio.game.world.chunk.format.WorldFormat;
import de.labystudio.game.world.generator.WorldGenerator;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class World implements IWorldAccess {

    public static final int TOTAL_HEIGHT = ChunkSection.SIZE * 16 - 1;
    public Map<Long, Chunk> chunks = new HashMap<>();

    public boolean updateLightning = false;
    private final Queue<Long> lightUpdateQueue = new LinkedList<>();

    private final WorldGenerator generator = new WorldGenerator(this, (int) (System.currentTimeMillis() % 100000));
    public WorldFormat format = new WorldFormat(this, new File("saves/World1"));

    public World() {
        load();
    }

    public void load() {
        if (this.format.exists()) {
            // Load chunks
            this.format.load((x, z, array) -> {
                Chunk chunk = getChunkAt(x, z);
                chunk.setSections(array);
                chunk.queueForRebuild();
            });

            this.updateLightning = true;
        } else {
            int radius = 20;

            // Generator new spawn chunks
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    this.generator.generateChunk(x, z);
                }
            }

            // Populate trees
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    this.generator.populateChunk(x, z);
                }
            }

            this.updateLightning = true;
        }
    }

    public void save() {
        try {
            this.format.saveChunks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onTick() {
        // Light updates
        if (!this.lightUpdateQueue.isEmpty()) {

            // Handle 128 light updates per tick
            for (int i = 0; i < 128; i++) {

                // Get next position to update
                Long positionIndex = this.lightUpdateQueue.poll();
                if (positionIndex != null) {
                    updateBlockLightsAtXZ((int) (positionIndex >> 32L), positionIndex.intValue());
                } else {
                    break;
                }
            }
        }
    }

    public ArrayList<BoundingBox> getCollisionBoxes(BoundingBox aabb) {
        ArrayList<BoundingBox> boundingBoxList = new ArrayList<>();

        int minX = (int) (Math.floor(aabb.minX) - 1);
        int maxX = (int) (Math.ceil(aabb.maxX) + 1);
        int minY = (int) (Math.floor(aabb.minY) - 1);
        int maxY = (int) (Math.ceil(aabb.maxY) + 1);
        int minZ = (int) (Math.floor(aabb.minZ) - 1);
        int maxZ = (int) (Math.ceil(aabb.maxZ) + 1);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    if (isSolidBlockAt(x, y, z)) {
                        boundingBoxList.add(new BoundingBox(x, y, z, x + 1, y + 1, z + 1));
                    }
                }
            }
        }
        return boundingBoxList;
    }

    public void blockChanged(int x, int y, int z) {
        setDirty(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    public void allChunksChanged() {
        for (Chunk chunk : this.chunks.values()) {
            chunk.queueForRebuild();
        }
    }

    public void setDirty(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        // To chunk coordinates
        minX = minX >> 4;
        maxX = maxX >> 4;
        minY = minY >> 4;
        maxY = maxY >> 4;
        minZ = minZ >> 4;
        maxZ = maxZ >> 4;

        // Minimum and maximum y
        minY = Math.max(0, minY);
        maxY = Math.min(15, maxY);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    getChunkAt(x, y, z).queueForRebuild();
                }
            }
        }
    }

    public void setBlockAt(int x, int y, int z, int type) {
        ChunkSection chunkSection = getChunkAtBlock(x, y, z);
        if (chunkSection != null) {
            chunkSection.setBlockAt(x & 15, y & 15, z & 15, type);

            if (this.updateLightning) {
                updateBlockLightAt(x, y, z);
            }

            blockChanged(x, y, z);
        }
    }

    public void updateBlockLightAt(int x, int y, int z) {
        // Calculate brightness for target block
        int lightLevel = isHighestBlockAt(x, y, z) ? 15 : calculateLightAt(x, y, z);

        // Update target block light
        getChunkAtBlock(x, y, z).setLightAt(x & 15, y & 15, z & 15, lightLevel);

        // Update block lights below the target block and the surrounding blocks
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                updateBlockLightsAtXZ(x + offsetX, z + offsetZ);
            }
        }
    }

    private void updateBlockLightsAtXZ(int x, int z) {
        boolean lightChanged = false;
        int skyLevel = 15;

        // Scan from the top to the bottom
        for (int y = TOTAL_HEIGHT; y >= 0; y--) {
            if (!isTransparentBlockAt(x, y, z)) {
                // Sun is blocked because of solid block
                skyLevel = 0;
            } else {
                // Get opacity of this block
                short typeId = getBlockAt(x, y, z);
                float translucence = typeId == 0 ? 1.0F : 1.0F - Block.getById(typeId).getOpacity();

                // Decrease strength of the skylight by the opacity of the block
                skyLevel *= translucence;

                // Get previous block light
                float prevBlockLight = getLightAt(x, y, z);

                // Combine skylight with the calculated block light and decrease strength by the opacity of the block
                int blockLight = (int) (Math.max(skyLevel, calculateLightAt(x, y, z)) * translucence);

                // Did one of the light change inside of the range?
                if (prevBlockLight != blockLight) {
                    lightChanged = true;
                }

                // Apply the new light to the block
                setLightAt(x, y, z, blockLight);
            }
        }

        // Chain reaction, update next affected blocks
        if (lightChanged && this.lightUpdateQueue.size() < 512) {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                    long positionIndex = (long) (x + offsetX) << 32 | (z + offsetZ) & 0xFFFFFFFFL;

                    // Add block range to update queue
                    if (!this.lightUpdateQueue.contains(positionIndex)) {
                        this.lightUpdateQueue.add(positionIndex);
                    }
                }
            }
        }
    }

    private void setLightAt(int x, int y, int z, int light) {
        ChunkSection chunkSection = getChunkAtBlock(x, y, z);
        if (chunkSection != null) {
            chunkSection.setLightAt(x & 15, y & 15, z & 15, light);
            chunkSection.queueForRebuild();
        }
    }

    @Override
    public int getLightAt(int x, int y, int z) {
        ChunkSection chunkSection = getChunkAtBlock(x, y, z);
        return chunkSection == null ? 15 : chunkSection.getLightAt(x & 15, y & 15, z & 15);
    }

    private boolean isHighestBlockAt(int x, int y, int z) {
        for (int i = y + 1; i < TOTAL_HEIGHT; i++) {
            if (isSolidBlockAt(x, i, z)) {
                return false;
            }
        }
        return true;
    }

    public int getHighestBlockYAt(int x, int z) {
        for (int y = TOTAL_HEIGHT; y > 0; y--) {
            if (isSolidBlockAt(x, y, z)) {
                return y;
            }
        }
        return 0;
    }

    private int calculateLightAt(int x, int y, int z) {
        int maxBrightness = 0;

        // Get maximal brightness of surround blocks
        for (EnumBlockFace face : EnumBlockFace.values()) {
            if (isTransparentBlockAt(x + face.x, y + face.y, z + face.z)) {
                int brightness = getLightAt(x + face.x, y + face.y, z + face.z);

                maxBrightness = Math.max(maxBrightness, brightness);
            }
        }

        // Decrease maximum brightness by 6%
        return Math.max(0, maxBrightness - 1);
    }

    public boolean isSolidBlockAt(int x, int y, int z) {
        short typeId = getBlockAt(x, y, z);
        return typeId != 0 && Block.getById(typeId).isSolid();
    }

    public boolean isTransparentBlockAt(int x, int y, int z) {
        short typeId = getBlockAt(x, y, z);
        return typeId == 0 || Block.getById(typeId).isTransparent();
    }

    @Override
    public short getBlockAt(int x, int y, int z) {
        ChunkSection chunkSection = getChunkAtBlock(x, y, z);
        return chunkSection == null ? 0 : chunkSection.getBlockAt(x & 15, y & 15, z & 15);
    }

    public ChunkSection getChunkAt(int chunkX, int layerY, int chunkZ) {
        return getChunkAt(chunkX, chunkZ).getSection(layerY);
    }

    public Chunk getChunkAt(int x, int z) {
        long chunkIndex = Chunk.getIndex(x, z);
        Chunk chunk = this.chunks.get(chunkIndex);
        if (chunk == null) {
            chunk = new Chunk(this, x, z);

            // Copy map because of ConcurrentModificationException
            Map<Long, Chunk> chunks = new HashMap<>(this.chunks);
            chunks.put(chunkIndex, chunk);
            this.chunks = chunks;
        }
        return chunk;
    }

    public boolean isChunkLoaded(int x, int z) {
        long chunkIndex = (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
        return this.chunks.containsKey(chunkIndex);
    }

    public boolean isChunkLoadedAt(int x, int y, int z) {
        return isChunkLoaded(x >> 4, z >> 4);
    }

    public ChunkSection getChunkAtBlock(int x, int y, int z) {
        Chunk chunk = getChunkAt(x >> 4, z >> 4);
        return y < 0 || y > TOTAL_HEIGHT ? null : chunk.getSection(y >> 4);
    }
}
