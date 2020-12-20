package de.labystudio.game.world.generator;

import de.labystudio.game.world.World;
import de.labystudio.game.world.block.Block;
import de.labystudio.game.world.chunk.Chunk;
import de.labystudio.game.world.generator.noise.NoiseGeneratorCombined;
import de.labystudio.game.world.generator.noise.NoiseGeneratorOctaves;

import java.util.Random;

public final class WorldGenerator {

    private final World world;
    private final Random random;

    private final NoiseGenerator groundHeightNoise;
    private final NoiseGenerator hillNoise;

    public WorldGenerator(World world, int seed) {
        this.world = world;
        this.random = new Random(seed);

        // Create noise for the ground height
        this.groundHeightNoise = new NoiseGeneratorOctaves(this.random, 8);
        this.hillNoise = new NoiseGeneratorCombined(new NoiseGeneratorOctaves(this.random, 4),
                new NoiseGeneratorCombined(new NoiseGeneratorOctaves(this.random, 4),
                        new NoiseGeneratorOctaves(this.random, 8)));
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
                double hillValue = Math.max(0, this.hillNoise.perlin(absoluteX / 18d, absoluteZ / 18d) * 6);

                // Calculate final height for this position
                int groundHeightY = (int) (heightValue / 10 + 60 + hillValue);

                // Generate height, the highest block is grass
                for (int y = 0; y <= groundHeightY; y++) {
                    int blockId = y == groundHeightY ? Block.GRASS.getId() : groundHeightY - y < 3 ? Block.DIRT.getId() : Block.STONE.getId();

                    this.world.setBlockAt(absoluteX, y, absoluteZ, blockId);
                }
            }
        }
    }

    public void populateChunk(int chunkX, int chunkZ) {
        for (int index = 0; index < 10; index++) {
            if (this.random.nextInt(20) == 0) {
                int x = this.random.nextInt(Chunk.SIZE);
                int z = this.random.nextInt(Chunk.SIZE);

                // Absolute position of the block
                int absoluteX = chunkX * Chunk.SIZE + x;
                int absoluteZ = chunkZ * Chunk.SIZE + z;

                // Get highest block at this position
                int highestY = this.world.getHighestBlockYAt(absoluteX, absoluteZ);

                // Don't place a tree if there is no grass
                if (this.world.getBlockAt(absoluteX, highestY, absoluteZ) == Block.GRASS.getId()) {
                    int treeHeight = this.random.nextInt(2) + 5;

                    // Create tree log
                    for (int i = 0; i < treeHeight; i++) {
                        this.world.setBlockAt(absoluteX, highestY + i + 1, absoluteZ, Block.LOG.getId());
                    }

                    // Create big leave ring
                    for (int tx = -2; tx <= 2; tx++) {
                        for (int ty = 0; ty < 2; ty++) {
                            for (int tz = -2; tz <= 2; tz++) {
                                boolean isCorner = Math.abs(tx) == 2 && Math.abs(tz) == 2;
                                if (isCorner && this.random.nextBoolean()) {
                                    continue;
                                }

                                // Place leave if there is no block yet
                                if (!this.world.isSolidBlockAt(absoluteX + tx, highestY + treeHeight + ty - 2, absoluteZ + tz)) {
                                    this.world.setBlockAt(absoluteX + tx, highestY + treeHeight + ty - 2, absoluteZ + tz, Block.LEAVE.getId());
                                }
                            }
                        }
                    }

                    // Create small leave ring on top
                    for (int tx = -1; tx <= 1; tx++) {
                        for (int ty = 0; ty < 2; ty++) {
                            for (int tz = -1; tz <= 1; tz++) {
                                boolean isCorner = Math.abs(tx) == 1 && Math.abs(tz) == 1;
                                if (isCorner && (ty == 1 || this.random.nextBoolean())) {
                                    continue;
                                }

                                // Place leave if there is no block yet
                                if (!this.world.isSolidBlockAt(absoluteX + tx, highestY + treeHeight + ty, absoluteZ + tz)) {
                                    this.world.setBlockAt(absoluteX + tx, highestY + treeHeight + ty, absoluteZ + tz, Block.LEAVE.getId());
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
