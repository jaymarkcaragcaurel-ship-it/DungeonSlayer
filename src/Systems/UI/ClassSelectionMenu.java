package Systems.UI;

import Systems.Core.ClassSystem;
import Systems.Core.NameRules;
import Systems.Player.Stat;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.font.TextAttribute;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Character creation screen: pick a class, type a name, confirm.
 * Blocks the calling thread until the player confirms or closes the window.
 * Reads everything from ClassSystem, so it picks up new classes automatically.
 */
public final class ClassSelectionMenu {

    private ClassSelectionMenu() {}

    /** What the player chose. The name is already normalized and valid. */
    public record Choice(String name, int classIndex) {}

    /** Empty if the player closed the window without confirming. Safe to call from any thread. */
    public static Optional<Choice> show() {
        AtomicReference<Choice> result = new AtomicReference<>();
        Runnable task = () -> result.set(new MenuDialog().open());
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Class selection menu failed", e.getCause());
            }
        }
        return Optional.ofNullable(result.get());
    }

    // ---- design tokens -------------------------------------------------------------------------

    private static final Color BG      = new Color(0x0E1014);
    private static final Color SURFACE = new Color(0x161922);
    private static final Color LINE    = new Color(0x262A36);
    private static final Color TRACK   = new Color(0x232736);
    private static final Color TEXT    = new Color(0xECEEF3);
    private static final Color BODY    = new Color(0xB4BAC8);
    private static final Color MUTED   = new Color(0x7E8496);
    private static final Color ERROR_RED   = new Color(0xFF7A7A);

    /** One accent per class, cycled if there are more classes than colours. */
    private static final Color[] ACCENTS = {
        new Color(0x6C8CFF), new Color(0xFF7A6B), new Color(0x5FD6A4),
        new Color(0xF2C14E), new Color(0xB58CFF), new Color(0xFF8FC7)
    };

    private static Font font(int style, float size, float tracking) {
        Font f = new Font(Font.SANS_SERIF, style, 1).deriveFont(size);
        return tracking == 0 ? f : f.deriveFont(Map.of(TextAttribute.TRACKING, tracking));
    }

    private static final Font CAPTION = font(Font.BOLD, 11f, 0.18f);
    private static final Font TAB     = font(Font.BOLD, 12f, 0.14f);
    private static final Font HERO    = font(Font.BOLD, 40f, 0.02f);
    private static final Font BODY_F  = font(Font.PLAIN, 15f, 0f);
    private static final Font STAT    = font(Font.BOLD, 11f, 0.12f);
    private static final Font VALUE   = font(Font.BOLD, 13f, 0f);
    private static final Font INPUT   = font(Font.PLAIN, 18f, 0f);
    private static final Font BUTTON  = font(Font.BOLD, 12f, 0.14f);

    // Must match the order of ClassSystem.normalizedStats().
    private static final Stat[] STAT_ORDER = {
        Stat.HP, Stat.MP, Stat.SPEED, Stat.ATTACK, Stat.DEFENSE, Stat.CRIT_CHANCE, Stat.CRIT_MULTIPLIER
    };
    private static final String[] STAT_LABELS = {
        "HEALTH", "MANA", "SPEED", "ATTACK", "DEFENSE", "CRIT CHANCE", "CRIT DAMAGE"
    };

    private static Graphics2D prep(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    private static float width(Graphics2D g, String s) {
        return (float) g.getFont().getStringBounds(s, g.getFontRenderContext()).getWidth();
    }

    private static Color mix(Color a, Color b, float t) {
        return new Color(
            Math.round(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            Math.round(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }

    private static JLabel caption(String text) {
        JLabel l = new JLabel(text);
        l.setFont(CAPTION);
        l.setForeground(MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ---- components ----------------------------------------------------------------------------

    /** Flat class tab; the selected one gets an accent underline. */
    private static final class Tab extends JToggleButton {
        private final String label;
        private final Supplier<Color> accent;

        Tab(String name, Supplier<Color> accent) {
            this.label = name.toUpperCase();
            this.accent = accent;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 44));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = prep(g);
            g2.setFont(TAB);
            FontMetrics fm = g2.getFontMetrics();
            boolean sel = isSelected();
            g2.setColor(sel ? TEXT : getModel().isRollover() ? BODY : MUTED);
            g2.drawString(label, (getWidth() - width(g2, label)) / 2f,
                          (getHeight() + fm.getAscent() - fm.getDescent()) / 2f - 1);
            if (sel) {
                g2.setColor(accent.get());
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
            }
            g2.dispose();
        }
    }

    /** Row of tabs sitting on a hairline. */
    private static final class TabRow extends JPanel {
        TabRow(int n) { super(new GridLayout(1, n)); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            g.setColor(LINE);
            g.fillRect(0, getHeight() - 1, getWidth(), 1);
        }
    }

    /** Short accent bar under the class name. */
    private static final class Rule extends JComponent {
        private final Supplier<Color> accent;
        Rule(Supplier<Color> accent) {
            this.accent = accent;
            Dimension d = new Dimension(40, 3);
            setPreferredSize(d); setMaximumSize(d); setMinimumSize(d);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        @Override protected void paintComponent(Graphics g) {
            g.setColor(accent.get());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /** Rounded surface card. */
    private static final class Card extends JPanel {
        Card(LayoutManager lm) { super(lm); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = prep(g);
            g2.setColor(SURFACE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.setColor(LINE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            g2.dispose();
        }
    }

    /** Borderless text field with an underline that lights up on focus. */
    private static final class NameField extends JTextField {
        private final Supplier<Color> accent;

        NameField(int columns, Supplier<Color> accent) {
            super(columns);
            this.accent = accent;
            setOpaque(false);
            setFont(INPUT);
            setForeground(TEXT);
            setCaretColor(TEXT);
            setBorder(BorderFactory.createEmptyBorder(8, 0, 10, 0));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e)   { repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = prep(g);
            if (getText().isEmpty()) {
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new Color(0x545A6C));
                g2.drawString("Enter a name", getInsets().left,
                              (getHeight() + fm.getAscent() - fm.getDescent()) / 2f - 1);
            }
            boolean focus = hasFocus();
            g2.setColor(focus ? accent.get() : LINE);
            g2.fillRect(0, getHeight() - (focus ? 2 : 1), getWidth(), focus ? 2 : 1);
            g2.dispose();
        }
    }

    /** Solid, rounded call-to-action. */
    private static final class PrimaryButton extends JButton {
        private final Supplier<Color> accent;

        PrimaryButton(String text, Supplier<Color> accent) {
            super(text.toUpperCase());
            this.accent = accent;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(BUTTON);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(210, 46));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = prep(g);
            Color fill = !isEnabled() ? SURFACE
                       : getModel().isRollover() ? mix(accent.get(), Color.WHITE, 0.14f)
                       : accent.get();
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            if (!isEnabled()) {
                g2.setColor(LINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(isEnabled() ? BG : MUTED);
            g2.drawString(getText(), (getWidth() - width(g2, getText())) / 2f,
                          (getHeight() + fm.getAscent() - fm.getDescent()) / 2f - 1);
            g2.dispose();
        }
    }

    // ---- dialog --------------------------------------------------------------------------------

    private static final class MenuDialog extends JDialog {

        private final Tab[] tabs = new Tab[ClassSystem.count()];
        private final JLabel className = new JLabel(" ");
        private final JTextArea description = new JTextArea();
        private final StatBars statBars = new StatBars(this::accent);
        private final NameField nameField = new NameField(NameRules.MAX_LENGTH, this::accent);
        private final JLabel errorLabel = new JLabel(" ");
        private final PrimaryButton confirm = new PrimaryButton("Enter the dungeon", this::accent);

        private int selected = 0;
        private Choice result = null;

        MenuDialog() {
            super((Frame) null, "New character", true);
            if (STAT_ORDER.length != ClassSystem.NORMALIZED_STAT_COUNT
                    || STAT_LABELS.length != STAT_ORDER.length) {
                throw new IllegalStateException("Stat display tables out of sync with ClassSystem");
            }
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel root = new JPanel(new BorderLayout(0, 28));
            root.setBackground(BG);
            root.setBorder(BorderFactory.createEmptyBorder(28, 36, 32, 36));
            setContentPane(root);

            root.add(buildHeader(), BorderLayout.NORTH);
            root.add(buildBody(), BorderLayout.CENTER);
            root.add(buildFooter(), BorderLayout.SOUTH);

            selectClass(0);
            refreshName();
            pack();
            setResizable(false);
            setLocationRelativeTo(null);
            SwingUtilities.invokeLater(nameField::requestFocusInWindow);
        }

        Choice open() {
            setVisible(true); // blocks until disposed (modal)
            return result;
        }

        private Color accent() { return ACCENTS[selected % ACCENTS.length]; }

        // ---- building --------------------------------------------------------------------

        private JComponent buildHeader() {
            JPanel header = new JPanel(new BorderLayout(0, 14));
            header.setOpaque(false);
            header.add(caption("NEW CHARACTER"), BorderLayout.NORTH);

            TabRow row = new TabRow(ClassSystem.count());
            ButtonGroup group = new ButtonGroup();
            for (int i = 0; i < ClassSystem.count(); i++) {
                final int index = i;
                Tab t = new Tab(ClassSystem.name(i), this::accent);
                t.addActionListener(e -> selectClass(index));
                group.add(t);
                row.add(t);
                tabs[i] = t;
            }
            header.add(row, BorderLayout.CENTER);
            return header;
        }

        private JComponent buildBody() {
            JPanel body = new JPanel(new BorderLayout(48, 0));
            body.setOpaque(false);

            // left: class name + description
            JPanel hero = new JPanel(new BorderLayout());
            hero.setOpaque(false);
            hero.setPreferredSize(new Dimension(420, 0));

            className.setFont(HERO);
            className.setForeground(TEXT);
            className.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel head = new JPanel();
            head.setOpaque(false);
            head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
            head.add(caption("CLASS"));
            head.add(Box.createVerticalStrut(6));
            head.add(className);
            head.add(Box.createVerticalStrut(14));
            head.add(new Rule(this::accent));

            description.setEditable(false);
            description.setFocusable(false);
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setFont(BODY_F);
            description.setForeground(BODY);
            description.setOpaque(false);
            description.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

            hero.add(head, BorderLayout.NORTH);
            hero.add(description, BorderLayout.CENTER);

            // right: stats card
            Card card = new Card(new BorderLayout());
            card.setBorder(BorderFactory.createEmptyBorder(22, 26, 14, 26));
            card.setPreferredSize(new Dimension(370, 0));
            card.add(statBars, BorderLayout.CENTER);

            body.add(hero, BorderLayout.CENTER);
            body.add(card, BorderLayout.EAST);
            return body;
        }

        private JComponent buildFooter() {
            JPanel footer = new JPanel(new BorderLayout(28, 0));
            footer.setOpaque(false);

            JPanel field = new JPanel(new BorderLayout(0, 2));
            field.setOpaque(false);
            JLabel cap = caption("CHARACTER NAME");
            cap.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            field.add(cap, BorderLayout.NORTH);
            field.add(nameField, BorderLayout.CENTER);
            errorLabel.setFont(font(Font.PLAIN, 12f, 0f));
            errorLabel.setForeground(ERROR_RED);
            errorLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            field.add(errorLabel, BorderLayout.SOUTH);

            nameField.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e)  { refreshName(); }
                @Override public void removeUpdate(DocumentEvent e)  { refreshName(); }
                @Override public void changedUpdate(DocumentEvent e) { refreshName(); }
            });
            nameField.addActionListener(e -> confirm());

            confirm.addActionListener(e -> confirm());
            getRootPane().setDefaultButton(confirm);
            JPanel east = new JPanel(new BorderLayout());
            east.setOpaque(false);
            east.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
            east.add(confirm, BorderLayout.NORTH);

            footer.add(field, BorderLayout.CENTER);
            footer.add(east, BorderLayout.EAST);
            return footer;
        }

        // ---- behaviour -------------------------------------------------------------------

        private void selectClass(int index) {
            selected = index;
            tabs[index].setSelected(true);
            className.setText(ClassSystem.name(index));
            description.setText(ClassSystem.description(index));
            statBars.show(index);
            getContentPane().repaint(); // accent colour changed
        }

        private void refreshName() {
            String raw = nameField.getText();
            if (NameRules.normalize(raw).isEmpty()) {
                errorLabel.setText(" ");
                confirm.setEnabled(false);
                return;
            }
            String problem = NameRules.validate(raw);
            errorLabel.setText(problem == null ? " " : problem);
            confirm.setEnabled(problem == null);
        }

        private void confirm() {
            if (!NameRules.isValid(nameField.getText())) return;
            result = new Choice(NameRules.normalize(nameField.getText()), selected);
            dispose();
        }
    }

    // ---- stat bars -----------------------------------------------------------------------------

    /** Thin animated bars, each filled relative to the best class for that stat. */
    private static final class StatBars extends JPanel {

        private static final int ROW_H = 38;
        private static final int BAR_H = 4;

        private final Supplier<Color> accent;
        private final double[] shown  = new double[ClassSystem.NORMALIZED_STAT_COUNT];
        private double[] target       = new double[ClassSystem.NORMALIZED_STAT_COUNT];
        private final String[] values = new String[ClassSystem.NORMALIZED_STAT_COUNT];
        private final Timer anim = new Timer(15, e -> step());

        StatBars(Supplier<Color> accent) {
            this.accent = accent;
            setOpaque(false);
            setPreferredSize(new Dimension(0, ROW_H * ClassSystem.NORMALIZED_STAT_COUNT));
        }

        void show(int classIndex) {
            target = ClassSystem.normalizedStats(classIndex).clone();
            for (int i = 0; i < STAT_ORDER.length; i++) {
                values[i] = format(ClassSystem.stat(classIndex, STAT_ORDER[i]));
            }
            anim.start();
            repaint();
        }

        private void step() {
            boolean moving = false;
            for (int i = 0; i < shown.length; i++) {
                double d = target[i] - shown[i];
                if (Math.abs(d) < 0.002) {
                    shown[i] = target[i];
                } else {
                    shown[i] += d * 0.2;
                    moving = true;
                }
            }
            repaint();
            if (!moving) anim.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = prep(g);
            int w = getWidth();
            for (int i = 0; i < STAT_ORDER.length; i++) {
                int top = i * ROW_H;
                int textY = top + 14;

                g2.setFont(STAT);
                g2.setColor(MUTED);
                g2.drawString(STAT_LABELS[i], 0, textY);

                g2.setFont(VALUE);
                g2.setColor(TEXT);
                String v = values[i] == null ? "" : values[i];
                g2.drawString(v, w - width(g2, v), textY);

                int barY = top + 22;
                g2.setColor(TRACK);
                g2.fillRoundRect(0, barY, w, BAR_H, BAR_H, BAR_H);

                double frac = shown[i];
                if (target[i] > 0) frac = Math.max(frac, 0.03); // an existing stat always shows a sliver
                int fw = (int) Math.round(w * Math.min(1.0, frac));
                if (fw > 0) {
                    g2.setColor(accent.get());
                    g2.fillRoundRect(0, barY, Math.max(fw, BAR_H), BAR_H, BAR_H, BAR_H);
                }
            }
            g2.dispose();
        }

        private static String format(double v) {
            return v == Math.rint(v) ? String.valueOf((long) v) : String.format("%.2f", v);
        }
    }
}