package ui;

import javax.swing.*;
import java.awt.*;

public class MainChatPage extends BaseFrame {
    private JList<String> chatHistoryList;
    private JTextArea chatArea;
    private JTextField inputField;

    public MainChatPage() {
        super("Main Chat Page");
        initUI();
    }

    private void initUI() {
        contentPanel.setLayout(new BorderLayout());

        // Sidebar for Chat History
        JPanel sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));
        sidebarPanel.setBorder(BorderFactory.createTitledBorder("Chat History"));

        chatHistoryList = new JList<>(new String[] { "Chat 1", "Chat 2", "Chat 3" }); // Placeholder data
        sidebarPanel.add(new JScrollPane(chatHistoryList), BorderLayout.CENTER);

        contentPanel.add(sidebarPanel, BorderLayout.WEST);

        // Chat Area
        JPanel chatPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(BorderFactory.createTitledBorder("Conversation"));

        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Input Field and Send Button
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendButton = new JButton("Send");

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        contentPanel.add(chatPanel, BorderLayout.CENTER);
    }

    @Override
    public void applyTheme(boolean darkMode) {
        if (darkMode) {
            contentPanel.setBackground(Color.DARK_GRAY);
            chatArea.setBackground(Color.BLACK);
            chatArea.setForeground(Color.WHITE);
            inputField.setBackground(Color.DARK_GRAY);
            inputField.setForeground(Color.WHITE);
        } else {
            contentPanel.setBackground(Color.LIGHT_GRAY);
            chatArea.setBackground(Color.WHITE);
            chatArea.setForeground(Color.BLACK);
            inputField.setBackground(Color.WHITE);
            inputField.setForeground(Color.BLACK);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainChatPage mainChatPage = new MainChatPage();
            mainChatPage.applyTheme(false); // Default to light mode
            mainChatPage.setVisible(true);
        });
    }
}
