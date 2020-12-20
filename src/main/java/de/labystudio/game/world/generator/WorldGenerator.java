package de.labystudio.game.world.generator;

import de.labystudio.game.world.World;
import de.labystudio.game.world.block.Block;
import de.labystudio.game.world.chunk.Chunk;
import de.labystudio.game.world.generator.noise.NoiseGeneratorOctaves;

import java.util.Random;

public final class WorldGenerator {

    private final World world;
    private final Random random;

    private final NoiseGenerator groundHeightNoise;

    public WorldGenerator(World world, int seed) {
        this.world = world;
        this.random = new Random(seed);

        // Create noise for the ground height
        this.groundHeightNoise = new NoiseGeneratorOctaves(this.random, 8);
    }

    public void generateChunk(int chunkX, int chunkZ) {
        // For each block in the chunk
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int z = 0; z < Chunk.SIZE; z++) {

                // Absolute position of the block
                int absoluteX = chunkX * Chunk.SIZE + x;
                int absoluteZ = chunkZ * Chunk.SIZE + z;

                // Extract height value of the noise
                double heightValue = this.groundHeightNoise.perlin(absoluteX, absoluteZ);
                int groundHeightY = (int) heightValue / 10 + 60;

                // Generate height, the highest block is grass
                for (int y = 0; y <= groundHeightY; y++) {
                    this.world.setBlockAt(absoluteX, y, absoluteZ, y == groundHeightY ? Block.GRASS.getId() : Block.STONE.getId());
                }
            }
        }
    }
}
