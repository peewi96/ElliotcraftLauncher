/*
 * This file is part of Technic Launcher.
 *
 * Copyright (c) 2013-2013, Technic <http://www.technicpack.net/>
 * Technic Launcher is licensed under the Spout License Version 1.
 *
 * Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Technic Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spoutcraft.launcher.technic.skin;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;
import org.spoutcraft.launcher.skin.MetroLoginFrame;
import org.spoutcraft.launcher.skin.components.LiteButton;
import org.spoutcraft.launcher.skin.components.LiteTextBox;
import org.spoutcraft.launcher.technic.CustomInfo;
import org.spoutcraft.launcher.technic.rest.RestAPI;
import org.spoutcraft.launcher.util.Utils;

import static org.spoutcraft.launcher.util.TextSource.lang;

public class ImportOptions extends JDialog implements ActionListener, MouseListener, MouseMotionListener, DocumentListener {
	private static final long serialVersionUID = 1L;
	private static final String QUIT_ACTION = "quit";
	private static final String IMPORT_ACTION = "import";
	private static final String CHANGE_FOLDER = "folder";
	private static final String PASTE_URL = "paste";
	private static final int FRAME_WIDTH = 520;
	private static final int FRAME_HEIGHT = 222;
	private JLabel msgLabel;
	private JLabel background;
	private LiteButton save;
	private LiteButton folder;
	private LiteButton paste;
	private LiteTextBox install;
	private JFileChooser fileChooser;
	private int mouseX = 0, mouseY = 0;
	private CustomInfo info = null;
	private String url = "";
	private Document urlDoc;
	private File installDir;
	
	public ImportOptions() {
		setTitle(lang("platform.addpack.title"));
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		addMouseListener(this);
		addMouseMotionListener(this);
		setResizable(false);
		setUndecorated(true);
		initComponents();
	}
	
	public void initComponents() {
		Font fontregular = MetroLoginFrame.getClassicFont(13);
		Font fontbold = MetroLoginFrame.getClassicBoldFont(13);
		
		//  Handle escape key to close the dialog
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction()
		{ public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);

		background = new JLabel();
		background.setBounds(0,0, FRAME_WIDTH, FRAME_HEIGHT);
		MetroLoginFrame.setIcon(background, "platformBackground.png", background.getWidth(), background.getHeight());
		
		Container contentPane = getContentPane();
		contentPane.setLayout(null);
		
		ImageButton optionsQuit = new ImageButton(MetroLoginFrame.getIcon("exit.png", 16, 16), MetroLoginFrame.getIcon("exit.png", 16, 16));
		optionsQuit.setRolloverIcon(MetroLoginFrame.getIcon("exit_hover.png", 16, 16));
		optionsQuit.setBounds(FRAME_WIDTH - 10 - 16, 10, 16, 16);
		optionsQuit.setActionCommand(QUIT_ACTION);
		optionsQuit.addActionListener(this);
		
		msgLabel = new JLabel();
		msgLabel.setBounds(10, 75, FRAME_WIDTH - 20, 25);
		msgLabel.setText(lang("platform.addpack"));
		msgLabel.setForeground(Color.white);
		msgLabel.setFont(fontregular.deriveFont(14F));
		
		LiteTextBox url = new LiteTextBox(this, lang("platform.pasteurl"));
		url.setBounds(10, msgLabel.getY() + msgLabel.getHeight() + 5, FRAME_WIDTH - 115, 30);
		url.setFont(fontregular);
		url.getDocument().addDocumentListener(this);
		urlDoc = url.getDocument();
		
		save = new LiteButton(lang("platform.add"), FRAME_WIDTH - 145, FRAME_HEIGHT - 40, 135, 30);
		save.setFont(fontbold.deriveFont(14F));
		save.setForeground(Color.WHITE);
		save.setActionCommand(IMPORT_ACTION);
		save.addActionListener(this);
		
		fileChooser = new JFileChooser(Utils.getLauncherDirectory());
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		folder = new LiteButton(lang("options.changefolder"), FRAME_WIDTH - 290, FRAME_HEIGHT - 40, 135, 30);
		folder.setFont(fontbold.deriveFont(14F));
		folder.setForeground(Color.WHITE);
		folder.setActionCommand(CHANGE_FOLDER);
		folder.addActionListener(this);
		
		paste = new LiteButton(lang("platform.paste"), FRAME_WIDTH - 95, msgLabel.getY() + msgLabel.getHeight() + 5, 85, 30);
		paste.setFont(fontbold.deriveFont(16F));
		paste.setForeground(Color.WHITE);
		paste.setActionCommand(PASTE_URL);
		paste.addActionListener(this);
		paste.setVisible(true);

		install = new LiteTextBox(this, "");
		install.setBounds(10, FRAME_HEIGHT - 75, FRAME_WIDTH - 20, 25);
		install.setFont(fontbold.deriveFont(10F));
		install.setEnabled(false);
		install.setVisible(false);
		
		enableComponent(save, false);
		enableComponent(folder, false);
		enableComponent(paste, true);

		contentPane.add(install);
		contentPane.add(optionsQuit);
		contentPane.add(msgLabel);
		contentPane.add(folder);
		contentPane.add(paste);
		contentPane.add(url);
		contentPane.add(save);
		contentPane.add(background);
		
		setLocationRelativeTo(this.getOwner());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComponent) {
			action(e.getActionCommand(), (JComponent)e.getSource());
		}
	}
	
	private void action(String action, JComponent c) {
		if (action.equals(QUIT_ACTION)) {
			dispose();
		} else if (action.equals(CHANGE_FOLDER)) {
			int result = fileChooser.showOpenDialog(this);
			
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				file.exists();
				installDir = file;
				if (info.isForceDir() && installDir.getAbsolutePath().startsWith(Utils.getSettingsDirectory().getAbsolutePath())) {
					install.setText(lang("platform.selectdir")+" " + Utils.getSettingsDirectory().getAbsolutePath());
				} else {
					install.setText(lang("platform.location")+" " + installDir.getPath());
					folder.setText(lang("options.changefolder"));
					folder.setLocation(FRAME_WIDTH - 290, FRAME_HEIGHT - 40);
					enableComponent(save, true);
				}
			}
		} else if (action.equals(IMPORT_ACTION)) {
			if (info != null || url.isEmpty()) {
				Settings.setPackCustom(info.getName(), true);
				Settings.setPackDirectory(info.getName(), installDir);
				Settings.getYAML().save();
				info.init();
				Launcher.getFrame().getSelector().addPack(info.getPack());
				dispose();
			}
		} else if (action.equals(PASTE_URL)) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable clipData = clipboard.getContents(clipboard);
			if(clipData != null) {
				try {
					if(clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
						String s = (String)(clipData.getTransferData(DataFlavor.stringFlavor));
						urlDoc.remove(0, urlDoc.getLength());
						urlDoc.insertString(0, s, new SimpleAttributeSet());
					}
				} catch(UnsupportedFlavorException e) {
					e.printStackTrace();
				} catch(IOException e) {
					e.printStackTrace();
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void urlUpdated(Document doc) {
		try {
			String url = doc.getText(0, doc.getLength());
			if (url.isEmpty()) {
				msgLabel.setText(lang("platform.addpack"));
				enableComponent(save, false);
				enableComponent(folder, false);
				info = null;
				this.url = "";
				return;
			} else if (url.matches("http://beta.technicpack.net/api/modpack/([a-zA-Z0-9-]+)")) {
				try {
					info = RestAPI.getCustomModpack(url);
					msgLabel.setText(lang("platform.modpack")+" " + info.getDisplayName());
					this.url = url;
					enableComponent(folder, true);
					enableComponent(install, true);
					enableComponent(paste, true);
					if (info.isForceDir()) {
						install.setText(lang("platform.selectinstalldir"));
						folder.setText(lang("platform.select"));
						folder.setLocation(FRAME_WIDTH - 145, FRAME_HEIGHT - 40);
						enableComponent(save, false);
					} else {
						installDir = new File(Utils.getLauncherDirectory(), info.getName());
						install.setText(lang("platform.location")+" " + installDir.getPath());
						enableComponent(save, true);
					}
				} catch (RestfulAPIException e) {
					msgLabel.setText(lang("platform.errorparsing"));
					enableComponent(save, false);
					enableComponent(folder, false);
					enableComponent(install, false);
					info = null;
					this.url = "";
					e.printStackTrace();
				}
			} else {
				msgLabel.setText(lang("platform.invalidurl"));
				enableComponent(save, false);
				enableComponent(folder, false);
				enableComponent(install, false);
				info = null;
				this.url = "";
			}
			
		} catch (BadLocationException e) {
			//This should never ever happen.
			//Java is stupid for not having a getAllText of some kind on the Document class
			e.printStackTrace();
		}
	}

	public void enableComponent(JComponent component, boolean enable) {
		component.setEnabled(enable);
		component.setVisible(enable);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		this.setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		urlUpdated(e.getDocument());
	}
	

	@Override
	public void insertUpdate(DocumentEvent e) {
		urlUpdated(e.getDocument());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		urlUpdated(e.getDocument());
	}
}
