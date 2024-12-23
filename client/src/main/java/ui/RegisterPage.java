package ui;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class RegisterPage extends BaseFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    private static final String API_BASE_URL = "http://13.229.209.199:3010";
    // private static final String API_BASE_URL = "http://localhost:3000";

    public RegisterPage() {
        super("Register Page");
        // Ganti ikon aplikasi
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
        contentPanel.setBackground(new Color(240, 240, 240)); // Warna abu-abu terang

        // Global Insets (Margin antar elemen)
        gbc.insets = new Insets(15, 15, 15, 15);

        // Judul Halaman
        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 0, 0)); // Warna hitam
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(titleLabel, gbc);

        // Label Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        usernameLabel.setForeground(new Color(0, 0, 0)); // Warna hitam
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(usernameLabel, gbc);

        // Input Username
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        gbc.gridx = 1;
        contentPanel.add(usernameField, gbc);

        // Label Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordLabel.setForeground(new Color(0, 0, 0)); // Warna hitam
        gbc.gridy = 2;
        gbc.gridx = 0;
        contentPanel.add(passwordLabel, gbc);

        // Input Password
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        gbc.gridx = 1;
        contentPanel.add(passwordField, gbc);

        // Tombol Sign Up
        JButton registerButton = new JButton("Sign Up");
        styleButton(registerButton);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(registerButton, gbc);

        // Label Login
        JLabel loginLabel = new JLabel("Already have an account? Log In");
        loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginLabel.setForeground(new Color(33, 150, 243)); // Warna biru
        loginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 15, 15, 15);
        contentPanel.add(loginLabel, gbc);

        registerButton.addActionListener(e -> {
            try {
                performRegistration(HttpClient.newHttpClient());
                JOptionPane.showMessageDialog(this,
                        "Registrasi berhasil! Silakan login.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                LoginPage loginPage = new LoginPage();
                loginPage.applyTheme(false);
                loginPage.setVisible(true);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error connecting to server: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
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

    // Method untuk memperindah tombol
    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(33, 150, 243)); // Warna biru
        button.setForeground(Color.WHITE); // Teks putih
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Method untuk memperindah text field
    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        textField.setBackground(Color.WHITE); // Warna putih
        textField.setForeground(Color.BLACK); // Teks hitam
        textField.setCaretColor(Color.BLACK); // Kursor hitam
    }

    @Override
    public void applyTheme(boolean darkMode) {
        if (darkMode) {
            contentPanel.setBackground(new Color(30, 30, 30)); // Warna latar belakang gelap
        } else {
            contentPanel.setBackground(new Color(240, 240, 240)); // Warna latar belakang terang
        }
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public void performRegistration(HttpClient client) throws Exception {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            throw new Exception("Username dan password harus diisi");
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("username", username);
        requestBody.put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/api/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            JSONObject error = new JSONObject(response.body());
            throw new Exception(error.getString("error"));
        }
    }
}