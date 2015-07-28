package org.zalgosircular.extempfiller2.authentication;

import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.ResearchWorker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

/**
 * Created by Walt on 7/8/2015.
 */
public class AuthManager {
    private final static String FILENAME = ".extemp.keys";
    private final static String SEP = "|";
    private final static Map<String, String> keyMap = Collections.synchronizedMap(new HashMap<String, String>());
    private final static Object lock = new Object();
    private static void initMap() throws IOException {
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

    // this is called every respondAuth; since we don't know when the
    // program is closing, the only other option would be to
    // set a timer (eugh)
    private static void saveMap() throws IOException {
        Path path = Paths.get(FILENAME);
        if (Files.exists(path)) {
            StringBuilder output = new StringBuilder();
            synchronized (keyMap) {
                for (String name : keyMap.keySet()) {
                    output.append(name);
                    output.append(SEP);
                    output.append(keyMap.get(name));
                    output.append('\n');
                }
            }

            Files.write(path, output.toString().getBytes(),
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            throw new RuntimeException(FILENAME + " doesn't exist");
        }
    }

    public static AuthResponse requestAuth(BlockingQueue<OutMessage> outQueue, AuthRequest request) throws InterruptedException {
        //First access
        if (keyMap.size() == 0) {
            try {
                initMap();
            } catch (IOException e) {
                //Happens when the file is inaccessible for operating system reasons (permissions etc)
                e.printStackTrace();
                return null;
            }
        }

        String[] authFields = request.getAuthFields();
        String[] authResponses = new String[authFields.length];
        //store array position and name together
        HashMap<Integer, String> missedFields = new HashMap<Integer, String>(authFields.length);

        for (int i = 0; i < authFields.length; i++) {
            String field = authFields[i];
            // get the key if it is saved
            if (keyMap.containsKey(field)) {
                authResponses[i] = keyMap.get(field);
            } else {
                missedFields.put(i, authFields[i]);
            }
        }

        //if the key wasn't contained in the map
        //request the key from ui and block until it is found
        //this only works if the calling thread is the researcher thread
        if (missedFields.size() != 0 && Thread.currentThread().getName().equals(ResearchWorker.THREAD_NAME)) {
            request = new AuthRequest(missedFields.values().toArray(new String[missedFields.values().size()]));
            outQueue.add(new OutMessage(OutMessage.Type.AUTH_REQUEST, request));
            synchronized (lock) {
                lock.wait();
            }
            for (Map.Entry<Integer, String> entry : missedFields.entrySet()) {
                authResponses[entry.getKey()] = keyMap.get(entry.getValue());
            }
        }

        return new AuthResponse(authFields, authResponses);
    }

    public static void respondAuth(AuthResponse response) {
        if (keyMap.size() == 0) {
            try {
                initMap();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        for (int i = 0; i < response.getRequestFields().length; i++) {
            keyMap.put(response.getRequestFields()[i], response.getResponses()[i]);
        }
        synchronized (lock) {
            lock.notifyAll();
        }
        try {
            saveMap();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
