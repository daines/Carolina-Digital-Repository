package edu.unc.lib.dl.ui.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderMenuSettings {
	public static Pattern entryMatcher = Pattern.compile("^menu\\.(\\w+)(\\.(label|url|entry|order))?(\\.(\\w+).(label|url))?$");

	private HeaderMenu menuRoot;
	//private Map<String, HeaderMenu> headerMenuMap;
	
	public HeaderMenuSettings() {
		//this.headerMenuMap = new HashMap<String, HeaderMenu>();
		this.menuRoot = new HeaderMenu("root");
	}

	public void setProperties(Properties properties) {
		Iterator<Entry<Object, Object>> propertiesIt = properties.entrySet().iterator();
		while (propertiesIt.hasNext()) {
			Entry<Object, Object> property = propertiesIt.next();
			String key = (String) property.getKey();

			Matcher keyMatcher = entryMatcher.matcher(key);
			if (keyMatcher.find()) {
				String menuName = keyMatcher.group(1);
				HeaderMenu headerMenu = this.menuRoot.subMenus.get(menuName);
				if (headerMenu == null) {
					headerMenu = new HeaderMenu(menuName);
					this.menuRoot.subMenus.put(menuName, headerMenu);
				}
				
				HeaderMenuEntry menuEntry;
				String menuType = keyMatcher.group(2);
				if ("entry".equals(menuType) && keyMatcher.groupCount() > 2) {
					String subMenuName = keyMatcher.group(4);
					menuEntry = headerMenu.getSubListEntry(subMenuName);
					if (menuEntry == null)
						menuEntry = headerMenu.addSubListEntry(subMenuName);
					menuType = keyMatcher.group(5);
				} else {
					menuEntry = headerMenu;
				}
				
				if ("label".equals(menuType)) {
					menuEntry.setLabel((String)property.getValue());
				} else {
					menuEntry.setUrl((String)property.getValue());
				}
			}
		}
	}

	public HeaderMenu getMenuRoot() {
		return menuRoot;
	}

	public static class HeaderMenu extends HeaderMenuEntry {
		private Map<String, HeaderMenu> subMenus;
		private String order;
		
		public HeaderMenu(String key) {
			super(key);
			this.subMenus = new LinkedHashMap<String, HeaderMenu>();
		}
		
		public Map<String, HeaderMenu> getSubMenus() {
			return subMenus;
		}

		public void setSubMenus(Map<String, HeaderMenu> subMenus) {
			this.subMenus = subMenus;
		}

		public String getOrder() {
			return order;
		}

		public void setOrder(String order) {
			this.order = order;
		}
	}

	public static class HeaderMenuEntry {
		private String key;
		private String label;
		private String url;

		public HeaderMenuEntry(String key) {
			this.key = key;
		}
		
		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
		
		public String getKey() {
			return this.key;
		}
	}
}
