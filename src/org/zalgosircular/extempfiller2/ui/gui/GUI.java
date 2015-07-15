package org.zalgosircular.extempfiller2.ui.gui;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/15/2015.
 */
public class GUI {
    private final GUIWindow window;
    private final BlockingQueue<OutMessage> outQueue;

    public GUI(BlockingQueue<InMessage> inQueue, BlockingQueue<OutMessage> outQueue) {
        this.window = new GUIWindow(inQueue);
        this.outQueue = outQueue;
    }

    public void run() {
        window.init();
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                window.start();
            }
        });

        addDebugMessage("Initializing ExtempFiller2...");

        boolean running = true;
        while (running) {
            try {
                OutMessage msg = outQueue.take();
                Topic topic;
                switch (msg.getMessageType()) {
                    case LOADING:
                        addDebugMessage("Loading topics from filesystem...");
                        break;
                    case LOADED:
                        addDebugMessage("Loaded topics from filesystem.");
                        break;
                    case RETRIEVED:
                        addDebugMessage("Loaded topics from cache.");
                        break;
                    case DEBUG:
                        addDebugMessage((String) msg.getData());
                        break;
                    case SEARCHING:
                        topic = (Topic)msg.getData();
                        addDebugMessage("Now researching topic: "+topic.getTopic());
                        setTopicState(topic, TopicState.RESEARCHING);
                        break;
                    case DONE:
                        topic = (Topic)msg.getData();
                        addDebugMessage("Finished researching message: "+topic.getTopic());
                        setTopicState(topic, TopicState.RESEARCHED);
                        break;
                    case DELETING:
                        topic = (Topic)msg.getData();
                        addDebugMessage("Deleting message: "+topic.getTopic());
                        setTopicState(topic, TopicState.DELETING);
                        break;
                    case DELETED:
                        topic = (Topic)msg.getData();
                        addDebugMessage("Deleted message: "+topic.getTopic());
                        setTopicState(topic, TopicState.DELETED);
                        break;
                    case ERROR:
                        topic = (Topic)msg.getData();
                        addDebugMessage("Error researching message: "+topic.getTopic());
                        setTopicState(topic, TopicState.ERROR);
                        break;
                    case CLOSED:
                        closeWindow();
                        running = false;
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private void addDebugMessage(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.addDebugMessage(msg);
            }
        });
    }

    private void setTopicState(final Topic topic, final TopicState state) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.setTopicState(topic, state);
            }
        });
    }

    private void closeWindow() {
        System.out.println("Closing ExtempFiller2.");
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    window.dispose();
                }
            });
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            return;
        }
    }

    public enum TopicState {
        RESEARCHING,
        RESEARCHED,
        DELETING,
        DELETED, // used to delete a topic from a list
        ERROR
    }


}
