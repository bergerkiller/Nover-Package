package com.bergerkiller.bukkit.noverpackage;

import java.lang.reflect.Field;

/**
 * Wraps around the java.lang.reflect.Field class to provide an error-free alternative<br>
 * Exceptions are logged, isValid can be used to check if the Field is actually working
 * 
 * @param <T> type of the Field
 */
class SafeField<T> {
	private Field field;

	public SafeField(Object value, String name) {
		load(value == null ? null : value.getClass(), name);
	}

	public SafeField(Class<?> source, String name) {
		load(source, name);
	}

	private void load(Class<?> source, String name) {
		if (source == null) {
			new Exception("Can not load field '" + name + "' because the class is null!").printStackTrace();
			return;
		}
		// try to find the field
		Class<?> tmp = source;
		while (tmp != null) {
			try {
				this.field = tmp.getDeclaredField(name);
				this.field.setAccessible(true);
				return;
			} catch (NoSuchFieldException ex) {
				tmp = tmp.getSuperclass();
			} catch (SecurityException ex) {
				new Exception("No permission to access field '" + name + "' in class file '" + source.getSimpleName() + "'").printStackTrace();
				return;
			}
		}
		new RuntimeException("Field not found in class '" + source.getName() + "': " + name).printStackTrace();
	}

	public T transfer(Object from, Object to) {
		if (this.field == null) {
			return null;
		}
		T old = get(to);
		set(to, get(from));
		return old;
	}

	@SuppressWarnings("unchecked")
	public T get(Object object) {
		if (this.field == null)
			return null;
		try {
			return (T) this.field.get(object);
		} catch (Throwable t) {
			t.printStackTrace();
			this.field = null;
			return null;
		}
	}

	public boolean set(Object object, T value) {
		if (this.field != null) {
			try {
				this.field.set(object, value);
				return true;
			} catch (Throwable t) {
				t.printStackTrace();
				this.field = null;
			}
		}
		return false;
	}

	/**
	 * Tries to set a Field for a certain Object
	 * 
	 * @param source to set a Field for
	 * @param fieldname to set
	 * @param value to set to
	 * @return True if successful, False if not
	 */
	public static <T> void set(Object source, String fieldname, T value) {
		new SafeField<T>(source, fieldname).set(source, value);
	}

	/**
	 * Tries to set a static Field in a certain Class
	 * 
	 * @param clazz to set the static field in
	 * @param fieldname of the static field
	 * @param value to set to
	 * @return True if successful, False if not
	 */
	public static <T> void setStatic(Class<?> clazz, String fieldname, T value) {
		new SafeField<T>(clazz, fieldname).set(null, value);
	}

	/**
	 * Tries to get a Field from a certain Object
	 * 
	 * @param source to get the Field from
	 * @param fieldname to get
	 * @return The Field value, or null if not possible
	 */
	public static <T> T get(Object source, String fieldname) {
		return new SafeField<T>(source, fieldname).get(source);
	}

	/**
	 * Tries to get a static Field from a class
	 * 
	 * @param clazz to get the field value for
	 * @param fieldname of the field value
	 * @return The Field value, or null if not possible
	 */
	public static <T> T get(Class<?> clazz, String fieldname) {
		return new SafeField<T>(clazz, fieldname).get(null);
	}
}
