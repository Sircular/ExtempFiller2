package org.zalgosircular.extempfiller2.ui.gui;

import org.zalgosircular.extempfiller2.authentication.AuthManager;
import org.zalgosircular.extempfiller2.authentication.AuthRequest;
import org.zalgosircular.extempfiller2.authentication.AuthResponse;
import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.ui.gui.panels.TopicManagerPanel;
import org.zalgosircular.extempfiller2.ui.gui.panels.TopicManagerPanel.TopicState;
import org.zalgosircular.extempfiller2.ui.gui.subwindows.DebugWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/15/2015.
 */
class GUIWindow extends JFrame {

    private DebugWindow debugWindow;
    private BlockingQueue<InMessage> inQueue;
    private TopicManagerPanel managerPanel;

    private JMenuBar menuBar;

    GUIWindow(BlockingQueue<InMessage> inQueue) {
        this.inQueue = inQueue;
        this.debugWindow = new DebugWindow();
    }

    public void init() {
        this.setTitle("ExtempFiller2");
        this.setPreferredSize(new Dimension(640, 400));

        // add a handler for closing
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    close();
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // set up the menu
        menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.add(createMenuItem("Close", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    close();
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }));

        final JMenu helpMenu = new JMenu("Help");
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

        this.pack();

    }

    @Override
    public void dispose() {
        debugWindow.dispose();
        super.dispose();
    }

    // basic running methods
    private void close() throws InterruptedException {
        inQueue.put(new InMessage(InMessage.Type.CLOSE, null));
        addDebugMessage("Closing ExtempFiller2...");
    }

    public void start() {
        addDebugMessage("Starting ExtempFiller2...");
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

    public void showError(Throwable exception) {
        addDebugMessage(exception.toString());

        //Copied from throwable.java
        StackTraceElement[] trace = exception.getStackTrace();
        for (StackTraceElement traceElement : trace)
            addDebugMessage("\tat " + traceElement);

        //Quite annoying
        //JOptionPane.showMessageDialog(this, exception.toString());
    }

    public void removeTopic(Topic topic) {
        managerPanel.removeTopic(topic);
    }

    public void setTopicState(Topic topic, TopicState state) {
        managerPanel.setTopicState(topic, state);
    }

    public void setTopics(java.util.List<Topic> topics) {
        managerPanel.setResearchedTopics(topics);
    }

    public void requestAuth(AuthRequest request){
        String[] names = request.getRequestFields();
        String[] keys = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            // prompt the user with a dialog
            String key = "";
            while (key.length() == 0) {
                key = (String)JOptionPane.showInputDialog(this, "Please enter key for: "+names[i],
                        "ExtempFiller2", JOptionPane.PLAIN_MESSAGE);
            }
            keys[i] = key;
        }
        AuthManager.respondAuth(new AuthResponse(names, keys));
    }

    // helper factory methods
    private JMenuItem createMenuItem(String name, ActionListener listener) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(listener);
        return item;
    }
}
