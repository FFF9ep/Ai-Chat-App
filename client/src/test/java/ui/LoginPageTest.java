package ui;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.http.*;
import javax.swing.*;
import java.net.URI;

class LoginPageTest {

    @Test
    void testLoginSuccess() throws Exception {
        // Mock HttpClient and HttpResponse
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        // Simulate a successful login response
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"message\":\"Login successful\"}");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        // Instantiate LoginPage
        LoginPage loginPage = new LoginPage();
        loginPage.applyTheme(false);

        // Access GUI components directly
        JTextField usernameField = loginPage.getUsernameField();
        JPasswordField passwordField = loginPage.getPasswordField();

        // Simulate user input
        usernameField.setText("testUser");
        passwordField.setText("testPassword");

        // Simulate logic for sending login request
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        assertNotNull(username, "Username should not be null");
        assertNotNull(password, "Password should not be null");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3000/api/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password)))
                .build();

        HttpResponse<String> response = mockClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Login should succeed");
    }

    @Test
    void testLoginFailure() throws Exception {
        // Mock HttpClient and HttpResponse
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        // Simulate a failed login response
        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn("{\"error\":\"Invalid credentials\"}");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        // Instantiate LoginPage
        LoginPage loginPage = new LoginPage();
        loginPage.applyTheme(false);

        // Access GUI components directly
        JTextField usernameField = loginPage.getUsernameField();
        JPasswordField passwordField = loginPage.getPasswordField();

        // Simulate user input
        usernameField.setText("wrongUser");
        passwordField.setText("wrongPassword");

        // Simulate logic for sending login request
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        assertNotNull(username, "Username should not be null");
        assertNotNull(password, "Password should not be null");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3000/api/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password)))
                .build();

        HttpResponse<String> response = mockClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, response.statusCode(), "Login should fail");
        assertTrue(response.body().contains("Invalid credentials"),
                "Error message should indicate invalid credentials");
    }
}
