package de.labystudio.game.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.labystudio.game.util.AABB;
import de.labystudio.game.world.chunk.Chunk;

public class World {

    public Map<Long, Chunk[]> chunks = new HashMap<>();

    public int rebuiltThisFrame = 0;
    public int updates = 0;

    public World() {
        //  calcLightDepths(0, 0, w, h);
        load();
    }

    public void load() {
        /*
        try {
            DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(new File("level.dat"))));
            dis.readFully(this.blocks);
            calcLightDepths(0, 0, this.width, this.height);
            for (int i = 0; i < this.worldListeners.size(); i++) {
                ((WorldListener) this.worldListeners.get(i)).allChanged();
            }
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
         */

        int size = Chunk.SIZE * 10;

        int i = 0;
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                for (int y = 0; y < 64 - x / Chunk.SIZE + z / Chunk.SIZE; y++) {
                    setBlockAt(x, y, z, 1);
                }
            }
        }

        allChunksChanged();
    }

    public void save() {
        /*
        try {
            DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File("level.dat"))));
            dos.write(this.blocks);
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
         */
    }

    public boolean isSolidBlockAt(int x, int y, int z) {
        return getBlockAt(x, y, z) != 0;
    }

    public int getBlockAt(int x, int y, int z) {
        Chunk chunk = getChunkAtBlock(x, y, z);
        return chunk == null ? 0 : chunk.getBlockAt(x & 15, y & 15, z & 15);
    }

    public Chunk getChunkAt(int x, int y, int z) {
        return getChunkLayersAt(x, z)[y];
    }

    public Chunk[] getChunkLayersAt(int x, int z) {
        long chunkIndex = (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
        Chunk[] chunkLayers = this.chunks.get(chunkIndex);
        if (chunkLayers == null) {
            chunkLayers = new Chunk[16];
            for (int y = 0; y < 16; y++) {
                chunkLayers[y] = new Chunk(this, x, y, z);
            }

            // Copy map because of ConcurrentModificationException
            Map<Long, Chunk[]> chunks = new HashMap<>(this.chunks);
            chunks.put(chunkIndex, chunkLayers);
            this.chunks = chunks;
        }
        return chunkLayers;
    }

    public Chunk getChunkAtBlock(int x, int y, int z) {
        Chunk[] chunkLayers = getChunkLayersAt(x >> 4, z >> 4);
        return y < 0 || y >= Chunk.SIZE * 16 ? null : chunkLayers[y >> 4];
    }


    public ArrayList<AABB> getCollisionBoxes(AABB aabb) {
        ArrayList<AABB> aABBs = new ArrayList<>();

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
                        aABBs.add(new AABB(x, y, z, x + 1, y + 1, z + 1));
                    }
                }
            }
        }
        return aABBs;
    }

    public void blockChanged(int x, int y, int z) {
        setDirty(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
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
                    getChunkAt(x, y, z).setDirty();
                }
            }
        }
    }

    public void allChunksChanged() {
        for (Chunk[] chunkLayers : this.chunks.values()) {
            for (Chunk chunk : chunkLayers) {
                chunk.setDirty();
            }
        }
    }

    public void setBlockAt(int x, int y, int z, int type) {
        Chunk chunk = getChunkAtBlock(x, y, z);
        if (chunk != null) {
            chunk.setBlockAt(x & 15, y & 15, z & 15, type);

            // calcLightDepths(x, z, 1, 1);
            blockChanged(x, y, z);
        }
    }

    /*
    public void lightColumnChanged(int x, int z, int y0, int y1) {
        setDirty(x - 1, y0 - 1, z - 1, x + 1, y1 + 1, z + 1);
    }
    */

    /*
    public void calcLightDepths(int x0, int y0, int x1, int y1) {
        for (int x = x0; x < x0 + x1; x++) {
            for (int z = y0; z < y0 + y1; z++) {
                int oldDepth = this.lightDepths[(x + z * this.width)];
                int y = this.depth - 1;
                while ((y > 0) && (!isLightBlocker(x, y, z))) {
                    y--;
                }
                this.lightDepths[(x + z * this.width)] = y;
                if (oldDepth != y) {
                    int yl0 = oldDepth < y ? oldDepth : y;
                    int yl1 = oldDepth > y ? oldDepth : y;
                    for (int i = 0; i < this.worldListeners.size(); i++) {
                        ((WorldListener) this.worldListeners.get(i)).lightColumnChanged(x, z, yl0, yl1);
                    }
                }
            }
        }
    }


    public void addListener(WorldListener worldListener) {
        this.worldListeners.add(worldListener);
    }

    public void removeListener(WorldListener worldListener) {
        this.worldListeners.remove(worldListener);
    }
*/


    /*
    public boolean isLightBlocker(int x, int y, int z) {
        return isSolidTile(x, y, z);
    }
    */

    /*
    public float getBrightness(int x, int y, int z) {
        float dark = 0.8F;
        float light = 1.0F;
        if ((x < 0) || (y < 0) || (z < 0) || (x >= this.width) || (y >= this.depth) || (z >= this.height)) {
            return light;
        }
        if (y < this.lightDepths[(x + z * this.width)]) {
            return dark;
        }
        return light;
    }
*/

    public float getBrightness(int x, int y, int z) {
        return 1;
    }
}
