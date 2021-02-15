package org;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import com.google.gson.JsonSyntaxException;

import org.payable.ecr.ECRTerminal;
import org.payable.ecr.PAYableRequest;
import org.payable.ecr.PAYableResponse;

// mvn install:install-file -Dfile=/home/aslam/PAYable/ecr-sdks/java-ui/lib/ecr-1.0.jar -DgroupId=org.payable -DartifactId=ecr -Dversion=1.0

public class HomeController {

    @FXML
    private Text txtConsole;

    @FXML
    private TextField edtAddress, edtAuth;

    private ECRTerminal ecrTerminal;

    @FXML
    private void onBtnConnect() {
        
            try {

            if(ecrTerminal != null) {
                ecrTerminal.close();
            }

            ecrTerminal = new ECRTerminal("4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw=", "JAVA-POS", new ECRTerminal.Listener() {

                        @Override
                        public void onOpen(String data) {

                            PAYableRequest request = new PAYableRequest("PP35271812000161", PAYableRequest.ENDPOINT_PAYMENT, 252, 256.00, PAYableRequest.METHOD_CARD);
                            String jsonRequest = request.toJson();
                            ecrTerminal.send(jsonRequest);

                            txtConsole.setText(data);
                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {

                        }

                        @Override
                        public void onMessage(String message) {
                            try {

                                // PAYableResponse response = PAYableResponse.from(message);
                                // System.out.println("RESPONSE: " + response.status);

                                // if (response.status.equals("STATUS_TERMINAL_UNAUTHORIZED")) {
                                //     PAYableRequest request = new PAYableRequest("PP35271812000161", 44280);
                                //     ecrTerminal.send(request.toJson());
                                // }

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
            ecrTerminal.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBtnAuth() {
        
    }

    @FXML
    private void onBtnSale() {
        
    }
}
