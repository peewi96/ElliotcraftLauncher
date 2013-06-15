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

package org.spoutcraft.launcher.skin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.*;

import net.minecraft.Launcher;

import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.entrypoint.SpoutcraftLauncher;
import org.spoutcraft.launcher.skin.components.BackgroundImage;
import org.spoutcraft.launcher.skin.components.DynamicButton;
import org.spoutcraft.launcher.skin.components.HyperlinkJLabel;
import org.spoutcraft.launcher.skin.components.ImageHyperlinkButton;
import org.spoutcraft.launcher.skin.components.LiteButton;
import org.spoutcraft.launcher.skin.components.LitePasswordBox;
import org.spoutcraft.launcher.skin.components.LiteProgressBar;
import org.spoutcraft.launcher.skin.components.LiteTextBox;
import org.spoutcraft.launcher.skin.components.TransparentJLabel;
import org.spoutcraft.launcher.tracking.system.AWTSystemPopulator;
import org.spoutcraft.launcher.technic.AddPack;
import org.spoutcraft.launcher.technic.PackInfo;
import org.spoutcraft.launcher.technic.skin.ImageButton;
import org.spoutcraft.launcher.technic.skin.LauncherOptions;
import org.spoutcraft.launcher.technic.skin.ModpackOptions;
import org.spoutcraft.launcher.technic.skin.ModpackSelector;
import org.spoutcraft.launcher.technic.skin.NewsComponent;
import org.spoutcraft.launcher.technic.skin.RoundedBox;
import org.spoutcraft.launcher.util.*;

import org.spoutcraft.launcher.tracking.AnalyticsConfigData;
import org.spoutcraft.launcher.tracking.JGoogleAnalyticsTracker;
import org.spoutcraft.launcher.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;

import static org.spoutcraft.launcher.util.TextSource.lang;

public class TechnicLoginFrame extends LoginFrame implements ActionListener, KeyListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;
	private static final int FRAME_WIDTH = 880;
	private static final int FRAME_HEIGHT = 520;
	public static final int FRAME_SIDE_SPACING = 22;
	public static final int FRAME_TOP_SPACING = 15;
	private static final String OPTIONS_ACTION = "options";
	private static final String CONSOLE_ACTION = "console";
	private static final String PACK_OPTIONS_ACTION = "packoptions";
	private static final String PACK_REMOVE_ACTION = "packremove";
	private static final String EXIT_ACTION = "exit";
	private static final String PACK_LEFT_ACTION = "packleft";
	private static final String PACK_RIGHT_ACTION = "packright";
	private static final String LOGIN_ACTION = "login";
	private static final String IMAGE_LOGIN_ACTION = "image_login";
	private static final String REMOVE_USER = "remove";
	public static final Color TRANSPARENT = new Color(45, 45, 45, 160);
	private static final int SPACING = 7;
	private final Map<JButton, DynamicButton> removeButtons = new HashMap<JButton, DynamicButton>();
	private final Map<String, DynamicButton> userButtons = new HashMap<String, DynamicButton>();
	private LiteTextBox name;
	private LitePasswordBox pass;
	private LiteButton login;
	private JCheckBox remember;
	private LiteProgressBar progressBar;
	private LauncherOptions launcherOptions = null;
	private ModpackOptions packOptions = null;
	private ModpackSelector packSelector;
	private BackgroundImage packBackground;
	private ImageButton packOptionsBtn;
	private ImageButton packRemoveBtn;
	private ImageHyperlinkButton platform;
	private JLabel packShadow;
	private JLabel customName;
	private NewsComponent news;
	private long previous = 0L;
	static AnalyticsConfigData config = new AnalyticsConfigData("UA-34064856-2");
	private boolean consoleToggle = false;
	public static JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker(config, GoogleAnalyticsVersion.V_4_7_2);


	public TechnicLoginFrame() {
		initComponents();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((dim.width - FRAME_WIDTH) / 2, (dim.height - FRAME_HEIGHT) / 2, FRAME_WIDTH, FRAME_HEIGHT);
		setResizable(false);
		packBackground = new BackgroundImage(this, FRAME_WIDTH, FRAME_HEIGHT);
		this.addMouseListener(packBackground);
		this.addMouseMotionListener(packBackground);
		this.addMouseWheelListener(this);
		getContentPane().add(packBackground);
		TrackerUtils.sendPageView("skin/TechnicLoginFrame.java", "Launcher Start v" + Settings.getLauncherBuild());
		AWTSystemPopulator.populateConfigData(config);
		this.setUndecorated(true);
	}

	private void initComponents() {
		Font minecraft = TechnicLoginFrame.getMinecraftFont(12);
		Font fontbold = TechnicLoginFrame.getClassicBoldFont(13);

		// Login Strip
		TransparentJLabel loginStrip = new TransparentJLabel();
		// 379 is the center of the bottom
		loginStrip.setBounds(FRAME_WIDTH-260, FRAME_HEIGHT-190, 260-FRAME_SIDE_SPACING, 170);
		loginStrip.setTransparency(0.95F);
		loginStrip.setHoverTransparency(0.95F);
		loginStrip.setBackground(new Color(181, 225, 17));
		//setIcon(loginStrip, "xx.png", loginStrip.getWidth(), loginStrip.getHeight());

		// Setup username box
		name = new LiteTextBox(this, lang("gui.username"));
		name.setBounds(loginStrip.getX() + 1, loginStrip.getY() + 24, 115, 24);
		name.setFont(minecraft);
		name.addKeyListener(this);

		// Setup password box
		pass = new LitePasswordBox(this, lang("gui.password"));
		//orig- pass.setBounds(620, loginStrip.getY() + loginStrip.getHeight() / 2 - 12, 115, 24);
		pass.setBounds(name.getX() + 120, loginStrip.getY() + 24, 115, 24);
		pass.setFont(minecraft);
		pass.addKeyListener(this);

		// Setup remember checkbox
		remember = new JCheckBox(lang("gui.remember"));
		remember.setBounds(pass.getX(), loginStrip.getY(), 117, 24);
		remember.setFont(fontbold);
		remember.setOpaque(false);
		remember.setBorderPainted(false);
		remember.setFocusPainted(false);
		remember.setContentAreaFilled(false);
		remember.setBorder(null);
		remember.setForeground(Color.WHITE);
		remember.setBackground(Color.RED);
		remember.setHorizontalAlignment(SwingConstants.RIGHT);
		remember.setHorizontalTextPosition(SwingConstants.LEFT);
		remember.setIconTextGap(10);
		remember.addKeyListener(this);

		// Setup login button
		login = new LiteButton(lang("gui.launch"), pass.getX(), pass.getY() + 29, 115, 24);
		login.setFont(fontbold);
		login.setForeground(Color.WHITE);
		login.setActionCommand(LOGIN_ACTION);
		login.addActionListener(this);
		login.addKeyListener(this);

		// Technic logo
//		JLabel logo = new JLabel();
//		ImageIcon logoIcon = new ImageIcon(ImageUtils.scaleWithAspectWidth(getImage("header.png"), 275));
//		logo.setIcon(logoIcon);
//		logo.setBounds(600, 6, logoIcon.getIconWidth(), logoIcon.getIconHeight());

		// Tear
		HyperlinkJLabel tear = new HyperlinkJLabel("", "http://www.technicpack.net");
		ImageIcon tearIcon = new ImageIcon(ImageUtils.scaleWithAspectWidth(getImage("tear1.png"), 100));
		tear.setIcon(tearIcon);
		tear.setBounds(520, 0, 140, 361);

		// Pack Selector Background
		JLabel selectorBackground = new JLabel();
		selectorBackground.setBounds(FRAME_SIDE_SPACING, FRAME_HEIGHT / 2 - 100, FRAME_WIDTH - (2*FRAME_SIDE_SPACING), 1);
		setIcon(selectorBackground, "colored_strip.png", selectorBackground.getWidth(), selectorBackground.getHeight());

		// Pack Select Left
		ImageButton switchLeft = new ImageButton(getIcon("arrow_left.png"), getIcon("arrow_left_hover.png"));
		switchLeft.setBounds(FRAME_SIDE_SPACING, selectorBackground.getY(), 26, 146);
		switchLeft.setActionCommand(PACK_LEFT_ACTION);
		switchLeft.addActionListener(this);

		// Pack Select Right
		ImageButton switchRight = new ImageButton(getIcon("arrow_right.png"), getIcon("arrow_right_hover.png"));
		switchRight.setBounds(FRAME_WIDTH - (26+FRAME_SIDE_SPACING), selectorBackground.getY(), 26, 146);
		switchRight.setActionCommand(PACK_RIGHT_ACTION);
		switchRight.addActionListener(this);

		// Progress Bar
		progressBar = new LiteProgressBar();
		progressBar.setBounds((FRAME_WIDTH / 2) - (395 / 2), 130, 395, 23);
		progressBar.setVisible(false);
		progressBar.setStringPainted(true);
		progressBar.setOpaque(true);
		progressBar.setTransparency(0.70F);
		progressBar.setHoverTransparency(0.70F);
		progressBar.setFont(minecraft);

		// Home Link
		Font largerMinecraft;
		if (OperatingSystem.getOS().isUnix()) {
			largerMinecraft = fontbold.deriveFont((float)18);
		} else {
			largerMinecraft = fontbold.deriveFont((float)20);
		}

		// News Items
		news = new NewsComponent();
		news.setBounds(50, loginStrip.getY() + 10, 500, 160);

		// Link background box
		RoundedBox linkArea = new RoundedBox(TRANSPARENT);
		linkArea.setBounds(605, 250, 265, 120);

		HyperlinkJLabel home = new HyperlinkJLabel(lang("gui.link.home"), "http://www.technicraft.cz");
		home.setFont(largerMinecraft);
		home.setBounds(545, 35, 65, 20);
		home.setForeground(Color.WHITE);
		home.setOpaque(false);
		home.setTransparency(0.70F);
		home.setHoverTransparency(1F);

		// Forums link
		JButton forums = new ImageHyperlinkButton("http://forum.majncraft.cz/");
		forums.setToolTipText(lang("gui.tooltip.forum"));
		forums.setBounds(9, loginStrip.getY() + 9, 90, 90);
		forums.setBorderPainted(false);
		ImageIcon forumIcon = getIcon("forums.png");
		forums.setIcon(forumIcon);
		forums.setRolloverIcon(forumIcon);
		forums.setPressedIcon(forumIcon);
		forums.setSelectedIcon(forumIcon);
		forums.setRolloverSelectedIcon(forumIcon);

		// Donate link
		JButton donate = new ImageHyperlinkButton("http://www.technicpack.net/donate/");
		donate.setToolTipText(lang("gui.tooltip.donate"));
		donate.setBounds(9 + forums.getWidth() + 9, loginStrip.getY() + 9, 90, 90);
		donate.setBorderPainted(false);
		ImageIcon donateIcon = getIcon("donate.png");
		donate.setIcon(donateIcon);
		donate.setRolloverIcon(donateIcon);
		donate.setPressedIcon(donateIcon);
		donate.setSelectedIcon(donateIcon);
		donate.setRolloverSelectedIcon(donateIcon);

		// Issues link
		HyperlinkJLabel issues = new HyperlinkJLabel(lang("gui.link.issues"), "http://forum.majncraft.cz/forums/bugy.179//");
		issues.setFont(largerMinecraft);
		issues.setBounds(723, 35, 100, 20);
		issues.setForeground(Color.WHITE);
		issues.setOpaque(false);
		issues.setTransparency(0.70F);
		issues.setHoverTransparency(1F);

		// Pack Options Button
		packOptionsBtn = new ImageButton(getIcon("settings.png"), getIcon("settings_hover.png"));
		packOptionsBtn.setBounds(FRAME_WIDTH / 2 - (ModpackSelector.bigWidth / 2) + 5, FRAME_HEIGHT / 2 + (ModpackSelector.bigHeight / 2) - 35, 16, 16);
		packOptionsBtn.setActionCommand(PACK_OPTIONS_ACTION);
		packOptionsBtn.addActionListener(this);


		// Platform website button
		platform = new ImageHyperlinkButton("http://www.technicpack.net/");
		platform.setIcon(getIcon("openPlatformPage.png", 20, 20));
		platform.setRolloverIcon(getIcon("openPlatformPageInverted.png"));
		platform.setBounds(FRAME_WIDTH / 2 - (ModpackSelector.bigWidth / 2) + 25, FRAME_HEIGHT / 2 + (ModpackSelector.bigHeight / 2) - 35, 20, 20);
		platform.setVisible(false); // hide it by default

		// Pack Remove Button
		packRemoveBtn = new ImageButton(getIcon("delete.png", 26, 26), getIcon("delete_hover.png", 26, 26));
		packRemoveBtn.setBounds(FRAME_WIDTH / 2 + 94, FRAME_HEIGHT / 2 + 56, 26, 26);
		packRemoveBtn.setActionCommand(PACK_REMOVE_ACTION);
		packRemoveBtn.addActionListener(this);

		// Exit Button
		ImageButton exit = new ImageButton(getIcon("exit.png"), getIcon("exit_hover.png"));
		exit.setBounds(FRAME_WIDTH - FRAME_SIDE_SPACING - 16, FRAME_TOP_SPACING, 16, 16);
		exit.setActionCommand(EXIT_ACTION);
		exit.addActionListener(this);

		// Options Button
		ImageButton options = new ImageButton(getIcon("settings.png"), getIcon("settings_hover.png"));
		options.setBounds(exit.getX()-30, FRAME_TOP_SPACING, 16, 16);
		options.setActionCommand(OPTIONS_ACTION);
		options.addActionListener(this);
		options.setBorder(BorderFactory.createEmptyBorder());
		options.setContentAreaFilled(false);
		options.addKeyListener(this);

		// Console Button
		consoleToggle = Settings.getShowLauncherConsole();
		ImageButton console = new ImageButton(getIcon("console.png"), getIcon("console_hover.png"));
		console.setBounds(options.getX()-30, FRAME_TOP_SPACING, 16, 16);
		console.setActionCommand(CONSOLE_ACTION);
		console.addActionListener(this);

		// Facebook button
		JButton facebook = new ImageHyperlinkButton("https://www.facebook.com/TechniCraftcz");
		facebook.setRolloverIcon(getIcon("social_face_hover.png"));
		facebook.setToolTipText(lang("gui.social.facebook.tooltip"));
		facebook.setBounds(FRAME_SIDE_SPACING, FRAME_TOP_SPACING, 32, 32);
		setIcon(facebook, "social_face.png", 32);

		// Twitter button
		JButton twitter = new ImageHyperlinkButton("https://twitter.com/TechniCraftCZ");
		twitter.setRolloverIcon(getIcon("social_twitter_hover.png"));
		twitter.setToolTipText(lang("gui.social.twitter.tooltip"));
		twitter.setBounds(FRAME_SIDE_SPACING, facebook.getY() + 42, 32, 32);
		setIcon(twitter, "social_twitter.png", 32);

		// Google+ button
		JButton gplus = new ImageHyperlinkButton("http://gplus.to/majncraft");
		gplus.setRolloverIcon(getIcon("social_gplus_hover.png"));
		gplus.setToolTipText(lang("gui.social.gplus.tooltip"));
		gplus.setBounds(FRAME_SIDE_SPACING, twitter.getY() + 42, 32, 32);
		setIcon(gplus, "social_gplus.png", 32);

		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		// Pack Selector
		packSelector = new ModpackSelector(this);
		packSelector.setBounds(FRAME_SIDE_SPACING, selectorBackground.getY()+1, FRAME_WIDTH - (2*FRAME_SIDE_SPACING), 145);
		
		// Custom Pack Name Label
		customName = new JLabel("", JLabel.CENTER);
		customName.setBounds(FRAME_WIDTH / 2 - (192 /2), FRAME_HEIGHT / 2 + (110 / 2) - 30, 192, 30);
		customName.setFont(fontbold.deriveFont(14F));
		customName.setVisible(false);
		customName.setForeground(Color.white);

		// User Faces
		java.util.List<String> savedUsers = getSavedUsernames();
		int users = Math.min(5, this.getSavedUsernames().size());

		for (int i = 0; i < users; i++) {
			String accountName = savedUsers.get(i);
			String userName = this.getUsername(accountName);

			BufferedImage image = getImage("face.png", 45, 45);
			File assets = new File(Utils.getAssetsDirectory(), "avatars");
			File face = new File(assets, userName + ".png");
			if (face.exists()) {
				try {
					image = ImageIO.read(face);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			DynamicButton userButton = new DynamicButton(this, image, 1, accountName, userName);
			userButton.setFont(fontbold.deriveFont(13F));

			userButton.setBounds(FRAME_WIDTH - ((i + 1) * 70), FRAME_HEIGHT - 87, 45, 45);
			contentPane.add(userButton);
			userButton.setActionCommand(IMAGE_LOGIN_ACTION);
			userButton.addActionListener(this);
			setIcon(userButton.getRemoveIcon(), "remove.png", 16);
			userButton.getRemoveIcon().addActionListener(this);
			userButton.getRemoveIcon().setActionCommand(REMOVE_USER);
			userButton.getRemoveIcon().setBorder(BorderFactory.createEmptyBorder());
			userButton.getRemoveIcon().setContentAreaFilled(false);
			removeButtons.put(userButton.getRemoveIcon(), userButton);
			userButtons.put(userName, userButton);
		}

		contentPane.add(switchLeft);
		contentPane.add(switchRight);
		contentPane.add(customName);
		contentPane.add(packOptionsBtn);
		contentPane.add(packRemoveBtn);
		contentPane.add(platform);
		contentPane.add(packSelector);
		contentPane.add(selectorBackground);
		contentPane.add(name);
		contentPane.add(pass);
		contentPane.add(remember);
		contentPane.add(login);
		contentPane.add(gplus);
		contentPane.add(twitter);
		contentPane.add(facebook);
		//contentPane.add(home);
		contentPane.add(forums);
		//contentPane.add(donate);
		//contentPane.add(issues);
		//contentPane.add(logo);
		contentPane.add(loginStrip);
		//contentPane.add(news);
		//contentPane.add(tear);
		contentPane.add(options);
		contentPane.add(console);
		contentPane.add(exit);
		contentPane.add(progressBar);
		
		setFocusTraversalPolicy(new LoginFocusTraversalPolicy());
	}

	public void setUser(String name) {
		if (name != null) {
			DynamicButton user = userButtons.get(this.getUsername(name));
			if (user != null) {
				user.doClick();
			}
		}
	}

	public ModpackSelector getSelector() {
		return packSelector;
	}

	public NewsComponent getNews() {
		return news;
	}

	public BackgroundImage getBackgroundImage() {
		return packBackground;
	}

	public static ImageIcon getIcon(String iconName) {
		return new ImageIcon(Launcher.class.getResource("/org/spoutcraft/launcher/resources/" + iconName));
	}

	public static BufferedImage getImage(String imageName) {
		try {
			return ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + imageName));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static BufferedImage getImage(String imageName, int w, int h) {
		try {
			return ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + imageName)), w, h);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ImageIcon getIcon(String iconName, int w, int h) {
		try {
			return new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + iconName)), w, h));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static ImageIcon getResizedIcon(String iconName, int w, int h) {
		try {
			return new ImageIcon(ImageUtils.resizeImage(ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + iconName)), w, h));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	private void setIcon(JButton button, String iconName, int size) {
		try {
			button.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + iconName)), size, size)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setIcon(JLabel label, String iconName, int w, int h) {
		try {
			label.setIcon(new ImageIcon(ImageUtils.scaleImage(ImageIO.read(ResourceUtils.getResourceAsStream("/org/spoutcraft/launcher/resources/" + iconName)), w, h)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateFaces() {
		for (String user : userButtons.keySet()) {
			BufferedImage image = getUserImage(user);
			if (image != null) {
				userButtons.get(user).updateIcon(image);
			}
		}
	}

	private BufferedImage getUserImage(String user) {
		File assets = new File(Utils.getAssetsDirectory(), "avatars");
		assets.mkdirs();
		File file = new File(assets, user + ".png");
		try {
			Download download = DownloadUtils.downloadFile("http://skins.majncraft.cz/helm/" + user + "/64", file.getAbsolutePath());
			if (download.getResult().equals(Download.Result.SUCCESS)) {
				return ImageIO.read(download.getOutFile());
			}
		} catch (IOException e) {
			if (Utils.getStartupParameters().isDebugMode()) {
				org.spoutcraft.launcher.api.Launcher.getLogger().log(Level.INFO, "Error downloading user face image: " + user, e);
			} else {
				org.spoutcraft.launcher.api.Launcher.getLogger().log(Level.INFO, "Error downloading user face image: " + user);
			}
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComponent) {
			action(e.getActionCommand(), (JComponent)e.getSource());
		}
	}

	private void action(String action, JComponent c) {
		if (action.equals(OPTIONS_ACTION)) {
			if (launcherOptions == null || !launcherOptions.isVisible()) {
				launcherOptions = new LauncherOptions();
				launcherOptions.setModal(true);
				launcherOptions.setVisible(true);
				tracker.trackEvent("Login Frame", action);
			}
		} else if(action.equals(PACK_REMOVE_ACTION)) {
			int result = JOptionPane.showConfirmDialog(this, lang("gui.custompack.delete.question") + " " + getSelector().getSelectedPack().getPackDirectory(), lang("gui.custompack.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				getSelector().removePack();
				tracker.trackEvent("Login Frame", action, getSelector().getSelectedPack().getDisplayName());
			}
		} else if (action.equals(PACK_OPTIONS_ACTION)) {
			if (packOptions == null || !packOptions.isVisible()) {
				packOptions = new ModpackOptions(getSelector().getSelectedPack());
				packOptions.setModal(true);
				packOptions.setVisible(true);
				tracker.trackEvent("Login Frame", action, getSelector().getSelectedPack().getDisplayName());
			}
		} else if (action.equals(EXIT_ACTION)) {
			tracker.trackEvent("Login Frame", action);
			System.exit(0);
		} else if (action.equals(PACK_LEFT_ACTION)) {
			getSelector().selectPreviousPack();
		} else if (action.equals(PACK_RIGHT_ACTION)) {
			getSelector().selectNextPack();
		} else if (action.equals(LOGIN_ACTION)) {
			tracker.trackEvent("Login Frame", action);
			PackInfo pack = getSelector().getSelectedPack();
			if (pack.usePermGen()) {
				int result = JOptionPane.showConfirmDialog(this, lang("modpack.permgen.message"), lang("modpack.permgen.title"), JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					TechnicLoginFrame.tracker.trackEvent("Launcher Options", action, "RESTART_LAUNCHER", 1);
					Settings.setPermGen(true);
					Settings.getYAML().save();
					SpoutcraftLauncher.relaunch(true);
				}
			}

			if (pack instanceof AddPack) {
				return;
			}
			if (pack.getModpack() == null || pack.getModpack().getMinecraftVersion() == null) {
				JOptionPane.showMessageDialog(this, "Error retrieving information for selected pack: " + pack.getDisplayName(), "Error", JOptionPane.WARNING_MESSAGE);
				return;
			}

			String pass = new String(this.pass.getPassword());
			if (getSelectedUser().length() > 0 && pass.length() > 0) {
				lockLoginButton(false);
				this.doLogin(getSelectedUser(), pass, pack);
				if (remember.isSelected()) {
					saveUsername(getSelectedUser(), pass);
					Settings.setLastUser(getSelectedUser());
					Settings.getYAML().save();
				}
			}
		} else if (action.equals(IMAGE_LOGIN_ACTION)) {
			DynamicButton userButton = (DynamicButton)c;
			this.name.setText(userButton.getAccount());
			this.pass.setText(this.getSavedPassword(userButton.getAccount()));
			this.remember.setSelected(true);
			pass.setLabelVisible(false);
			name.setLabelVisible(false);
			tracker.trackEvent("Login Frame", action);
		}  else if (action.equals(REMOVE_USER)) {
			DynamicButton userButton = removeButtons.get((JButton)c);
			this.removeAccount(userButton.getAccount());
			userButton.setVisible(false);
			userButton.setEnabled(false);
			getContentPane().remove(userButton);
			c.setVisible(false);
			c.setEnabled(false);
			getContentPane().remove(c);
			removeButtons.remove(c);
			writeUsernameList();
			tracker.trackEvent("Login Frame", action);
		} else if (action.equals(CONSOLE_ACTION)) {
			consoleToggle =! consoleToggle;
			tracker.trackEvent("Login Frame", action);
			Settings.setShowLauncherConsole(consoleToggle);
			if (consoleToggle) {
				SpoutcraftLauncher.setupConsole();
			} else {
				SpoutcraftLauncher.destroyConsole();
			}
			Settings.getYAML().save();
		}
	}

	@Override
	public void stateChanged(final String status, final float progress) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				int intProgress = Math.round(progress);
				progressBar.setValue(intProgress);
				String text = status;
				if (text.length() > 60) {
					text = text.substring(0, 60) + "...";
				}
				progressBar.setString(intProgress + "% " + text);
			}
		});
	}

	@Override
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	@Override
	public void disableForm() {
	}

	@Override
	public void enableForm() {
		progressBar.setVisible(false);
		lockLoginButton(true);
	}

	@Override
	public String getSelectedUser() {
		return this.name.getText();
	}

	public ImageButton getPackOptionsBtn() {
		return packOptionsBtn;
	}

	public ImageButton getPackRemoveBtn() {
		return packRemoveBtn;
	}

	public JLabel getPackShadow() {
		return packShadow;
	}
	
	public JLabel getCustomName() {
		return customName;
	}

	public ImageHyperlinkButton getPlatform() {
		return platform;
	}

	public void enableComponent(JComponent component, boolean enable) {
		component.setVisible(enable);
		component.setEnabled(enable);
	}

	public void setCustomName(String packName) {
		customName.setText(packName);
	}
	
	public void lockLoginButton(boolean unlock) {
		if (unlock) {
			login.setText(lang("gui.login"));
			login.setForeground(Color.WHITE);
		} else {
			login.setText(lang("gui.launching"));
			login.setForeground(new Color(51, 45, 73));
		}
		login.setEnabled(unlock);
		packRemoveBtn.setEnabled(unlock);
		packOptionsBtn.setEnabled(unlock);
	}

	// Emulates tab focus policy of name -> pass -> remember -> login
	private class LoginFocusTraversalPolicy extends FocusTraversalPolicy{
		@Override
		public Component getComponentAfter(Container con, Component c) {
			if (c == name) {
				return pass;
			} else if (c == pass) {
				return remember;
			} else if (c == remember) {
				return login;
			} else if (c == login) {
				return name;
			}
			return getFirstComponent(con);
		}

		@Override
		public Component getComponentBefore(Container con, Component c) {
			if (c == name) {
				return login;
			} else if (c == pass) {
				return name;
			} else if (c == remember) {
				return pass;
			} else if (c == login) {
				return remember;
			}
			return getFirstComponent(con);
		}

		@Override
		public Component getFirstComponent(Container c) {
			return name;
		}

		@Override
		public Component getLastComponent(Container c) {
			return login;
		}

		@Override
		public Component getDefaultComponent(Container c) {
			return name;
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER){
			// Allows the user to press enter and log in from the login box focus, username box focus, or password box focus
			if (e.getComponent() == login || e.getComponent() == name || e.getComponent() == pass) {
				action(LOGIN_ACTION, (JComponent) e.getComponent());
			} else if (e.getComponent() == remember) {
				remember.setSelected(!remember.isSelected());
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWhen() != previous) {
			if (e.getUnitsToScroll() > 0) {
				getSelector().selectNextPack();
			} else if (e.getUnitsToScroll() < 0){
				getSelector().selectPreviousPack();
			}
			this.previous = e.getWhen();
		}
		
	}
}
