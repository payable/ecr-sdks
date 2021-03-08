package org.payable.ecr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
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

    public ECRTerminal(String token, String pos, Listener listener, int timeout) throws URISyntaxException, IOException, InterruptedException {
        super(prepareURI("ecr", token, pos), new Draft_6455(), null, timeout);
        this.clientListener = listener;
    }

    public ECRTerminal(String token, String pos, Listener listener) throws URISyntaxException, IOException, InterruptedException {
        super(prepareURI("ecr", token, pos));
        this.clientListener = listener;
    }

    public ECRTerminal(String address, String token, String pos, Listener listener) throws URISyntaxException, IOException, InterruptedException {
        super(prepareURI(address, token, pos));
        this.clientListener = listener;
    }

    public ECRTerminal(String token, String pos, int connectTimeout, Listener listener) throws URISyntaxException, IOException, InterruptedException {
        super(prepareURI("ecr", token, pos), new Draft_6455(), null, connectTimeout);
        this.clientListener = listener;
    }

    public ECRTerminal(String address, String token, String pos, int connectTimeout, Listener listener) throws URISyntaxException, IOException, InterruptedException {
        super(prepareURI(address, token, pos), new Draft_6455(), null, connectTimeout);
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

    private static URI prepareURI(String address, String token, String pos) throws URISyntaxException, IOException, InterruptedException {

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

        } else if (address.equals("ecr")) {
            return new URI("ws://ecr.payable.lk?token=" + token + "&pos=" + pos);
        }

        return address.contains("ws://") ? new URI(address) : new URI("ws://" + address + ":45454?token=" + token + "&pos=" + pos);
    }

    public void consoleLog(String message) {
        if (debug) {
            System.out.println("ECR_SDK: " + message);
        }
    }
}
