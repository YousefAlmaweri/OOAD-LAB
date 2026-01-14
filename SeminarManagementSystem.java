package Lab_Exercise;

import javax.swing.*;
import java.awt.*;

public class SeminarManagementSystem {

    // =========================
    // Entry point
    // =========================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setNiceLookAndFeel();
            App app = new App();
            app.show();
        });
    }

    static void setNiceLookAndFeel() {
        try {
            // Nimbus is usually smooth and modern-looking
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        // small UI tweaks
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ScrollBar.thumbArc", 12);
        UIManager.put("ScrollBar.thumbHeight", 12);
    }
}
