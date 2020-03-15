package main;

import javax.swing.*;

public class GuiUtil {
    public static void showMessage(String text, int type) {
        JOptionPane.showMessageDialog(MainFrame.getInstance(), text, "", type);
    }

    public static void showInfoMessage(String text) {
        showMessage(text, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorMessage(String text) {
        showMessage(text, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarnMessage(String text) {
        showMessage(text, JOptionPane.WARNING_MESSAGE);
    }
}
