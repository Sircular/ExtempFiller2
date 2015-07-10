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
    private final static String FILENAME = ".extemp.keys";
    private static HashMap<String, String> keyMap;
    private final static String SEP = "|";

    private static void initMap() throws IOException {
        keyMap = new HashMap<String, String>();
        // initialize the map for the first time
        final Path keyFilePath = Paths.get(FILENAME);
        if (Files.exists(keyFilePath)) {
            // we can actually initialize the keys, so we may as well
            // create the object
            final Scanner keyScanner = new Scanner(keyFilePath);
            while (keyScanner.hasNext()) {
                final String[] tokens = keyScanner.nextLine().split(Pattern.quote(SEP));
                keyMap.put(tokens[0], tokens[1]);
            }
            keyScanner.close();
        } else {
            throw new RuntimeException(FILENAME + " doesn't exist");
        }
    }

    public static String getKey(String account) {
        //First access
        if (keyMap == null) {
            try {
                initMap();
            } catch (IOException e) {
                //Happens when the file is inaccessible for operating system reasons (permissions etc)
                e.printStackTrace();
                return null;
            }
        }
        // we can finally actually get the keys
        if (keyMap.containsKey(account)) {
            return keyMap.get(account);
        }
        return null;
    }
}
