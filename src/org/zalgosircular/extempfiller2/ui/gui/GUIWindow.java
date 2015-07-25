package org.zalgosircular.extempfiller2.ui.gui;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.ui.gui.TopicManagerPanel.TopicState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Queue;

/**
 * Created by Walt on 7/15/2015.
 */
public class GUIWindow extends JFrame {

    private DebugWindow debugWindow;
    private Queue<InMessage> inQueue;
    private TopicManagerPanel managerPanel;

    private JMenuBar menuBar;

    public GUIWindow(Queue<InMessage> inQueue) {
        this.inQueue = inQueue;
        this.debugWindow = new DebugWindow();
    }

    public void init() {
        this.setTitle("ExtempFiller2");
        this.setPreferredSize(new Dimension(640, 400));
        this.pack();
        // add a handler
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // set up the menu
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(createMenuItem("Close", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        }));

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(createMenuItem("Show Debug Window", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                debugWindow.setVisible(true);
            }
        }));

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        this.setJMenuBar(menuBar);

        managerPanel = new TopicManagerPanel(inQueue);
        this.add(managerPanel);

    }

    // basic running methods
    private void close() {
        inQueue.add(new InMessage(InMessage.Type.CLOSE, null));
        JOptionPane.showMessageDialog(this, "Closing ExtempFiller2...");
    }

    public void start() {
        this.setVisible(true);
        inQueue.add(new InMessage(InMessage.Type.OPEN, null));
        inQueue.add(new InMessage(InMessage.Type.LOAD, null));
    }

    public void setEnabled(boolean value) {
        managerPanel.setEnabled(value);
    }
    public void addDebugMessage(String msg) {
        System.out.println(msg);
        debugWindow.addDebugMessage(msg);
    }

    public void showError(String msg) {
        addDebugMessage(msg);
        JOptionPane.showMessageDialog(this, msg);
    }

    public void setTopicState(Topic topic, TopicState state) {
        managerPanel.setTopicState(topic, state);
    }

    public void setTopics(java.util.List<Topic> topics) {
        managerPanel.setResearchedTopics(topics);
    }

    // helper factory methods
    private JMenuItem createMenuItem(String name, ActionListener listener) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(listener);
        return item;
    }
}
