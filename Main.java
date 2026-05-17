import utils.AppConstants;
import view.LoginView;
import view.UIHelper;

import javax.swing.*;

/**
 * Main - Application entry point for Smart Expense Tracker.
 *
 * Starts the Swing Event Dispatch Thread and launches the login screen.
 *
 * Run command (from project root):
 *   javac -cp "lib/*:src" -d out $(find src -name "*.java")
 *   java  -cp "lib/*:out" Main
 */
public class Main {

    public static void main(String[] args) {
        // Apply Swing UI configuration on the EDT
        SwingUtilities.invokeLater(() -> {
            setupLookAndFeel();
            UIHelper.setGlobalFont();
            new LoginView();
        });
    }

    private static void setupLookAndFeel() {
        try {
            // Prefer system L&F for native OS decorations, then Nimbus
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    // Override Nimbus color defaults with our palette
                    UIManager.put("nimbusBase",           AppConstants.PRIMARY);
                    UIManager.put("nimbusBlueGrey",       AppConstants.BG_MAIN);
                    UIManager.put("control",              AppConstants.BG_MAIN);
                    UIManager.put("text",                 AppConstants.TEXT_PRIMARY);
                    UIManager.put("nimbusSelectedText",   java.awt.Color.WHITE);
                    UIManager.put("nimbusSelectionBackground", AppConstants.PRIMARY);
                    break;
                }
            }
        } catch (Exception e) {
            // Fall back to system L&F silently
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        // Global rendering hints for crisp text
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext",                "true");
        System.setProperty("sun.java2d.uiScale",          "1.0");
    }
}
