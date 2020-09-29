package org.payable.ecr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class ECRTerminal extends WebSocketClient {

    public boolean debug = false;
    private Listener clientListener;

    public interface Listener {

        void onOpen(String data);

        void onClose(int code, String reason, boolean remote);

        void onMessage(String message);

        void onMessage(ByteBuffer message);

        void onError(Exception ex);
    }

    public ECRTerminal(String address, Listener listener) throws URISyntaxException, IOException, InterruptedException {
        super(prepareURI(address));
        this.clientListener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        consoleLog("new connection opened");
        clientListener.onOpen("connected");
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

    private static URI prepareURI(String address) throws URISyntaxException, IOException, InterruptedException {

        if (address.equals("127.0.0.1")) {

            String cmd = "adb forward tcp:45454 tcp:45454";
            Runtime run = Runtime.getRuntime();
            Process pr = run.exec(cmd);
            pr.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
            while ((line = buf.readLine()) != null) {
                System.out.println(line);
            }
        }

        return address.contains("ws://") ? new URI(address) : new URI("ws://" + address + ":45454");
    }

    public void consoleLog(String message) {
        if (debug) {
            System.out.println("ECR_SDK: " + message);
        }
    }
}
