/*
 * TextSource.java
 *
 *  created: 5.8.2011
 *  charset: UTF-8
 *  license: MIT (X11) (See LICENSE file for full license)
 */
package org.spoutcraft.launcher.util;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


public class TextSource {
	public static final ResourceBundle MAIN =
			PropertyResourceBundle.getBundle(
					"org.spoutcraft.launcher.resources.lang.Main",
					Locale.getDefault() );

	public static String lang(String s) {
		return MAIN.getString(s);
	}
}   // TextSource
