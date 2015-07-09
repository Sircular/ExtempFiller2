package org.zalgosircular.extempfiller2.authentication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by Walt on 7/8/2015.
 */
public class KeyManager {
    private static String workingDir;
    private final static String FILENAME = ".extempKeys";
    private static HashMap<String, String> keyMap;

    private static boolean initWorkingDir() {
        if (workingDir == null) {
            workingDir = KeyManager.class.getProtectionDomain().getCodeSource().getLocation().getFile();
            return true;
        }
        return false;
    }

    private static boolean initMap() {
        if (keyMap == null) {
            keyMap = new HashMap<>();
            return true;
        }
        return false;
    }

    public static String getKey(String account) {
        if (keyMap == null) {
            try {
                initWorkingDir();
                initMap();
                // initialize the map for the first time
                Path keyFilePath = Paths.get(workingDir, FILENAME);
                if (Files.exists(keyFilePath)) {
                    // we can actually initialize the keys, so we may as well
                    // create the object
                    Scanner keyScanner = new Scanner(keyFilePath);
                    String line;
                    while ((line = keyScanner.nextLine()) != null) {
                        String[] tokens = line.split(Pattern.quote("|"));
                        keyMap.put(tokens[0], tokens[1]);
                    }
                } else {
                    throw new RuntimeException(".extempKeys doesn't exist");
                }
            } catch (IOException e) {
                // we know it exists, it's just the scanner being stupid
                // just go with it. Scanner is a brat
                return null;
            }
        }
        // we can finally actually get the keys
        if (keyMap.containsKey(account)) {
            return keyMap.get(account);
        } else {
            return null;
        }
    }
}
