package utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Singletons is an useful template but it does have one big issue: it doesnn't
 * comply with the Single Responsability Principle as it both holds an instance
 * and manages its lifetime.
 * 
 * This class only offers a way to store and access a global instance as long as
 * the using code manages the lifetime of said global
 * 
 * @author Florian Dormont
 *
 */
public class GlobalHolder {
	static private Map<String, Object> globals = null;

	private static void lazyInstanciation() {
		if (globals == null)
			globals = new HashMap<String, Object>();
	}

	public static void set(Object object) {
		lazyInstanciation();
		globals.put(object.getClass().getCanonicalName(), object);
	}

	@SuppressWarnings("unchecked")
	public static <C> C get(Class<?> cls) {
		lazyInstanciation();
		return (C) globals.get(cls.getCanonicalName());
	}

	public static <C> C release(Class<?> cls) {
		lazyInstanciation();
		C c = get(cls);
		globals.put(c.getClass().getCanonicalName(), null);
		return c;
	}

}
