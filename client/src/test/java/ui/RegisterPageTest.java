package ui;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.http.*;
import java.net.URI;

class RegisterPageTest {

    @Test
    void testRegistrationSuccess() throws Exception {
        // Mock HttpClient and HttpResponse
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        // Simulate a successful registration response
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn("{\"message\":\"Registration successful\"}");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        RegisterPage registerPage = new RegisterPage();
        registerPage.applyTheme(false);

        // Simulate user input
        registerPage.getUsernameField().setText("newUser");
        registerPage.getPasswordField().setText("newPassword");

        // Assert that the registration call works
        assertDoesNotThrow(() -> registerPage.performRegistration(mockClient));
    }

    @Test
    void testRegistrationFailure() throws Exception {
        // Mock HttpClient and HttpResponse
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        // Simulate a failed registration response
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("{\"error\":\"User already exists\"}");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        RegisterPage registerPage = new RegisterPage();
        registerPage.applyTheme(false);

        // Simulate user input
        registerPage.getUsernameField().setText("existingUser");
        registerPage.getPasswordField().setText("existingPassword");

        // Assert that registration fails
        Exception exception = assertThrows(Exception.class, () -> registerPage.performRegistration(mockClient));
        assertEquals("User already exists", exception.getMessage());
    }
}
