package org.payable.ecrclient.services;

import java.awt.*;

public class SoundUtils {

    public static void make() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Toolkit.getDefaultToolkit().beep();
            }
        }).start();
    }

}
