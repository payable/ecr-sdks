package org.payable.ecrclient.services;

import org.payable.ecrclient.MainActivity;

import java.util.prefs.Preferences;

public class StaticMemory {

    public static String connectedTerminal;
    private static Preferences prefs;

    public static Preferences getPrefs() {

        if (prefs == null) {
            prefs = Preferences.userNodeForPackage(MainActivity.class);
        }

        return prefs;
    }
}
