package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LandingPage extends BaseFrame {
    public LandingPage() {
        super("Landing Page");
        
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("../assets/images/logo.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Ikon tidak ditemukan: " + e.getMessage());
        }
        initUI();
    }

    private void initUI() {
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Background Color
        contentPanel.setBackground(new Color(40, 44, 52)); // Warna abu-abu gelap

        // Global Insets (Margin antar elemen)
        gbc.insets = new Insets(15, 15, 15, 15);

        // Gambar Logo
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/assets/images/logo.png"));
        Image image = logoIcon.getImage(); // Transformasi gambar
        Image newimg = image.getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH); // Resize gambar
        logoIcon = new ImageIcon(newimg); // Transform kembali
        JLabel logoLabel = new JLabel(logoIcon);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Mengambil dua kolom
        contentPanel.add(logoLabel, gbc);

        // Tulisan Title
        JLabel titleLabel = new JLabel("Welcome to AI Chat App");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 0, 0)); // Warna hitam
        gbc.gridy = 1; // Baris berikutnya
        gbc.gridwidth = 2;
        contentPanel.add(titleLabel, gbc);

        // Tombol Login
        JButton loginButton = new JButton("Log In");
        styleButton(loginButton);
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridx = 0; // Kolom pertama
        contentPanel.add(loginButton, gbc);

        // Tombol Sign Up
        JButton signUpButton = new JButton("Create an Account");
        styleButton(signUpButton);
        gbc.gridx = 1; // Kolom kedua
        contentPanel.add(signUpButton, gbc);

        // Action Listener untuk tombol login
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginPage loginPage = new LoginPage();
                loginPage.applyTheme(false);
                loginPage.setVisible(true);
                dispose();
            }
        });

        // Action Listener untuk tombol sign up
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

    // Method untuk memperindah tombol
    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(33, 150, 243)); // Warna biru
        button.setForeground(Color.WHITE); // Teks putih
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void applyTheme(boolean darkMode) {
        if (darkMode) {
            contentPanel.setBackground(new Color(30, 30, 30)); // Warna latar belakang gelap
        } else {
            contentPanel.setBackground(new Color(240, 240, 240)); // Warna latar belakang terang
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
