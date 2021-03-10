package org.payable.ecrclient;

import com.google.gson.JsonSyntaxException;
import com.sun.xml.internal.ws.util.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.payable.ecr.ECRTerminal;
import org.payable.ecr.PAYableRequest;
import org.payable.ecr.PAYableResponse;
import org.payable.ecrclient.services.MySocketWebServer;
import org.payable.ecrclient.services.SoundUtils;
import org.payable.ecrclient.services.StaticMemory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static java.awt.Frame.*;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class MainActivity {

    private JFrame frame;
    private JPanel mainPanel;
    private JTextField textFieldAddress;
    private JTextPane textPaneResponse;
    private JButton buttonDisconnect;
    private JButton buttonConnect;
    private JTextField textFieldAuthCode;
    private JTextField textFieldCallbackPort;
    private JCheckBox checkBoxReconnect;
    private JButton buttonStop;
    private JScrollPane jScrollPaneResponse;
    private JTextField textFieldAmount;
    private JButton buttonSend;
    private JComboBox comboBoxType;
    private JLabel labelTerminal;
    private JLabel labelTerminalStatus;
    private JLabel labelConnType;
    private TrayIcon trayIcon;

    private int logLine = 0;
    private ECRTerminal ecrTerminal;
    private int ecrTerminalHashCode;
    private String address;
    private boolean canReconnect;
    private int socketConnectTimeout = 1000 * 15;

    private int STATUS_CONNECTING = 1;
    private int STATUS_CONNECTED = 2;
    private int STATUS_DISCONNECTED = 3;
    private int STATUS_RECONNECTING = 4;
    private int STATUS_NOT_CONNECTED = 5;

    public MainActivity(final JFrame frame) {

        this.frame = frame;
        String reconnectPref = StaticMemory.getPrefs().get("reconnect", "");

        textFieldAddress.setText(StaticMemory.getPrefs().get("terminal", ""));
        textFieldAuthCode.setText(StaticMemory.getPrefs().get("auth_code", ""));
        textFieldCallbackPort.setText(StaticMemory.getPrefs().get("callback_port", ""));
        checkBoxReconnect.setSelected(reconnectPref.equals("1"));

        statusPanelUpdate(STATUS_NOT_CONNECTED);

        if (reconnectPref.equals("1")) {
            connectECR();
        }

        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFormValidated()) {
                    connectECR();
                }
            }
        });

        buttonDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectECR();
            }
        });

        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopECR();
            }
        });

        buttonSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!textFieldAmount.getText().matches("-?\\d+(\\.\\d+)?") || textFieldAmount.getText().length() > 6) {
                    showMessageDialog(frame, "Invalid amount!", "Error", ERROR_MESSAGE);
                } else if (ecrTerminal == null || !ecrTerminal.isOpen()) {
                    showMessageDialog(frame, "Terminal is not connected!", "Error", ERROR_MESSAGE);
                } else {
                    String method = comboBoxType.getSelectedItem().toString().equalsIgnoreCase("WALLET") ? PAYableRequest.METHOD_WALLET : PAYableRequest.METHOD_CARD;
                    PAYableRequest request = new PAYableRequest(PAYableRequest.ENDPOINT_PAYMENT, 1, Double.valueOf(textFieldAmount.getText()), method);
                    ecrTerminal.send(request.toJson());
                }
            }
        });

        new MySocketWebServer(new MySocketWebServer.Listener() {

            @Override
            public void onOpen() {
                logResponse("HTTP-onOpen");
            }

            @Override
            public void onError(Exception ex) {
                logResponse("HTTP-onError: " + ex);
            }

            @Override
            public String onRequest(String headers, String payload) {

                logResponse("HTTP-onRequest:\nheaders:\n" + headers + "\npayload:\n" + payload);

                try {

                    if (headers.startsWith("POST /")) {

                        PAYableRequest payableRequest = PAYableRequest.from(payload);

                        if (payableRequest == null) {

                            return "{\"message\":\"STATUS_INVALID_DATA\"}";

                        } else if (ecrTerminal != null && ecrTerminal.isOpen()) {

                            if (textFieldAddress.getText().contains("PP")) {
                                payableRequest.terminal = textFieldAddress.getText();
                            }

                            ecrTerminal.send(payableRequest.toJson());
                            return "{\"status\":\"STATUS_REQUEST_SENT\"}";

                        } else {
                            return "{\"status\":\"STATUS_TERMINAL_UNREACHABLE\"}";
                        }
                    }

                } catch (WebsocketNotConnectedException ex) {
                    logResponse("onRequest-onError: " + ex);
                    return "{\"status\":\"STATUS_TERMINAL_UNREACHABLE\"}";
                }

                return null;
            }

        }).start();

        startTray();
    }

    private void startTray() {

        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        PopupMenu popup = new PopupMenu();
        try {
            trayIcon = new TrayIcon(ImageIO.read(MainActivity.class.getResourceAsStream("/icon.png")), "ECR Client");
            trayIcon.setImageAutoSize(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SystemTray.getSystemTray().remove(trayIcon);
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                ShellUtils.exec("adb kill-server");
                System.exit(0);
            }
        });

        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setState(NORMAL);
                // frame.setVisible(true);
            }
        });

        frame.addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if (e.getNewState() == ICONIFIED) {
                    // frame.setState(ICONIFIED);
                    // frame.setVisible(false);
                }
            }
        });

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                frame.setState(ICONIFIED);
            }
        });

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void connectECR() {

        statusPanelUpdate(STATUS_RECONNECTING);

        logResponse(textFieldAddress.getText() + " is being connected...");

        storeData();

        try {

            disconnectECR();

            address = textFieldAddress.getText().contains("PP") ? "ecr" : textFieldAddress.getText();

            ecrTerminal = new ECRTerminal(address, "4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw", "ECR-CLIENT", socketConnectTimeout, new ECRTerminal.Listener() {

                @Override
                public void onOpen(String s) {

                    if (ecrTerminal == null || ecrTerminalHashCode != ecrTerminal.hashCode()) return;

                    logResponse("onOpen: " + (textFieldAddress.getText().contains("PP") ? "ECR Server" : textFieldAddress.getText()) + " is connected");

                    statusPanelUpdate(STATUS_CONNECTED);

                    if (address.equals("ecr")) {
                        PAYableRequest request = new PAYableRequest(textFieldAddress.getText(), Integer.valueOf(textFieldAuthCode.getText()));
                        ecrTerminal.send(request.toJson());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {

                    if (ecrTerminal == null || ecrTerminalHashCode != ecrTerminal.hashCode()) return;

                    logResponse("onClose: closed with exit code " + code + " additional info: " + reason);

                    StaticMemory.connectedTerminal = null;
                    statusPanelUpdate(STATUS_DISCONNECTED);

                    if (canReconnect && !reason.contains("STATUS")) {
                        reconnect();
                    }
                }

                @Override
                public void onMessage(String message) {

                    if (ecrTerminal == null || ecrTerminalHashCode != ecrTerminal.hashCode()) return;

                    logResponse("onMessage: " + message);

                    try {

                        PAYableResponse response = PAYableResponse.from(message);

                        if (response.status.equals(PAYableResponse.STATUS_TERMINAL_AUTHORIZED)) {

                            StaticMemory.connectedTerminal = response.origin;
                            statusPanelUpdate(STATUS_CONNECTED);

                        } else if (response.status.equals("STATUS_TERMINAL_UNREACHABLE")) {
                            disconnectECR(1009, "STATUS_TERMINAL_UNREACHABLE");
                        } else if (response.status.equals("STATUS_INVALID_AUTHCODE")) {
                            disconnectECR(1009, "STATUS_INVALID_AUTHCODE");
                        } else if (!textFieldCallbackPort.getText().isEmpty()) {
                            sendPost(textFieldCallbackPort.getText(), response.toJson());
                        }

                    } catch (JsonSyntaxException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onMessage(ByteBuffer byteBuffer) {
                    if (ecrTerminal == null || ecrTerminalHashCode != ecrTerminal.hashCode()) return;
                }

                @Override
                public void onError(Exception ex) {

                    if (ecrTerminal == null || ecrTerminalHashCode != ecrTerminal.hashCode()) return;

                    logResponse("onError: " + ex);

                    buttonConnect.setEnabled(true);
                    textFieldAddress.setEnabled(true);
                    textFieldAuthCode.setEnabled(true);
                    textFieldCallbackPort.setEnabled(true);
                    checkBoxReconnect.setEnabled(true);
                    buttonDisconnect.setEnabled(false);

                    StaticMemory.connectedTerminal = null;
                    statusPanelUpdate(STATUS_DISCONNECTED);
                }
            });

            ecrTerminalHashCode = ecrTerminal.hashCode();
            ecrTerminal.debug = true;
            ecrTerminal.setConnectionLostTimeout(textFieldAddress.getText().contains("PP") ? 10 : 5);
            ecrTerminal.connect();

        } catch (Exception ex) {

            ex.printStackTrace();

            buttonConnect.setEnabled(true);
            textFieldAddress.setEnabled(true);
            textFieldAuthCode.setEnabled(true);
            textFieldCallbackPort.setEnabled(true);
            checkBoxReconnect.setEnabled(true);
            buttonDisconnect.setEnabled(false);
        }
    }

    private void statusPanelUpdate(int status) {

        labelConnType.setText(StringUtils.capitalize(getConnectionType().equals("internal") ? "Internal LAN" : "External WAN"));

        if (status == STATUS_CONNECTED) {

            buttonConnect.setEnabled(false);
            textFieldAddress.setEnabled(false);
            textFieldAuthCode.setEnabled(false);
            textFieldCallbackPort.setEnabled(false);
            checkBoxReconnect.setEnabled(false);
            buttonDisconnect.setEnabled(true);
            buttonStop.setEnabled(false);

            textFieldAmount.setEnabled(true);
            comboBoxType.setEnabled(true);
            buttonSend.setEnabled(true);

            labelTerminal.setText(StaticMemory.connectedTerminal);
            labelTerminalStatus.setText("Online");
            labelTerminalStatus.setForeground(Color.decode("#008b00"));

        } else if (status == STATUS_DISCONNECTED) {

            buttonConnect.setEnabled(true);
            textFieldAddress.setEnabled(true);
            textFieldAuthCode.setEnabled(true);
            textFieldCallbackPort.setEnabled(true);
            checkBoxReconnect.setEnabled(true);
            buttonDisconnect.setEnabled(false);
            buttonStop.setEnabled(false);

            textFieldAmount.setEnabled(false);
            comboBoxType.setEnabled(false);
            buttonSend.setEnabled(false);

            labelTerminal.setText("Disconnected");
            labelTerminalStatus.setText("Offline");
            labelTerminalStatus.setForeground(Color.decode("#ff0000"));

        } else if (status == STATUS_CONNECTING) {

            buttonConnect.setEnabled(false);
            textFieldAddress.setEnabled(false);
            textFieldAuthCode.setEnabled(false);
            textFieldCallbackPort.setEnabled(false);
            checkBoxReconnect.setEnabled(false);
            buttonDisconnect.setEnabled(false);
            buttonStop.setEnabled(true);

            textFieldAmount.setEnabled(false);
            comboBoxType.setEnabled(false);
            buttonSend.setEnabled(false);

            labelTerminal.setText(StaticMemory.connectedTerminal != null ? StaticMemory.connectedTerminal : "Not ready");
            labelTerminalStatus.setText("Connecting...");
            labelTerminalStatus.setForeground(Color.decode("#ff0000"));

        } else if (status == STATUS_NOT_CONNECTED) {

            buttonConnect.setEnabled(true);
            textFieldAddress.setEnabled(true);
            textFieldAuthCode.setEnabled(true);
            textFieldCallbackPort.setEnabled(true);
            checkBoxReconnect.setEnabled(true);
            buttonDisconnect.setEnabled(false);
            buttonStop.setEnabled(false);

            textFieldAmount.setEnabled(false);
            comboBoxType.setEnabled(false);
            buttonSend.setEnabled(false);

            labelTerminal.setText("Not ready");
            labelTerminalStatus.setText("Not connected");
            labelTerminalStatus.setForeground(Color.decode("#ff0000"));
        }
    }

    public String getConnectionType() {
        return textFieldAddress.getText().contains("PP") ? "external" : "internal";
    }

    private void disconnectECR(int code, String message) {
        try {
            if (ecrTerminal != null && ecrTerminal.isOpen()) {
                ecrTerminal.close(code, message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void disconnectECR() {
        try {
            if (ecrTerminal != null && ecrTerminal.isOpen()) {
                ecrTerminal.close(1009, "STATUS_DISCONNECTED");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void reconnect() {
        logResponse("Retrying...");
        statusPanelUpdate(STATUS_RECONNECTING);
        connectECR();
    }

    private void stopECR() {
        canReconnect = false;
        StaticMemory.connectedTerminal = null;
        ecrTerminal.closeConnection(1009, "STATUS_STOPPED");
        ecrTerminal = null;
        statusPanelUpdate(STATUS_DISCONNECTED);
    }

    private boolean isFormValidated() {

        if (!textFieldAddress.getText().contains("PP") && !InetAddressUtils.isIPv4Address(textFieldAddress.getText()) || textFieldAddress.getText().length() > 20) {
            showMessageDialog(frame, "Invalid IP address or device serial number!", "Error", ERROR_MESSAGE);
            return false;
        } else if (!textFieldAuthCode.getText().isEmpty() && !textFieldAuthCode.getText().matches("-?\\d+(\\.\\d+)?") || textFieldAuthCode.getText().length() > 5) {
            showMessageDialog(frame, "Invalid ECR Auth Code!", "Error", ERROR_MESSAGE);
            return false;
        } else if (!textFieldCallbackPort.getText().isEmpty() && !textFieldCallbackPort.getText().matches("-?\\d+(\\.\\d+)?") || textFieldCallbackPort.getText().length() > 5) {
            showMessageDialog(frame, "Invalid Port number!", "Error", ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void storeData() {

        StaticMemory.connectedTerminal = null;
        statusPanelUpdate(STATUS_CONNECTING);

        if (!textFieldAddress.getText().isEmpty()) {
            StaticMemory.getPrefs().put("terminal", textFieldAddress.getText());
        } else {
            StaticMemory.getPrefs().remove("terminal");
        }

        if (!textFieldAuthCode.getText().isEmpty()) {
            StaticMemory.getPrefs().put("auth_code", textFieldAuthCode.getText());
        } else {
            StaticMemory.getPrefs().remove("auth_code");
        }

        if (!textFieldCallbackPort.getText().isEmpty()) {
            StaticMemory.getPrefs().put("callback_port", textFieldCallbackPort.getText());
        } else {
            StaticMemory.getPrefs().remove("callback_port");
        }

        StaticMemory.getPrefs().put("reconnect", checkBoxReconnect.isSelected() ? "1" : "0");

        canReconnect = checkBoxReconnect.isSelected();
    }

    private void sendPost(final String port, final String json) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpPost post = new HttpPost("http://127.0.0.1:" + port);
                    RequestConfig.Builder requestConfig = RequestConfig.custom();
                    int timeout = 30 * 1000;
                    requestConfig.setConnectTimeout(timeout);
                    requestConfig.setConnectionRequestTimeout(timeout);
                    requestConfig.setSocketTimeout(timeout);
                    post.setConfig(requestConfig.build());
                    post.addHeader("content-type", "application/json");
                    post.setEntity(new StringEntity(json));
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    CloseableHttpResponse response = httpClient.execute(post);
                    logResponse(EntityUtils.toString(response.getEntity()));
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                    logResponse("sendPost-error: " + ex.getMessage());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    logResponse("sendPost-error: " + ex.getMessage());
                }
            }
        }).start();
    }

    private void logResponse(String message) {
        textPaneResponse.setText((++logLine) + " - " + message + "\n" + textPaneResponse.getText());
        textPaneResponse.setCaretPosition(0);
    }

    public static void open() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JFrame frame = new JFrame("ECR Client - v1.1.1");
        frame.setContentPane(new MainActivity(frame).mainPanel);
        frame.setSize(550, 450);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        try {
            frame.setIconImage(ImageIO.read(MainActivity.class.getResourceAsStream("/icon.png")));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        frame.setVisible(true);
    }
}
