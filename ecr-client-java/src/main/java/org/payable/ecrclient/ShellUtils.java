package org.payable.ecrclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellUtils {

    // public static String adb = System.getProperty("user.dir") + "\\sys\\bin\\adb-v23.1\\adb.exe ";
    public static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public static String exec(String command) {

        try {

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                response.append(line + "\n");
            }

            return response.toString();

        } catch (IOException ex) {
            return "IOException: " + ex.toString();
        }
    }

    public static boolean isError(String exception) {
        return exception.contains("IOException");
    }
}
