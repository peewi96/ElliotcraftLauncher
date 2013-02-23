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

package org.spoutcraft.launcher.skin.components;

import org.spoutcraft.launcher.skin.MetroLoginFrame;
import org.spoutcraft.launcher.util.ImageUtils;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import static org.spoutcraft.launcher.util.ResourceUtils.getResourceAsStream;

public class LiteButton extends JButton{
	private static final long serialVersionUID = 1L;
	private boolean clicked = false;

	public LiteButton(String label) {
		this.setText(label);
		this.setBackground(new Color(207, 84, 16));
		this.setBorder(new LiteBorder(5, getBackground()));
	}

	public LiteButton(String label, int x, int y, int w, int h) {
		this.setText(label);
		this.setBounds(x, y, w, h);
		this.setHorizontalTextPosition(SwingConstants.CENTER);
		this.setIcon(MetroLoginFrame.getResizedIcon("button.png", w, h));
		this.setRolloverIcon(MetroLoginFrame.getResizedIcon("button_hover.png", w, h));
	}

}
