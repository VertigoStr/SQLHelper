package code.Utils;

import code.Controller.JMenuController;
import code.DataBaseHelper.DBHelper;
import code.DataBaseHelper.DeviceDBHelper;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Класс проверки подключений.
 */
public class Utils {

    /**
     * Проверка типа операционной системы.
     * @return результат работы.
     */
    public static  boolean isWin() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Проверка путей к Android SDK.
     * @param main Родительский контейнер
     * @param defaultFolderName Имя папки по умолчанию
     * @return результат работы.
     */
    public static boolean checkSDK(JFrame main, String defaultFolderName) {
        boolean flag = false;
        if (Utils.checkSDK(defaultFolderName))
            flag = true;
        else {
            JOptionPane.showMessageDialog(main, "Can\'t find adb location!", "Error", JOptionPane.ERROR_MESSAGE);
            String sdk;
            if (!(sdk = JMenuController.selectDirectory(main)).isEmpty() && Utils.checkADB(sdk + (sdk.contains(":") ? "\\" : "/"))) {
                Keeper.pushSDKLocation(sdk + (sdk.contains(":") ? "\\" : "/"));
                flag = true;
            }
            else
                JOptionPane.showMessageDialog(main, "Fatal error: can\'t find adb location!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return flag;
    }

    /**
     * Проверка наличия утилиты sqlite3 на мобильном устройстве.
     * @param dialog Родительский контейнер
     * @param deviceName Имя устройства
     * @return результат работы.
     */
    public static boolean isSQLite3Available(JDialog dialog, String deviceName) {
        DeviceDBHelper dDB = new DeviceDBHelper(Keeper.getSDKLocation());
        boolean flag = false;
        if (dDB.isSQLite3Available(deviceName)) {
            flag = true;
        }
        else {
            JOptionPane.showMessageDialog(dialog, "SQLite3 not find on device: " + deviceName, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return flag;
    }

    /**
     * Проверка наличия утилиты sqlite3 на мобильном устройстве.
     * @param frame Родительский контейнер
     * @param deviceName Имя устройства
     * @return результат работы.
     */
    public static boolean isSQLite3Available(JFrame frame, String deviceName) {
        DeviceDBHelper dDB = new DeviceDBHelper(Keeper.getSDKLocation());
        boolean flag = false;
        if (dDB.isSQLite3Available(deviceName)) {
            flag = true;
        }
        else {
            JOptionPane.showMessageDialog(frame, "SQLite3 not find on device: " + deviceName, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return flag;
    }

    /**
     * Проверка: доступна ли выбранная база данных.
     * @param frame Родительский контейнер
     * @param dataBaseName Имя базы данных
     * @return результат работы.
     */
    public static boolean isExist(JFrame frame, String dataBaseName) {
        boolean flag = true;
        String[] params = dataBaseName.split("\t");
        if (params.length != 2) {
            if (!new DBHelper(dataBaseName).connect()) {
                JOptionPane.showMessageDialog(frame, dataBaseName + " does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
                flag = false;
            }
        } else {
            DeviceDBHelper dDB = new DeviceDBHelper(Keeper.getSDKLocation());
            flag = dDB.isDeviceAvailable(params[0]);
            if (flag) {
                flag = isSQLite3Available(frame, params[0]);
            }
            if (flag) {
                flag = dDB.isDataBaseAvailable(params[1], params[0]);
            }
        }
        return flag;
    }

    /**
     * Получение пути к Android SDK из файла local.properties.
     * @param file Путь к файлу
     * @return путь к Android SDK.
     */
    public static String getSDKLocation(String file) {
        String string = "";
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line=br.readLine())!=null){
                if (line.contains("sdk.dir=")) {
                    System.out.println(line);
                    string += line;
                }
            }
            br.close();
        }
        catch (Exception e){
            System.out.println("Get sdk location error: " + e.toString());
        }
        return string;
    }

    /**
     * Проверка пути к Android SDK, хранящийся в классе Keeper.
     * @return результат работы.
     * @see code.Utils.Keeper
     */
    public static boolean checkSDK() {
        boolean flag = false;
        String sdk;
        if ((sdk = Keeper.getSDKLocation()) != null && new DeviceDBHelper(sdk).checkADBConnection()) {
            flag = true;
        }
        return flag;
    }

    /**
     * Проврека подключения к Android Debug Bridge.
     * @param sdk String
     * @return результат работы.
     */
    public static boolean checkADB(String sdk) {
        return new DeviceDBHelper(sdk).checkADBConnection();
    }

    /**
     * Получение пути к Android SDK из файла local.properties.
     * @param file Путь к файлу
     * @return путь к Android SDK.
     */
    public static boolean checkSDK(String file) {
        boolean flag;
        String sdk;
        if (checkSDK()) {
            flag = true;
        }
        else {
            sdk = Utils.getSDKLocation(file.replace("\\", "/") + "/local.properties");
            //sdk + (sdk.contains(":") ? sdk + "\\" : "/"))
            Keeper.pushSDKLocation(sdk.replace("\\\\", "\\").replace("\\:", ":").replace("sdk.dir=", "") + "\\platform-tools\\");
            flag = checkSDK();
        }
        return flag;
    }
}
