package app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketThread extends Thread {

    public enum SocketType {SERVER, CLIENT}

    String host = "127.0.0.1";
    int port = 45454;
    SocketType socketType;
    ServerSocket serverSocket;
    CommunicationThread.SocketListener socketListener;
    CommunicationThread communicationThread;

    public SocketThread(SocketType socketType, int port, String host, CommunicationThread.SocketListener socketListener) {
        this.socketType = socketType;
        this.port = port > 0 ? port : this.port;
        this.host = host != null ? host : this.host;
        this.socketListener = socketListener;
    }

    @Override
    public void run() {
        try {
            if (socketType == SocketType.SERVER) {
                serverSocket = new ServerSocket(port);
                while (true) {
                    Socket socket = serverSocket.accept();
                    socketListener.onConnected("Client connected from: " + socket.getRemoteSocketAddress());
                    communicationThread = new CommunicationThread(socket, socketListener);
                    communicationThread.start();
                }
            } else if (socketType == SocketType.CLIENT) {
                Socket socket = new Socket(host, port);
                socketListener.onConnected("Server connected from: " + socket.getRemoteSocketAddress());
                communicationThread = new CommunicationThread(socket, socketListener);
                communicationThread.start();
            }
        } catch (IOException e) {
            socketListener.onFailed(e);
        }
    }

    @Override
    public void interrupt() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (communicationThread != null) {
                communicationThread.interrupt();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        super.interrupt();
    }

    public void sendData(String data) {
        communicationThread.sendData(data);
    }
}
