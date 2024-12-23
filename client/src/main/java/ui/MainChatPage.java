package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.util.*;
import java.net.http.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.json.*;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;

public class MainChatPage extends BaseFrame {
    private static final String API_BASE_URL = "http://localhost:3000";
    // private static final String API_BASE_URL = "http://13.229.209.199:3010";
    private static final int BUBBLE_PADDING = 15;
    private boolean isDarkMode = false;
    private JList<String> chatList;
    private DefaultListModel<String> listModel;
    private JPanel rightPanel;
    private JScrollPane chatScrollPane;
    private JTextArea inputArea;
    private JButton sendButton;
    private Map<String, ArrayList<String>> chatHistory;
    private String currentChat;
    private final HttpClient client;
    private boolean isProcessing;
    private Map<String, String> chatNames;
    private final String username;
    private JPopupMenu chatPopupMenu;

    private class MessagePanel extends JPanel {
        private final String message;
        private final boolean isUser;
        private final Color backgroundColor;
        private static final int RADIUS = 20;

        public MessagePanel(String message, boolean isUser) {
            this.message = message;
            this.isUser = isUser;
            this.backgroundColor = isUser ? new Color(0, 132, 255)
                    : (isDarkMode ? new Color(58, 58, 58) : new Color(241, 241, 241));

            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(BUBBLE_PADDING, BUBBLE_PADDING, BUBBLE_PADDING, BUBBLE_PADDING));

            JTextArea textArea = new JTextArea(message);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setOpaque(false);
            textArea.setEditable(false);
            textArea.setForeground(isUser ? Color.WHITE : (isDarkMode ? Color.WHITE : Color.BLACK));
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            // Set maximum width for text area
            int maxWidth = 400;
            FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
            int lines = calculateLines(message, fm, maxWidth - (BUBBLE_PADDING * 2));
            textArea.setSize(new Dimension(maxWidth, fm.getHeight() * lines));

            add(textArea, BorderLayout.CENTER);

            // Add vertical strut for spacing between messages
            add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        }

        private int calculateLines(String text, FontMetrics fm, int maxWidth) {
            int lines = 1;
            int lineWidth = 0;
            String[] words = text.split(" ");

            for (String word : words) {
                int wordWidth = fm.stringWidth(word + " ");
                if (lineWidth + wordWidth > maxWidth) {
                    lines++;
                    lineWidth = wordWidth;
                } else {
                    lineWidth += wordWidth;
                }
            }
            return lines;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(backgroundColor);
            RoundRectangle2D bubble = new RoundRectangle2D.Float(
                    0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
            g2d.fill(bubble);
            g2d.dispose();
        }
    }

    public MainChatPage(String username) {
        super("AI Chat Application");
        this.username = username;
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        this.chatNames = new HashMap<>();

        initComponents();
        setupLayout();
        setupChatPopupMenu();
        setupEventListeners();
        setupMenuBar();
        loadUserChats();
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu themeMenu = new JMenu("Theme");

        JMenuItem lightTheme = new JMenuItem("Light Mode");
        JMenuItem darkTheme = new JMenuItem("Dark Mode");

        lightTheme.addActionListener(e -> {
            isDarkMode = false;
            applyTheme(false);
        });

        darkTheme.addActionListener(e -> {
            isDarkMode = true;
            applyTheme(true);
        });

        themeMenu.add(lightTheme);
        themeMenu.add(darkTheme);
        menuBar.add(themeMenu);
        setJMenuBar(menuBar);
    }

    private void setupChatPopupMenu() {
        chatPopupMenu = new JPopupMenu();

        JMenuItem editTitleItem = new JMenuItem("Edit Title");
        editTitleItem.addActionListener(e -> {
            String selectedChat = chatList.getSelectedValue();
            if (selectedChat != null) {
                String chatId = chatNames.get(selectedChat);
                String newTitle = JOptionPane.showInputDialog(
                        this,
                        "Masukkan judul baru:",
                        "Edit Title",
                        JOptionPane.PLAIN_MESSAGE);

                if (newTitle != null && !newTitle.trim().isEmpty()) {
                    updateChatTitle(chatId, newTitle.trim());
                }
            }
        });

        JMenuItem deleteItem = new JMenuItem("Delete Chat");
        deleteItem.addActionListener(e -> {
            String selectedChat = chatList.getSelectedValue();
            if (selectedChat != null) {
                int confirm = JOptionPane.showConfirmDialog(
                        MainChatPage.this,
                        "Apakah Anda yakin ingin menghapus chat ini?",
                        "Konfirmasi Hapus",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    String chatId = chatNames.get(selectedChat);
                    deleteChat(chatId);
                }
            }
        });

        chatPopupMenu.add(editTitleItem);
        chatPopupMenu.add(deleteItem);
    }

    private void initComponents() {
        listModel = new DefaultListModel<>();
        chatList = new JList<>(listModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        inputArea = new JTextArea(3, 40);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);

        sendButton = new JButton("Send");
        sendButton.setEnabled(false);

        chatHistory = new HashMap<>();
        isProcessing = false;
    }

    private void setupLayout() {
        contentPanel.setLayout(new BorderLayout(0, 0));

        // Header panel untuk tombol logout
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Apakah Anda yakin ingin logout?",
                    "Konfirmasi Logout",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                LoginPage loginPage = new LoginPage();
                loginPage.applyTheme(isDarkMode);
                loginPage.setVisible(true);
                dispose();
            }
        });
        headerPanel.add(logoutButton);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Left panel setup
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        JButton newChatButton = new JButton("New Chat");
        newChatButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newChatButton.setPreferredSize(new Dimension(200, 40));

        leftPanel.add(newChatButton, BorderLayout.NORTH);

        // Chat list with delete buttons
        JPanel chatListPanel = new JPanel(new BorderLayout());
        chatList.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Custom list cell renderer for delete buttons
        chatList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JPanel panel = new JPanel(new BorderLayout());
                JLabel label = new JLabel(value.toString());
                label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                label.setBorder(new EmptyBorder(5, 5, 5, 5));

                if (isSelected) {
                    panel.setBackground(list.getSelectionBackground());
                    label.setForeground(list.getSelectionForeground());
                } else {
                    panel.setBackground(list.getBackground());
                    label.setForeground(list.getForeground());
                }

                panel.add(label, BorderLayout.CENTER);
                return panel;
            }
        });

        // Add mouse listener for delete buttons
        chatList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = chatList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String chatName = listModel.getElementAt(index);
                        int confirm = JOptionPane.showConfirmDialog(
                                MainChatPage.this,
                                "Delete chat '" + chatName + "'?",
                                "Confirm Delete",
                                JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteChat(chatName);
                        }
                    }
                }
            }
        });

        chatListPanel.add(new JScrollPane(chatList), BorderLayout.CENTER);
        leftPanel.add(chatListPanel, BorderLayout.CENTER);

        // Right panel setup
        rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.SOUTH;

        // Tambahkan panel kosong yang akan "mendorong" pesan ke bawah
        JPanel spacer = new JPanel();
        gbc.weighty = 1;
        rightPanel.add(spacer, gbc);
        gbc.weighty = 0;

        chatScrollPane = new JScrollPane(rightPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Input panel setup
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setPreferredSize(new Dimension(70, 40));

        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Main split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, chatScrollPane);
        splitPane.setDividerLocation(200);

        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(inputPanel, BorderLayout.SOUTH);

        newChatButton.addActionListener(e -> createNewChat());
    }

    // private void deleteChat(String chatName) {
    // listModel.removeElement(chatName);
    // chatHistory.remove(chatName);
    // chatNames.remove(chatName);
    // if (currentChat != null && currentChat.equals(chatName)) {
    // currentChat = null;
    // rightPanel.removeAll();
    // rightPanel.revalidate();
    // rightPanel.repaint();
    // }
    // }

    private void createNewChat() {
        String chatId = UUID.randomUUID().toString();
        String chatName = "Chat " + (listModel.size() + 1);

        listModel.addElement(chatName);
        chatHistory.put(chatName, new ArrayList<>());
        chatNames.put(chatName, chatId);

        chatList.setSelectedValue(chatName, true);
        currentChat = chatName;
        rightPanel.removeAll();
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void setupEventListeners() {
        sendButton.addActionListener(e -> sendMessage());

        inputArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateSendButton();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateSendButton();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateSendButton();
            }
        });

        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown() && sendButton.isEnabled()) {
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

        chatList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }
        });
    }

    private void showPopupMenu(MouseEvent e) {
        int index = chatList.locationToIndex(e.getPoint());
        if (index >= 0) {
            chatList.setSelectedIndex(index);
            chatPopupMenu.show(chatList, e.getX(), e.getY());
        }
    }

    private void updateSendButton() {
        sendButton.setEnabled(!inputArea.getText().trim().isEmpty() && !isProcessing);
    }

    private void loadChat(String chatName) {
        currentChat = chatName;
        rightPanel.removeAll();

        // Tambahkan GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 5, 2, 5);

        ArrayList<String> messages = chatHistory.get(chatName);
        for (String message : messages) {
            boolean isUser = message.startsWith("You: ");

            JPanel containerPanel = new JPanel(new BorderLayout());
            containerPanel.setOpaque(false);

            MessagePanel messagePanel = new MessagePanel(message, isUser);

            if (isUser) {
                containerPanel.add(Box.createHorizontalGlue(), BorderLayout.WEST);
                containerPanel.add(messagePanel, BorderLayout.EAST);
            } else {
                containerPanel.add(messagePanel, BorderLayout.WEST);
                containerPanel.add(Box.createHorizontalGlue(), BorderLayout.EAST);
            }

            rightPanel.add(containerPanel, gbc);
        }

        // Tambah spacer di akhir
        gbc.weighty = 1;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        rightPanel.add(spacer, gbc);

        rightPanel.revalidate();
        rightPanel.repaint();

        // Scroll ke bawah
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void addMessageToChat(String message, boolean isUser) {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setOpaque(false);

        MessagePanel messagePanel = new MessagePanel(message, isUser);

        if (isUser) {
            containerPanel.add(Box.createHorizontalGlue(), BorderLayout.WEST);
            containerPanel.add(messagePanel, BorderLayout.EAST);
        } else {
            containerPanel.add(messagePanel, BorderLayout.WEST);
            containerPanel.add(Box.createHorizontalGlue(), BorderLayout.EAST);
        }

        // Tambahkan pesan baru dengan GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 5, 2, 5);

        // Hapus spacer lama jika ada
        if (rightPanel.getComponentCount() > 0) {
            Component spacer = rightPanel.getComponent(rightPanel.getComponentCount() - 1);
            rightPanel.remove(spacer);
        }

        // Tambah pesan baru
        rightPanel.add(containerPanel, gbc);

        // Tambah spacer baru
        gbc.weighty = 1;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        rightPanel.add(spacer, gbc);

        rightPanel.revalidate();
        rightPanel.repaint();

        // Scroll ke bawah
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void sendMessage() {
        if (isProcessing)
            return;

        if (currentChat == null) {
            createNewChat();
        }

        String message = inputArea.getText().trim();
        if (message.isEmpty())
            return;

        isProcessing = true;
        sendButton.setEnabled(false);
        inputArea.setEnabled(false);

        // Add user message to chat
        addMessageToChat("You: " + message, true);
        chatHistory.get(currentChat).add("You: " + message);

        // Clear input area
        inputArea.setText("");

        // Send request to API
        sendApiRequest(message);
    }

    private void sendApiRequest(String message) {
        String chatId = chatNames.get(currentChat);

        JSONObject requestBody = new JSONObject();
        requestBody.put("idChat", chatId);
        requestBody.put("question", message);
        requestBody.put("username", username);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        CompletableFuture<HttpResponse<String>> future = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());

        future.thenAccept(response -> {
            if (response.statusCode() == 200) {
                try {
                    JSONObject jsonResponse = new JSONObject(response.body());
                    String generatedText = jsonResponse.getString("response");
                    SwingUtilities.invokeLater(() -> {
                        addMessageToChat("AI: " + generatedText, false);
                        chatHistory.get(currentChat).add("AI: " + generatedText);
                    });
                } catch (JSONException e) {
                    showError("Error parsing response: " + e.getMessage());
                }
            } else {
                showError("Server returned error: " + response.statusCode());
            }
        }).exceptionally(e -> {
            showError("Network error: " + e.getMessage());
            return null;
        }).whenComplete((v, e) -> {
            SwingUtilities.invokeLater(() -> {
                isProcessing = false;
                inputArea.setEnabled(true);
                updateSendButton();
            });
        });
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    @Override
    public void applyTheme(boolean darkMode) {
        isDarkMode = darkMode;
        Color bgColor = darkMode ? new Color(30, 30, 30) : Color.WHITE;
        Color fgColor = darkMode ? Color.WHITE : Color.BLACK;

        rightPanel.setBackground(bgColor);
        contentPanel.setBackground(bgColor);
        inputArea.setBackground(darkMode ? new Color(45, 45, 45) : Color.WHITE);
        inputArea.setForeground(fgColor);
        inputArea.setCaretColor(fgColor);
        chatList.setBackground(darkMode ? new Color(45, 45, 45) : Color.WHITE);
        chatList.setForeground(fgColor);

        sendButton.setBackground(darkMode ? new Color(0, 120, 215) : new Color(0, 132, 255));
        sendButton.setForeground(Color.WHITE);

        // Refresh all components
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void loadUserChats() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/api/user/chats/" + username))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            JSONArray chats = new JSONArray(response.body());
                            SwingUtilities.invokeLater(() -> {
                                // Clear existing data
                                listModel.clear();
                                chatHistory.clear();
                                chatNames.clear();

                                for (int i = 0; i < chats.length(); i++) {
                                    JSONObject chat = chats.getJSONObject(i);
                                    String chatId = chat.getString("idChat");
                                    String chatTitle = chat.getString("title");

                                    // Simpan history chat
                                    ArrayList<String> messages = new ArrayList<>();
                                    JSONArray history = chat.getJSONArray("history");
                                    for (int j = 0; j < history.length(); j++) {
                                        JSONObject message = history.getJSONObject(j);
                                        String role = message.getString("role");
                                        String text = message.getJSONArray("parts")
                                                .getJSONObject(0)
                                                .getString("text");

                                        if (role.equals("user")) {
                                            messages.add("You: " + text);
                                        } else if (role.equals("model")) {
                                            messages.add("AI: " + text);
                                        }
                                    }

                                    listModel.addElement(chatTitle);
                                    chatHistory.put(chatTitle, messages);
                                    chatNames.put(chatTitle, chatId);
                                }
                            });
                        }
                    })
                    .exceptionally(e -> {
                        showError("Error loading chats: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            showError("Error loading chats: " + e.getMessage());
        }
    }

    private void updateChatTitle(String chatId, String newTitle) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("idChat", chatId);
            requestBody.put("title", newTitle);
            requestBody.put("username", username);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/api/chat/title"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            // Reload chats setelah update berhasil
                            loadUserChats();
                        } else {
                            JSONObject error = new JSONObject(response.body());
                            showError(error.getString("error"));
                        }
                    })
                    .exceptionally(e -> {
                        showError("Error updating chat title: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            showError("Error updating chat title: " + e.getMessage());
        }
    }

    private void deleteChat(String chatId) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("username", username);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/api/chat/" + chatId))
                    .header("Content-Type", "application/json")
                    .method("DELETE", HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            SwingUtilities.invokeLater(() -> {
                                // Reload daftar chat setelah berhasil menghapus
                                loadUserChats();
                            });
                        } else {
                            JSONObject error = new JSONObject(response.body());
                            showError(error.getString("error"));
                        }
                    })
                    .exceptionally(e -> {
                        showError("Error deleting chat: " + e.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            showError("Error deleting chat: " + e.getMessage());
        }
    }
}