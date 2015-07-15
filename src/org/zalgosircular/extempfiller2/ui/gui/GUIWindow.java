package org.zalgosircular.extempfiller2.ui.gui;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.research.Topic;

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

    private JMenuBar menuBar;

    public GUIWindow(Queue<InMessage> inQueue) {
        this.inQueue = inQueue;
        this.debugWindow = new DebugWindow();
    }

    public void init() {
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
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        fileMenu.add(closeItem);
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);

    }

    private void close() {
        inQueue.add(new InMessage(InMessage.Type.CLOSE, null));
        JOptionPane.showMessageDialog(null, "Closing ExtempFiller2...");
    }

    public void start() {
        this.setVisible(true);
        this.debugWindow.setVisible(true);
        inQueue.add(new InMessage(InMessage.Type.OPEN, null));
        inQueue.add(new InMessage(InMessage.Type.LOAD, null));
    }

    public void addDebugMessage(String msg) {
        System.out.println(msg);
        debugWindow.addDebugMessage(msg);
    }

    public void setTopicState(Topic topic, GUI.TopicState state) {
        // do nothing for now
    }
}
