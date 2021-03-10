package org.payable.ecrclient.services;

import com.google.gson.JsonObject;
import org.payable.ecr.PAYableResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MySocketWebServer extends Thread {

    public interface Listener {
        void onOpen();

        void onError(Exception ex);

        String onRequest(String headers, String payload);
    }

    Listener listener;
    ServerSocket serverSocket;

    public MySocketWebServer(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {

        try {

            serverSocket = new ServerSocket(45455);
            if (listener != null) listener.onOpen();

            while (true) {

                final Socket socket = serverSocket.accept();
                SoundUtils.make();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            PrintWriter os = new PrintWriter(socket.getOutputStream(), true);

                            StringBuilder headers = new StringBuilder();
                            String headerLine;
                            while ((headerLine = br.readLine()).length() != 0) {
                                headers.append(headerLine);
                            }

                            StringBuilder payload = new StringBuilder();
                            while (br.ready()) {
                                payload.append((char) br.read());
                            }

                            /**
                             StringBuilder sb = new StringBuilder();
                             Scanner scanner = new Scanner(MySocketWebServer.class.getResourceAsStream("/server.html"));
                             while (scanner.hasNextLine()) {
                             sb.append(scanner.nextLine());
                             sb.append('\n');
                             }
                             response = sb.toString();
                             response = response.replaceAll("%VERSION%", "v1.5");
                             response = response.replaceAll("%TERMINAL%", StaticMemory.connectedTerminal == null ? "NULL" : StaticMemory.connectedTerminal);

                             os.print("HTTP/1.0 200" + "\r\n");
                             os.print("Content type: text/html" + "\r\n");
                             os.print("Content length: " + response.length() + "\r\n");
                             */

                            String response = null;
                            if (listener != null) {
                                response = listener.onRequest(headers.toString(), payload.toString());
                            }

                            if (response == null) {
                                JsonObject object = new JsonObject();
                                object.addProperty("status", StaticMemory.connectedTerminal == null ? PAYableResponse.STATUS_TERMINAL_UNREACHABLE : PAYableResponse.STATUS_TERMINAL_AUTHORIZED);
                                object.addProperty("terminal", StaticMemory.connectedTerminal);
                                object.addProperty("callback_port", StaticMemory.getPrefs().get("callback_port", null) == null ? null : Integer.parseInt(StaticMemory.getPrefs().get("callback_port", null)));
                                object.addProperty("message", "PAYable ECR Client is working");
                                response = object.toString();
                            }

                            os.print("HTTP/1.0 200" + "\r\n");
                            os.print("Content type: application/json" + "\r\n");
                            os.print("Content length: " + response.length() + "\r\n");

                            os.print("\r\n");
                            os.print(response + "\r\n");
                            os.flush();
                            socket.close();

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    }

                }).start();
            }
        } catch (Exception ex) {
            if (listener != null) listener.onError(ex);
        }
    }

    @Override
    public void interrupt() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.interrupt();
    }
}