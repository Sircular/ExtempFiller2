package org.zalgosircular.extempfiller2.ui.gui.panels;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan Lembke on 7/28/2015.
 */
public class TopicListPanel extends JPanel {
    private final BlockingQueue<InMessage> inQueue;
    private final ArrayList<TopicListItem> topicItems;

    public TopicListPanel(BlockingQueue<InMessage> inQueue) {
        this.inQueue = inQueue;
        this.topicItems = new ArrayList<TopicListItem>();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        DeleteKeyBind deleteKeyBind = new DeleteKeyBind();
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put
                (KeyStroke.getKeyStroke(deleteKeyBind.getKey(), 0), deleteKeyBind.getName());
        getActionMap().put(deleteKeyBind.getName(), deleteKeyBind);
    }

    public void addTopic(Topic t) {
        addTopic(t, TopicState.QUEUED_RESEARCH);
    }

    public void addTopic(Topic t, TopicState state) {
        boolean found = false;
        for (int i = 0; i < topicItems.size() && !found; i++) {
            TopicListItem tli = topicItems.get(i);
            if (tli.getTopic().equals(t))
                found = true;
        }

        if (!found) {
            final TopicListItem panel = new TopicListItem(t, state);
            topicItems.add(panel);
            add(panel);
            revalidate();
        }
    }

    public void setTopicState(Topic t, TopicState state) {
        TopicListItem panel = null;
        for (int i = 0; i < topicItems.size() && panel == null; i++) {
            TopicListItem tli = topicItems.get(i);
            if (tli.getTopic().equals(t))
                panel = tli;
        }

        if (panel != null) {
            panel.setTopicState(state);
        }
    }

    public void deleteTopic(Topic t) throws InterruptedException {
        TopicListItem panel = null;
        for (int i = 0; i < topicItems.size() && panel == null; i++) {
            TopicListItem tli = topicItems.get(i);
            if (tli.getTopic().equals(t))
                panel = tli;
        }

        deleteTopic(panel);
    }

    private void deleteTopic(TopicListItem tli) throws InterruptedException {
        switch (tli.getTopicState()) {
            case RESEARCHED:
                tli.setTopicState(TopicState.QUEUED_DELETION);
                inQueue.put(new InMessage(InMessage.Type.DELETE, tli.getTopic().getTopic()));
                break;
            case QUEUED_RESEARCH:
                removeTopic(tli);
                inQueue.put(new InMessage(InMessage.Type.DELETE, tli.getTopic().getTopic()));
                break;
        }
    }

    public boolean removeTopic(Topic t) {
        TopicListItem panel = null;
        for (int i = 0; i < topicItems.size() && panel == null; i++) {
            TopicListItem tli = topicItems.get(i);
            if (tli.getTopic().equals(t))
                panel = tli;
        }

        return removeTopic(panel);
    }

    private boolean removeTopic(TopicListItem panel) {
        if (panel != null && topicItems.contains(panel)) {
            topicItems.remove(panel);
            remove(panel);
            revalidate();
            repaint();
            return true;
        }
        return false;
    }

    public void setResearchedTopics(List<Topic> topics) {
        for (Topic t : topics) {
            addTopic(t, TopicState.RESEARCHED);
        }
    }

    public List<Topic> getTopics() {
        final List<Topic> topicList = new ArrayList<Topic>(topicItems.size());
        for (TopicListItem tli : topicItems) {
            final Topic t = tli.getTopic();
            topicList.add(t);
        }
        return topicList;
    }

    public void deleteSelected() throws InterruptedException {
        List<TopicListItem> topics = getSelected();
        for (TopicListItem tli : topics) {
            deleteTopic(tli);
        }
    }

    public void cancelQueuedTopics() throws InterruptedException {
        List<TopicListItem> toDelete = new LinkedList<TopicListItem>();
        for (TopicListItem item : topicItems) {
            if (item.getTopicState() == TopicState.QUEUED_RESEARCH)
                toDelete.add(item);
        }
        for (TopicListItem item : toDelete) {
            deleteTopic(item);
        }
    }

    private LinkedList<TopicListItem> getSelected() {
        LinkedList<TopicListItem> selected = new LinkedList<TopicListItem>();
        for (TopicListItem panel : topicItems) {
            if (panel.isSelected()) {
                selected.add(panel);
            }
        }
        return selected;
    }

    private void clearSelection() {
        for (TopicListItem tli : topicItems) {
            tli.setSelected(false);
        }
    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        for (TopicListItem item : topicItems) {
            item.setEnabled(value);
        }
    }

    private class DeleteKeyBind extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                deleteSelected();
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }
        }

        public int getKey() {
            return KeyEvent.VK_DELETE;
        }

        public String getName() {
            return "DELETE";
        }
    }

    private class TopicListItem extends JPanel {
        private static final int MIN_WIDTH = 300;
        private static final int HEIGHT = 30;
        private final Topic topic;
        private final JLabel label;
        private final JButton delete;
        private boolean selected;
        private TopicState topicState;
        private MouseListener clickListener = new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
                    int selectionEnd = -1;
                    int thisIndex = -1;
                    for (int i = 0; i < topicItems.size(); i++) {
                        TopicListItem tli = topicItems.get(i);
                        if (tli.equals(e.getSource())) {
                            thisIndex = i;
                        }
                        if (tli.isSelected()) {
                            selectionEnd = i;
                        }
                    }
                    for (int i = Math.min(selectionEnd, thisIndex); i <= Math.max(selectionEnd, thisIndex); i++) {
                        topicItems.get(i).setSelected(true);
                    }
                } else if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
                    setSelected(true);
                } else {
                    clearSelection();
                    setSelected(true);
                }
                ((Container) e.getSource()).requestFocus();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };

        private TopicListItem(Topic topic, TopicState topicState) {
            this.topic = topic;
            setSelected(false);
            label = new JLabel();
            delete = createDeleteButton();
            setLayout(new BorderLayout());
            add(label, BorderLayout.WEST);
            add(delete, BorderLayout.EAST);
            addMouseListener(clickListener);
            setMinimumSize(new Dimension(MIN_WIDTH, HEIGHT));

            setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));

            this.setTopicState(topicState);
        }

        public boolean isSelected() {
            return selected;
        }

        private void setSelected(boolean selected) {
            if (isEnabled()) { // no sense in allowing selections
                this.selected = selected;
                if (selected) {
                    setBackground(UIManager.getColor("List.selectionBackground"));
                    setForeground(UIManager.getColor("List.selectionForeground"));
                } else {
                    setBackground(UIManager.getColor("List.background"));
                    setForeground(UIManager.getColor("List.foreground"));
                }
            }
        }

        public Topic getTopic() {
            return topic;
        }

        public TopicState getTopicState() {
            return topicState;
        }

        public void setTopicState(TopicState topicState) {
            this.topicState = topicState;
            updateLabel();
            updateButton();
            revalidate();
        }

        private void updateLabel() {
            String stateStr;
            switch (topicState) {
                case QUEUED_RESEARCH:
                    stateStr = "[Queued for Research]";
                    break;
                case RESEARCHING:
                    stateStr = "[Researching]";
                    break;
                case RESEARCHED:
                    stateStr = "[Researched][" + topic.getArticleCount() + "]";
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
            label.setText(String.format("%s %s", stateStr, topic.getTopic()));
        }

        private void updateButton() {
            switch (topicState) {
                case RESEARCHED:
                    delete.setText("Delete");
                    delete.setVisible(true);
                    delete.setEnabled(true);
                    break;
                case QUEUED_RESEARCH:
                    delete.setText("Cancel");
                    delete.setVisible(true);
                    delete.setEnabled(true);
                    break;
                default:
                    delete.setVisible(false);
                    delete.setEnabled(false);
                    break;
            }
        }

        private JButton createDeleteButton() {
            final JButton button = new JButton("Delete");
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        deleteTopic(((TopicListItem) ((JButton) e.getSource()).getParent()));
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            button.setVisible(false); // we'll make it show up when it's useful
            return button;
        }

        @Override
        public void setEnabled(boolean value) {
            delete.setEnabled(value);
            label.setEnabled(value);
        }
    }

}
