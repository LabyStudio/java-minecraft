package de.labystudio.game.util;

public class Timer {
    private static final long NS_PER_SECOND = 1000000000L;
    private static final long MAX_NS_PER_UPDATE = 1000000000L;
    private static final int MAX_TICKS_PER_UPDATE = 100;
    private float ticksPerSecond;
    private long lastTime;
    public int ticks;
    public float partialTicks;
    public float timeScale = 1.0F;
    public float fps = 0.0F;
    public float passedTime = 0.0F;

    public Timer(float ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
        this.lastTime = System.nanoTime();
    }

    public void advanceTime() {
        long now = System.nanoTime();
        long passedNs = now - this.lastTime;
        this.lastTime = now;
        if (passedNs < 0L) {
            passedNs = 0L;
        }
        if (passedNs > MAX_NS_PER_UPDATE) {
            passedNs = MAX_NS_PER_UPDATE;
        }
        this.fps = ((float) (NS_PER_SECOND / passedNs));

        this.passedTime += (float) passedNs * this.timeScale * this.ticksPerSecond / 1.0E+009F;

        this.ticks = ((int) this.passedTime);
        if (this.ticks > MAX_TICKS_PER_UPDATE) {
            this.ticks = MAX_TICKS_PER_UPDATE;
        }
        this.passedTime -= this.ticks;
        this.partialTicks = this.passedTime;
    }
}
