package de.labystudio.game;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MinecraftWindow {

    public static final int DEFAULT_WIDTH = 854;
    public static final int DEFAULT_HEIGHT = 480;

    private final Minecraft game;

    protected final Canvas canvas;
    protected final Frame frame;

    protected boolean fullscreen;
    protected boolean enableVsync;

    public int displayWidth = DEFAULT_WIDTH;
    public int displayHeight = DEFAULT_HEIGHT;

    public MinecraftWindow(Minecraft game) {
        this.game = game;

        // Create canvas
        this.canvas = new Canvas();
        this.canvas.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        // Create frame
        this.frame = new Frame("3DGame");
        this.frame.setLayout(new BorderLayout());
        this.frame.add(this.canvas, "Center");
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);

        // Close listener
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                game.shutdown();
            }
        });
    }

    public void init() throws LWJGLException {
        Graphics g = this.canvas.getGraphics();
        if (g != null) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, this.displayWidth, this.displayHeight);
            g.dispose();
        }
        Display.setParent(this.canvas);

        // Init
        Display.setTitle(this.frame.getTitle());

        try {
            Display.create();
        } catch (LWJGLException lwjglexception) {
            lwjglexception.printStackTrace();

            // Try again in one second
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {
            }

            Display.create();
        }

        Display.swapBuffers();
    }

    public void toggleFullscreen() {
        try {
            this.fullscreen = !this.fullscreen;

            System.out.println("Toggle fullscreen!");

            if (this.fullscreen) {
                Display.setDisplayMode(Display.getDesktopDisplayMode());

                this.displayWidth = Display.getDisplayMode().getWidth();
                this.displayHeight = Display.getDisplayMode().getHeight();

                if (this.displayWidth <= 0) {
                    this.displayWidth = 1;
                }
                if (this.displayHeight <= 0) {
                    this.displayHeight = 1;
                }
            } else {
                this.displayWidth = this.canvas.getWidth();
                this.displayHeight = this.canvas.getHeight();

                if (this.displayWidth <= 0) {
                    this.displayWidth = 1;
                }
                if (this.displayHeight <= 0) {
                    this.displayHeight = 1;
                }

                Display.setDisplayMode(new org.lwjgl.opengl.DisplayMode(DEFAULT_WIDTH, DEFAULT_HEIGHT));
            }

            Display.setFullscreen(this.fullscreen);
            Display.update();

            Thread.sleep(1000L);
            System.out.println("Size: " + this.displayWidth + ", " + this.displayHeight);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void update() {
        Display.update();

        if (!this.fullscreen && (this.canvas.getWidth() != this.displayWidth || this.canvas.getHeight() != this.displayHeight)) {
            this.displayWidth = this.canvas.getWidth();
            this.displayHeight = this.canvas.getHeight();

            if (this.displayWidth <= 0) {
                this.displayWidth = 1;
            }

            if (this.displayHeight <= 0) {
                this.displayHeight = 1;
            }

            this.resize(this.displayWidth, this.displayHeight);
        }
    }

    private void resize(int width, int height) {
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }

        this.displayWidth = width;
        this.displayHeight = height;

        this.game.gui.init(this);
    }

    public void destroy() {
        Display.destroy();
    }
}
