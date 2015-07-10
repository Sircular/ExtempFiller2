package org.zalgosircular.extempfiller2.ui;

import org.zalgosircular.extempfiller2.messaging.*;
import org.zalgosircular.extempfiller2.messaging.Error;
import org.zalgosircular.extempfiller2.research.Topic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/10/2015.
 */
public class CLI implements Runnable {

    private BlockingQueue<InMessage> inQueue;
    private BlockingQueue<OutMessage> outQueue;
    private Thread inputThread;

    public CLI(BlockingQueue<InMessage> inQueue, BlockingQueue<OutMessage> outQueue) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;

        inputThread = new Thread(new InputRunnable(inQueue));
    }

    public void run() {
        System.out.println("Starting program.");
        boolean running = true;
        inputThread.start();
        while (running) {
            try {
                OutMessage msg = outQueue.take();
                // switches are weird
                String msgStr;
                Topic topic;
                switch (msg.getMessageType()) {
                    case DEBUG:
                        String debugMsg = (String)msg.getData();
                        System.out.println("[DEBUG] "+debugMsg);
                        break;
                    case SEARCHING:
                        topic = (Topic)msg.getData();
                        System.out.println("Now researching topic: "+topic.getTopic());
                        break;
                    case SAVING:
                        topic = (Topic)msg.getData();
                        System.out.println("Now saving articles for topic: "+topic.getTopic());
                        break;
                    case DONE:
                        topic = (Topic)msg.getData();
                        System.out.println("Finished researching topic: "+topic.getTopic());
                        break;
                    case ERROR:
                        Error e = (Error)msg.getData();
                        System.out.println("[ERROR] Exception while researching.");
                        e.getException().printStackTrace();
                        break;
                    case LOADING:
                        System.out.println("Loading saved topics...");
                        break;
                    case LOADED:
                        System.out.println("Loaded topic cache.");
                        break;
                    case CLOSED:
                        System.out.println("Researcher closed.");
                        running = false;
                        break;
                    case DELETING:
                        topic = (Topic)msg.getData();
                        System.out.println("Now deleting topic: "+topic.getTopic());
                        break;
                    case DELETED:
                        topic = (Topic)msg.getData();
                        System.out.println("Successfully deleted topic: "+topic.getTopic());
                        break;
                }
            } catch (InterruptedException e) {
                System.err.println("[ERROR] Message queue interrupted");
            }
        }
        inputThread.interrupt();
        System.out.println("Exiting program.");
    }

    private class InputRunnable implements Runnable {
        private BlockingQueue<InMessage> inQueue;
        public InputRunnable(BlockingQueue<InMessage> inQueue) {
            this.inQueue = inQueue;
        }
        public void run() {
            // this is some somewhat complex code, but it just
            // makes it possible to interrupt
            BufferedReader reader = new BufferedReader(
              new InputStreamReader(System.in)
            );
            boolean running = true;
            while (running) {
                try {
                    while (!reader.ready()) {
                        Thread.sleep(50);
                    }
                    String input = reader.readLine();
                    String[] words = input.split("\\s+");
                    if (words.length > 0) {
                        String command = words[0].toLowerCase();
                        if (command.equals("research")) {
                            if (words.length > 1) {
                                StringBuilder sb = new StringBuilder(words[1]);
                                for (int i = 2; i < words.length; i++) {
                                    sb.append(' ');
                                    sb.append(words[i]);
                                }
                                inQueue.add(new InMessage(InMessage.Type.RESEARCH, sb.toString()));
                            } else {
                                System.err.println("Syntax: research <topic>");
                            }
                        } else if (command.equals("delete")) {
                            if (words.length > 1) {
                                StringBuilder sb = new StringBuilder(words[1]);
                                for (int i = 2; i < words.length; i++) {
                                    sb.append(' ');
                                    sb.append(words[i]);
                                }
                                inQueue.add(new InMessage(InMessage.Type.DELETE, new Topic(sb.toString())));
                            } else {
                                System.err.println("Syntax: delete <topic>");
                            }
                        } else if (command.equals("exit") || command.equals("quit") ||
                                command.equals("stop")) {
                            inQueue.add(new InMessage(InMessage.Type.CLOSE, null));
                        }
                    }
                } catch (InterruptedException e) {
                    // this is expected; just end execution
                    running = false;
                } catch (IOException e) {
                    // this is not expected
                    e.printStackTrace();
                }
            }
        }
    }

}
