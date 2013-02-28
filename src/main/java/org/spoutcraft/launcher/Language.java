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

package org.spoutcraft.launcher;

public final class Language {
	public static final Language[] languageOptions = {
		(new Language("cs_CZ", "Česky")),
		(new Language("en_US", "English")),
		(new Language("sk_SK", "Slovensky")),
	};

	public static final Language DEFAULT_LANG = languageOptions[1];

	String language;
	String text;

	private Language(String language, String text) {
		this.language = language;
		this.text = text;
	}

	public String getLanguage() {
		return language;
	}

	public String getDescription() {
		return text;
	}

	public static String getLanguage(String lang) {
		for (Language m : languageOptions) {
			if (m.getLanguage().equals(lang)) {
				return m.getLanguage();
			}
		}
		return DEFAULT_LANG.getLanguage();
	}

	public static String getLanguageName(String id) {
		for (Language m : languageOptions) {
			if (m.getLanguage().equals(id)) {
				return m.getDescription();
			}
		}
		return DEFAULT_LANG.getDescription();
	}

	public static Language getLanguageFromId(String id) {
		for (Language m : languageOptions) {
			if (m.getLanguage() == id) {
				return m;
			}
		}
		return DEFAULT_LANG;
	}

	/*public static int getLanguageIndex(int id) {
		for (int i = 0; i < languageOptions.length; i++) {
			if (languageOptions[i].getLanguage() == id) {
				return i;
			}
		}
		return id;
	}   */
}
