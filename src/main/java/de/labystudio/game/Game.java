package de.labystudio.game;

import java.io.IOException;
import java.nio.FloatBuffer;

import de.labystudio.game.render.Tesselator;
import de.labystudio.game.player.Player;
import de.labystudio.game.util.AABB;
import de.labystudio.game.util.EnumBlockFace;
import de.labystudio.game.util.HitResult;
import de.labystudio.game.util.Timer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import de.labystudio.game.world.World;
import de.labystudio.game.world.WorldRenderer;

public class Game implements Runnable {
    private static final boolean FULLSCREEN_MODE = false;
    private int width;
    private int height;
    private FloatBuffer fogColor = BufferUtils.createFloatBuffer(4);
    private Timer timer = new Timer(20.0F);
    private World world;
    private WorldRenderer worldRenderer;
    private Player player;

    private boolean paused = false;

    private HitResult hitResult = null;

    private Tesselator tesselator = new Tesselator();

    public void init() throws LWJGLException, IOException {
        int col = 920330;
        float fr = 0.0F;
        float fg = 0.0F;
        float fb = 0.0F;
        this.fogColor.put(
                new float[]{(col >> 16 & 0xFF) / 255.0F, (col >> 8 & 0xFF) / 255.0F, (col & 0xFF) / 255.0F, 1.0F});
        this.fogColor.flip();

        Display.setDisplayMode(new DisplayMode(1600, 980));

        Display.create();
        Keyboard.create();
        Mouse.create();

        this.width = Display.getDisplayMode().getWidth();
        this.height = Display.getDisplayMode().getHeight();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glClearColor(fr, fg, fb, 0.0F);
        GL11.glClearDepth(1.0D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        this.world = new World();
        this.worldRenderer = new WorldRenderer(this.world);
        this.player = new Player(this.world);

        Mouse.setGrabbed(true);
    }

    public void destroy() {
        this.world.save();

        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
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
                render(this.timer.partialTicks);
                frames++;
                while (System.currentTimeMillis() >= lastTime + 1000L) {
                    System.out.println(frames + " fps, " + this.world.updates);
                    this.world.updates = 0;

                    lastTime += 1000L;
                    frames = 0;
                }
                if (Keyboard.isKeyDown(1)) {
                    paused = true;
                    Mouse.setGrabbed(false);
                    // break;
                }
            } while (!Display.isCloseRequested());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            destroy();
        }
    }

    public void tick() {
        this.player.tick();
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
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(85.0F + this.player.getFOVModifier(), 1.5f, 0.05F, 1000.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        moveCameraToPlayer(partialTicks);
    }

    private void setupUICamera(float partialTicks) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(45.0F, 1.5f, 0.05F, 1000.0F);
        //GL11.glOrtho(GL11.GL_POINTS, 1600, 0, 980, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glTranslatef(GL11.GL_POINTS, 0, -0.1F);
    }

    public void render(float partialTicks) {
        float mouseMoveX = Mouse.getDX();
        float mouseMoveY = Mouse.getDY();

        if (!paused) {
            this.player.turn(mouseMoveX, mouseMoveY);
        }

        this.hitResult = getTargetBlock(partialTicks);

        while (Mouse.next()) {
            if ((Mouse.getEventButton() == 0) && (Mouse.getEventButtonState())) {
                if (paused) {
                    paused = false;
                    Mouse.setGrabbed(true);
                } else {
                    if (this.hitResult != null) {
                        this.world.setBlockAt(this.hitResult.x, this.hitResult.y, this.hitResult.z, 0);
                    }
                }
            }
            if ((Mouse.getEventButton() == 1) && (Mouse.getEventButtonState())) {
                if (this.hitResult != null) {
                    int x = this.hitResult.x - this.hitResult.face.x;
                    int y = this.hitResult.y - this.hitResult.face.y;
                    int z = this.hitResult.z - this.hitResult.face.z;

                    AABB placedBoundingBox = new AABB(x, y, z, x + 1, y + 1, z + 1);
                    if (!placedBoundingBox.intersects(this.player.boundingBox)) {
                        this.world.setBlockAt(x, y, z, 1);
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
        GL11.glEnable(GL11.GL_FOG);

        GL11.glFogi(GL11.GL_FOG_MODE, 2048);
        GL11.glFogf(GL11.GL_FOG_DENSITY, 0.2F);
        GL11.glFog(GL11.GL_FOG_COLOR, this.fogColor);

        GL11.glDisable(GL11.GL_FOG);
        this.worldRenderer.render((int) this.player.x >> 4, (int) this.player.z >> 4, 0);
        GL11.glEnable(GL11.GL_FOG);
        this.worldRenderer.render((int) this.player.x >> 4, (int) this.player.z >> 4, 1);

        if (this.hitResult != null) {
            renderSelection(this.hitResult);
        }

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_FOG);

        setupUICamera(partialTicks);
        renderCrosshair(partialTicks);

        Display.update();
    }

    private void renderCrosshair(float partialTicks) {
        // GL11.glEnable(GL11.GL_BLEND);
        GL11.glLogicOp(GL11.GL_COPY_INVERTED);
        GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);

        // GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        double size = 0.001;

        GL11.glLineWidth(4);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(-size, 0, 0);
        GL11.glVertex3d(size, 0, 0);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(GL11.GL_POINTS, -size, 0);
        GL11.glVertex3d(GL11.GL_POINTS, size, 0);
        GL11.glEnd();

        GL11.glLogicOp(GL11.GL_COPY);
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
        // GL11.glDisable(GL11.GL_BLEND);
    }

    public void renderSelection(HitResult hitResult) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        drawBoundingBox(hitResult.x, hitResult.y, hitResult.z,
                hitResult.x + 1, hitResult.y + 1, hitResult.z + 1);
    }

    private void drawBoundingBox(double minX, double minY, double minZ,
                                 double maxX, double maxY, double maxZ) {
        GL11.glLineWidth(4);

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
                if (prevAirX == hitX - type.x && prevAirY == hitY - type.y && prevAirZ == hitZ - type.z) {
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
        new Thread(new Game()).start();
    }
}
