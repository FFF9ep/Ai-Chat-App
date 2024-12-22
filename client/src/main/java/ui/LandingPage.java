package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LandingPage extends BaseFrame {
    public LandingPage() {
        super("Landing Page");
        initUI();
    }

    private void initUI() {
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // ImageIcon logoIcon = new
        // ImageIcon(getClass().getResource("/assets/images/logo.png"));
        // Image image = logoIcon.getImage(); // transform it
        // Image newimg = image.getScaledInstance(100, 100,
        // java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        // logoIcon = new ImageIcon(newimg); // transform it back
        // JLabel logoLabel = new JLabel(logoIcon);
        // gbc.gridx = 0;
        // gbc.gridy = 0;
        // gbc.gridwidth = 2;
        // contentPanel.add(logoLabel, gbc);
        JLabel titleLabel = new JLabel("Welcome to AI Chat App");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(titleLabel, gbc);

        JButton loginButton = new JButton("Log In");
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        contentPanel.add(loginButton, gbc);

        JButton signUpButton = new JButton("Create an Account");
        gbc.gridx = 1;
        contentPanel.add(signUpButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginPage loginPage = new LoginPage();
                loginPage.applyTheme(false);
                loginPage.setVisible(true);
                dispose();
            }
        });

        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegisterPage registerPage = new RegisterPage();
                registerPage.applyTheme(false);
                registerPage.setVisible(true);
                dispose();
            }
        });
    }

    @Override
    public void applyTheme(boolean darkMode) {
        if (darkMode) {
            contentPanel.setBackground(Color.DARK_GRAY);
            contentPanel.setForeground(Color.WHITE);
        } else {
            contentPanel.setBackground(Color.LIGHT_GRAY);
            contentPanel.setForeground(Color.BLACK);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LandingPage landingPage = new LandingPage();
            landingPage.applyTheme(false);
            landingPage.setVisible(true);
        });
    }
}