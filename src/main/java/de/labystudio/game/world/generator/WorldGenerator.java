package de.labystudio.game.world.generator;

import de.labystudio.game.world.World;
import de.labystudio.game.world.block.Block;
import de.labystudio.game.world.chunk.ChunkSection;
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
    private final NoiseGenerator holeNoise;
    private final NoiseGenerator islandNoise;
    private final NoiseGenerator caveNoise;

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

        // Hole in hills and islands
        this.holeNoise = new NoiseGeneratorOctaves(this.random, 3);
        this.islandNoise = new NoiseGeneratorOctaves(this.random, 3);

        // Caves
        this.caveNoise = new NoiseGeneratorOctaves(this.random, 8);

        // Population
        this.forestNoise = new NoiseGeneratorOctaves(this.random, 8);
    }

    public void generateChunk(int chunkX, int chunkZ) {
        // For each block in the chunk
        for (int relX = 0; relX < ChunkSection.SIZE; relX++) {
            for (int relZ = 0; relZ < ChunkSection.SIZE; relZ++) {

                // Absolute position of the block
                int x = chunkX * ChunkSection.SIZE + relX;
                int z = chunkZ * ChunkSection.SIZE + relZ;

                // Extract height value of the noise
                double heightValue = this.groundHeightNoise.perlin(x, z);
                double hillValue = Math.max(0, this.hillNoise.perlin(x / 18d, z / 18d) * 6);

                // Calculate final height for this position
                int groundHeightY = (int) (heightValue / 10 + this.waterLevel + hillValue);

                if (groundHeightY < this.waterLevel) {
                    // Generate water
                    for (int y = 0; y <= this.waterLevel; y++) {
                        // Use noise to place sand in water
                        boolean sandInWater = this.sandInWaterNoise.perlin(x, z) < 0;
                        Block block = y > groundHeightY ? Block.WATER : groundHeightY - y < 3 && sandInWater ? Block.SAND : Block.STONE;

                        // Send water, sand and stone
                        this.world.setBlockAt(x, y, z, block.getId());
                    }
                } else {
                    // Generate height, the highest block is grass
                    for (int y = 0; y <= groundHeightY; y++) {
                        // Use the height map to determine the start of the water by shifting it
                        boolean isBeach = heightValue < 5 && y < this.waterLevel + 2;
                        Block block = y == groundHeightY ? isBeach ? Block.SAND : Block.GRASS : groundHeightY - y < 3 ? Block.DIRT : Block.STONE;

                        // Set sand, grass, dirt and stone
                        this.world.setBlockAt(x, y, z, block.getId());
                    }
                }

                /*
                int holeY = (int) (this.holeNouse.perlin(-x / 20F, -z / 20F) * 3F + this.waterLevel + 10);
                int holeHeight = (int) this.holeNouse.perlin(x / 4F, -z / 4F);

                if (holeHeight > 0) {
                    for (int y = holeY - holeHeight; y <= holeY + holeHeight; y++) {
                        this.world.setBlockAt(x, y, z, 1);
                    }
                }
                */

                // Random holes in hills
                int holePositionY = (int) (this.holeNoise.perlin(-x / 20F, -z / 20F) * 3F + this.waterLevel + 10);
                int holeHeight = (int) this.holeNoise.perlin(x / 4F, -z / 4F);

                if (holeHeight > 0) {
                    for (int y = holePositionY - holeHeight; y <= holePositionY + holeHeight; y++) {
                        if (y > this.waterLevel) {
                            this.world.setBlockAt(x, y, z, 0);
                        }
                    }
                }

                // Floating islands
                int islandPositionY = (int) (this.islandNoise.perlin(-x / 10F, -z / 10F) * 3F + this.waterLevel + 10);
                int islandHeight = (int) (this.islandNoise.perlin(x / 4F, -z / 4F) * 4F);
                int islandRarity = (int) (this.islandNoise.perlin(x / 40F, z / 40F) * 4F) - 10;

                if (islandHeight > 0 && islandRarity > 0) {
                    for (int y = islandPositionY - islandHeight; y <= islandPositionY + islandHeight; y++) {
                        Block block = y == islandPositionY + islandHeight ? Block.GRASS : (islandPositionY + islandHeight) - y < 2 ? Block.DIRT : Block.STONE;
                        this.world.setBlockAt(x, y, z, block.getId());
                    }
                }

                // Caves
            }
        }
    }

    public void populateChunk(int chunkX, int chunkZ) {
        for (int index = 0; index < 10; index++) {
            int x = this.random.nextInt(ChunkSection.SIZE);
            int z = this.random.nextInt(ChunkSection.SIZE);

            // Absolute position of the block
            int absoluteX = chunkX * ChunkSection.SIZE + x;
            int absoluteZ = chunkZ * ChunkSection.SIZE + z;

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
