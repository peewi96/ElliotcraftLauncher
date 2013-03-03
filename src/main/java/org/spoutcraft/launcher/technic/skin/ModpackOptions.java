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
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.UpdateThread;
import org.spoutcraft.launcher.skin.MetroLoginFrame;
import org.spoutcraft.launcher.skin.components.LiteButton;
import org.spoutcraft.launcher.skin.components.LiteTextBox;
import org.spoutcraft.launcher.technic.PackInfo;
import org.spoutcraft.launcher.util.Compatibility;
import org.spoutcraft.launcher.util.Utils;

import static org.spoutcraft.launcher.util.TextSource.lang;

public class ModpackOptions extends JDialog implements ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private static final int FRAME_WIDTH = 300;
	private static final int FRAME_HEIGHT = 300;
	private static final String QUIT_ACTION = "quit";
	private static final String SAVE_ACTION = "save";
	private static final String BUILD_ACTION = "build";
	private static final String REC_ACTION = "rec";
	private static final String LATEST_ACTION = "latest";
	private static final String MANUAL_ACTION = "manual";
	private static final String CHANGEFOLDER_ACTION = "changefolder";
	private static final String OPENFOLDER_ACTION = "openfolder";
	private static final String CLEAN_BIN_ACTION = "cleanbin";
	private static final String ESCAPE_ACTION = "escape";

	public static final String RECOMMENDED = "recommended";
	public static final String LATEST = "latest";

	private String build;
	private JLabel buildLabel;
	private JLabel background;
	private PackInfo installedPack;
	private JComboBox buildSelector;
	private LiteTextBox packLocation;
	private LiteButton openFolder;
	private LiteButton cleanBin;
	private File installedDirectory;
	private JFileChooser fileChooser;
	private boolean directoryChanged = false;
	private int mouseX = 0, mouseY = 0;
	
	public ModpackOptions(PackInfo installedPack) {
		this.installedPack = installedPack;
		setTitle(lang("modpackoptions.title"));
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		addMouseListener(this);
		addMouseMotionListener(this);
		setResizable(false);
		setUndecorated(true);
		initComponents();
	}
	
	private void initComponents() {
		Font fontregular = MetroLoginFrame.getClassicFont(13);
		Font fontbold = MetroLoginFrame.getClassicBoldFont(13);
		
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};

		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, ESCAPE_ACTION);
		getRootPane().getActionMap().put(ESCAPE_ACTION, escapeAction);

		background = new JLabel();
		background.setBounds(0,0, FRAME_WIDTH, FRAME_HEIGHT);
		MetroLoginFrame.setIcon(background, "optionsBackground.png", background.getWidth(), background.getHeight());
		
		Container contentPane = getContentPane();
		contentPane.setLayout(null);
		
		JLabel optionsTitle = new JLabel();
		optionsTitle.setBounds(10, 7, FRAME_WIDTH, 25);
		optionsTitle.setText(installedPack.getDisplayName() + " "+lang("modpackoptions.options.suffix"));
		optionsTitle.setForeground(Color.white);
		optionsTitle.setFont(fontbold.deriveFont(16F));
		
		ImageButton optionsQuit = new ImageButton(MetroLoginFrame.getIcon("exit.png", 16, 16), MetroLoginFrame.getIcon("exit.png", 16, 16));
		optionsQuit.setRolloverIcon(MetroLoginFrame.getIcon("exit_hover.png", 16, 16));
		optionsQuit.setBounds(FRAME_WIDTH - 10 - 16, 10, 16, 16);
		optionsQuit.setActionCommand(QUIT_ACTION);
		optionsQuit.addActionListener(this);
		
		buildLabel = new JLabel();
		buildLabel.setBounds(10, 50, 100, 25);
		buildLabel.setText(lang("modpackoptions.build"));
		buildLabel.setForeground(Color.white);
		buildLabel.setFont(fontregular.deriveFont(14F));
		
		buildSelector = new JComboBox();
		buildSelector.setBounds((FRAME_WIDTH/2)-50, 50, 195, 25);
		buildSelector.setActionCommand(BUILD_ACTION);
		buildSelector.addActionListener(this);
		populateBuilds(buildSelector);
		
		build = Settings.getModpackBuild(installedPack.getName());
		if (build == null) {
			build = RECOMMENDED;
		}
		
		ButtonGroup group = new ButtonGroup();
		
		JRadioButton versionRec = new JRadioButton(lang("modpackoptions.recommneded"));
		versionRec.setBounds(10, buildLabel.getY() + buildLabel.getHeight() + 10, FRAME_WIDTH - 20, 30);
		versionRec.setFont(fontregular);
		versionRec.setForeground(Color.white);
		versionRec.setContentAreaFilled(false);
		versionRec.setActionCommand(REC_ACTION);
		versionRec.addActionListener(this);
		group.add(versionRec);
		
		JRadioButton versionLatest = new JRadioButton(lang("modpackoptions.latest"));
		versionLatest.setBounds(10, versionRec.getY() + versionRec.getHeight(), FRAME_WIDTH - 20, 30);
		versionLatest.setFont(fontregular);
		versionLatest.setForeground(Color.white);
		versionLatest.setContentAreaFilled(false);
		versionLatest.setActionCommand(LATEST_ACTION);
		versionLatest.addActionListener(this);
		group.add(versionLatest);
		
		JRadioButton versionManual = new JRadioButton(lang("modpackoptions.manual"));
		versionManual.setBounds(10, versionLatest.getY() + versionLatest.getHeight(), FRAME_WIDTH - 20, 30);
		versionManual.setFont(fontregular);
		versionManual.setForeground(Color.white);
		versionManual.setContentAreaFilled(false);
		versionManual.setActionCommand(MANUAL_ACTION);
		versionManual.addActionListener(this);
		group.add(versionManual);
		
		if (build.equals("latest")) {
			buildSelector.setEnabled(false);
			buildSelector.setSelectedItem(new BuildLabel(installedPack.getLatest()));
			versionLatest.setSelected(true);
			build = LATEST;
		} else if (build.equals("recommended") || build == null) {
			buildSelector.setEnabled(false);
			buildSelector.setSelectedItem(new BuildLabel(installedPack.getRecommended()));
			versionRec.setSelected(true);
			build = RECOMMENDED;
		} else {
			versionManual.setSelected(true);
			buildSelector.setSelectedItem((String) build);
		}
		
		installedDirectory = installedPack.getPackDirectory();
		
		packLocation = new LiteTextBox(this, "");
		packLocation.setBounds(10, FRAME_HEIGHT-105, FRAME_WIDTH - 20, 25);
		packLocation.setFont(fontregular.deriveFont(10F));
		packLocation.setText(installedDirectory.getPath());
		packLocation.setEnabled(false);
		
		fileChooser = new JFileChooser(Utils.getLauncherDirectory());
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		LiteButton changeFolder = new LiteButton(lang("options.changefolder"), FRAME_WIDTH / 2 + 10, packLocation.getY() + packLocation.getHeight() + 10, FRAME_WIDTH / 2 - 20, 25);
		changeFolder.setFont(fontbold);
		changeFolder.setForeground(Color.WHITE);
		changeFolder.setActionCommand(CHANGEFOLDER_ACTION);
		changeFolder.addActionListener(this);
		
		openFolder = new LiteButton(lang("modpackoptions.openfolder"), 10, packLocation.getY() + packLocation.getHeight() + 10, FRAME_WIDTH / 2 - 20, 25);
		openFolder.setFont(fontbold);
		openFolder.setForeground(Color.WHITE);
		openFolder.setActionCommand(OPENFOLDER_ACTION);
		openFolder.addActionListener(this);
		
		if (!installedDirectory.exists()) {
			openFolder.setVisible(false);
		}

		LiteButton save = new LiteButton(lang("options.save"), FRAME_WIDTH / 2 + 10, openFolder.getY() + openFolder.getHeight() + 10, FRAME_WIDTH / 2 - 20, 25);
		save.setFont(fontbold);
		save.setForeground(Color.WHITE);
		save.setActionCommand(SAVE_ACTION);
		save.addActionListener(this);

		cleanBin = new LiteButton(lang("modpackoptions.resetpack"), 10, openFolder.getY() + openFolder.getHeight() + 10, FRAME_WIDTH / 2 - 20, 25);
		cleanBin.setFont(fontbold);
		cleanBin.setForeground(Color.WHITE);
		cleanBin.setActionCommand(CLEAN_BIN_ACTION);
		cleanBin.addActionListener(this);
		
		contentPane.add(optionsTitle);
		contentPane.add(optionsQuit);
		contentPane.add(buildLabel);
		contentPane.add(buildSelector);
		contentPane.add(versionRec);
		contentPane.add(versionLatest);
		contentPane.add(versionManual);
		contentPane.add(packLocation);
		contentPane.add(changeFolder);
		contentPane.add(openFolder);
		contentPane.add(save);
		contentPane.add(cleanBin);
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
			MetroLoginFrame.tracker.trackEvent("Modpack Options ("+installedPack.getDisplayName()+")", action);
			dispose();
		} else if (action.equals(SAVE_ACTION)) {
			Settings.setModpackBuild(installedPack.getName(), build);
			if (directoryChanged) {
				installedPack.setPackDirectory(installedDirectory);
			}
			Settings.getYAML().save();
			MetroLoginFrame.tracker.trackEvent("Modpack Options ("+installedPack.getDisplayName()+")", action);
			dispose();
		} else if (action.equals(BUILD_ACTION)) {
			build = ((BuildLabel) buildSelector.getSelectedItem()).getBuild();
			MetroLoginFrame.tracker.trackEvent("Modpack Options ("+installedPack.getDisplayName()+")", action);
		} else if (action.equals(REC_ACTION)) {
			buildSelector.setEnabled(false);
			buildSelector.setSelectedItem(new BuildLabel(installedPack.getRecommended()));
			build = RECOMMENDED;
			MetroLoginFrame.tracker.trackEvent("Modpack Options ("+installedPack.getDisplayName()+")", action);
		} else if (action.equals(LATEST_ACTION)) {
			buildSelector.setEnabled(false);
			buildSelector.setSelectedItem(new BuildLabel(installedPack.getLatest()));
			build = LATEST;
			MetroLoginFrame.tracker.trackEvent("Modpack Options ("+installedPack.getDisplayName()+")", action);
		} else if (action.equals(MANUAL_ACTION)) {
			buildSelector.setEnabled(true);
			build = ((BuildLabel) buildSelector.getSelectedItem()).getBuild();
			MetroLoginFrame.tracker.trackEvent("Modpack Options ("+installedPack.getDisplayName()+")", action, build);
		} else if (action.equals(OPENFOLDER_ACTION)) {
			if (installedDirectory.exists()) {
				Compatibility.open(installedDirectory);
			}
		} else if (action.equals(CHANGEFOLDER_ACTION)) {
			int result = fileChooser.showOpenDialog(this);
			
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				packLocation.setText(file.getPath());
				installedDirectory = file;
				directoryChanged = true;
				if (file.exists()) {
					openFolder.setVisible(true);
				}
			}
			MetroLoginFrame.tracker.trackEvent("Modpack Options ("+installedPack.getDisplayName()+")", action);
		} else if (action.equals(CLEAN_BIN_ACTION)) {
			int result = JOptionPane.showConfirmDialog(c, lang("modpackoptions.reset.question"), lang("modpackoptions.reset.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				MetroLoginFrame.tracker.trackEvent("Modpack Options ("+installedPack.getDisplayName()+")", action);
				cleanBin();
				dispose();
			}
		}
	}

	private void cleanBin() {
		UpdateThread.cleanupBinFolders(installedPack);
		UpdateThread.cleanupModsFolders(installedPack);
	}

	private void populateBuilds(JComboBox buildSelector) {
		for (String build : installedPack.getBuilds()) {
			String display = build;
			if (build.equals(installedPack.getLatest())) {
				display += " - "+lang("modpackoptions.build.latest");
			} else if (build.equals(installedPack.getRecommended())) {
				display += " - "+lang("modpackoptions.build.recommended");
			}
			BuildLabel label = new BuildLabel(build, display);
			buildSelector.addItem(label);
		}
	}

	private class BuildLabel {
		private final String build;
		private final String display;

		public BuildLabel(String build) {
			this(build, build);
		}

		public BuildLabel(String build, String display) {
			this.build = build;
			this.display = display;
		}

		public String getBuild() {
			return build;
		}

		@Override
		public String toString() {
			return display;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof BuildLabel) {
				BuildLabel label = (BuildLabel) obj;
				return (getBuild().equals(label.getBuild()));
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return build.hashCode();
		}
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

}
