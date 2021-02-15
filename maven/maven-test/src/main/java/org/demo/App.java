package org.demo;

import org.payable.ecr.ECRTerminal;

import java.nio.ByteBuffer;

/**
 * Hello world!
 */
public class App {

    public static void main(String[] args) {

        System.out.println("Hello World!");

        try {

            ECRTerminal ecrTerminal = new ECRTerminal("4DqxynHGtHNckmCrRzvVxkwuSfr8faRmPrLIX0hmkqw", "JAVA", new ECRTerminal.Listener() {

                @Override
                public void onOpen(String s) {

                }

                @Override
                public void onClose(int i, String s, boolean b) {

                }

                @Override
                public void onMessage(String s) {
                    
                }

                @Override
                public void onMessage(ByteBuffer byteBuffer) {

                }

                @Override
                public void onError(Exception e) {

                }
            });

            ecrTerminal.debug = true;
            ecrTerminal.connect();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
