package panels;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import raven.toast.Notifications;

public class ClientPanel extends JPanel {
    // Thông tin kết nối
    private static final String SERVER_IP = "administrator";
    private static final int SERVER_PORT = 6777;

    // Components
    private JTextField txtMessage;
    private JButton btnSend;
    private JButton btnFile;
    private JButton btnConnect;
    private JTextField txtUsername;
    private JTextArea txtChatArea;
    private JScrollPane scrollPane;
    private JLabel lblStatus;
    private JProgressBar progressBar;

    // Kết nối
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isConnected = false;
    private String username = "Anonymous";

    // Thread nhận tin nhắn
    private Thread receiveThread;

    public ClientPanel() {
        initComponents();
        setupLayout();
        addEventListeners();
    }

    private void initComponents() {
        txtMessage = new JTextField();
        btnSend = new JButton("Gửi");
        btnFile = new JButton("File");
        btnConnect = new JButton("Kết nối");
        txtUsername = new JTextField("User" + (int)(Math.random() * 1000));
        txtChatArea = new JTextArea();
        txtChatArea.setEditable(false);
        txtChatArea.setLineWrap(true);
        txtChatArea.setWrapStyleWord(true);
        scrollPane = new JScrollPane(txtChatArea);
        lblStatus = new JLabel("Chưa kết nối");
        lblStatus.setForeground(Color.RED);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panel phía trên - thông tin kết nối
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel userPanel = new JPanel(new BorderLayout(5, 0));
        userPanel.add(new JLabel("Username:"), BorderLayout.WEST);
        userPanel.add(txtUsername, BorderLayout.CENTER);

        topPanel.add(userPanel, BorderLayout.CENTER);
        topPanel.add(btnConnect, BorderLayout.EAST);
        topPanel.add(lblStatus, BorderLayout.SOUTH);

        // Panel phía dưới - nhập tin nhắn
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel sendPanel = new JPanel(new BorderLayout(5, 0));
        sendPanel.add(txtMessage, BorderLayout.CENTER);
        sendPanel.add(btnSend, BorderLayout.EAST);
        sendPanel.add(btnFile, BorderLayout.WEST);

        bottomPanel.add(sendPanel, BorderLayout.CENTER);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);

        // Panel chính - khu vực chat
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Thêm các panel vào panel chính
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addEventListeners() {
        btnConnect.addActionListener(e -> toggleConnection());
        btnSend.addActionListener(e -> sendMessage());
        btnFile.addActionListener(e -> selectAndSendFile());

        txtMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
    }

    private void toggleConnection() {
        if (isConnected) {
            disconnectFromServer();
        } else {
            connectToServer();
        }
    }

    private void connectToServer() {
        try {
            username = txtUsername.getText().trim();
            if (username.isEmpty()) {
                username = "Anonymous";
                txtUsername.setText(username);
            }

            socket = new Socket(SERVER_IP, SERVER_PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // Gửi username cho server
            dos.writeUTF("USERNAME:" + username);

            // Bắt đầu thread nhận tin nhắn
            startReceiveThread();

            isConnected = true;
            updateConnectionStatus(true);

            // Thông báo kết nối thành công
            addToChat("Hệ thống", "Đã kết nối đến server!");
            Notifications.getInstance().show(Notifications.Type.SUCCESS, "Kết nối thành công!");

        } catch (IOException e) {
            addToChat("Lỗi", "Không thể kết nối đến server: " + e.getMessage());
            Notifications.getInstance().show(Notifications.Type.ERROR, "Không thể kết nối đến server!");
        }
    }

    private void disconnectFromServer() {
        try {
            isConnected = false;

            // Dừng thread nhận tin nhắn
            if (receiveThread != null) {
                receiveThread.interrupt();
            }

            // Đóng các kết nối
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null) socket.close();

            updateConnectionStatus(false);

            // Thông báo ngắt kết nối
            addToChat("Hệ thống", "Đã ngắt kết nối khỏi server!");

        } catch (IOException e) {
            addToChat("Lỗi", "Lỗi khi ngắt kết nối: " + e.getMessage());
        }
    }

    private void startReceiveThread() {
        receiveThread = new Thread(() -> {
            try {
                while (isConnected) {
                    // Đọc loại tin nhắn
                    String messageType = dis.readUTF();

                    switch (messageType) {
                        case "TEXT":
                            // Nhận tin nhắn văn bản
                            String sender = dis.readUTF();
                            String message = dis.readUTF();
                            addToChat(sender, message);
                            break;

                        case "FILE":
                            // Nhận thông tin file
                            String fileSender = dis.readUTF();
                            String fileName = dis.readUTF();
                            long fileSize = dis.readLong();

                            // Hiển thị thông báo
                            addToChat("Hệ thống", "Đang nhận file '" + fileName + "' từ " + fileSender + "...");

                            // Hiển thị hộp thoại để chọn vị trí lưu file
                            int choice = JOptionPane.showConfirmDialog(this,
                                    "Bạn có muốn tải xuống file '" + fileName + "' không?",
                                    "Tải xuống file", JOptionPane.YES_NO_OPTION);

                            if (choice == JOptionPane.YES_OPTION) {
                                JFileChooser fileChooser = new JFileChooser();
                                fileChooser.setSelectedFile(new File(fileName));
                                int res = fileChooser.showSaveDialog(this);

                                if (res == JFileChooser.APPROVE_OPTION) {
                                    File saveFile = fileChooser.getSelectedFile();

                                    // Nhận và lưu file
                                    try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                                        byte[] buffer = new byte[4096];
                                        int bytesRead;
                                        long totalBytesRead = 0;

                                        while (totalBytesRead < fileSize) {
                                            bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead));
                                            if (bytesRead == -1) break;

                                            fos.write(buffer, 0, bytesRead);
                                            totalBytesRead += bytesRead;

                                            // Cập nhật tiến độ
                                            final int progress = (int) ((totalBytesRead * 100) / fileSize);
                                            SwingUtilities.invokeLater(() -> {
                                                progressBar.setValue(progress);
                                                progressBar.setString("Đang nhận: " + progress + "%");
                                            });
                                        }
                                    }
                                    // Thông báo hoàn thành
                                    addToChat("Hệ thống", "Đã nhận file từ " + fileSender + "! Lưu tại: " + saveFile.getAbsolutePath());
                                } else {
                                    skipFile(fileSize);
                                    addToChat("Hệ thống", "Đã bỏ qua file '" + fileName + "'");
                                }
                            } else {
                                skipFile(fileSize);
                                addToChat("Hệ thống", "Đã bỏ qua file '" + fileName + "'");
                            }

                            // Ẩn progress bar
                            SwingUtilities.invokeLater(() -> {
                                progressBar.setVisible(false);
                            });
                            break;
                    }
                }
            } catch (IOException e) {
                if (isConnected) {
                    addToChat("Lỗi", "Mất kết nối đến server: " + e.getMessage());
                    disconnectFromServer();
                }
            }
        });

        receiveThread.start();
    }

    private void skipFile(long fileSize) throws IOException {
        long skipped = 0;
        byte[] buffer = new byte[4096];
        while (skipped < fileSize) {
            int toRead = (int) Math.min(buffer.length, fileSize - skipped);
            int bytesRead = dis.read(buffer, 0, toRead);
            if (bytesRead == -1) break;
            skipped += bytesRead;
        }
    }


    private void sendMessage() {
        if (!isConnected) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Chưa kết nối đến server!");
            return;
        }

        String message = txtMessage.getText().trim();
        if (message.isEmpty()) return;

        try {
            // Gửi tin nhắn văn bản
            dos.writeUTF("TEXT");
            dos.writeUTF(message);
            dos.flush();

            // Hiển thị tin nhắn của chính mình trên giao diện
            addToChat(username, message);

            // Xóa tin nhắn đã gửi
            txtMessage.setText("");
            txtMessage.requestFocus();

        } catch (IOException e) {
            addToChat("Lỗi", "Không thể gửi tin nhắn: " + e.getMessage());
            disconnectFromServer();
        }
    }


    private void selectAndSendFile() {
        if (!isConnected) {
            Notifications.getInstance().show(Notifications.Type.WARNING, "Chưa kết nối đến server!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file để gửi");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            sendFile(selectedFile);
        }
    }

    private void sendFile(File file) {
        new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Hiển thị progress bar
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(0);
                        progressBar.setString("Đang gửi: 0%");
                        progressBar.setVisible(true);
                    });

                    // Thông báo đang gửi file
                    addToChat("Hệ thống", "Đang gửi file '" + file.getName() + "'...");

                    // Gửi thông tin file
                    dos.writeUTF("FILE");
                    dos.writeUTF(file.getName());
                    dos.writeLong(file.length());

                    // Gửi nội dung file
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalBytesSent = 0;
                        long fileSize = file.length();

                        while ((bytesRead = fis.read(buffer)) != -1) {
                            dos.write(buffer, 0, bytesRead);
                            totalBytesSent += bytesRead;

                            // Cập nhật tiến độ
                            int progress = (int)((totalBytesSent * 100) / fileSize);
                            publish(progress);
                        }

                        // Đảm bảo dữ liệu được gửi đi
                        dos.flush();
                    }

                    // Thêm dòng này: Hiển thị thông báo hoàn thành gửi file
                    addToChat("Hệ thống", "Đã gửi file '" + file.getName() + "' thành công!");

                } catch (IOException e) {
                    addToChat("Lỗi", "Không thể gửi file: " + e.getMessage());
                    disconnectFromServer();
                }

                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                progressBar.setValue(progress);
                progressBar.setString("Đang gửi: " + progress + "%");
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
            }
        }.execute();
    }

    private void addToChat(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String timestamp = sdf.format(new Date());

            txtChatArea.append("[" + timestamp + "] " + sender + ": " + message + "\n");
            txtChatArea.setCaretPosition(txtChatArea.getDocument().getLength());
        });
    }

    private void updateConnectionStatus(boolean connected) {
        if (connected) {
            lblStatus.setText("Đã kết nối với tên: " + username);
            lblStatus.setForeground(new Color(0, 150, 0));
            btnConnect.setText("Ngắt kết nối");
            txtUsername.setEditable(false);
        } else {
            lblStatus.setText("Chưa kết nối");
            lblStatus.setForeground(Color.RED);
            btnConnect.setText("Kết nối");
            txtUsername.setEditable(true);
        }
    }
}

