package code.Utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.prefs.Preferences;

/**
 * Класс, хранящий информацию о недавних файлах и путях к Android SDK.
 */
public class Keeper {
    /** Длина списка недавних файлов */
    private final static int recentLength = 4;
    /** Переменная, хранящая настройки */
    private static Preferences prefs = Preferences.userNodeForPackage(Keeper.class);

    /**
     * Удаление информации о пути к Android SDK.
     */
    public static void dropSDKLocation() {
        prefs.put("SDK", "value");
    }

    /**
     * Добавление информации о пути к Android SDK.
     * @param name Путь к Android SDK
     */
    public static void pushSDKLocation(String name) {
        prefs.put("SDK", name);
    }

    /**
     * Получение информации о пути к Android SDK.
     * @return путь к Android SDK.
     */
    public static String getSDKLocation() {
        String value = "value";
        String result = prefs.get("SDK", value);
        return !result.equals("value") ? result : null;
    }

    /**
     * Добавление нового файла к списку недавних файлов.
     * @param name Имя файла
     */
    public static void push(String name)
    {
        Random random = new Random();
        if (!isAdded(name)) {
            int key = random.nextInt(recentLength);
            prefs.put(key + "", name);
        }
    }

    /**
     * Проверка: был ли данный файл уже добавлен к списку.
     * @param name Имя файла
     * @return результат работы.
     */
    private static boolean isAdded(String name)
    {
        boolean result = false;
        String tmp = "value";
        for (int i = 0; i < recentLength; i++)
            if (prefs.get(i + "", tmp).equals(name))
            {
                result = true;
                break;
            }
        return result;
    }

    /**
     * Получение списка всех недавних файлов в виде элементов класса JMenuItem.
     * @return список недавних файлов.
     */
    public static ArrayList<JMenuItem> getRecentFilesAsItems()
    {
        String value = "value";
        ArrayList<JMenuItem> items = new ArrayList<JMenuItem>();
        for (int i = 0; i < recentLength; i++)
            if (!prefs.get(i + "", value).equals("value"))
                items.add(new JMenuItem(prefs.get(i + "", value)));
        return items;
    }
}
