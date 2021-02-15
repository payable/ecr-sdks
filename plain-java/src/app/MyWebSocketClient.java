package app;

import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

public class MyWebSocketClient extends WebSocketClient {

    WebSocketListeners.Client clientListener;

    public MyWebSocketClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public MyWebSocketClient(URI serverURI, WebSocketListeners.Client clientListener) {
        super(serverURI);
        this.clientListener = clientListener;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        consoleLog("new connection opened");
        clientListener.onOpen(handshakeData);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        consoleLog("closed with exit code " + code + " additional info: " + reason);
        clientListener.onClose(code, reason, remote);
    }

    @Override
    public void onMessage(String message) {
        consoleLog("received message: " + message);
        clientListener.onMessage(message);
    }

    @Override
    public void onMessage(ByteBuffer message) {
        consoleLog("received ByteBuffer");
        clientListener.onMessage(message);
    }

    @Override
    public void onError(Exception ex) {
        consoleLog("an error occurred:" + ex);
        clientListener.onError(ex);
    }

    public void consoleLog(String message) {
        System.out.println("WEB_SOCKET_CLIENT: " + message);
    }

}
