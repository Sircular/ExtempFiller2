package org.zalgosircular.extempfiller2.ui.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Walt on 7/15/2015.
 */
public class DebugWindow extends JFrame {
    private final JTextArea area;

    public DebugWindow() {
        this.setTitle("ExtempFiller2 Debug");
        area = new JTextArea();
        area.setPreferredSize(new Dimension(640, 480));
        area.setEditable(false);
        this.add(area);
        this.pack();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void addDebugMessage(String msg) {
        area.append(msg+"\n");
        this.validate();
    }
}
