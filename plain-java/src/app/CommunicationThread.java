package app;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class CommunicationThread extends Thread {

    public interface SocketListener {

        void onConnected(String message);

        void onDisconnected(EOFException exception);

        void onFailed(Exception exception);

        void onDataReceived(String data);
    }

    private Socket clientSocket;
    private SocketListener socketListener;
    private InputStream inputStream;
    private OutputStream outputStream;
    private PrintWriter printWriter;

    public CommunicationThread(Socket socket, SocketListener socketListener) {
        try {
            this.clientSocket = socket;
            this.socketListener = socketListener;
            this.inputStream = clientSocket.getInputStream();
            this.outputStream = clientSocket.getOutputStream();
            this.printWriter = new PrintWriter(outputStream);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                int c;
                String fromClient = "";
                do {
                    c = inputStream.read();
                    fromClient += (char) c;
                } while (inputStream.available() > 0);
                if (c == -1) {
                    throw new EOFException();
                }
                socketListener.onDataReceived(fromClient);
            }
        } catch (EOFException e) {
            socketListener.onDisconnected(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interrupt() {
        try {
            inputStream.close();
            outputStream.close();
            printWriter.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.interrupt();
    }

    public void sendData(final String data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                printWriter.write(data);
                printWriter.flush();
            }
        }).start();
    }
}
