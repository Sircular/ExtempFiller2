package org.zalgosircular.extempfiller2.ui.gui.subwindows;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by Walt on 7/15/2015.
 */
public class DebugWindow extends JFrame {
    private final String ENDL = System.getProperty("line.separator");
    private final JTextArea area;
    private final JMenuBar menu;

    private String outputPath = "";

    public DebugWindow() {
        this.setTitle("ExtempFiller2 Debug");

        menu = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        final JMenuItem closeItem = new JMenuItem("Close");
        final JMenuItem saveItem = new JMenuItem("Save");
        final JMenuItem saveAsItem = new JMenuItem("Save As...");

        // add listeners
        closeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideWindow();
            }
        });
        saveAsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputPath = selectFileOutputPath();
                if (!outputPath.equals(""))
                    saveLog(outputPath);
            }
        });
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (outputPath.equals(""))
                    outputPath = selectFileOutputPath();
                if (!outputPath.equals(""))
                    saveLog(outputPath);
            }
        });

        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(closeItem);

        menu.add(fileMenu);

        area = new JTextArea();
        area.setEditable(false);
        area.setAutoscrolls(true);

        // create a scroll pane
        final JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(640, 480));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        this.add(scrollPane);
        this.setJMenuBar(menu);
        this.pack();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void addDebugMessage(String msg) {
        area.append(msg+ENDL);
        this.validate();
    }

    private void saveLog(String outputPath) {
        System.out.println(outputPath);
        byte[] data = area.getText().getBytes();
        try {
            Files.write(Paths.get(outputPath), data, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
            addDebugMessage("Saved debug log.");
        } catch (IOException e) {
            addDebugMessage("Error: Could not write debug log.");
        }
    }

    private String selectFileOutputPath() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                return f.getName().toLowerCase().endsWith(".txt");
            }

            @Override
            public String getDescription() {
                return "Text Files";
            }
        });
        int choice = chooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION)
            return chooser.getSelectedFile().getPath();
        return "";
    }

    private void hideWindow() {
        this.setVisible(false);
    }
}
