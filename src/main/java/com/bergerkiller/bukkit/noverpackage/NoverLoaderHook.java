package com.bergerkiller.bukkit.noverpackage;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

/**
 * Hooks into the Java plugin manager to automatically convert plugin class loaders
 */
class NoverLoaderHook {
	private static final SafeField<String> classLoaderOf = new SafeField<String>(PluginDescriptionFile.class, "classLoaderOf");
	private static final SafeField<Map<Pattern, PluginLoader>> fileAssociations = new SafeField<Map<Pattern, PluginLoader>>(SimplePluginManager.class, "fileAssociations");
	private static final SafeField<Map<String, PluginClassLoader>> loaders = new SafeField<Map<String, PluginClassLoader>>(JavaPluginLoader.class, "loaders");
	private static final String NOVER_IDENTIFIER = "noverpackage";
	private static final NoverJavaPluginLoader newLoader = new NoverJavaPluginLoader(Bukkit.getServer());
	private static final ClassLoader parentLoader = NoverLoaderHook.class.getClassLoader().getParent();
	private static File file;
	private static boolean noverEnabledAll = false;
	private static Set<String> noverEnabledPlugins = new HashSet<String>();

	static void loadClass() {
		NoverLoaderMapListener.class.getName();
		NoverJavaPluginLoader.class.getName();
	}

	private static void loadConfig(File file) {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			YamlConfiguration config = new YamlConfiguration();
			config.load(file);
			// ======================
			if (!config.contains("enabledInAllPlugins") || !config.isBoolean("enabledInAllPlugins")) {
				config.set("enabledInAllPlugins", false);
			}
			if (!config.contains("enabledPlugins") || !config.isList("enabledPlugins")) {
				config.set("enabledPlugins", Arrays.asList("SomePlugin1", "SomePlugin2", "SomePlugin3"));
			}
			noverEnabledAll = config.getBoolean("enabledInAllPlugins");
			if (!noverEnabledAll) {
				for (Object o : config.getList("enabledPlugins")) {
					noverEnabledPlugins.add(o.toString());
				}
			}
			// ======================
			config.save(file);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void init() {
		// Load config
		String folderName = Bukkit.getUpdateFolderFile().toString();
		int folderEndIdx = folderName.indexOf(File.separator);
		if (folderEndIdx != -1) {
			folderName = folderName.substring(0, folderEndIdx);
		}
		folderName += File.separator + "Nover Package";
		loadConfig(new File(folderName, "config.yml"));
		// Register new loader
		Map<Pattern, PluginLoader> fileAssoc = fileAssociations.get(Bukkit.getPluginManager());
		JavaPluginLoader oldLoader = null;
		for (Map.Entry<Pattern, PluginLoader> e : fileAssoc.entrySet()) {
			if (e.getValue().getClass().equals(JavaPluginLoader.class)) {
				oldLoader = (JavaPluginLoader) e.getValue();
				e.setValue(newLoader);
			}
		}

		// Replace classes mapping
		Map<String, PluginClassLoader> oldMap = loaders.get(oldLoader);
		LinkedHashMap<String, PluginClassLoader> newMap = new NoverLoaderMapListener();
		newMap.putAll(oldMap);
		loaders.set(newLoader, newMap);
	}

	private static boolean isPluginEnabled(String pluginName) {
		return noverEnabledAll || noverEnabledPlugins.contains(pluginName);
	}

	private static class NoverLoaderMapListener extends LinkedHashMap<String, PluginClassLoader> {
		private static final long serialVersionUID = 1L;

		@Override
		public PluginClassLoader get(Object key) {
			if (key.toString().startsWith(NOVER_IDENTIFIER)) {
				try {
					String real = key.toString().substring(NOVER_IDENTIFIER.length());
					PluginClassLoader loader;
					if (real.isEmpty()) {
						loader = new PluginClassLoader(newLoader, new URL[] { file.toURI().toURL() }, parentLoader);
					} else {
						loader = super.get(real);
					}
					if (loader != null) {
						NoverClassLoader.undoPackageVersioning(loader);
					}
					return loader;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			return super.get(key);
		}
	}
	
	/**
	 * Replaces the java plugin loader to alter the class loader setting of the plugin automatically
	 */
	private static class NoverJavaPluginLoader extends JavaPluginLoader {

		public NoverJavaPluginLoader(Server instance) {
			super(instance);
		}

		@Override
		public PluginDescriptionFile getPluginDescription(File arg0) throws InvalidDescriptionException {
			PluginDescriptionFile dfile = super.getPluginDescription(arg0);
			if (isPluginEnabled(dfile.getName()) || dfile.getDepend().contains("NoverPackage")) {
				// Plugin is enabled by Nover, replace the class loader so it is detected
				String old = classLoaderOf.get(dfile);
				if (old == null) {
					old = "";
				}
				file = arg0;
				classLoaderOf.set(dfile, NOVER_IDENTIFIER + old);
			}
			return dfile;
		}
	}
}
