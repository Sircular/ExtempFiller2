package org.zalgosircular.extempfiller2.authentication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    // this is called every putKey; since we don't know when the
    // program is closing, the only other option would be to
    // set a timer (eugh)
    private static void saveMap() throws IOException {
        Path path = Paths.get(FILENAME);
        if (Files.exists(path)) {
            StringBuilder output = new StringBuilder();
            for (String name : keyMap.keySet()) {
                output.append(name);
                output.append(SEP);
                output.append(keyMap.get(name));
                output.append('\n');
            }

            Files.write(path, output.toString().getBytes(),
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
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

    public static void putKey(String account, String key) {
        if (keyMap == null) {
            try {
                initMap();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        keyMap.put(account, key);
        try {
            saveMap();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
