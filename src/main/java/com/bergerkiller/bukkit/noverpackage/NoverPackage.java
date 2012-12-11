package com.bergerkiller.bukkit.noverpackage;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class file to interact with Nover Package
 */
public class NoverPackage extends JavaPlugin {
	/**
	 * The Minecraft version the current server software is built against<br>
	 * Check against this version to provide safety for future updates that could
	 * cause your code to break<br><br>
	 * 
	 * This variable is an empty String if no version could be detected (no package versioning used)
	 */
	public static final String MC_VERSION;
	static {
		MC_VERSION = NoverClassLoader.MC_VERSION;
		NoverLoaderHook.loadClass();
		undoPackageVersioning(NoverPackage.class);
		NoverLoaderHook.init();
	}

	/**
	 * Applies the NoverClassLoader to the plugin class loader chain to get around package versioning
	 * 
	 * @param pluginClass - main plugin class of your plugin
	 */
	public static void undoPackageVersioning(Class<?> pluginClass) {
		undoPackageVersioning(pluginClass.getClassLoader());
	}

	/**
	 * Applies the NoverClassLoader to the plugin class loader chain to get around package versioning
	 * 
	 * @param loader - main plugin classloader of your plugin
	 */
	public static void undoPackageVersioning(ClassLoader loader) {
		NoverClassLoader.undoPackageVersioning(loader);
	}
}
