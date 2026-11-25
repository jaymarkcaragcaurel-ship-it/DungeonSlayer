package Systems.UI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Pixel-art loading screen for Dungeon Crawler.
 *
 * Usage (from the main thread, NOT the Swing event thread):
 *   LoadingScreen screen = new LoadingScreen();
 *   screen.addStep("Reading class data", () -> ClassSystem.count());
 *   boolean ok = screen.run();
 *
 * Same API as before: run() opens the window, executes each step on the
 * calling thread, then waits for the player to click "Begin your descent".
 * Returns true if every step succeeded and the player clicked through.
 *
 * LOGO: drop your finished logo at  src/main/resources/logo.png  (classpath
 * root "/logo.png") or at  assets/logo.png  next to the game. It is scaled
 * with nearest-neighbour so pixel art stays crisp. Until then, a built-in
 * pixel-style text title is shown.
 *
 * A LoadingScreen instance is single-use.
 */
public final class LoadingScreen {

    /** A unit of startup work. May throw. */
    @FunctionalInterface
    public interface Task {
        void run() throws Exception;
    }

    private record Step(String label, Task task) {}

    // Layout: the scene is drawn small (LW x LH) and scaled up for a chunky pixel look.
    private static final int SCALE = 4;
    private static final int LW = 160, LH = 100;
    private static final int W = LW * SCALE, H = LH * SCALE;

    private static final Color BG     = new Color(0x15, 0x11, 0x0E);
    private static final Color GOLD   = new Color(0xE8, 0xB8, 0x4A);
    private static final Color GOLD_L = new Color(0xFF, 0xE0, 0x8A);
    private static final Color TEXT   = new Color(0xD8, 0xCF, 0xC0);
    private static final Color DIM    = new Color(0x8C, 0x82, 0x72);
    private static final Color BAR_BG = new Color(0x2A, 0x24, 0x1E);
    private static final Color BAD    = new Color(0xE0, 0x5A, 0x4F);

    private static final String[] TIPS = {
        "Combat is real time. Monsters keep attacking while you choose.",
        "Bosses can't be fled from. Stock up on potions first.",
        "Floor bosses guard the stairs. The dungeon boss guards the loot.",
        "The final boss enrages at low HP. Save your best skill for then.",
        "Prefixes and suffixes on weapons can change your stats.",
        "Your pack holds 30 items. Sell the junk before it fills up.",
        "Armor can never be the thing that kills you. Equip freely.",
        "Warrior, Mage, Paladin, Archer, Assassin: five paths, one dungeon.",
        "Level 30 is the cap. Every level grows your stats.",
        "Retreat is always cheaper than dying."
    };

    // Tiny hero sprite (8x8) that runs along the progress bar.
    private static final String[] HERO = {
        "..gggg..",
        ".gggggg.",
        ".gsssg..",
        "..rrrr.w",
        ".rrrrrrw",
        "..rrrr.w",
        "..d..d..",
        ".dd..dd."
    };

    private final List<Step> steps = new ArrayList<>();
    private final boolean animate;
    private final long minStepMillis;
    private final Random rng;

    // Released when the player clicks the button (or closes the window).
    private final CountDownLatch dismissed = new CountDownLatch(1);
    private volatile boolean closed = false;

    // Swing components: only touched on the event thread.
    private JFrame frame;
    private Scene scene;
    private JButton button;
    private Timer ticker;

    /** Default: animated, each step shown at least 250 ms. */
    public LoadingScreen() {
        this(true, 250L, new Random());
    }

    public LoadingScreen(boolean animate, long minStepMillis, Random rng) {
        this.animate = animate;
        this.minStepMillis = Math.max(0, minStepMillis);
        this.rng = rng;
    }

    public LoadingScreen addStep(String label, Task task) {
        if (label == null || task == null) throw new IllegalArgumentException("label and task required");
        steps.add(new Step(label, task));
        return this;
    }

    /** Shows the window, runs all steps, waits for the player. Returns true on success. */
    public boolean run() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                "run() blocks until loading finishes; call it from the main thread, not the Swing event thread.");
        }
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException("Swing needs a graphical display, none was found.");
        }

        onEdt(this::buildUi);

        int total = steps.size();
        if (total == 0) show(0, 1, "Preparing...");
        for (int i = 0; i < total; i++) {
            if (closed) return false;

            Step step = steps.get(i);
            show(i, total, step.label() + "...");
            long start = System.currentTimeMillis();
            try {
                step.task().run();
            } catch (Exception | AssertionError e) {
                e.printStackTrace();
                onEdt(() -> showFailure(step.label(), e));
                return finish(false);
            }
            pad(start);
            show(i + 1, total, step.label() + " - done");
        }

        if (closed) return false;
        onEdt(this::showReady);
        return finish(true);
    }

    // ------------------------------------------------------------------
    // UI construction (event thread)
    // ------------------------------------------------------------------

    private void buildUi() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to the default look
        }

        frame = new JFrame("Dungeon Crawler");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                closed = true;
                if (ticker != null) ticker.stop();
                frame.dispose();
                dismissed.countDown();
            }
        });

        scene = new Scene(loadLogo());
        scene.setLayout(null);

        button = new JButton("Loading...");
        button.setEnabled(false);
        button.setBackground(GOLD);
        button.setForeground(BG);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(GOLD_L, 2));
        button.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        button.setBounds(W / 2 - 120, H - 46, 240, 32);
        button.addActionListener(e -> dismissed.countDown());
        scene.add(button);

        frame.setContentPane(scene);
        frame.getRootPane().setDefaultButton(button); // Enter clicks it once enabled
        frame.pack();
        frame.setLocationRelativeTo(null);

        ticker = new Timer(33, e -> scene.tick());
        ticker.start();

        frame.setVisible(true);
    }

    /** Optional logo from the classpath or assets folder; null if none. */
    private static BufferedImage loadLogo() {
        try {
            var url = LoadingScreen.class.getResource("/logo.png");
            if (url != null) return ImageIO.read(url);
            File f = new File("assets/logo.png");
            if (f.isFile()) return ImageIO.read(f);
        } catch (Exception ignored) {
            // no logo, use the text fallback
        }
        return null;
    }

    // ------------------------------------------------------------------
    // State changes
    // ------------------------------------------------------------------

    private void show(int done, int total, String text) {
        int percent = (int) Math.round(100.0 * done / total);
        SwingUtilities.invokeLater(() -> {
            scene.status = text;
            scene.target = percent;
            if (!animate) scene.display = percent;
        });
    }

    private void showReady() {
        scene.target = 100;
        scene.display = 100;
        scene.ready = true;
        scene.status = "Ready.";
        scene.tipText = "Tip: " + TIPS[rng.nextInt(TIPS.length)];
        button.setText("Begin your descent");
        button.setEnabled(true);
        button.requestFocusInWindow();
    }

    private void showFailure(String label, Throwable e) {
        scene.failed = true;
        scene.status = "[FAILED] " + label;
        scene.tipText = e.getClass().getSimpleName()
                + (e.getMessage() != null ? ": " + e.getMessage() : "");
        button.setText("Close");
        button.setEnabled(true);
        button.requestFocusInWindow();
    }

    /** Waits for the player, closes the window, reports the result. */
    private boolean finish(boolean ok) {
        try {
            dismissed.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            closed = true;
        }
        onEdt(() -> {
            if (ticker != null) ticker.stop();
            frame.dispose();
        });
        return ok && !closed;
    }

    /** Keeps fast steps visible long enough to read. */
    private void pad(long start) {
        if (!animate) return;
        long remaining = minStepMillis - (System.currentTimeMillis() - start);
        if (remaining > 0) {
            try {
                Thread.sleep(remaining);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void onEdt(Runnable r) {
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    // ------------------------------------------------------------------
    // The animated scene (event thread only)
    // ------------------------------------------------------------------

    private final class Scene extends JPanel {
        // Shared state, written on the event thread.
        String status = "Starting...";
        String tipText = TIPS[0];
        int target = 0;
        float display = 0;
        boolean ready = false;
        boolean failed = false;

        private final BufferedImage logo;
        private final BufferedImage wall = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_RGB);
        private final BufferedImage low = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_RGB);
        private final List<float[]> embers = new ArrayList<>(); // x, y, vx, vy, life
        private final Random fx = new Random();
        private final int[] torchX = {20, LW - 20};
        private final int torchY = 40;
        private int frames = 0;
        private int tipIndex = 0;

        Scene(BufferedImage logo) {
            this.logo = logo;
            setPreferredSize(new Dimension(W, H));
            setBackground(BG);
            paintWall();
        }

        /** Stone bricks, drawn once. */
        private void paintWall() {
            Random r = new Random(7);
            Graphics2D g = wall.createGraphics();
            g.setColor(new Color(0x0F, 0x0C, 0x0A));
            g.fillRect(0, 0, LW, LH);
            int bw = 16, bh = 8;
            for (int row = 0; row * bh < LH; row++) {
                int off = (row % 2 == 0) ? 0 : -bw / 2;
                for (int x = off; x < LW; x += bw) {
                    int shade = 38 + r.nextInt(18);
                    g.setColor(new Color(shade + 6, shade, shade - 6));
                    g.fillRect(x + 1, row * bh + 1, bw - 1, bh - 1);
                    g.setColor(new Color(shade + 16, shade + 10, shade + 2));
                    g.fillRect(x + 1, row * bh + 1, bw - 1, 1); // top highlight
                }
            }
            g.dispose();
        }

        /** Advances animation one frame (~30 fps). */
        void tick() {
            frames++;

            if (animate && display < target) {
                display = Math.min(target, display + Math.max(0.4f, (target - display) / 8f));
            }

            // New tip every ~3.5 seconds while loading.
            if (!ready && !failed && frames % 105 == 0) {
                int next;
                do { next = fx.nextInt(TIPS.length); } while (next == tipIndex && TIPS.length > 1);
                tipIndex = next;
                tipText = TIPS[tipIndex];
            }

            // Embers drift up from the torches.
            if (fx.nextInt(3) == 0) {
                int t = fx.nextInt(2);
                embers.add(new float[]{torchX[t] + fx.nextInt(5) - 2, torchY - 4,
                        (fx.nextFloat() - 0.5f) * 0.3f, -0.25f - fx.nextFloat() * 0.35f, 1f});
            }
            embers.removeIf(e -> {
                e[0] += e[2] + (float) Math.sin((frames + e[1]) * 0.2) * 0.08f;
                e[1] += e[3];
                e[4] -= 0.012f;
                return e[4] <= 0 || e[1] < 0;
            });

            repaint();
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;

            // 1) Low-res scene: wall, torch light, torches, embers.
            Graphics2D lg = low.createGraphics();
            lg.drawImage(wall, 0, 0, null);
            for (int t = 0; t < torchX.length; t++) {
                float flicker = 0.85f + 0.15f * (float) Math.sin(frames * 0.35 + t * 2)
                        + (fx.nextFloat() - 0.5f) * 0.06f;
                float radius = 62 * flicker;
                lg.setPaint(new RadialGradientPaint(torchX[t], torchY, radius,
                        new float[]{0f, 1f},
                        new Color[]{new Color(255, 160, 50, 120), new Color(255, 120, 20, 0)}));
                lg.fillRect(0, 0, LW, LH);
                drawTorch(lg, torchX[t], torchY, flicker);
            }
            for (float[] e : embers) {
                int a = Math.max(0, Math.min(255, (int) (e[4] * 255)));
                lg.setColor(new Color(255, 190 - (int) ((1 - e[4]) * 100), 60, a));
                lg.fillRect((int) e[0], (int) e[1], 1, 1);
            }
            lg.dispose();
            g.drawImage(low, 0, 0, W, H, null); // nearest-neighbour: stays pixelated

            // 2) Vignette and a dark floor so text stays readable.
            g.setPaint(new RadialGradientPaint(W / 2f, H * 0.4f, W * 0.7f,
                    new float[]{0.4f, 1f},
                    new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 190)}));
            g.fillRect(0, 0, W, H);
            g.setPaint(new GradientPaint(0, H * 0.55f, new Color(0, 0, 0, 0),
                    0, H * 0.75f, new Color(8, 6, 4, 225)));
            g.fillRect(0, (int) (H * 0.55f), W, H);

            // 3) Logo (fades in).
            float fade = Math.min(1f, frames / 30f);
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.SrcOver.derive(fade));
            drawLogo(g);
            g.setComposite(old);

            // 4) Status, bar, tip.
            drawStatus(g);
            drawBar(g);
            drawTip(g);
        }

        private void drawTorch(Graphics2D g, int x, int y, float flicker) {
            g.setColor(new Color(0x4A, 0x32, 0x1C));
            g.fillRect(x - 1, y, 3, 14);              // handle
            g.setColor(new Color(0x2E, 0x20, 0x12));
            g.fillRect(x - 3, y - 1, 7, 2);           // cup
            int h = 5 + (int) (flicker * 3) + fx.nextInt(2);
            g.setColor(new Color(0xE0, 0x5A, 0x1F));
            g.fillRect(x - 2, y - 1 - h, 5, h);
            g.setColor(new Color(0xFF, 0xA8, 0x30));
            g.fillRect(x - 1, y - 1 - h + 2, 3, h - 2);
            g.setColor(new Color(0xFF, 0xE8, 0x90));
            g.fillRect(x, y - 1 - h + 4, 1, Math.max(1, h - 5));
        }

        private void drawLogo(Graphics2D g) {
            int boxW = 440, boxH = 190, top = 22;
            if (logo != null) {
                double s = Math.min((double) boxW / logo.getWidth(), (double) boxH / logo.getHeight());
                int w = (int) (logo.getWidth() * s), h = (int) (logo.getHeight() * s);
                g.drawImage(logo, (W - w) / 2, top + (boxH - h) / 2, w, h, null);
                return;
            }
            // Fallback title until the real logo exists. No anti-aliasing, so it looks blocky.
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            Font big = new Font(Font.MONOSPACED, Font.BOLD, 58);
            int bob = (int) Math.round(Math.sin(frames * 0.08) * 2);
            centered(g, "DUNGEON", big, W / 2, top + 76 + bob, GOLD, 4);
            centered(g, "CRAWLER", big, W / 2, top + 140 + bob, GOLD_L, 4);
            centered(g, "~ a dungeon crawling RPG ~",
                    new Font(Font.MONOSPACED, Font.PLAIN, 13), W / 2, top + 168, DIM, 1);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
        }

        private void drawStatus(Graphics2D g) {
            Color c = failed ? BAD : (ready ? GOLD_L : TEXT);
            centered(g, status, new Font(Font.MONOSPACED, Font.BOLD, 14), W / 2, 282, c, 1);
        }

        private void drawBar(Graphics2D g) {
            int x = 60, y = 292, w = W - 120, h = 22;
            g.setColor(BAR_BG);
            g.fillRect(x, y, w, h);
            g.setColor(failed ? BAD : DIM);
            g.setStroke(new BasicStroke(2));
            g.drawRect(x, y, w, h);

            // Segmented fill, like a health bar.
            int segW = 10, gap = 2, pad = 3;
            int segs = (w - pad * 2 + gap) / (segW + gap);
            int lit = (int) Math.round(display / 100.0 * segs);
            for (int i = 0; i < lit; i++) {
                int sx = x + pad + i * (segW + gap);
                g.setColor(failed ? BAD : GOLD);
                g.fillRect(sx, y + pad, segW, h - pad * 2);
                g.setColor(failed ? new Color(0xF0, 0x8A, 0x80) : GOLD_L);
                g.fillRect(sx, y + pad, segW, 3); // top shine
            }

            // Little hero runs at the front of the bar.
            if (!failed) {
                int hx = x + (int) (display / 100.0 * (w - 16));
                int bob = (frames / 4 % 2 == 0 && !ready) ? 0 : 2;
                drawHero(g, hx, y - 18 - 2 + bob);
            }

            centered(g, (int) display + "%", new Font(Font.MONOSPACED, Font.BOLD, 13),
                    x + w / 2, y + 16, TEXT, 1);
        }

        private void drawHero(Graphics2D g, int px, int py) {
            for (int r = 0; r < HERO.length; r++) {
                for (int c = 0; c < HERO[r].length(); c++) {
                    Color col = switch (HERO[r].charAt(c)) {
                        case 'g' -> new Color(0xB0, 0xB6, 0xC0);
                        case 's' -> new Color(0xF0, 0xC8, 0x9A);
                        case 'r' -> new Color(0xC0, 0x40, 0x38);
                        case 'w' -> new Color(0xE8, 0xE8, 0xF0);
                        case 'd' -> new Color(0x40, 0x30, 0x28);
                        default -> null;
                    };
                    if (col == null) continue;
                    g.setColor(col);
                    g.fillRect(px + c * 2, py + r * 2, 2, 2);
                }
            }
        }

        private void drawTip(Graphics2D g) {
            String t = tipText;
            if (t.length() > 78) t = t.substring(0, 75) + "...";
            Color c = failed ? BAD : DIM;
            centered(g, t, new Font(Font.MONOSPACED, Font.ITALIC, 12), W / 2, 340, c, 1);
        }

        /** Draws text centered on cx with a dark drop shadow. */
        private void centered(Graphics2D g, String s, Font f, int cx, int y, Color c, int shadow) {
            g.setFont(f);
            int x = cx - g.getFontMetrics().stringWidth(s) / 2;
            g.setColor(new Color(0, 0, 0, 200));
            g.drawString(s, x + shadow, y + shadow);
            g.setColor(c);
            g.drawString(s, x, y);
        }
    }
}