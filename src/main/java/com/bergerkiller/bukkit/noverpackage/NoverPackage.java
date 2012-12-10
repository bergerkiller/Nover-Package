package com.bergerkiller.bukkit.noverpackage;

import org.bukkit.plugin.java.JavaPlugin;

public class NoverPackage extends JavaPlugin {
	static {
		undoPackageVersioning(NoverPackage.class);
	}

	/**
	 * Applies the NoverClassLoader to the plugin class loader chain to get around package version
	 * 
	 * @param pluginClass - main plugin class of your plugin
	 */
	public static void undoPackageVersioning(Class<?> pluginClass) {
		undoPackageVersioning(pluginClass.getClassLoader());
	}

	/**
	 * Applies the NoverClassLoader to the plugin class loader chain to get around package version
	 * 
	 * @param loader - main plugin classloader of your plugin
	 */
	public static void undoPackageVersioning(ClassLoader loader) {
		NoverClassLoader.undoPackageVersioning(loader);
	}
}
