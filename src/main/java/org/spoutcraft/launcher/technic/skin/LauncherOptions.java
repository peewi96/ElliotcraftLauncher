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
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import javax.swing.*;

import org.spoutcraft.launcher.Memory;
import org.spoutcraft.launcher.Language;
import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.UpdateThread;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.entrypoint.SpoutcraftLauncher;
import org.spoutcraft.launcher.exceptions.RestfulAPIException;
import org.spoutcraft.launcher.skin.MetroLoginFrame;
import org.spoutcraft.launcher.skin.components.LiteButton;
import org.spoutcraft.launcher.skin.components.LiteTextBox;
import org.spoutcraft.launcher.technic.rest.RestAPI;
import org.spoutcraft.launcher.util.Compatibility;
import org.spoutcraft.launcher.util.FileUtils;
import org.spoutcraft.launcher.util.Utils;

import static org.spoutcraft.launcher.util.TextSource.lang;

public class LauncherOptions extends JDialog implements ActionListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private static final int FRAME_WIDTH = 300;
	private static final int FRAME_HEIGHT = 350;
	private static final String LAUNCHER_PREPEND = lang("options.build")+" ";
	private static final String QUIT_ACTION = "quit";
	private static final String SAVE_ACTION = "save";
	private static final String LOGS_ACTION = "logs";
	private static final String CONSOLE_ACTION = "console";
	private static final String CHANGEFOLDER_ACTION = "changefolder";
	private static final String BETA_ACTION = "beta";
	private static final String ESCAPE_ACTION = "escape";

	private JLabel background;
	private JLabel build;

	private JComboBox memory;
	private JComboBox language;
	private JCheckBox permgen;
	private JCheckBox latestLWJGL;
	private JCheckBox beta;
	private JFileChooser fileChooser;
	private int mouseX = 0, mouseY = 0;
	private String installedDirectory;
	private LiteTextBox packLocation;
	private boolean directoryChanged = false;
	private boolean streamChanged = false;
	private String buildStream = "stable";
	private LiteButton changeFolder;
	private LiteButton logs;
	private LiteButton save;
	private LiteButton console;

	public LauncherOptions() {
		setTitle(lang("options.title"));
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

		ImageButton optionsQuit = new ImageButton(MetroLoginFrame.getIcon("exit.png", 16, 16), MetroLoginFrame.getIcon("exit.png", 16, 16));
		optionsQuit.setRolloverIcon(MetroLoginFrame.getIcon("exit_hover.png", 16, 16));
		optionsQuit.setBounds(FRAME_WIDTH - 10 - 16, 10, 16, 16);
		optionsQuit.setActionCommand(QUIT_ACTION);
		optionsQuit.addActionListener(this);

		JLabel title = new JLabel(lang("options.title"));
		title.setFont(fontbold.deriveFont(16F));
		title.setBounds(50, 10, 200, 20);
		title.setForeground(Color.WHITE);
		title.setHorizontalAlignment(SwingConstants.CENTER);

		build = new JLabel(LAUNCHER_PREPEND + Settings.getLauncherBuild());
		build.setBounds(15, title.getY() + title.getHeight() + 10, FRAME_WIDTH - 20, 20);
		build.setFont(fontregular);
		build.setText(LAUNCHER_PREPEND + getLatestLauncherBuild(buildStream));
		build.setForeground(Color.WHITE);
		
		beta = new JCheckBox(lang("options.beta"));
		beta.setBounds(10, build.getY() + build.getHeight() + 5, FRAME_WIDTH - 20, 20);
		beta.setFont(fontregular);
		beta.setForeground(Color.WHITE);
		beta.setContentAreaFilled(false);
		beta.setFocusPainted(false);
		beta.setBorderPainted(false);
		beta.setActionCommand(BETA_ACTION);
		beta.addActionListener(this);
		
		buildStream = Settings.getBuildStream();
		if (buildStream.equals("stable")) {
			beta.setSelected(false);
		} else if (buildStream.equals("beta")) {
			beta.setSelected(true);
		}

		JLabel languageLabel = new JLabel(lang("options.lang")+" ");
		languageLabel.setFont(fontregular);
		languageLabel.setBounds(15, beta.getY() + beta.getHeight() + 10, 74, 20);
		languageLabel.setForeground(Color.WHITE);
		languageLabel.setHorizontalAlignment(SwingConstants.LEFT);

		language = new JComboBox();
		language.setBounds(languageLabel.getX() + languageLabel.getWidth() + 10, languageLabel.getY(), 130, 20);
		populateLanguages(language);

		JLabel memoryLabel = new JLabel(lang("options.mem")+" ");
		memoryLabel.setFont(fontregular);
		memoryLabel.setBounds(15, languageLabel.getY() + languageLabel.getHeight() + 10, 74, 20);
		memoryLabel.setForeground(Color.WHITE);
		memoryLabel.setHorizontalAlignment(SwingConstants.LEFT);

		memory = new JComboBox();
		memory.setBounds(memoryLabel.getX() + memoryLabel.getWidth() + 10, memoryLabel.getY(), 130, 20);
		populateMemory(memory);

		permgen = new JCheckBox(lang("options.permgen"));
		permgen.setFont(fontregular);
		permgen.setBounds(10, memoryLabel.getY() + memoryLabel.getHeight() + 2, FRAME_WIDTH - 20, 25);
		permgen.setSelected(Settings.getPermGen());
		permgen.setBorderPainted(false);
		permgen.setFocusPainted(false);
		permgen.setContentAreaFilled(false);
		permgen.setForeground(Color.WHITE);
		permgen.setIconTextGap(15);

		latestLWJGL = new JCheckBox(lang("options.latestlwjgl"));
		latestLWJGL.setFont(fontregular);
		latestLWJGL.setBounds(10, permgen.getY() + permgen.getHeight() + 2, FRAME_WIDTH - 20, 25);
		latestLWJGL.setSelected(Settings.getLatestLWJGL());
		latestLWJGL.setBorderPainted(false);
		latestLWJGL.setFocusPainted(false);
		latestLWJGL.setContentAreaFilled(false);
		latestLWJGL.setForeground(Color.WHITE);
		latestLWJGL.setIconTextGap(15);

		installedDirectory = Settings.getLauncherDir();

		packLocation = new LiteTextBox(this, "");
		packLocation.setBounds(10, FRAME_HEIGHT-105, FRAME_WIDTH - 20, 25);
		packLocation.setFont(fontregular.deriveFont(10F));
		packLocation.setForeground(Color.WHITE);
		packLocation.setText(installedDirectory);
		packLocation.setEnabled(false);

		changeFolder = new LiteButton(lang("options.changefolder"), FRAME_WIDTH / 2 + 5, packLocation.getY() + packLocation.getHeight() + 10, FRAME_WIDTH / 2 - 15, 25);
		changeFolder.setFont(fontbold);
		changeFolder.setForeground(Color.WHITE);
		changeFolder.setActionCommand(CHANGEFOLDER_ACTION);
		changeFolder.addActionListener(this);
		changeFolder.setEnabled(!Utils.getStartupParameters().isPortable());

		logs = new LiteButton(lang("options.logs"), 10, packLocation.getY() + packLocation.getHeight() + 10, FRAME_WIDTH / 2 - 15, 25);
		logs.setFont(fontbold.deriveFont(14F));
		logs.setForeground(Color.WHITE);
		logs.setActionCommand(LOGS_ACTION);
		logs.addActionListener(this);

		save = new LiteButton(lang("options.save"), FRAME_WIDTH / 2 + 5, logs.getY() + logs.getHeight() + 10, FRAME_WIDTH / 2 - 15, 25);
		save.setFont(fontbold.deriveFont(14F));
		save.setForeground(Color.WHITE);
		save.setActionCommand(SAVE_ACTION);
		save.addActionListener(this);

		console = new LiteButton(lang("options.console"), 10, logs.getY() + logs.getHeight() + 10, FRAME_WIDTH / 2 - 15, 25);
		console.setFont(fontbold.deriveFont(14F));
		console.setForeground(Color.WHITE);
		console.setActionCommand(CONSOLE_ACTION);
		console.addActionListener(this);

		fileChooser = new JFileChooser(Utils.getLauncherDirectory());
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		Container contentPane = getContentPane();
		contentPane.add(permgen);
		contentPane.add(latestLWJGL);
		contentPane.add(build);
		contentPane.add(beta);
		contentPane.add(changeFolder);
		contentPane.add(packLocation);
		contentPane.add(logs);
		contentPane.add(console);
		contentPane.add(optionsQuit);
		contentPane.add(title);
		contentPane.add(language);
		contentPane.add(languageLabel);
		contentPane.add(memory);
		contentPane.add(memoryLabel);
		contentPane.add(save);
		contentPane.add(background);

		setLocationRelativeTo(this.getOwner());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComponent) {
			action(e.getActionCommand(), (JComponent) e.getSource());
		}
	}

	public void action(String action, JComponent c) {
		if (action.equals(QUIT_ACTION)) {
			dispose();
		} else if (action.equals(SAVE_ACTION)) {
			int oldMem = Settings.getMemory();
			int mem = Memory.memoryOptions[memory.getSelectedIndex()].getSettingsId();
			Settings.setMemory(mem);
			boolean oldperm = Settings.getPermGen();
			boolean perm = permgen.isSelected();
			Settings.setPermGen(perm);
			Settings.setBuildStream(buildStream);
			if (directoryChanged) {
				Settings.setMigrate(true);
				Settings.setMigrateDir(installedDirectory);
			}
			String oldLang = Settings.getLanguage();
			String lang = Language.languageOptions[language.getSelectedIndex()].getLanguage();
			Settings.setLanguage(lang);
			Settings.getYAML().save();

			if (directoryChanged || streamChanged) {
				JOptionPane.showMessageDialog(c, lang("options.manualrestart.question"), lang("options.restart.title"), JOptionPane.INFORMATION_MESSAGE);
				dispose();
			}
			if (mem != oldMem || oldperm != perm) {
				int result = JOptionPane.showConfirmDialog(c, lang("options.restart.question", lang), lang("options.restart.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					MetroLoginFrame.tracker.trackEvent("Launcher Options", action, "RESTART_LAUNCHER", 1);
					SpoutcraftLauncher.relaunch(true);
				} else {
					MetroLoginFrame.tracker.trackEvent("Launcher Options", action, "RESTART_LAUNCHER", 0);
				}
			}
			if (latestLWJGL.isSelected() != Settings.getLatestLWJGL()) {
				System.out.println("[LWJGL] Version changed! Clearing lwjgl from cache.");
				Settings.setLatestLWJGL(latestLWJGL.isSelected());
				Settings.getYAML().save();
				UpdateThread.cleanupLWJGL();
			}
			MetroLoginFrame.tracker.trackEvent("Launcher Options", action);
			dispose();
		} else if (action.equals(LOGS_ACTION)) {
			File logDirectory = new File(Utils.getLauncherDirectory(), "logs");
			Compatibility.open(logDirectory);
			MetroLoginFrame.tracker.trackEvent("Launcher Options", action);
		} else if (action.equals(CONSOLE_ACTION)) {
			MetroLoginFrame.tracker.trackEvent("Launcher Options", action);
			SpoutcraftLauncher.setupConsole();
			dispose();
		} else if (action.equals(CHANGEFOLDER_ACTION)) {
			int result = fileChooser.showOpenDialog(this);
			MetroLoginFrame.tracker.trackEvent("Launcher Options", action);

			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				if (!FileUtils.checkLaunchDirectory(file)) {
					JOptionPane.showMessageDialog(c, lang("options.selectemptyfolder.dialog"), lang("options.selectemptyfolder.invalid"), JOptionPane.WARNING_MESSAGE);
					return;
				}
				packLocation.setText(file.getPath());
				installedDirectory = file.getAbsolutePath();
				directoryChanged = true;
			}
		} else if (action.equals(BETA_ACTION) && beta.isSelected()) {
			buildStream = "beta";
			build.setText(LAUNCHER_PREPEND + getLatestLauncherBuild(buildStream));
			streamChanged = true;
		} else if (action.equals(BETA_ACTION) && !beta.isSelected()) {
			buildStream = "stable";
			build.setText(LAUNCHER_PREPEND + getLatestLauncherBuild(buildStream));
			streamChanged = true;
		}
		
	}
	
	private int getLatestLauncherBuild(String buildStream) {
		int build = Settings.getLauncherBuild();
		try {
			build = RestAPI.getLatestLauncherBuild(buildStream);
			return build;
		} catch (RestfulAPIException e) {
			e.printStackTrace();
		}
		
		return build;
	}

	@SuppressWarnings("restriction")
	private void populateMemory(JComboBox memory) {
		long maxMemory = 1024;
		String architecture = System.getProperty("sun.arch.data.model", "32");
		boolean bit64 = architecture.equals("64");

		try {
			OperatingSystemMXBean osInfo = ManagementFactory.getOperatingSystemMXBean();
			if (osInfo instanceof com.sun.management.OperatingSystemMXBean) {
				maxMemory = ((com.sun.management.OperatingSystemMXBean) osInfo).getTotalPhysicalMemorySize() / 1024 / 1024;
			}
		} catch (Throwable t) {
		}
		maxMemory = Math.max(512, maxMemory);

		if (maxMemory >= Memory.MAX_32_BIT_MEMORY && !bit64) {
			memory.setToolTipText(lang("options.memtooltip.no64"));
		} else {
			memory.setToolTipText(lang("options.memtooltip.setram"));
		}

		if (!bit64) {
			maxMemory = Math.min(Memory.MAX_32_BIT_MEMORY, maxMemory);
		}
		System.out.println(lang("options.maxmem")+" " + maxMemory + " mb");

		for (Memory mem : Memory.memoryOptions) {
			if (maxMemory >= mem.getMemoryMB()) {
				memory.addItem(mem.getDescription());
			}
		}

		int memoryOption = Settings.getMemory();
		try {
			Settings.setMemory(memoryOption);
			memory.setSelectedIndex(Memory.getMemoryIndexFromId(memoryOption));
		} catch (IllegalArgumentException e) {
			memory.removeAllItems();
			memory.addItem(String.valueOf(Memory.memoryOptions[0]));
			Settings.setMemory(1); // 512 == 1
			memory.setSelectedIndex(0); // 1st element
		}
	}

	@SuppressWarnings("restriction")
	private void populateLanguages(JComboBox language) {
		for (Language lang : Language.languageOptions) {
				language.addItem(lang.getDescription());
		}

		String languageOption = Settings.getLanguage();
		try {
			Settings.setLanguage(languageOption);
			language.setSelectedItem(Language.getLanguageName(languageOption));
		} catch (IllegalArgumentException e) {
			language.removeAllItems();
			language.addItem(Language.languageOptions[0].getDescription());
			Settings.setLanguage("en_US");
			language.setSelectedIndex(0);
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
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
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

}
