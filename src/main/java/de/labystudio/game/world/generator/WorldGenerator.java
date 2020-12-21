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
    private final NoiseGenerator sandInWaterNoise;
    private final NoiseGenerator forestNoise;

    private final int waterLevel = 64;

    public WorldGenerator(World world, int seed) {
        this.world = world;
        this.random = new Random(seed);

        // Create noise for the ground height
        this.groundHeightNoise = new NoiseGeneratorOctaves(this.random, 8);
        this.hillNoise = new NoiseGeneratorCombined(new NoiseGeneratorOctaves(this.random, 4),
                new NoiseGeneratorCombined(new NoiseGeneratorOctaves(this.random, 4),
                        new NoiseGeneratorOctaves(this.random, 4)));

        // Water noise
        this.sandInWaterNoise = new NoiseGeneratorOctaves(this.random, 8);

        // Population
        this.forestNoise = new NoiseGeneratorOctaves(this.random, 8);
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
                int groundHeightY = (int) (heightValue / 10 + this.waterLevel + hillValue);

                if (groundHeightY < this.waterLevel) {
                    // Generate water
                    for (int y = 0; y <= this.waterLevel; y++) {
                        // Use noise to place sand in water
                        boolean sandInWater = this.sandInWaterNoise.perlin(absoluteX, absoluteZ) < 0;
                        Block block = y > groundHeightY ? Block.WATER : groundHeightY - y < 3 && sandInWater ? Block.SAND : Block.STONE;

                        // Send water, sand and stone
                        this.world.setBlockAt(absoluteX, y, absoluteZ, block.getId());
                    }
                } else {
                    // Generate height, the highest block is grass
                    for (int y = 0; y <= groundHeightY; y++) {
                        // Use the height map to determine the start of the water by shifting it
                        boolean isBeach = heightValue < 5 && y < this.waterLevel + 2;
                        Block block = y == groundHeightY ? isBeach ? Block.SAND : Block.GRASS : groundHeightY - y < 3 ? Block.DIRT : Block.STONE;

                        // Set sand, grass, dirt and stone
                        this.world.setBlockAt(absoluteX, y, absoluteZ, block.getId());
                    }
                }
            }
        }
    }

    public void populateChunk(int chunkX, int chunkZ) {
        for (int index = 0; index < 10; index++) {
            int x = this.random.nextInt(Chunk.SIZE);
            int z = this.random.nextInt(Chunk.SIZE);

            // Absolute position of the block
            int absoluteX = chunkX * Chunk.SIZE + x;
            int absoluteZ = chunkZ * Chunk.SIZE + z;

            // Use noise for a forest pattern
            double perlin = this.forestNoise.perlin(absoluteX * 10, absoluteZ * 10);
            if (perlin > 0 && this.random.nextInt(2) == 0) {

                // Get highest block at this position
                int highestY = this.world.getHighestBlockYAt(absoluteX, absoluteZ);

                // Don't place a tree if there is no grass
                if (this.world.getBlockAt(absoluteX, highestY, absoluteZ) == Block.GRASS.getId()
                        && this.world.getBlockAt(absoluteX, highestY + 1, absoluteZ) == 0) {
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
