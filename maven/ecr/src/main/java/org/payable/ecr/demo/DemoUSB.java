package org.payable.ecr.demo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import com.google.gson.JsonSyntaxException;

import org.payable.ecr.ECRTerminal;
import org.payable.ecr.PAYableRequest;
import org.payable.ecr.PAYableResponse;

/**
 * PAYable SDK Demo - Internal (USB)
 *
 */
class DemoUSB {

    ECRTerminal ecrTerminal;

    public void start() {

        try {

            // Terminal IP Address, Token, POS-Name
            ecrTerminal = new ECRTerminal("127.0.0.1", "4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=", "JAVA-POS", new ECRTerminal.Listener() {

                        @Override
                        public void onOpen(String data) {

                            /*
                             * After the connection is successfully established you can start to send sale
                             * request to terminal.
                             * 
                             * Request in JSON: {"endpoint":"PAYMENT",
                             * "amount":20,"id":1,"method":"CARD","order_tracking":
                             * "some_id","receipt_email":"aslam@payable.lk","receipt_sms": "0762724081",
                             * "txid":14526}
                             * 
                             */

                            // 1. Construct the sale request object
                            PAYableRequest request = new PAYableRequest(PAYableRequest.ENDPOINT_PAYMENT, 252, 256.00,
                                    PAYableRequest.METHOD_CARD);

                            // 2. Convert to JSON
                            String jsonRequest = request.toJson();

                            // 3. Send to terminal
                            ecrTerminal.send(jsonRequest);

                            // 4. Expect the reponse at 'onMessage' method

                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {

                        }

                        @Override
                        public void onMessage(String message) {

                            // Map JSON to Java object (optional) or handle from String data
                            try {
                                PAYableResponse response = PAYableResponse.from(message);
                                System.out.println("RESPONSE: " + response.status);
                            } catch (JsonSyntaxException ex) {
                                // Invalid JSON
                            }
                        }

                        @Override
                        public void onMessage(ByteBuffer message) {
                            // Optional implementation
                        }

                        @Override
                        public void onError(Exception ex) {
                            // Handle connection exceptions
                        }
                    });

            // Just for the console print
            ecrTerminal.debug = true;

            // Initiate the terminal connection, call this method once and handle error when
            // terminal disconnected
            ecrTerminal.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new DemoUSB().start();
    }
}
