package edu.unc.lib.dl.ui.util;

import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import edu.unc.lib.dl.ui.util.HeaderMenuSettings.HeaderMenu;

public class HeaderMenuSettingsTest extends Assert {

	@Test
	public void loadTest() throws Exception {
		Properties properties = new Properties();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("headerMenu.properties");
		properties.load(inputStream);
		
		HeaderMenuSettings menuSettings = new HeaderMenuSettings();
		menuSettings.setProperties(properties);
		
		assertEquals(3, menuSettings.getHeaderMenuMap().size());
		
		HeaderMenu browseMenu = menuSettings.getHeaderMenuMap().get("browse");
		assertEquals("Browse", browseMenu.getLabel());
		assertEquals("/browse", browseMenu.getUrl());
		assertEquals(2, browseMenu.getSubList().size());
		assertEquals("Browse Departments", browseMenu.getSubListEntry("depts").getLabel());
		assertEquals("Browse Collections", browseMenu.getSubListEntry("collections").getLabel());
		assertEquals("/collections", browseMenu.getSubListEntry("collections").getUrl());
		
		HeaderMenu contactMenu = menuSettings.getHeaderMenuMap().get("about");
		assertEquals(4, browseMenu.getSubList().size());
		
		assertEquals("Browse Departments", browseMenu.getSubListEntry("depts").getLabel());
	}
}
