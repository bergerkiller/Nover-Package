package com.bergerkiller.bukkit.noverpackage;

import java.io.IOException;
import java.io.InputStream;

import com.bergerkiller.bukkit.noverpackage.org.objectweb.asm.ClassReader;
import com.bergerkiller.bukkit.noverpackage.org.objectweb.asm.ClassWriter;
import com.bergerkiller.bukkit.noverpackage.org.objectweb.asm.commons.Remapper;
import com.bergerkiller.bukkit.noverpackage.org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * Re-maps class references from non-versioned packages to the correct versioned package<br><br>
 */
class NoverRemapper extends Remapper {
	private static final String[] PACKAGE_ROOTS = {"net/minecraft/server/", "org/bukkit/craftbukkit/"};

	@Override
	public String mapDesc(String desc) {
		return filter(desc);
	}

	@Override
	public String map(String typeName) {
		return filter(typeName);
	}

	private static String filter(String text) {
		int idx;
		for (String packageRoot : PACKAGE_ROOTS) {
			if ((idx = text.indexOf(packageRoot)) != -1) {
				return convert(text, packageRoot, idx);
			}
		}
		return text;
	}

	private static String convert(String text, String packagePath, int startIndex) {
		String name = text.substring(startIndex + packagePath.length());
		String header = text.substring(0, startIndex);
		if (name.startsWith("v")) {
			int firstidx = name.indexOf('_');
			if (firstidx != -1) {
				// Check if the major version is a valid number
				String major = name.substring(0, firstidx);
				try {
					Integer.parseInt(major);
					// Major test success
					int end = name.indexOf('/');
					if (end != -1) {
						// Get rid of the version (removes 'v1_4_5.')
						name = name.substring(end + 1);
					}
				} catch (NumberFormatException ex) {
					// Major test fail
				}
			}
		}
		if (NoverClassLoader.MC_VERSION.isEmpty()) {
			return header + packagePath + name;
		} else {
			return header + packagePath + NoverClassLoader.MC_VERSION + '/' + name;
		}
	}

	public static byte[] remap(InputStream stream) throws IOException {
		ClassReader classReader = new ClassReader(stream);
		ClassWriter classWriter = new ClassWriter(classReader, 0);
		Remapper remapper = new NoverRemapper();
		classReader.accept(new RemappingClassAdapter(classWriter, remapper), ClassReader.EXPAND_FRAMES);
		return classWriter.toByteArray();
	}
}
