package ui;

import javax.swing.*;
import java.awt.*;

public class RegisterPage extends BaseFrame {
    public RegisterPage() {
        super("Register Page");
        initUI();
    }

    private void initUI() {
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title
        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(titleLabel, gbc);

        // Email Field
        JLabel emailLabel = new JLabel("Email address:");
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        contentPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(20);
        gbc.gridx = 1;
        contentPanel.add(emailField, gbc);

        // Password Field
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridy = 2;
        gbc.gridx = 0;
        contentPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        contentPanel.add(passwordField, gbc);

        // Register Button
        JButton registerButton = new JButton("Sign Up");
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        contentPanel.add(registerButton, gbc);

        // Link to Login
        JLabel loginLabel = new JLabel("Already have an account? Log In");
        loginLabel.setForeground(Color.BLUE);
        loginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 4;
        contentPanel.add(loginLabel, gbc);

        // Action Listeners
        registerButton.addActionListener(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.applyTheme(false);
            loginPage.setVisible(true);
            dispose();
        });

        loginLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                LoginPage loginPage = new LoginPage();
                loginPage.applyTheme(false);
                loginPage.setVisible(true);
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
}