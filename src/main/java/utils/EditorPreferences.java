package utils;

import java.util.prefs.Preferences;

public class EditorPreferences {
    private static String userDir() {
        return System.getProperty("user.dir");
    }

    public static String getKey(String name, String defaultValue) {
        return GlobalHolder.get(Preferences.class).get(name, defaultValue);
    }

    public static void putKey(String name, String value) {
        GlobalHolder.get(Preferences.class).put(name, value);
    }

    public static String lastPath(String extension) {
        return getKey("lastPath" + extension, userDir());
    }

    public static void setLastPath(String extension, String value) {
        putKey("lastPath" + extension, value);
    }
}
