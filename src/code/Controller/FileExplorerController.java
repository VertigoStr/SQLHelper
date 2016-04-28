package code.Controller;

import code.DataBaseHelper.DeviceDBHelper;
import code.Utils.InputCreator;
import code.Utils.Regex;
import code.Utils.Utils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;

import java.util.ArrayList;

/**
 * Контроллер для просмотра файлов на мобильном устройстве.
 */
public class FileExplorerController {

    /** Родительский контейнер. */
    private JDialog dialog;

    /** Дерево элементов. */
    private JTree tree;

    /** Экземпляр класса DeviceDBHelper, для работы с базой данных, расположенной на мобильном устройстве.
     * @see code.DataBaseHelper.DeviceDBHelper
     * */
    private DeviceDBHelper dDB;

    /** Выбранное мобильное устройство. */
    private String currentDeviceName;

    /** Выбранный файл. */
    private String currentFile;

    /**
     * Конструктор класса.
     * @param path Путь к Android Debug Bridge
     */
    public FileExplorerController(String path) {
        dDB = new DeviceDBHelper(path);
    }

    /**
     * Инициализация родительского контейнера.
     * @param value Родительский контейнер
     */
    public void setDialog(JDialog value) {
        this.dialog = value;
    }

    /**
     * Инициализация дерева элеменов.
     * @param value Дерево элементов
     */
    public void setTree(JTree value) {
        this.tree = value;
    }

    /**
     * Обработчик нажатия на кнопку cancel: закрывает окно.
     */
    public void cancelListener() {
        dialog.setVisible(false);
        dialog.dispose();
    }

    /**
     * Обработчик, открывающий базу данных.
     * @return путь к базе данных.
     */
    public String openListener() {
        String result = null;
        if (currentFile != null) {
            if (currentFile.endsWith(".db") || currentFile.endsWith(".s3db"))
                result = currentDeviceName + currentFile;
            else
                JOptionPane.showMessageDialog(dialog, "This file isn\'t database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else
            JOptionPane.showMessageDialog(dialog, "This file isn\'t database.", "Error", JOptionPane.ERROR_MESSAGE);
        return result;
    }

    /**
     * Обработчик, создающий базу данных.
     * @return путь к базе данных.
     */
    public String newDataBaseListener() {
        String tmp = null;
        if (Utils.isSQLite3Available(dialog, currentDeviceName)) {
            if (currentFile != null) {
                tmp = InputCreator.getInputDialog(currentFile, currentDeviceName, dDB);
                tmp = tmp != null ? currentDeviceName + tmp : null;
            } else {
                JOptionPane.showMessageDialog(dialog, "This folder can\'t be selected.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return  tmp;
    }

    /**
     * Обработчик, перемещающий файлы с компьютера на устройство и наоборот.
     * @param flag Push - true, Pull - false
     * @param dbName Имя базы данных
     */
    public void PushPullListener(boolean flag, String dbName) {
        if (currentFile != null) {
            if (flag) {
                if (dDB.isFolder(currentFile, currentDeviceName)) {
                    String dbOnlyNamePart = Regex.getNameOfDataBase(dbName);
                    dbOnlyNamePart = dbOnlyNamePart != null ? dbOnlyNamePart : dbName;
                    String whatFolder = currentFile + "/" + dbOnlyNamePart;
                    int n = JOptionPane.showConfirmDialog(dialog, "Push file in this folder: " + currentFile + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (n == JOptionPane.YES_OPTION
                            && dDB.shellCommand("touch " + whatFolder, currentDeviceName)
                            && dDB.shellCommand("chmod 666 " + whatFolder, currentDeviceName)
                            && dDB.pushPullCommand(dbName, whatFolder, currentDeviceName, false)
                            ) {
                        JOptionPane.showMessageDialog(dialog, "File pushed!", "Info", JOptionPane.INFORMATION_MESSAGE);
                        cancelListener();
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "It isn\'t folder!", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                if (!dDB.isFolder(currentFile, currentDeviceName)) {
                    String where;
                    int n = JOptionPane.showConfirmDialog(dialog, "Pull this file: " + currentFile + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (n == JOptionPane.YES_OPTION
                            && !(where = JMenuController.selectDirectory(dialog)).isEmpty()
                            && dDB.shellCommand("chmod 666 " + currentFile, currentDeviceName)
                            && dDB.pushPullCommand(currentFile, where, currentDeviceName, true)) {
                        JOptionPane.showMessageDialog(dialog, "File pulled!", "Info", JOptionPane.INFORMATION_MESSAGE);
                        cancelListener();
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "It isn\'t file!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Обработчик нажатия на узел дерева элементов.
     * @param tp TreePath
     */
    public void treeListener(TreePath tp) {
        String current = tp.getLastPathComponent().toString();
        String root = tree.getModel().getRoot().toString();
        if (!current.equals(root)) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();

            if (node.getLevel() == 1)
                currentDeviceName = current;

            checkDevice();

            String path = "";
            TreeNode []tn = node.getPath();
            for(int i = 2; i < tn.length; i++)
                path += tn[i].toString() + "/";

            if (node.getChildCount() < 1)
                for (String st : dDB.getFolderContent(currentDeviceName, path)) {
                    if (!st.contains("Not a directory") && !st.contains("No such file or directory"))
                        node.add(new DefaultMutableTreeNode(st));
                }


            currentFile = path.length() > 1 ? path.substring(0, path.length() - 1) : "";
        }
    }

    /**
     * Получение списка устройств.
     * @return список устройств.
     */
    public ArrayList<String> getDevices() {
        return dDB.getDevices();
    }

    /**
     * Проверка устройства.
     */
    private void checkDevice() {
        if (currentDeviceName.contains("offline")) {
            String text = "Device \"" + currentDeviceName.replace("offline", "")
                    + "\" is not connected to adb or is not responding.\n Please, fix it and open file explorer again.";
            JOptionPane.showMessageDialog(dialog, text, "Error", JOptionPane.ERROR_MESSAGE);
            cancelListener();
        }
        if (currentDeviceName.contains("unauthorized")) {
            String text = "Device \"" + currentDeviceName.replace("unauthorized", "")
                    + "\" is not authorized.\n Please, fix it and open file explorer again.";
            JOptionPane.showMessageDialog(dialog, text, "Error", JOptionPane.ERROR_MESSAGE);
            cancelListener();
        }
    }
}