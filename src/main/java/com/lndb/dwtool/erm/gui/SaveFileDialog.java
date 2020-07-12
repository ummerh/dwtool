package com.lndb.dwtool.erm.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import com.lndb.dwtool.erm.util.FileUtil;

public class SaveFileDialog {
    private class SaveAction implements ActionListener {
	private JFileChooser jFileChooser;
	private InputStream is;

	public SaveAction(JFileChooser jFileChooser, InputStream is) {
	    this.jFileChooser = jFileChooser;
	    this.is = is;
	}

	public void actionPerformed(ActionEvent event) {
	    if ("ApproveSelection".equals(event.getActionCommand())) {
		try {
		    FileUtil.writeOut(is, jFileChooser.getSelectedFile());
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
	    }
	}
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = -5677270531321583126L;

    private InputStream is;

    private String fileName;

    public SaveFileDialog(InputStream is, String fileName) {
	this.is = is;
	this.fileName = fileName;
    }

    public void displayAndSave() {
	try {
	    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    JFileChooser jFileChooser = new JFileChooser(new File("."));
	    jFileChooser.setSelectedFile(new File(this.fileName));
	    jFileChooser.addActionListener(new SaveAction(jFileChooser, this.is));
	    jFileChooser.showSaveDialog(null);
	} catch (Exception e) {
	    throw new RuntimeException("Error! Failed displaying dialog", e);
	}
    }
}
