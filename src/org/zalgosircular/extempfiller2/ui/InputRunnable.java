package org.zalgosircular.extempfiller2.ui;

import org.zalgosircular.extempfiller2.messaging.InMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan Lembke on 7/25/2015.
 */
class InputRunnable implements Runnable {
    private final BlockingQueue<InMessage> inQueue;
    private final InputStream inputStream;

    public InputRunnable(BlockingQueue<InMessage> inQueue, InputStream inputStream) {
        this.inQueue = inQueue;
        this.inputStream = inputStream;
    }

    public void run() {
        // this is some somewhat complex code, but it just
        // makes it possible to interrupt
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream)
        );
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final String[] words = getInput(reader);
                if (words.length > 0) {
                    final String command = words[0].toLowerCase();

                    if (command.equals("research")) {
                        research(words);
                    } else if (command.equals("researchfile")) {
                        researchFile(words);
                    } else if (command.equals("delete")) {
                        delete(words);
                    } else if (command.equals("view")) {
                        inQueue.put(new InMessage(InMessage.Type.GET, null));
                    } else if (command.equals("exit") ||
                            command.equals("quit") ||
                            command.equals("close")) {
                        inQueue.put(new InMessage(InMessage.Type.CLOSE, null));
                        throw new InterruptedException();
                    } else {
                        //Little bit of output code....
                        System.out.println("Commands: research [topic], researchFile [file], delete [topic], view, exit, help");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                //this is expected
            } catch (IOException e) {
                // this is not expected
                e.printStackTrace();
            }
        }
        try {
            reader.close();
        } catch (IOException e) {
            //should never happen
            e.printStackTrace();
        }
    }

    private String[] getInput(BufferedReader reader) throws IOException, InterruptedException {
        while (!reader.ready()) {
            Thread.sleep(50);
        }
        String input = reader.readLine();
        // split on one or more spaces
        return input.split("\\s+");
    }

    private void research(String[] words) throws InterruptedException {
        if (words.length > 1) {
            final StringBuilder sb = new StringBuilder(words[1]);
            for (int i = 2; i < words.length; i++) {
                sb.append(' ');
                sb.append(words[i]);
            }
            inQueue.put(new InMessage(InMessage.Type.RESEARCH, sb.toString()));
        } else {
            System.err.println("Syntax: research <topic>");
        }
    }

    private void researchFile(String[] words) throws InterruptedException {
        if (words.length > 1) {
            final StringBuilder sb = new StringBuilder(words[1]);
            for (int i = 2; i < words.length; i++) {
                sb.append(' ');
                sb.append(words[i]);
            }
            final String pathStr = sb.toString();
            final Path filePath = Paths.get(pathStr);
            if (Files.exists(filePath)) {
                try {
                    final List<String> lines = Files.readAllLines(filePath);
                    for (String line : lines) {
                        inQueue.put(new InMessage(InMessage.Type.RESEARCH, line));
                    }
                    System.out.println(String.format("Queued %d topics for research.", lines.size()));
                } catch (IOException e) {
                    System.err.println("Could not read from file: " + pathStr);
                    e.printStackTrace();
                }
            } else {
                System.err.println("No such file: " + pathStr);
            }
        } else {
            System.err.println("Syntax: researchFile <file>");
        }
    }

    private void delete(String[] words) throws InterruptedException {
        if (words.length > 1) {
            final StringBuilder sb = new StringBuilder(words[1]);
            for (int i = 2; i < words.length; i++) {
                sb.append(' ');
                sb.append(words[i]);
            }
            inQueue.put(new InMessage(InMessage.Type.DELETE, sb.toString()));
        } else {
            System.err.println("Syntax: delete <topic>");
        }
    }
}
