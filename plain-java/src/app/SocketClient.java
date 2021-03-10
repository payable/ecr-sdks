package app;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {

    private int serverPort = 45454;
    private String serverHost = "192.168.10.224";

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private PrintWriter printWriter;
    private SocketClientRunnable socketClientRunnable;
    private Thread socketClientThread;
    private SocketClientListener socketClientListener;

    public SocketClient(String host, SocketClientListener socketClientListener) {

        if (host != null) {
            this.serverHost = host;
        }

        this.socketClientListener = socketClientListener;
        socketClientRunnable = new SocketClientRunnable();

    }

    public void start() {

        if (socketClientThread != null && socketClientThread.isAlive()) {
            socketClientThread.interrupt();
        }

        socketClientThread = new Thread(socketClientRunnable);
        socketClientThread.start();
    }

    public void sendData(String data) {
        socketClientRunnable.sendData(data);
    }

    class SocketClientRunnable implements Runnable {

        @Override
        public void run() {

            try {

                // String cmd = "adb forward tcp:" + serverPort + " tcp:" + serverPort;
                // Runtime run = Runtime.getRuntime();
                // Process pr = run.exec(cmd);
                // pr.waitFor();
                // BufferedReader buf = new BufferedReader(new
                // InputStreamReader(pr.getInputStream()));
                // String line = "";
                // while ((line = buf.readLine()) != null) {
                // System.out.println(line);
                // }

                socket = new Socket(serverHost, serverPort);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                printWriter = new PrintWriter(outputStream);

                socketClientListener.onConnected("Just connected to " + socket.getRemoteSocketAddress());

                // out = new DataOutputStream(socket.getOutputStream());
                // out.writeUTF("Hello from " + socket.getLocalSocketAddress());
                sendData("Hello from " + socket.getLocalSocketAddress());

                while (true) {
                    // in = new DataInputStream(socket.getInputStream());
                    // socketClientListener.onDataReceived(in.readUTF());
                    int c;
                    String fromClient = "";
                    do {
                        c = inputStream.read();
                        fromClient += (char) c;
                    } while (inputStream.available() > 0);
                    if (c == -1) {
                        throw new EOFException();
                    }
                    socketClientListener.onDataReceived(fromClient);
                }

            } catch (EOFException ex) {
                socketClientListener.onDisconnected(ex);
            } catch (Exception ex) {
                if (ex.getMessage().contains("Cannot run program")) {
                    socketClientListener.onFailed(new NoADBException(ex.getMessage()));
                } else {
                    if (ex != null && ex.getMessage().contains("Socket closed")) {
                        return;
                    }
                    socketClientListener.onFailed(ex);
                }
            } finally {
                socketClientThread.interrupt();
            }

        }

        public void sendData(final String data) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // out.writeUTF(data);
                    printWriter.write(data);
                    printWriter.flush();
                }
            }).start();
        }

        public void stop() {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {

        if (socketClientRunnable != null) {
            socketClientRunnable.stop();
        }

        if (socketClientThread != null) {
            socketClientThread.interrupt();
        }
    }

    public interface SocketClientListener {

        void onConnected(String message);

        void onDisconnected(EOFException exception);

        void onFailed(Exception exception);

        void onDataReceived(String data);
    }

    public class NoADBException extends Exception {

        private static final long serialVersionUID = 1L;

        public NoADBException(String msg) {
            super(msg);
        }
    }
}
