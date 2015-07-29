package org.zalgosircular.extempfiller2.ui.gui.panels;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/15/2015.
 */
public class TopicManagerPanel extends JPanel {
    private final BlockingQueue<InMessage> inQueue;
    private final JList<TopicListItem> list;
    private final AddTopicPanel addTopicPanel;

    public TopicManagerPanel(BlockingQueue<InMessage> inQueue) {
        this.inQueue = inQueue;

        // initialize the actual GUI
        list = new JList<TopicListItem>(new DefaultListModel<TopicListItem>());

        // when you press delete, all selected topics
        // are queued for deletion
        list.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                    try {
                        deleteSelectedTopics();
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        // we need to encase it in a scrollpane
        final JScrollPane listPane = new JScrollPane(list);
        listPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.add(listPane, BorderLayout.CENTER);

        addTopicPanel = new AddTopicPanel(this, inQueue);
        addTopicPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        this.add(addTopicPanel, BorderLayout.SOUTH);
    }

    // methods for working with the list
    // split up into functions so that we aren't
    // writing tons of code when registering handlers
    private void deleteSelectedTopics() throws InterruptedException {
        final int[] selected = list.getSelectedIndices();
        final DefaultListModel<TopicListItem> model = (DefaultListModel<TopicListItem>) list.getModel();
        final java.util.List<Topic> toRemove = new ArrayList<Topic>();
        boolean invalid = false;

        for (int i : selected) {
            final TopicListItem item = model.get(i);
            switch (item.getState()) {
                case RESEARCHED:
                    setTopicState(item.getTopic(), TopicState.DELETING);
                    inQueue.put(new InMessage(InMessage.Type.DELETE, item.getTopic().getTopic()));
                    break;
                case QUEUED_RESEARCH:
                    toRemove.add(item.getTopic());
                    break;
                default:
                    invalid = true;
                    break;
            }
        }
        for (Topic t : toRemove) {
            removeTopic(t);
            // to negate the previous message sent
            inQueue.put(new InMessage(InMessage.Type.DELETE, t.getTopic()));
        }
        // todo: make the GUIWindow show this instead
        if (invalid) {
            JOptionPane.showMessageDialog(this, "Please wait until the current operation is finished.",
                    "ExtempFiller2", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addTopic(Topic topic) {
        DefaultListModel<TopicListItem> model = (DefaultListModel<TopicListItem>) list.getModel();
        // check to see if it's already in the list
        // might as well not add something that's already researched.
        boolean found = false;
        for (int i = 0; i < model.size() && !found; i++) {
            if (model.get(i).getTopic().equals(topic)) {
                found = true;
            }
        }
        if (!found) {
            model.addElement(new TopicListItem(topic, TopicState.QUEUED_RESEARCH));
        }
    }

    public boolean removeTopic(Topic topic) {
        DefaultListModel<TopicListItem> model = (DefaultListModel<TopicListItem>) list.getModel();
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i).getTopic().equals(topic)) {
                model.remove(i);
                return true;
            }
        }
        return false;
    }

    public void setTopicState(Topic topic, TopicState state) {
        final DefaultListModel<TopicListItem> model = (DefaultListModel<TopicListItem>) list.getModel();
        boolean found = false;
        for (int i = 0; i < model.size() && !found; i++) {
            final TopicListItem item = model.get(i);
            if (item.getTopic().equals(topic)) {
                setTopicState(i, state);
                found = true;
            }
        }
    }

    public void setTopicState(int topic, TopicState state) {
        final DefaultListModel<TopicListItem> model = (DefaultListModel<TopicListItem>) list.getModel();
        if (topic < model.size()) {
            model.get(topic).setState(state);
            list.repaint();
        }
    }

    public void setResearchedTopics(java.util.List<Topic> topics) {
        final DefaultListModel<TopicListItem> model =
                (DefaultListModel<TopicListItem>)list.getModel();
        model.clear();
        for (Topic topic : topics) {
            TopicListItem item = new TopicListItem(topic, TopicState.RESEARCHED);
            model.addElement(item);
        }
        list.revalidate();
    }

    // used to check for duplicates
    public java.util.List<String> getTopics() {
        final java.util.List<String> topics = new LinkedList<String>();
        final DefaultListModel<TopicListItem> model =
                (DefaultListModel<TopicListItem>)list.getModel();
        for (int i = 0; i < model.size(); i++) {
            topics.add(model.get(i).getTopic().getTopic());
        }
        return topics;
    }

    public void setEnabled(boolean value) {
        list.setEnabled(value);
        addTopicPanel.setEnabled(value);
    }

    private class TopicListItem {
        private final Topic topic;
        private TopicState state;

        public TopicListItem(Topic topic, TopicState state) {
            this.topic = topic;
            this.state = state;
        }

        @Override
        public String toString() {
            String stateStr;
            switch (getState()) {
                case QUEUED_RESEARCH:
                    stateStr = "[Queued for Research]";
                    break;
                case RESEARCHING:
                    stateStr = "[Researching]";
                    break;
                case RESEARCHED:
                    stateStr = "[Researched]["+getTopic().getArticleCount()+"]";
                    break;
                case QUEUED_DELETION:
                    stateStr = "[Queued for Deletion]";
                    break;
                case DELETING:
                    stateStr = "[Deleting]";
                    break;
                case ERROR:
                    stateStr = "[Research Error]";
                    break;
                default:
                    stateStr = "[?]";
                    break;
            }
            return String.format("%s %s", stateStr, topic.getTopic());
        }

        public Topic getTopic() {
            return topic;
        }

        public TopicState getState() {
            return state;
        }

        public void setState(TopicState state) {
            this.state = state;
        }
    }
}
