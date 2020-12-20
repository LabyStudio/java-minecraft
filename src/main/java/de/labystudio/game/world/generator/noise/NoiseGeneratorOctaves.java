package de.labystudio.game.world.generator.noise;

import de.labystudio.game.world.generator.NoiseGenerator;

import java.util.Random;

public final class NoiseGeneratorOctaves extends NoiseGenerator {

    private final NoiseGeneratorPerlin[] generatorCollection;
    private final int octaves;

    public NoiseGeneratorOctaves(Random random, int octaves) {
        this.octaves = octaves;
        this.generatorCollection = new NoiseGeneratorPerlin[octaves];

        for (int i = 0; i < octaves; ++i) {
            this.generatorCollection[i] = new NoiseGeneratorPerlin(random);
        }
    }

    @Override
    public final double perlin(double x, double z) {
        double total = 0.0;
        double frequency = 1.0;
        for (int i = 0; i < this.octaves; ++i) {
            total += this.generatorCollection[i].perlin(x / frequency, z / frequency) * frequency;
            frequency *= 2.0;
        }
        return total;
    }
}
