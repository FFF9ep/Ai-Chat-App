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

    // private static final String API_BASE_URL = "http://13.229.209.199:3010";
    private static final String API_BASE_URL = "http://localhost:3000";

    public RegisterPage() {
        super("Register Page");
        initUI();
    }

    private void initUI() {
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(titleLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        contentPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        contentPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridy = 2;
        gbc.gridx = 0;
        contentPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        contentPanel.add(passwordField, gbc);

        JButton registerButton = new JButton("Sign Up");
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        contentPanel.add(registerButton, gbc);

        JLabel loginLabel = new JLabel("Already have an account? Log In");
        loginLabel.setForeground(Color.BLUE);
        loginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 4;
        contentPanel.add(loginLabel, gbc);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Username dan password harus diisi",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("username", username);
                requestBody.put("password", password);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL + "/api/register"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 201) {
                    JOptionPane.showMessageDialog(this,
                            "Registrasi berhasil! Silakan login.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    LoginPage loginPage = new LoginPage();
                    loginPage.applyTheme(false);
                    loginPage.setVisible(true);
                    dispose();
                } else {
                    JSONObject error = new JSONObject(response.body());
                    JOptionPane.showMessageDialog(this,
                            error.getString("error"),
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE);
                }
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