package utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Singletons is an useful template but it does have one big issue: it doesnn't
 * comply with the Single Responsibility Principle as it both holds an instance
 * and manages its lifetime.
 * 
 * This class only offers a way to store and access a global instance as long as
 * the using code manages the lifetime of said global.
 * Allows for using a kind-of namespace and class override,
 * providing the overriding class is a child of the given object.
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

	public static <C> void set(C object, Class<C> cls) {
		lazyInstanciation();
		globals.put(cls.getCanonicalName(), object);
	}
	
	public static <C> void set(C object, Class<C> cls, String namespace) {
		lazyInstanciation();
		globals.put(namespace + "::" + cls.getCanonicalName(), object);
	}
	
	public static void set(Object object, String namespace) {
		lazyInstanciation();
		globals.put(namespace + "::" + object.getClass().getCanonicalName(), object);
	}

	@SuppressWarnings("unchecked")
	public static <C> C get(Class<C> cls) {
		lazyInstanciation();
		return (C) globals.get(cls.getCanonicalName());
	}

	@SuppressWarnings("unchecked")
	public static <C> C get(Class<C> cls, String namespace) {
		lazyInstanciation();
		return (C) globals.get(namespace + "::" + cls.getCanonicalName());
	}

	public static <C> C release(Class<C> cls) {
		lazyInstanciation();
		C c = get(cls);
		globals.put(c.getClass().getCanonicalName(), null);
		return c;
	}

}
