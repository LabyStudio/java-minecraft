package de.labystudio.game.world.chunk.format;

import de.labystudio.game.world.chunk.ChunkSection;

public interface WorldLoadingProgress {

    void onLoad(int x, int z, ChunkSection[] chunkSectionLayers);

}
