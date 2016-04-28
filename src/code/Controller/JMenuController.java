package code.Controller;

import code.GUI.FileExplorer;
import code.Utils.Regex;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * Контроллер для работы с меню.
 */
public class JMenuController {

    /**
     * Открыть базу данных с устройства.
     * @param parent Родительский контейнер
     * @return путь к базе данных.
     */
    public static String openFromDevice(JFrame parent) {
        return new FileExplorer(parent).getResult();
    }

    /**
     * Создать базу данных на компьютере.
     * @param parent Родительский контейнер
     * @param defaultFolderName Папка для создания по умолчанию
     * @return путь к базе данных.
     */
    public static String createOnDevice(JFrame parent, String defaultFolderName) {
        return new FileExplorer(parent, defaultFolderName, "Create").getResult();
    }

    /**
     * Перемещение базы данных на устройство.
     * @param parent Родительский контейнер
     * @param dbName Имя базы данных
     */
    public static void pushOnDevice(JFrame parent, String dbName) {
        if (dbName.contains("|"))
            JOptionPane.showMessageDialog(parent, "Database was opened on device. Get another database", "Warning", JOptionPane.WARNING_MESSAGE);
        else
            new FileExplorer(parent, dbName, "Push");
    }

    /**
     * Копирование базы данных с устройства.
     * @param parent Родительский контейнер
     * @param defaultFolderName Папка для копирования по умолчанию
     * @return путь к базе данных.
     */
    public static String pullFromDevice(JFrame parent, String defaultFolderName) {
        return new FileExplorer(parent, defaultFolderName, "Pull").getResult();
    }

    /**
     * Открытие базы данных на компьютере.
     * @param parent Родительский контейнер
     * @return путь к базе данных.
     */
    public static String openListener(JFrame parent)
    {
        String result = "";
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("SQLite3 DB", "s3db", "db");
        fileChooser.addChoosableFileFilter(filter);
        int ret = fileChooser.showDialog(parent, "Open  ");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            result = file.getPath();
        }
        return Regex.getMatches(".db|.s3db", result) > 0 ? result : null;
    }

    /**
     * Создание базы данных на компьютере.
     * @param parent Родительский контейнер
     * @return путь к базе данных.
     */
    public static String createListener(JFrame parent)
    {
        String result = "";
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("SQLite3 DB", "s3db", "db");
        fileChooser.addChoosableFileFilter(filter);
        int ret = fileChooser.showDialog(parent, "Create");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            result = file.getPath();
        }
        result = result.length() > 2 ?
                (result.substring(result.length() - 2, result.length()).contains("db") ?
                        result : result.replaceAll("\\.", "") + ".db"
                )
                : result;
        System.out.println(result);
        return Regex.getMatches(".db|.s3db", result) > 0 ? result : null;
    }

    /**
     * Выбор папки.
     * @param parent Родительский контейнер
     * @return путь к папке.
     */
    public static String selectDirectory(JDialog parent) {
        String result = "";
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = fileChooser.showDialog(parent, "Select");
        if (ret == JFileChooser.APPROVE_OPTION)
            result = fileChooser.getSelectedFile().getPath();

        return result;

    }

    /**
     * Выбор папки.
     * @param parent Родительский контейнер
     * @return путь к папке.
     */
    public static String selectDirectory(JFrame parent) {
        String result = "";
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = fileChooser.showDialog(parent, "Select");
        if (ret == JFileChooser.APPROVE_OPTION)
            result = fileChooser.getSelectedFile().getPath();

        return result;

    }
}
