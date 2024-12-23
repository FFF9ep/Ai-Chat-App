package ui;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.http.*;
import java.util.*;

class MainChatPageTest {

    @Test
    void testSendMessageSuccess() throws Exception {
        // Mock HttpClient and HttpResponse
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        // Simulate a successful AI response
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"response\":\"Hello, I am AI.\"}");
        when(mockClient.send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        MainChatPage chatPage = new MainChatPage("testUser");
        chatPage.applyTheme(false);

        // Simulate sending a message
        assertDoesNotThrow(() -> chatPage.sendMessage(mockClient, "Hello AI"));
    }

    @Test
    void testSendMessageFailure() throws Exception {
        // Mock HttpClient and HttpResponse
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        // Simulate a failed AI response
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("{\"error\":\"Server error\"}");
        when(mockClient.send(any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        MainChatPage chatPage = new MainChatPage("testUser");
        chatPage.applyTheme(false);

        // Simulate sending a message
        Exception exception = assertThrows(Exception.class, () -> chatPage.sendMessage(mockClient, "Hello AI"));
        assertEquals("Server error", exception.getMessage());
    }
}
