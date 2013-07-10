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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.spoutcraft.launcher.skin.TechnicLoginFrame;

import static org.spoutcraft.launcher.util.TextSource.lang;

public class PackButton extends JXButton {
	private static final long serialVersionUID = 1L;
	private int index;
	private JLabel label;

	public PackButton() {
		super();
		label = new JLabel(lang("modpackbuttton.loading"));
		label.setFont(TechnicLoginFrame.getMinecraftFont(12));
		label.setForeground(Color.WHITE);
		label.setBackground(new Color(35, 35, 35));
		label.setOpaque(true);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		DropShadowBorder shadow = new DropShadowBorder();
		shadow.setShadowColor(Color.BLACK);
        shadow.setShowTopShadow(true);
        shadow.setShowRightShadow(true);
        shadow.setShowBottomShadow(true);
        shadow.setShowLeftShadow(true);
        shadow.setShadowOpacity(0.6F);
        shadow.setShadowSize(7);
		this.setBorder(shadow);
        this.setOpaque(false);
        this.setContentAreaFilled(false);
        this.setFocusable(false);
	}

	@Override
	public void setIcon(Icon defaultIcon) {
		super.setIcon(defaultIcon);
		if (index == 0) {
			this.setSelectedIcon(defaultIcon);
			this.setRolloverIcon(defaultIcon);
			this.setPressedIcon(defaultIcon);
		}
	}

	public JLabel getJLabel() {
		return label;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
