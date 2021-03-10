package app;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.java_websocket.handshake.ServerHandshake;

import app.CommunicationThread.SocketListener;

public class UIDemo {

    int socketType = 2;
    SocketListener socketListener;
    SocketThread socketThread;
    MyWebSocketClient webSocketClient;

    JTextArea textArea;

    public void start() {

        JFrame frame = new JFrame("Socket Android");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 300);
        frame.setResizable(false);

        JPanel panel = new JPanel();

        JButton btnConnect = new JButton("Connect");
        JButton btnSend = new JButton("Send");
        btnSend.setEnabled(false);

        textArea = new JTextArea(15, 50);
        textArea.setText("Not connected.");
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        panel.add(btnConnect);
        panel.add(btnSend);
        panel.add(scrollPane);

        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setVisible(true);

        socketListener = new CommunicationThread.SocketListener() {

            @Override
            public void onConnected(String message) {
                System.out.println("onConnected: " + message);
                textArea.setText("onConnected: " + message + "\n" + textArea.getText());
                btnConnect.setEnabled(false);
                btnSend.setEnabled(true);
            }

            @Override
            public void onFailed(Exception exception) {
                System.out.println("onFailed: " + exception.getMessage());
                textArea.setText(exception.getMessage() + "\n" + textArea.getText());
            }

            @Override
            public void onDataReceived(String data) {
                System.out.println("onDataReceived: " + data);
                textArea.setText(data + "\n" + textArea.getText());
            }

            @Override
            public void onDisconnected(EOFException exception) {
                System.out.println("onDisconnected");
                textArea.setText("Disconnected.\n" + textArea.getText());
                btnConnect.setEnabled(true);
                btnSend.setEnabled(false);
            }
        };

        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent args) {
                if (socketType == 1) {
                    if (socketThread != null) {
                        socketThread.interrupt();
                    }
                    socketThread = new SocketThread(SocketThread.SocketType.CLIENT, 45454, "192.168.8.103",
                            socketListener);
                    socketThread.start();
                } else {
                    if (webSocketClient != null) {
                        webSocketClient.close();
                    }
                    try {
                        URI uri = new URI("ws://192.168.1.16:45454?token=4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=&pos=COMPANY-1");
                        webSocketClient = new MyWebSocketClient(uri, new WebSocketListeners.Client() {
                            @Override
                            public void onOpen(ServerHandshake handshakeData) {
                                consoleLog("WebSocket: new connection opened");
                                btnSend.setEnabled(true);
                            }

                            @Override
                            public void onClose(int code, String reason, boolean remote) {
                                consoleLog("WebSocket: closed with exit code " + code + " additional info: " + reason);
                            }

                            @Override
                            public void onMessage(String message) {
                                consoleLog("WebSocket: received message: " + message);
                            }

                            @Override
                            public void onMessage(ByteBuffer message) {
                                consoleLog("WebSocket: received ByteBuffer");
                            }

                            @Override
                            public void onError(Exception ex) {
                                consoleLog("WebSocket: an error occurred:" + ex);
                            }
                        });
                        webSocketClient.connect();

                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent args) {
                if(socketType == 1) {
                    socketThread.sendData("{\"amount\":20,\"endpoint\":\"PAYMENT\",\"method\":\"CARD\"}");
                } else {
                    if(webSocketClient != null) {
                        webSocketClient.send("{\"amount\":20,\"endpoint\":\"PAYMENT\",\"method\":\"CARD\"}");
                    }
                }
            }
        });
    }

    void consoleLog(String message) {
        textArea.setText(message + "\n" + textArea.getText());
    }
}