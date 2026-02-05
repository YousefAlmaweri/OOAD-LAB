package Lab_Exercise.V2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;

class UI {
    static JPanel page(String title, JComponent content, JComponent topRight) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(title);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 20f));
        header.add(lbl, BorderLayout.WEST);
        if (topRight != null) header.add(topRight, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    static JButton btn(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static JTextField tf(int cols) {
        JTextField t = new JTextField(cols);
        t.setFont(t.getFont().deriveFont(14f));
        return t;
    }

    static JTextArea ta(int rows, int cols) {
        JTextArea a = new JTextArea(rows, cols);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(a.getFont().deriveFont(14f));
        return a;
    }

    static JComboBox<String> combo(String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(c.getFont().deriveFont(14f));
        return c;
    }

    static JSpinner spinnerInt(int min, int max, int value) {
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, 1);
        JSpinner sp = new JSpinner(model);
        sp.setFont(sp.getFont().deriveFont(14f));
        return sp;
    }

    static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    static void err(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static String safe(String s) { return s == null ? "" : s; }

    static String fmtScore(double v) {
        return new DecimalFormat("0.00").format(v);
    }
}

// Needed for evaluator search regex quoting
class Pattern {
    static String quote(String s) { return java.util.regex.Pattern.quote(s); }
}
