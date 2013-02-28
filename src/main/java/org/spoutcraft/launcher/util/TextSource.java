/*
 * TextSource.java
 *
 *  created: 5.8.2011
 *  charset: UTF-8
 *  license: MIT (X11) (See LICENSE file for full license)
 */
package org.spoutcraft.launcher.util;

import org.spoutcraft.launcher.Settings;

import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class TextSource {
	public static final ResourceBundle MAIN = PropertyResourceBundle.getBundle(Settings.LANGUAGE_BUNDLE, getLocaleFromString(Settings.getLanguage()) );

	public static String lang(String s) {
		return MAIN.getString(s);
	}

	public static String lang(String s, String l) {
	 	ResourceBundle MAIN = PropertyResourceBundle.getBundle(Settings.LANGUAGE_BUNDLE, getLocaleFromString(l));
		return MAIN.getString(s);
	}

	public static Locale getLocaleFromString(String localeString)
	{
		if (localeString == null)
		{
			return null;
		}
		localeString = localeString.trim();
		if (localeString.toLowerCase().equals("default"))
		{
			return Locale.getDefault();
		}

		// Extract language
		int languageIndex = localeString.indexOf('_');
		String language = null;
		if (languageIndex == -1)
		{
			// No further "_" so is "{language}" only
			return new Locale(localeString, "");
		}
		else
		{
			language = localeString.substring(0, languageIndex);
		}

		// Extract country
		int countryIndex = localeString.indexOf('_', languageIndex + 1);
		String country = null;
		if (countryIndex == -1)
		{
			// No further "_" so is "{language}_{country}"
			country = localeString.substring(languageIndex+1);
			return new Locale(language, country);
		}
		else
		{
			// Assume all remaining is the variant so is "{language}_{country}_{variant}"
			country = localeString.substring(languageIndex+1, countryIndex);
			String variant = localeString.substring(countryIndex+1);
			return new Locale(language, country, variant);
		}
	}

}