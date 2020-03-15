
package main;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sasha
 */
public class MainFrame extends JFrame {
    private static MainFrame instance;
    private JTextArea textPane;

    public static synchronized MainFrame getInstance() {
        if (instance == null) {
            instance = new MainFrame();
        }
        return instance;
    }

    public MainFrame() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Сервер");

        textPane = new JTextArea();
        textPane.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(textPane);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        getContentPane().add(jsp);

        pack();
        Dimension dim = getToolkit().getScreenSize();
        setSize((int) dim.getWidth() / 2, (int) dim.getHeight());
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();

        int x = (int) rect.getMaxX() - getWidth();
        int y = 0;

        setLocation(x, y);
        setVisible(true);
    }

    public void showMessage(String message) {
        textPane.append(message + "\n");
    }
}
