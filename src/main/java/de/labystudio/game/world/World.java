package de.labystudio.game.world;

import de.labystudio.game.util.AABB;
import de.labystudio.game.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class World {

    public static final int TOTAL_HEIGHT = Chunk.SIZE * 16 - 1;
    public Map<Long, Chunk[]> chunks = new HashMap<>();

    public boolean rebuiltThisFrame = true;
    public int updates = 0;

    public World( ) {
        load();
    }

    public void load( ) {
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
        for ( int x = -size; x < size; x++ ) {
            for ( int z = -size; z < size; z++ ) {
                long chunkIndex = (long) ( x >> 4 ) & 4294967295L | ( (long) ( z >> 4 ) & 4294967295L ) << 32;
                int chunkHeight = (int) ( chunkIndex % 10 );

                for ( int y = 0; y < 64 - chunkHeight; y++ ) {
                    setBlockAt( x, y, z, Block.GRASS.getId() );
                }
                i++;
            }
        }

        allChunksChanged();
    }

    public void save( ) {
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

    public boolean isSolidBlockAt( int x, int y, int z ) {
        return getBlockAt( x, y, z ) != 0;
    }

    public int getBlockAt( int x, int y, int z ) {
        Chunk chunk = getChunkAtBlock( x, y, z );
        return chunk == null ? 0 : chunk.getBlockAt( x & 15, y & 15, z & 15 );
    }

    public Chunk getChunkAt( int x, int y, int z ) {
        return getChunkLayersAt( x, z )[y];
    }

    public Chunk[] getChunkLayersAt( int x, int z ) {
        long chunkIndex = (long) x & 4294967295L | ( (long) z & 4294967295L ) << 32;
        Chunk[] chunkLayers = this.chunks.get( chunkIndex );
        if ( chunkLayers == null ) {
            chunkLayers = new Chunk[16];
            for ( int y = 0; y < 16; y++ ) {
                chunkLayers[y] = new Chunk( this, x, y, z );
            }

            // Copy map because of ConcurrentModificationException
            Map<Long, Chunk[]> chunks = new HashMap<>( this.chunks );
            chunks.put( chunkIndex, chunkLayers );
            this.chunks = chunks;
        }
        return chunkLayers;
    }

    public Chunk getChunkAtBlock( int x, int y, int z ) {
        Chunk[] chunkLayers = getChunkLayersAt( x >> 4, z >> 4 );
        return y < 0 || y > TOTAL_HEIGHT ? null : chunkLayers[y >> 4];
    }


    public ArrayList<AABB> getCollisionBoxes( AABB aabb ) {
        ArrayList<AABB> aABBs = new ArrayList<>();

        int minX = (int) ( Math.floor( aabb.minX ) - 1 );
        int maxX = (int) ( Math.ceil( aabb.maxX ) + 1 );
        int minY = (int) ( Math.floor( aabb.minY ) - 1 );
        int maxY = (int) ( Math.ceil( aabb.maxY ) + 1 );
        int minZ = (int) ( Math.floor( aabb.minZ ) - 1 );
        int maxZ = (int) ( Math.ceil( aabb.maxZ ) + 1 );

        for ( int x = minX; x < maxX; x++ ) {
            for ( int y = minY; y < maxY; y++ ) {
                for ( int z = minZ; z < maxZ; z++ ) {
                    if ( isSolidBlockAt( x, y, z ) ) {
                        aABBs.add( new AABB( x, y, z, x + 1, y + 1, z + 1 ) );
                    }
                }
            }
        }
        return aABBs;
    }

    public void blockChanged( int x, int y, int z ) {
        setDirty( x - 1, y - 1, z - 1, x + 1, y + 1, z + 1 );
    }

    public void setDirty( int minX, int minY, int minZ, int maxX, int maxY, int maxZ ) {
        // To chunk coordinates
        minX = minX >> 4;
        maxX = maxX >> 4;
        minY = minY >> 4;
        maxY = maxY >> 4;
        minZ = minZ >> 4;
        maxZ = maxZ >> 4;

        // Minimum and maximum y
        minY = Math.max( 0, minY );
        maxY = Math.min( 15, maxY );

        for ( int x = minX; x <= maxX; x++ ) {
            for ( int y = minY; y <= maxY; y++ ) {
                for ( int z = minZ; z <= maxZ; z++ ) {
                    getChunkAt( x, y, z ).setDirty();
                }
            }
        }
    }

    public void allChunksChanged( ) {
        for ( Chunk[] chunkLayers : this.chunks.values() ) {
            for ( Chunk chunk : chunkLayers ) {
                chunk.setDirty();
            }
        }
    }

    public void setBlockAt( int x, int y, int z, int type ) {
        Chunk chunk = getChunkAtBlock( x, y, z );
        if ( chunk != null ) {
            chunk.setBlockAt( x & 15, y & 15, z & 15, type );

            //chunk.calcLightDepths(x & 15, z & 15, 1, 1);
            blockChanged( x, y, z );
        }
    }

    public void lightColumnChanged( int x, int z, int minY, int maxY ) {
        setDirty( x - 1, minY - 1, z - 1, x + 1, maxY + 1, z + 1 );
    }

    public float getBrightnessAtBlock( int x, int y, int z ) {
        Chunk chunk = getChunkAtBlock( x, y, z );
        return chunk == null ? 0 : chunk.getBrightnessAt( x & 15, y & 15, z & 15 );
    }
}
