package de.labystudio.game.world.generator.noise;

import de.labystudio.game.world.generator.NoiseGenerator;

public final class NoiseGeneratorCombined extends NoiseGenerator {

    private final NoiseGenerator firstGenerator;
    private final NoiseGenerator secondGenerator;

    public NoiseGeneratorCombined(NoiseGenerator firstGenerator, NoiseGenerator secondGenerator) {
        this.firstGenerator = firstGenerator;
        this.secondGenerator = secondGenerator;
    }

    @Override
    public final double perlin(double x,  double z) {
        return this.firstGenerator.perlin(x + this.secondGenerator.perlin(x, z), z);
    }
}
