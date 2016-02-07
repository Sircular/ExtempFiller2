package org.zalgosircular.extempfiller2.ui.gui.panels;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan Lembke on 7/25/2015.
 */
public class AddTopicPanel extends JPanel {
    private final MainPanel manager;
    private final BlockingQueue<InMessage> inQueue;
    private final JButton addButton;
    private final JTextField addField;

    public AddTopicPanel(MainPanel manager, BlockingQueue<InMessage> inQueue) {
        this.manager = manager;
        this.inQueue = inQueue;

        addField = new JTextField();
        addField.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                addButton.setEnabled(addField.getText().length() > 0);
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        enqueueTypedTopic();
                    } catch (InterruptedException e1) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            public void keyReleased(KeyEvent e) {

            }
        });

        addButton = new JButton("Add Topic");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    enqueueTypedTopic();
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
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

    private void enqueueTypedTopic() throws InterruptedException {
        if (addField.getText().length() > 0) {
            final String topic = addField.getText();
            addField.setText("");
            addButton.setEnabled(false);
            enqueueTopic(topic);
        }
    }

    private void enqueueTopic(String topic) throws InterruptedException {
        final Topic t = new Topic(topic);
        manager.addTopic(t);
        inQueue.put(new InMessage(InMessage.Type.RESEARCH, t));
    }
}
