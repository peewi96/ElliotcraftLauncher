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

import static org.spoutcraft.launcher.util.ResourceUtils.getResourceAsStream;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.spoutcraft.launcher.api.Event;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.technic.PackInfo;
import org.spoutcraft.launcher.util.DownloadListener;
import org.spoutcraft.launcher.util.Utils;

public abstract class LoginFrame extends JFrame implements DownloadListener {
	private static final long serialVersionUID = 2L;
	public static final URL technicIcon = LoginFrame.class.getResource("/org/spoutcraft/launcher/resources/icon.png");
	protected Map<String, UserPasswordInformation> usernames = new HashMap<String, UserPasswordInformation>();
	protected boolean offline = false;
	private PackInfo pack;

	public LoginFrame() {
		readSavedUsernames();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Elliotcraft Launcher");
		setIconImage(Toolkit.getDefaultToolkit().getImage(technicIcon));
	}

	public final List<String> getSavedUsernames() {
		return new ArrayList<String>(usernames.keySet());
	}

	public final boolean hasSavedPassword(String user) {
		return (usernames.containsKey(user) && usernames.get(user) != null);
	}

	public final String getSavedPassword(String user) {
		UserPasswordInformation pass = usernames.get(user);
		if (!pass.isHash) {
			return pass.password;
		}
		return null;
	}

	public final String getUsername(String account) {
		for (String key : usernames.keySet()) {
			if (key.equalsIgnoreCase(account)) {
				UserPasswordInformation info = usernames.get(key);
				return info.username;
			}
		}
		return account;
	}

	public final boolean removeAccount(String account) {
		Iterator<Entry<String, UserPasswordInformation>>  i = usernames.entrySet().iterator();
		while (i.hasNext()) {
			Entry<String, UserPasswordInformation> e = i.next();
			if (e.getKey().equalsIgnoreCase(account)) {
				i.remove();
				return true;
			}
		}
		return false;
	}

	public final String getAccountName(String username) {
		for (Entry<String, UserPasswordInformation> e: usernames.entrySet()) {
			if (e.getValue().username.equals(username)) {
				return e.getKey();
			}
		}
		return username;
	}

	public final void saveUsername(String user, String pass) {
		if (!hasSavedPassword(user) && pass != null && !Utils.isEmpty(pass)) {
			usernames.put(user, new UserPasswordInformation(pass));
		}
	}

	protected final boolean canPlayOffline() {
		return offline;
	}

	public final void doLogin(String user, PackInfo pack) {
		if (!hasSavedPassword(user)) {
			throw new NullPointerException("There is no saved password for the user '" + user + "'");
		}
		doLogin(user, getSavedPassword(user), pack);
	}

	public final void doLogin(String user, String pass, PackInfo pack) {
		if (pass == null) {
			throw new NullPointerException("The password was null when logging in as user: '" + user + "'");
		}

		this.pack = pack;

		LoginWorker loginThread = new LoginWorker(this);
		loginThread.setUser(user);
		loginThread.setPass(pass);
		loginThread.execute();
	}

	@SuppressWarnings("unused")
	private final void readSavedUsernames() {
		try {
			File lastLogin = new File(Utils.getLauncherDirectory(), "lastlogin");
			if (!lastLogin.exists()) {
				return;
			}
			Cipher cipher = getCipher(2, "passwordfile");

			DataInputStream dis;
			if (cipher != null) {
				dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
			} else {
				dis = new DataInputStream(new FileInputStream(lastLogin));
			}

			try {
				while (true) {
					// Read version
					int version = dis.readInt();
					// Read key
					String key = dis.readUTF();
					// Read user
					String user = dis.readUTF();
					// Read hash
					boolean isHash = dis.readBoolean();
					if (isHash) {
						byte[] hash = new byte[32];
						dis.read(hash);
						usernames.put(key, new UserPasswordInformation(hash));
					} else {
						String pass = dis.readUTF();
						usernames.put(key, new UserPasswordInformation(pass));
					}
					UserPasswordInformation info = usernames.get(key);

					info.username = user;
				}
			} catch (EOFException e) {
			}
			dis.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void writeUsernameList() {
		DataOutputStream dos = null;
		try {
			File lastLogin = new File(Utils.getLauncherDirectory(), "lastlogin");

			Cipher cipher = getCipher(1, "passwordfile");

			if (cipher != null) {
				dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
			} else {
				dos = new DataOutputStream(new FileOutputStream(lastLogin, true));
			}
			for (String user : usernames.keySet()) {
				UserPasswordInformation info = usernames.get(user);
				if (info.username == null) {
					info.username = user;
				}

				// Version
				dos.writeInt(UserPasswordInformation.version);
				// Key
				dos.writeUTF(user);
				// User
				dos.writeUTF(info.username);
				// Password
				dos.writeBoolean(info.isHash);
				if (info.isHash) {
					dos.write(info.passwordHash);
				} else {
					dos.writeUTF(info.password);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) { }
			}
		}
	}

	private final static Cipher getCipher(int mode, String password) throws Exception {
		Random random = new Random(43287234L);
		byte[] salt = new byte[8];
		random.nextBytes(salt);
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

		SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(mode, pbeKey, pbeParamSpec);
		return cipher;
	}

	public abstract JProgressBar getProgressBar();

	public abstract void disableForm();

	public abstract void enableForm();

	public abstract String getSelectedUser();

	public static final Font getMinecraftFont(int size) {
		Font minecraft;
		try {
			minecraft = Font.createFont(Font.TRUETYPE_FONT, getResourceAsStream("/org/spoutcraft/launcher/resources/minecraft.ttf"));
		} catch (Exception e) {
			e.printStackTrace();
			// Fallback
			minecraft = new Font("Verdana", Font.PLAIN, 13);
		}
		return minecraft.deriveFont((float)size);
	}

	public static final Font getClassicFont(int size) {
		Font minecraft;
		//try {
		//	minecraft = Font.createFont(Font.TRUETYPE_FONT, getResourceAsStream("/org/spoutcraft/launcher/resources/avalon.ttf"));
		//} catch (Exception e) {
			//e.printStackTrace();
			// Fallback
			minecraft = new Font("Trebuchet MS", Font.PLAIN, 13);
		//}
		return minecraft.deriveFont((float)size);
	}
	public static final Font getClassicBoldFont(int size) {
		Font minecraft;
		//try {
		//	minecraft = Font.createFont(Font.TRUETYPE_FONT, getResourceAsStream("/org/spoutcraft/launcher/resources/avalonbold.ttf"));
		//} catch (Exception e) {
			//e.printStackTrace();
			// Fallback
			minecraft = new Font("Trebuchet MS", Font.BOLD, 13);
		//}
		return minecraft.deriveFont((float)size);
	}
	public final void handleException(Exception e) {
		e.printStackTrace();
		ErrorDialog dialog = new ErrorDialog(this, e);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}

	public final void onEvent(Event event) {
		switch (event) {
			case GAME_LAUNCH:
				setVisible(false);
				dispose();
				break;
			case SUCESSFUL_LOGIN:
				writeUsernameList();
				Launcher.getGameUpdater().runGame(pack);
				break;
			case BAD_LOGIN:
				JOptionPane.showMessageDialog(getParent(), "Invalid username/password combination");
				enableForm();
				break;
			case ACCOUNT_MIGRATED:
				JOptionPane.showMessageDialog(getParent(), "Your account is a Mojang account, and uses your email address to login, not your game username.\nPlease use your email address to log in.", "Account Migrated", JOptionPane.WARNING_MESSAGE);
				removeAccount(getSelectedUser());
				enableForm();
				break;
			case USER_NOT_PREMIUM:
				JOptionPane.showMessageDialog(getParent(), "You must purchase a Minecraft account to play");
				enableForm();
				break;
			case MINECRAFT_NETWORK_DOWN:
				if (!canPlayOffline()) {
					JOptionPane.showMessageDialog(getParent(), "Unable to authenticate account with minecraft.net");
				} else {
					int result = JOptionPane.showConfirmDialog(getParent(), "Would you like to run in offline mode?", "Unable to connect to minecraft.net", JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						Launcher.getGameUpdater().runGame(pack);
					} else {
						enableForm();
					}
				}
				break;

			case PERMISSION_DENIED:
				JOptionPane.showMessageDialog(getParent(), "Ensure Elliotcraft Launcher is whitelisted with any antivirus applications.", "Permission Denied!", JOptionPane.WARNING_MESSAGE);
				enableForm();
				break;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			showJava15Warning();
		}
	}

	private void showJava15Warning() {
		String version = System.getProperty("java.version");
		if (version.startsWith("1.5")) {
			JLabel label = new JLabel();
			Font arial12 = new Font("Arial", Font.PLAIN, 12);
			label.setFont(arial12);

			StringBuffer style = new StringBuffer("font-family:" + arial12.getFamily() + ";");
			style.append("font-weight:" + (arial12.isBold() ? "bold" : "normal") + ";");
			style.append("font-size:" + arial12.getSize() + "pt;");

			JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">"
					+ "Elliotcraft Launcher requires Java 6 or greater to run, Download"
					+ "<br />java updates from http://spout.in/javaupdates</body></html>");

			ep.setEditable(false);
			ep.setBackground(label.getBackground());

			final Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(technicIcon));
			final String title = "Java 1.6 Required!";
			final String[] options = {"Ok", "Copy URL to clipboard"};

			if (JOptionPane.showOptionDialog(this, ep, title, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, icon, options, options[0]) != 0) {
				StringSelection ss = new StringSelection("http://spout.in/javaupdates");
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			}
			dispose();
			System.exit(0);
		}
	}

	protected static final class UserPasswordInformation {
		public static final int version = 2;
		public String username = null;
		public boolean isHash;
		public byte[] passwordHash = null;
		public String password = null;

		public UserPasswordInformation(String pass) {
			isHash = false;
			password = pass;
		}

		public UserPasswordInformation(byte[] hash) {
			isHash = true;
			passwordHash = hash;
		}
	}
}
