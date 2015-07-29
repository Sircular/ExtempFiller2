package org.zalgosircular.extempfiller2.ui.gui;

import org.zalgosircular.extempfiller2.authentication.AuthRequest;
import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.ui.gui.panels.TopicManagerPanel;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan Lembke on 7/25/2015.
 */
class OutputRunnable implements Runnable {
    private final GUIWindow window;
    private final BlockingQueue<OutMessage> outQueue;

    OutputRunnable(GUIWindow window, BlockingQueue<OutMessage> outQueue) {
        this.window = window;
        this.outQueue = outQueue;
    }

    @Override
    public void run() {
        addDebugMessage("Initializing ExtempFiller2...");
        setWindowEnabled(false); // wait until topics are loaded
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final OutMessage msg = outQueue.take();
                Topic topic;
                switch (msg.getMessageType()) {
                    case AUTH_REQUEST:
                        addDebugMessage("Requesting auth tokens.");
                        requestAuth((AuthRequest) msg.getData());
                        break;
                    case LOADING:
                        addDebugMessage("Loading topics");
                        setWindowEnabled(false);
                        break;
                    case LOADED:
                        addDebugMessage("Loaded topics");
                        setWindowEnabled(true);
                        window.setTopics((List<Topic>) msg.getData());
                        break;
                    case RETRIEVED:
                        addDebugMessage("Loaded topics from cache.");
                        break;
                    case DEBUG:
                        addDebugMessage((String) msg.getData());
                        break;
                    case SEARCHING:
                        topic = (Topic) msg.getData();
                        addDebugMessage("Now researching topic: " + topic.getTopic());
                        setTopicState(topic, TopicManagerPanel.TopicState.RESEARCHING);
                        break;
                    case DONE:
                        topic = (Topic) msg.getData();
                        addDebugMessage("Finished researching message: " + topic.getTopic());
                        setTopicState(topic, TopicManagerPanel.TopicState.RESEARCHED);
                        break;
                    case DELETING:
                        topic = (Topic) msg.getData();
                        addDebugMessage("Deleting message: " + topic.getTopic());
                        setTopicState(topic, TopicManagerPanel.TopicState.DELETING);
                        break;
                    case DELETED:
                        topic = (Topic) msg.getData();
                        addDebugMessage("Deleted message: " + topic.getTopic());
                        removeTopic(topic);
                        break;
                    case ERROR:
                        final ErrorMessage error = (ErrorMessage) msg.getData();
                        if (error.getTopic() != null) {
                            addDebugMessage("Error researching message: " + error.getTopic().getTopic());
                            setTopicState(error.getTopic(), TopicManagerPanel.TopicState.ERROR);
                        }
                        showError(error.getException());
                        break;
                    case CLOSED:
                        closeWindow();
                        Thread.currentThread().interrupt();
                        break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void addDebugMessage(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.addDebugMessage(msg);
            }
        });
    }

    private void showError(final Throwable exception) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.showError(exception);
            }
        });
    }

    private void removeTopic(final Topic topic) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.removeTopic(topic);
            }
        });
    }

    private void setTopicState(final Topic topic, final TopicManagerPanel.TopicState state) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.setTopicState(topic, state);
            }
        });
    }

    private void setWindowEnabled(final boolean value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.setEnabled(value);
            }
        });
    }

    private void closeWindow() {
        window.dispose();
    }

    private void requestAuth(final AuthRequest request) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.requestAuth(request);
            }
        });
    }
}
