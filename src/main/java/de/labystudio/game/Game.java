package de.labystudio.game;

import de.labystudio.game.player.Player;
import de.labystudio.game.render.Gui;
import de.labystudio.game.util.AABB;
import de.labystudio.game.util.EnumBlockFace;
import de.labystudio.game.util.HitResult;
import de.labystudio.game.util.Timer;
import de.labystudio.game.world.World;
import de.labystudio.game.world.WorldRenderer;
import de.labystudio.game.world.block.Block;
import de.labystudio.game.world.chunk.Chunk;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Game implements Runnable {

    private final GameWindow gameWindow = new GameWindow(this);
    protected final Gui gui = new Gui();

    // Game
    private final Timer timer = new Timer(20.0F);
    private World world;
    private WorldRenderer worldRenderer;
    private Player player;

    // States
    private boolean paused = false;
    private boolean running = true;

    public void init() throws LWJGLException {
        // Setup display
        this.gameWindow.init();
        this.gui.init(this.gameWindow);
        this.gui.loadTextures();

        // Setup rendering
        this.world = new World();
        this.worldRenderer = new WorldRenderer(this.world);
        this.player = new Player(this.world);

        // Setup controls
        Keyboard.create();
        Mouse.create();
        Mouse.setGrabbed(true);
    }

    public void run() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        long lastTime = System.currentTimeMillis();
        int frames = 0;
        try {
            do {
                this.timer.advanceTime();
                for (int i = 0; i < this.timer.ticks; i++) {
                    tick();
                }

                // Limit framerate
                Thread.sleep(5L);

                GL11.glViewport(0, 0, this.gameWindow.displayWidth, this.gameWindow.displayHeight);
                render(this.timer.partialTicks);
                this.gameWindow.update();
                checkError();

                frames++;
                while (System.currentTimeMillis() >= lastTime + 1000L) {
                    //System.out.println(frames + " fps, " + this.world.updates);
                    this.world.updates = 0;

                    lastTime += 1000L;
                    frames = 0;
                }

                // Escape
                if (Keyboard.isKeyDown(1) || !Display.isActive()) {
                    paused = true;
                    Mouse.setGrabbed(false);
                }

                // Toggle fullscreen
                if (Keyboard.isKeyDown(87)) {
                    this.gameWindow.toggleFullscreen();
                }

            } while (!Display.isCloseRequested() && this.running);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Shutdown
            this.world.save();
            this.gameWindow.destroy();

            Mouse.destroy();
            Keyboard.destroy();

            System.exit(0);
        }
    }

    public void shutdown() {
        this.running = false;
    }

    public void tick() {
        this.player.onTick();
        this.world.onTick();
    }

    private void moveCameraToPlayer(float partialTicks) {
        GL11.glTranslatef(0.0F, 0.0F, -0.3F);
        GL11.glRotatef(this.player.pitch, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(this.player.yaw, 0.0F, 1.0F, 0.0F);

        double x = this.player.prevX + (this.player.x - this.player.prevX) * partialTicks;
        double y = this.player.prevY + (this.player.y - this.player.prevY) * partialTicks;
        double z = this.player.prevZ + (this.player.z - this.player.prevZ) * partialTicks;
        GL11.glTranslated(-x, -y, -z);

        // Eye height
        GL11.glTranslatef(0.0F, -this.player.getEyeHeight(), 0.0F);
    }

    private void setupCamera(float partialTicks) {
        int zFar = (WorldRenderer.RENDER_DISTANCE * 2) * Chunk.SIZE;

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(85.0F + this.player.getFOVModifier(),
                (float) this.gameWindow.displayWidth / (float) this.gameWindow.displayHeight, 0.05F, zFar);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        moveCameraToPlayer(partialTicks);
    }


    public void render(float partialTicks) {
        float mouseMoveX = Mouse.getDX();
        float mouseMoveY = Mouse.getDY();

        if (!paused) {
            this.player.turn(mouseMoveX, mouseMoveY);
        }

        HitResult hitResult = getTargetBlock(partialTicks);

        while (Mouse.next()) {
            if ((Mouse.getEventButton() == 0) && (Mouse.getEventButtonState())) {
                if (paused) {
                    paused = false;
                    Mouse.setGrabbed(true);
                } else {
                    if (hitResult != null) {
                        this.world.setBlockAt(hitResult.x, hitResult.y, hitResult.z, 0);
                    }
                }
            }
            if ((Mouse.getEventButton() == 1) && (Mouse.getEventButtonState())) {
                if (hitResult != null) {
                    int x = hitResult.x + hitResult.face.x;
                    int y = hitResult.y + hitResult.face.y;
                    int z = hitResult.z + hitResult.face.z;

                    AABB placedBoundingBox = new AABB(x, y, z, x + 1, y + 1, z + 1);
                    if (!placedBoundingBox.intersects(this.player.boundingBox)) {
                        this.world.setBlockAt(x, y, z, Block.STONE.getId());
                    }

                }
            }
        }
        while (Keyboard.next()) {
            if ((Keyboard.getEventKey() == 28) && (Keyboard.getEventKeyState())) {
                this.world.save();
            }
        }

        GL11.glClear(16640);
        setupCamera(partialTicks);

        GL11.glEnable(GL11.GL_CULL_FACE);

        // Fog
        GL11.glEnable(GL11.GL_FOG);
        this.worldRenderer.setupFog();

        GL11.glDisable(GL11.GL_FOG);
        this.worldRenderer.render((int) this.player.x >> 4, (int) this.player.z >> 4);
        GL11.glEnable(GL11.GL_FOG);

        if (hitResult != null) {
            renderSelection(hitResult);
        }

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_FOG);

        this.gui.setupCamera();
        this.gui.renderCrosshair();
    }

    public void renderSelection(HitResult hitResult) {
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);
        GL11.glLineWidth(1);
        drawBoundingBox(hitResult.x, hitResult.y, hitResult.z,
                hitResult.x + 1, hitResult.y + 1, hitResult.z + 1);
    }

    private void drawBoundingBox(double minX, double minY, double minZ,
                                 double maxX, double maxY, double maxZ) {

        // Bottom
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glEnd();

        // Ceiling
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glEnd();
    }

    private HitResult getTargetBlock(float partialTicks) {
        double yaw = Math.toRadians(-this.player.yaw + 90);
        double pitch = Math.toRadians(-this.player.pitch);

        double xzLen = Math.cos(pitch);
        double vectorX = xzLen * Math.cos(yaw);
        double vectorY = Math.sin(pitch);
        double vectorZ = xzLen * Math.sin(-yaw);

        double targetX = this.player.x - vectorX;
        double targetY = this.player.y + this.player.getEyeHeight() - 0.08D - vectorY;
        double targetZ = this.player.z - vectorZ;

        int shift = -1;

        int prevAirX = (int) (targetX < 0 ? targetX + shift : targetX);
        int prevAirY = (int) (targetY < 0 ? targetY + shift : targetY);
        int prevAirZ = (int) (targetZ < 0 ? targetZ + shift : targetZ);

        for (int i = 0; i < 800; i++) {
            targetX += vectorX / 10D;
            targetY += vectorY / 10D;
            targetZ += vectorZ / 10D;

            int hitX = (int) (targetX < 0 ? targetX + shift : targetX);
            int hitY = (int) (targetY < 0 ? targetY + shift : targetY);
            int hitZ = (int) (targetZ < 0 ? targetZ + shift : targetZ);

            EnumBlockFace targetFace = null;
            for (EnumBlockFace type : EnumBlockFace.values()) {
                if (prevAirX == hitX + type.x && prevAirY == hitY + type.y && prevAirZ == hitZ + type.z) {
                    targetFace = type;
                    break;
                }
            }

            if (this.world.isSolidBlockAt(hitX, hitY, hitZ)) {
                if (targetFace == null) {
                    return null;
                }
                return new HitResult(hitX, hitY, hitZ, targetFace);
            } else {
                prevAirX = (int) (targetX < 0 ? targetX + shift : targetX);
                prevAirY = (int) (targetY < 0 ? targetY + shift : targetY);
                prevAirZ = (int) (targetZ < 0 ? targetZ + shift : targetZ);
            }
        }
        return null;
    }

    public static void checkError() {
        int error = GL11.glGetError();
        if (error != 0) {
            throw new IllegalStateException(GLU.gluErrorString(error));
        }
    }

    public static void main(String[] args) throws LWJGLException {
        new Thread(new Game(), "Game Thread").start();
    }
}
