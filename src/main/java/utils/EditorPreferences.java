package utils;

import java.util.prefs.Preferences;

public class EditorPreferences {
    private static String userDir() {
        return System.getProperty("user.dir");
    }

    private static String get(String name, String defaultValue) {
        return GlobalHolder.get(Preferences.class).get(name, defaultValue);
    }

    private static void put(String name, String value) {
        GlobalHolder.get(Preferences.class).put(name, value);
    }

    public static String lastPath(String extension) {
        return get("lastPath" + extension, userDir());
    }

    public static void setLastPath(String extension, String value) {
        put("lastPath" + extension, value);
    }
}
