package org.zalgosircular.extempfiller2.ui;

import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.messaging.SavedMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/10/2015.
 */
public class CLI {
    private final Thread output;
    private final Thread input;
    private final BlockingQueue<InMessage> inQueue;
    private final BlockingQueue<OutMessage> outQueue;

    public CLI(BlockingQueue<InMessage> inQueue, BlockingQueue<OutMessage> outQueue) {
        this.output = new Thread(new OutputRunnable(outQueue));
        this.input = new Thread(new InputRunnable(inQueue));
        this.outQueue = outQueue;
        this.inQueue = inQueue;
    }

    public void run() {
        output.start();
        input.start();
        inQueue.add(new InMessage(InMessage.Type.OPEN, null));
        inQueue.add(new InMessage(InMessage.Type.LOAD, null));
    }

    public void close() {
        input.interrupt();
        output.interrupt();
    }

    private class InputRunnable implements Runnable {
        private final BlockingQueue<InMessage> inQueue;
        private boolean running;

        public InputRunnable(BlockingQueue<InMessage> inQueue) {
            this.inQueue = inQueue;
            this.running = false;
        }

        public void run() {
            // this is some somewhat complex code, but it just
            // makes it possible to interrupt
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in)
            );
            running = true;
            while (running && !Thread.interrupted()) {
                try {
                    final String[] words = getInput(reader);
                    if (words.length > 0) {
                        final String command = words[0].toLowerCase();

                        if (command.equals("research")) {
                            research(words);
                        } else if (command.equals("delete")) {
                            delete(words);
                        } else if (command.equals("view")) {
                            inQueue.add(new InMessage(InMessage.Type.LOAD, null));
                        } else if (command.equals("exit") ||
                                command.equals("quit") ||
                                command.equals("close")) {
                            inQueue.add(new InMessage(InMessage.Type.CLOSE, null));
                            running = false;
                        } else {
                            //Little bit of output code....
                            System.out.println("Commands: research [topic], delete [topic], exit, help");
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

        private void research(String[] words) {
            if (words.length > 1) {
                final StringBuilder sb = new StringBuilder(words[1]);
                for (int i = 2; i < words.length; i++) {
                    sb.append(' ');
                    sb.append(words[i]);
                }
                inQueue.add(new InMessage(InMessage.Type.RESEARCH, sb.toString()));
            } else {
                System.err.println("Syntax: research <topic>");
            }
        }

        private void delete(String[] words) {
            if (words.length > 1) {
                final StringBuilder sb = new StringBuilder(words[1]);
                for (int i = 2; i < words.length; i++) {
                    sb.append(' ');
                    sb.append(words[i]);
                }
                inQueue.add(new InMessage(InMessage.Type.DELETE, new Topic(sb.toString())));
            } else {
                System.err.println("Syntax: delete <topic>");
            }
        }
    }

    private class OutputRunnable implements Runnable {
        private final BlockingQueue<OutMessage> outQueue;
        private boolean running;

        public OutputRunnable(BlockingQueue<OutMessage> outQueue) {
            this.outQueue = outQueue;
            this.running = false;
        }

        public void run() {
            System.out.println("Starting program.");
            running = true;
            while (running && !Thread.interrupted()) {
                try {
                    OutMessage msg = outQueue.take();
                    // switches are weird
                    String msgStr;
                    Topic topic;
                    switch (msg.getMessageType()) {
                        case DEBUG:
                            msgStr = (String) msg.getData();
                            System.out.println("[DEBUG] " + msgStr);
                            break;
                        case SEARCHING:
                            topic = (Topic) msg.getData();
                            System.out.println("Now researching topic: " + topic.getTopic());
                            break;
                        case SAVING:
                            topic = (Topic) msg.getData();
                            System.out.println("Now saving articles for topic: " + topic.getTopic());
                            break;
                        case SAVED:
                            final SavedMessage savedMessage = (SavedMessage) msg.getData();
                            System.out.println(
                                    "Saved " + savedMessage.getArticle().getTitle()
                                            + " under topic: " + savedMessage.getTopic().getTopic());
                            break;
                        case DONE:
                            topic = (Topic) msg.getData();
                            System.out.println(String.format("Found total of %d articles for topic: %s",
                                    topic.getArticleCount(), topic.getTopic()));
                            break;
                        case ALREADY_RESEARCHED:
                            topic = (Topic)msg.getData();
                            System.out.println("Already researched topic: " + topic.getTopic());
                            break;
                        case ERROR:
                            final ErrorMessage e = (ErrorMessage) msg.getData();
                            System.err.println("[ERROR] Exception while researching " + e.getTopic().toString());
                            e.getException().printStackTrace();
                            break;
                        case LOADING:
                            System.out.println("Loading saved topics...");
                            break;
                        case LOADED:
                            System.out.println("Currently researched topics:");
                            List<Topic> topics = (List<Topic>)msg.getData();
                            for (Topic t : topics) {
                                System.out.println(String.format(
                                        "[%2d] %s", t.getArticleCount(), t.getTopic()
                                ));
                            }
                            break;
                        case CLOSED:
                            System.out.println("Researcher closed.");
                            running = false;
                            break;
                        case DELETING:
                            topic = (Topic) msg.getData();
                            System.out.println("Now deleting topic: " + topic.getTopic());
                            break;
                        case DELETED:
                            topic = (Topic) msg.getData();
                            System.out.println("Successfully deleted topic: " + topic.getTopic());
                            break;
                    }
                } catch (InterruptedException e) {
                    System.err.println("[ERROR] Message queue interrupted");
                    running = false;
                }
            }
            System.out.println("Exiting program.");
        }
    }
}
