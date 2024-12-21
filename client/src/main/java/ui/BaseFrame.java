package ui;

import javax.swing.*;
import java.awt.*;

public abstract class BaseFrame extends JFrame {
    protected JPanel contentPanel;

    public BaseFrame(String title) {
        super(title);
        this.contentPanel = new JPanel();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
    }

    public abstract void applyTheme(boolean darkMode);
}