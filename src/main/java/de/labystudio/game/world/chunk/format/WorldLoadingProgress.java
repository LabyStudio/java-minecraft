package de.labystudio.game.world.chunk.format;

import de.labystudio.game.world.chunk.Chunk;

public interface WorldLoadingProgress {

    void onLoad(int x, int z, Chunk[] chunkLayers);

}
