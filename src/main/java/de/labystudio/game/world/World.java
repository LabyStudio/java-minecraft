package de.labystudio.game.world;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.labystudio.game.util.AABB;

public class World {
    public final int width;
    public final int height;
    public final int depth;
    private byte[] blocks;
    private int[] lightDepths;
    private ArrayList<WorldListener> worldListeners = new ArrayList();

    public World(int w, int h, int d) {
        this.width = w;
        this.height = h;
        this.depth = d;
        this.blocks = new byte[w * h * d];

        this.lightDepths = new int[w * h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < d; y++) {
                for (int z = 0; z < h; z++) {
                    int i = (y * this.height + z) * this.width + x;
                    this.blocks[i] = ((byte) (y <= d * 2 / 3 ? 1 : 0));
                }
            }
        }
        calcLightDepths(0, 0, w, h);

        load();
    }

    public void load() {
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
    }

    public void save() {
        try {
            DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File("level.dat"))));
            dos.write(this.blocks);
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public boolean isTile(int x, int y, int z) {
        if ((x < 0) || (y < 0) || (z < 0) || (x >= this.width) || (y >= this.depth) || (z >= this.height)) {
            return false;
        }
        return this.blocks[((y * this.height + z) * this.width + x)] == 1;
    }

    public boolean isSolidTile(int x, int y, int z) {
        return isTile(x, y, z);
    }

    public boolean isLightBlocker(int x, int y, int z) {
        return isSolidTile(x, y, z);
    }

    public ArrayList<AABB> getCubes(AABB aABB) {
        ArrayList<AABB> aABBs = new ArrayList();
        int minX = (int) aABB.minX;
        int maxX = (int) (aABB.maxX + 1.0F);
        int minY = (int) aABB.minY;
        int maxY = (int) (aABB.maxY + 1.0F);
        int minZ = (int) aABB.minZ;
        int maxZ = (int) (aABB.maxZ + 1.0F);
        if (minX < 0) {
            minX = 0;
        }
        if (minY < 0) {
            minY = 0;
        }
        if (minZ < 0) {
            minZ = 0;
        }
        if (maxX > this.width) {
            maxX = this.width;
        }
        if (maxY > this.depth) {
            maxY = this.depth;
        }
        if (maxZ > this.height) {
            maxZ = this.height;
        }
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    if (isSolidTile(x, y, z)) {
                        aABBs.add(new AABB(x, y, z, x + 1, y + 1, z + 1));
                    }
                }
            }
        }
        return aABBs;
    }

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

    public void setTile(int x, int y, int z, int type) {
        if ((x < 0) || (y < 0) || (z < 0) || (x >= this.width) || (y >= this.depth) || (z >= this.height)) {
            return;
        }
        this.blocks[((y * this.height + z) * this.width + x)] = ((byte) type);
        calcLightDepths(x, z, 1, 1);
        for (int i = 0; i < this.worldListeners.size(); i++) {
            ((WorldListener) this.worldListeners.get(i)).tileChanged(x, y, z);
        }
    }
}
