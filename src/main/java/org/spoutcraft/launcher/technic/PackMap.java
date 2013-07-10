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
package org.spoutcraft.launcher.technic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.StartupParameters;
import org.spoutcraft.launcher.api.Launcher;
import org.spoutcraft.launcher.rest.RestAPI;
import org.spoutcraft.launcher.technic.skin.ModpackSelector;

public class PackMap extends HashMap<String, PackInfo> {
	private static final long serialVersionUID = 1L;

	private final List<String> byIndex = new ArrayList<String>(0);

	private String selected = null;
	private int selectedIndex = 0;

	public PackInfo select(int index) {
		String name = byIndex.get(index);
		PackInfo info = null;
		if (name != null) {
			info = this.get(name);
		}
		if (info != null) {
			selected = name;
			this.selectedIndex = index;
		}
		return info;
	}

	public PackInfo select(String name) {
		PackInfo info = get(name);
		if (info != null) {
			selected = name;
			this.selectedIndex = byIndex.indexOf(name);
		}
		return info;
	}

	public PackInfo getSelected() {
		if (selected == null) {
			select(0);
		}
		return get(selected);
	}

	public int getIndex() {
		return selectedIndex;
	}

	public PackInfo getNext(int offset) {
		return get(this.selectedIndex + offset);
	}

	public PackInfo getPrevious(int offset) {
		return get(this.selectedIndex - offset);
	}

	public PackInfo addNew(PackInfo pack) {
		PackInfo info = super.put(pack.getName(), pack);
		if (info == null) {
			int loc = byIndex.size() - 1;
			byIndex.add(loc, pack.getName());
			select(loc);
		}
		return info;
	}

	public PackInfo add(PackInfo pack) {
		return this.put(pack.getName(), pack);
	}

	public void reorder(int index, String pack) {
		if (byIndex.remove(pack)) {
			byIndex.add(index, pack);
		}
	}

	@Override
	public PackInfo put(String key, PackInfo value) {
		PackInfo info = super.put(key, value);
		if (info == null) {
			byIndex.add(key);
		}
		return info;
	}

	@Override
	public PackInfo remove(Object key) {
		PackInfo info = super.remove(key);
		if (info != null) {
			byIndex.remove(key);
		}
		return info;
	}

	public PackInfo get(int index) {
		if (index >= byIndex.size()) {
			return this.get(index - byIndex.size());
		} else if (index < 0) {
			return this.get(byIndex.size() + index);
		}

		String pack = null;
		try {
			pack = byIndex.get(index);
		} catch (Exception e) {
			e.printStackTrace();
		}

		PackInfo info = null;
		if (pack != null) {
			info = get(pack);
		}

		return info;
	}

	public int getPackIndex(String pack) {
		return byIndex.indexOf(pack);
	}

	public void loadPack(String pack) {
		OfflineInfo offline = new OfflineInfo(pack);
		add(offline);
		PackThread thread = new PackThread(this, pack);
		thread.start();
	}

	public void initPacks() {
		String lastPack = Settings.getLastModpack();
		
		if (!Settings.getInstalledPacks().contains(lastPack)) {
			lastPack = ModpackSelector.DEFAULT_PACK;
		}

		loadDefaults(lastPack);

		for (String pack : Settings.getInstalledPacks()) {
			// Skip non custom packs
			if (!Settings.isPackCustom(pack)) {
				continue;
			}
			loadPack(pack);
			if (pack.equals(lastPack)) {
				select(pack);
			}
		}
		// Add in the add pack button when -custompack parameter or when offline first launch
		if (StartupParameters.isCustomPack() || (RestAPI.getDefaults().isEmpty() && Settings.getInstalledPacks().isEmpty())) {
			put("addpack", new AddPack());
		}
	}

	private void loadDefaults(final String lastPack) {
		for (String pack : Settings.getInstalledPacks()) {
			// Skip custom packs
			if (Settings.isPackCustom(pack)) {
				continue;
			}
			OfflineInfo offline = new OfflineInfo(pack);
			if (!offline.isHidden()) {
				add(offline);
			}
		}

		Thread thread = new Thread("Default Pack Thread") {
			@Override
			public void run() {
				int index = 0;
				for (PackInfo pack : RestAPI.getDefaults()) {
					pack.setRest(RestAPI.getDefault());
					if (pack.isHidden()) {
						// Do not add pack when hidden from solder
						System.out.println("Pack "+pack.getDisplayName()+" is hidden. Hiding from Launcher");
						continue;
					}
					add(pack);
					reorder(index, pack.getName());
					index++;
					if (pack.getName().equals(lastPack)) {
						select(pack.getName());
					}
				}
				Launcher.getFrame().getSelector().redraw(false);
			}
		};
		thread.start();
	}
}
