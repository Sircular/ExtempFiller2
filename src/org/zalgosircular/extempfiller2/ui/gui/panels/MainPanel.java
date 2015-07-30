package org.zalgosircular.extempfiller2.ui.gui.panels;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan Lembke on 7/29/2015.
 */
public class MainPanel extends JPanel {
    private final BlockingQueue<InMessage> inQueue;
    private final TopicListPanel topicPanel;
    private final AddTopicPanel addTopicPanel;

    public MainPanel(BlockingQueue<InMessage> inQueue) {
        this.inQueue = inQueue;

        // initialize the actual GUI
        topicPanel = new TopicListPanel(inQueue);

        // we need to encase it in a scrollpane
        final JScrollPane listPane = new JScrollPane(topicPanel);
        listPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.add(listPane, BorderLayout.CENTER);

        addTopicPanel = new AddTopicPanel(this, inQueue);
        addTopicPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        this.add(addTopicPanel, BorderLayout.SOUTH);
    }

    public void addTopic(Topic topic) {
        topicPanel.addTopic(topic);
    }

    public boolean removeTopic(Topic topic) {
        return topicPanel.removeTopic(topic);
    }

    public void setTopicState(Topic topic, TopicState state) {
        topicPanel.setTopicState(topic, state);
    }

    public void setResearchedTopics(List<Topic> topics) {
        topicPanel.setResearchedTopics(topics);
    }

    public void cancelQueuedTopics() throws InterruptedException { topicPanel.cancelQueuedTopics(); }

    public List<String> getTopics() {
        final List<Topic> topics = topicPanel.getTopics();
        final List<String> strings = new ArrayList<String>(topics.size());
        for (Topic t : topics) {
            strings.add(t.getTopic());
        }
        return strings;
    }

    public void setEnabled(boolean value) {
        topicPanel.setEnabled(value);
        addTopicPanel.setEnabled(value);
    }
}
