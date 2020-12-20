package de.labystudio.game.world.generator.noise;

import de.labystudio.game.world.generator.NoiseGenerator;

import java.util.Random;

public final class NoiseGeneratorPerlin extends NoiseGenerator {
    private final int[] permutations;

    public NoiseGeneratorPerlin() {
        this(new Random());
    }

    public NoiseGeneratorPerlin(Random random) {
        this.permutations = new int[512];
        for (int i = 0; i < 256; ++i) {
            this.permutations[i] = i;
        }
        for (int i = 0; i < 256; ++i) {
            final int n = random.nextInt(256 - i) + i;
            final int n2 = this.permutations[i];
            this.permutations[i] = this.permutations[n];
            this.permutations[n] = n2;
            this.permutations[i + 256] = this.permutations[i];
        }
    }

    private static double fade(double t) {
        // Fade function as defined by Ken Perlin.  This eases coordinate values
        // so that they will "ease" towards integral values.  This ends up smoothing
        // the final output.
        return t * t * t * (t * (t * 6 - 15) + 10);            // 6t^5 - 15t^4 + 10t^3
    }

    private static double lerp(double x, double a, double b) {
        return a + x * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;                                    // Take the hashed value and take the first 4 bits of it (15 == 0b1111)
        double u = h < 8 /* 0b1000 */ ? x : y;                // If the most significant bit (MSB) of the hash is 0 then set u = x.  Otherwise y.

        double v;                                             // In Ken Perlin's original implementation this was another conditional operator (?:).  I
        // expanded it for readability.
        if (h < 4 /* 0b0100 */)                                // If the first and second significant bits are 0 set v = y
            v = y;
        else if (h == 12 /* 0b1100 */ || h == 14 /* 0b1110*/)  // If the first and second significant bits are 1 set v = x
            v = x;
        else                                                  // If the first and second significant bits are not equal (0/1, 1/0) set v = z
            v = z;

        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v); // Use the last 2 bits to decide if u and v are positive or negative.  Then return their addition.
    }

    @Override
    public final double perlin(double x, double z) {
        double y;

        int xi = (int) Math.floor(x) & 0xFF;
        int zi = (int) Math.floor(z) & 0xFF;
        int yi = (int) Math.floor(0.0) & 0xFF;

        x -= Math.floor(x);
        z -= Math.floor(z);
        y = 0.0 - Math.floor(0.0);

        double u = fade(x);
        double w = fade(z);
        double v = fade(y);

        int xzi = this.permutations[xi] + zi;
        int xzyi = this.permutations[xzi] + yi;

        xzi = this.permutations[xzi + 1] + yi;
        xi = this.permutations[xi + 1] + zi;
        zi = this.permutations[xi] + yi;
        xi = this.permutations[xi + 1] + yi;

        return lerp(v,
                lerp(w,
                        lerp(u,
                                grad(this.permutations[xzyi], x, z, y),
                                grad(this.permutations[zi], x - 1.0, z, y)),
                        lerp(u,
                                grad(this.permutations[xzi], x, z - 1.0, y),
                                grad(this.permutations[xi], x - 1.0, z - 1.0, y))),
                lerp(w,
                        lerp(u,
                                grad(this.permutations[xzyi + 1], x, z, y - 1.0),
                                grad(this.permutations[zi + 1], x - 1.0, z, y - 1.0)),
                        lerp(u,
                                grad(this.permutations[xzi + 1], x, z - 1.0, y - 1.0),
                                grad(this.permutations[xi + 1], x - 1.0, z - 1.0, y - 1.0))));
    }
}
