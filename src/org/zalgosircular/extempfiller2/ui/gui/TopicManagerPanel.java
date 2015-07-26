package org.zalgosircular.extempfiller2.ui.gui;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Queue;

/**
 * Created by Walt on 7/15/2015.
 */
public class TopicManagerPanel extends JPanel {
    private final Queue<InMessage> inQueue;
    private final JList<TopicListItem> list;
    private final AddTopicPanel addTopicPanel;

    public TopicManagerPanel(Queue<InMessage> inQueue) {
        this.inQueue = inQueue;

        DefaultListModel<TopicListItem> model = new DefaultListModel<TopicListItem>();

        // initialize the actual GUI
        list = new JList<TopicListItem>(model);
        // when you press delete, all selected topics
        // are queued for deletion
        list.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE)
                    deleteSelectedTopics();
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        // we need to encase it in a scrollpane
        JScrollPane listPane = new JScrollPane(list);
        listPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.add(listPane, BorderLayout.CENTER);

        addTopicPanel = new AddTopicPanel();
        addTopicPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        this.add(addTopicPanel, BorderLayout.SOUTH);
    }

    // methods for working with the list
    // split up into functions so that we aren't
    // writing tons of code when registering handlers
    private void deleteSelectedTopics() {
        final int[] selected = list.getSelectedIndices();
        DefaultListModel<TopicListItem> model = (DefaultListModel<TopicListItem>)list.getModel();
        for (int i : selected) {
            setTopicState(i, TopicState.QUEUED_DELETION);
            inQueue.add(new InMessage(InMessage.Type.DELETE, model.get(i).getTopic()));
        }
    }

    public void addTopic(Topic topic) {
        DefaultListModel<TopicListItem> model = (DefaultListModel<TopicListItem>)list.getModel();
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
        DefaultListModel<TopicListItem> model = (DefaultListModel<TopicListItem>)list.getModel();
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i).getTopic().equals(topic)) {
                model.remove(i);
                return true;
            }
        }
        return false;
    }

    public void setTopicState(Topic topic, TopicState state) {
        DefaultListModel<TopicListItem> model = (DefaultListModel<TopicListItem>)list.getModel();
        boolean found = false;
        for (int i = 0; i < model.size() && !found; i++) {
            TopicListItem item = model.get(i);
            if (item.getTopic().equals(topic)) {
                setTopicState(i, state);
                found = true;
            }
        }
    }

    public void setTopicState(int topic, TopicState state) {
        DefaultListModel<TopicListItem> model = (DefaultListModel<TopicListItem>)list.getModel();
        if (topic < model.size()) {
            model.get(topic).setState(state);
            list.repaint();
        }
    }

    public void setResearchedTopics(java.util.List<Topic> topics) {
        DefaultListModel<TopicListItem> model =
                (DefaultListModel<TopicListItem>)list.getModel();
        for (Topic topic : topics) {
            TopicListItem item = new TopicListItem(topic, TopicState.RESEARCHED);
            model.addElement(item);
        }
        list.revalidate();
    }

    public void setEnabled(boolean value) {
        list.setEnabled(value);
        addTopicPanel.setEnabled(value);
    }

    private class AddTopicPanel extends JPanel {
        private JButton addButton;
        private JTextField addField;
        public AddTopicPanel() {
            addField = new JTextField();
            addField.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    addButton.setEnabled(addField.getText().length() > 0);
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER)
                        enqueueTypedTopic();
                }

                @Override
                public void keyReleased(KeyEvent e) {

                }
            });

            addButton = new JButton("Add Topic");
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    enqueueTypedTopic();
                }
            });
            addButton.setMinimumSize(addButton.getPreferredSize());
            addButton.setEnabled(false);
            // TODO: fix the extra gradient fill created by
            // adding a border
            addButton.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 10, 0, 0),
                    addButton.getBorder()));

            addField.setSize(100, addButton.getHeight());

            this.setLayout(new BorderLayout());
            this.add(addButton, BorderLayout.EAST);
            this.add(addField, BorderLayout.CENTER);
        }

        public void setEnabled(boolean value) {
            addField.setEnabled(value);
            addButton.setEnabled(value && addField.getText().length() > 0);
        }

        private void enqueueTypedTopic() {
            if (addField.getText().length() > 0) {
                final String topic = addField.getText();
                addField.setText("");
                addButton.setEnabled(false);
                enqueueTopic(topic);
            }
        }
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
            String stateStr = "";
            switch (state) {
                case QUEUED_RESEARCH:
                    stateStr = "Queued for Research";
                    break;
                case RESEARCHING:
                    stateStr = "Researching";
                    break;
                case RESEARCHED:
                    stateStr = "Researched";
                    break;
                case QUEUED_DELETION:
                    stateStr = "Queued for Deletion";
                    break;
                case DELETING:
                    stateStr = "Deleting";
                    break;
                case ERROR:
                    stateStr = "Research Error";
                    break;
                default:
                    stateStr = "?";
                    break;
            }
            return String.format("[%s] %s", stateStr, topic.getTopic());
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

    public enum TopicState {
        QUEUED_RESEARCH,
        RESEARCHING,
        RESEARCHED,
        QUEUED_DELETION,
        DELETING,
        ERROR
    }

    // internal methods used to work with queues and such
    private void enqueueTopic(String topic) {
        addTopic(new Topic(topic));
        inQueue.add(new InMessage(InMessage.Type.RESEARCH, topic));
    }
}
