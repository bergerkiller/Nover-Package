package com.bergerkiller.bukkit.noverpackage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wraps around the java.lang.reflect.Method class to provide an error-free alternative<br>
 * Exceptions are logged, isValid can be used to check if the Field is actually working
 */
class SafeMethod<T> {
	private Method method;

	public SafeMethod(Object value, String name, Class<?>... parameterTypes) {
		load(value == null ? null : value.getClass(), name, parameterTypes);
	}

	public SafeMethod(Class<?> source, String name, Class<?>... parameterTypes) {
		load(source, name, parameterTypes);
	}

	private void load(Class<?> source, String name, Class<?>... parameterTypes) {
		if (source == null) {
			new Exception("Can not load method '" + name + "' because the class is null!").printStackTrace();
			return;
		}
		// try to find the field
		Class<?> tmp = source;
		while (tmp != null) {
			try {
				this.method = tmp.getDeclaredMethod(name, parameterTypes);
				this.method.setAccessible(true);
				return;
			} catch (NoSuchMethodException ex) {
				tmp = tmp.getSuperclass();
			} catch (SecurityException ex) {
				new Exception("No permission to access method '" + name + "' in class file '" + source.getSimpleName() + "'").printStackTrace();
				return;
			}
		}
		name += "(";
		for (int i = 0; i < parameterTypes.length; i++) {
			if (i > 0) {
				name += ", ";
			}
			name += parameterTypes[i].getSimpleName();
		}
		name += ")";
		new RuntimeException("Method not found in class '" + source.getName() + "': " + name).printStackTrace();
	}

	@SuppressWarnings("unchecked")
	public T invoke(Object instance, Object... args) {
		if (this.method != null) {
			try {
				return (T) this.method.invoke(instance, args);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
