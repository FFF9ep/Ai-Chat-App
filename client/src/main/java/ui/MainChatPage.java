package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.json.*;

public class MainChatPage extends BaseFrame {
    private JList<String> chatList;
    private DefaultListModel<String> listModel;
    private JTextArea chatArea;
    private JTextArea inputArea;
    private JButton sendButton;
    private Map<String, ArrayList<String>> chatHistory;
    private String currentChat;
    private final String API_KEY;
    private final HttpClient client;

    public MainChatPage(String apiKey) {
        super("Main Chat Page");
        this.API_KEY = apiKey;
        this.client = HttpClient.newHttpClient();

        initComponents();
        setupLayout();
        setupEventListeners();
    }

    private void initComponents() {
        listModel = new DefaultListModel<>();
        chatList = new JList<>(listModel);
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputArea = new JTextArea(3, 40);
        sendButton = new JButton("Send");
        chatHistory = new HashMap<>();
    }

    private void setupLayout() {
        contentPanel.setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Left panel - chat list
        JPanel leftPanel = new JPanel(new BorderLayout());
        JButton newChatButton = new JButton("New Chat");
        leftPanel.add(new JScrollPane(chatList), BorderLayout.CENTER);
        leftPanel.add(newChatButton, BorderLayout.NORTH);

        // Right panel - chat interface
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(200);

        contentPanel.add(splitPane, BorderLayout.CENTER);

        newChatButton.addActionListener(e -> createNewChat());
    }

    private void setupEventListeners() {
        sendButton.addActionListener(e -> sendMessage());

        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    sendMessage();
                }
            }
        });

        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = chatList.getSelectedValue();
                if (selected != null) {
                    loadChat(selected);
                }
            }
        });
    }

    private void createNewChat() {
        String chatName = "Chat " + (listModel.size() + 1);
        listModel.addElement(chatName);
        chatHistory.put(chatName, new ArrayList<>());
        chatList.setSelectedValue(chatName, true);
        currentChat = chatName;
        chatArea.setText("");
    }

    private void loadChat(String chatName) {
        currentChat = chatName;
        ArrayList<String> messages = chatHistory.get(chatName);
        chatArea.setText("");
        for (String message : messages) {
            chatArea.append(message + "\n");
        }
    }

    private void sendMessage() {
        if (currentChat == null) {
            createNewChat();
        }

        String message = inputArea.getText().trim();
        if (message.isEmpty()) return;

        // Add user message to chat
        String userMessage = "You: " + message + "\n";
        chatArea.append(userMessage);
        chatHistory.get(currentChat).add(userMessage);

        // Clear input area
        inputArea.setText("");

        // Send request to API
        sendApiRequest(message);
    }

    private void sendApiRequest(String message) {
        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s",
                API_KEY
        );

        // Prepare JSON request body
        String jsonRequest = String.format(
                "{\"contents\":[{\"parts\":[{\"text\": \"%s\"}]}]}",
                message.replace("\"", "\\\"")
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest, StandardCharsets.UTF_8))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body());
                        String generatedText = extractGeneratedText(jsonResponse);

                        SwingUtilities.invokeLater(() -> {
                            String aiResponse = "Gemini: " + generatedText + "\n";
                            chatArea.append(aiResponse);
                            chatHistory.get(currentChat).add(aiResponse);
                        });
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> {
                            String errorMessage = "Error: Failed to get response from Gemini\n";
                            chatArea.append(errorMessage);
                            chatHistory.get(currentChat).add(errorMessage);
                        });
                    }
                });
    }

    private String extractGeneratedText(JSONObject response) {
        try {
            JSONArray candidates = response.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject content = firstCandidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            JSONObject firstPart = parts.getJSONObject(0);
            return firstPart.getString("text");
        } catch (JSONException e) {
            return "Error parsing response";
        }
    }

    @Override
    public void applyTheme(boolean darkMode) {
        if (darkMode) {
            contentPanel.setBackground(Color.DARK_GRAY);
            chatArea.setBackground(Color.BLACK);
            chatArea.setForeground(Color.WHITE);
            inputArea.setBackground(Color.DARK_GRAY);
            inputArea.setForeground(Color.WHITE);
        } else {
            contentPanel.setBackground(Color.LIGHT_GRAY);
            chatArea.setBackground(Color.WHITE);
            chatArea.setForeground(Color.BLACK);
            inputArea.setBackground(Color.WHITE);
            inputArea.setForeground(Color.BLACK);
        }
    }
}
