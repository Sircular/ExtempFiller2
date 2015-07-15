package org.zalgosircular.extempfiller2.ui.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Walt on 7/15/2015.
 */
public class DebugWindow extends JFrame {
    private final JList<String> output;
    private final DefaultListModel<String> model;

    public DebugWindow() {
        model = new DefaultListModel<String>();
        output = new JList<String>(model);
        output.setPreferredSize(new Dimension(640, 400));
        this.add(output);
        this.pack();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void addDebugMessage(String msg) {
        model.addElement(msg);
        output.updateUI();
    }
}
