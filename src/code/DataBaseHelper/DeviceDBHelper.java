package code.DataBaseHelper;

import code.CodeGenerator.Parameter;
import code.Utils.Cast;
import code.Utils.Regex;
import code.Utils.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Класс работы с базой данных через Android Debug Bridge на мобильном устройстве.
 */
public class DeviceDBHelper {
    /** Путь к Android Debug Bridge. */
    private String adbPath;
    /** Имя устройства. */
    private String deviceName;
    /** Имя базы данных. */
    private String dataBaseName;
    /** Переменная, хранаящая результат проверки: имеет ли устройство root доступ или нет. */
    private boolean isDevice;
    /** Количество косых черт в строке запроса. */
    private String slash;

    /**
     * Конструктор класса.
     * @param adbPath Путь к Android Debug Bridge
     */
    public DeviceDBHelper(String adbPath) {
        this.adbPath = adbPath + "adb";
        slash = Utils.isWin() ? "\\\"" : "\"";
    }

    /**
     * Конструктор класса.
     * @param adbPath Путь к Android Debug Bridge
     * @param deviceName Имя устройства
     * @param dataBaseName Имя базы данных
     */
    public DeviceDBHelper(String adbPath, String deviceName, String dataBaseName) {
        this.adbPath = adbPath + "adb";
        this.deviceName = deviceName;
        this.dataBaseName = dataBaseName;
        this.isDevice = isDevice(deviceName);
        slash = Utils.isWin() ? "\\\"" : "\"";
    }

    /**
     * Проверка root доступа у устройства.
     * @param deviceName Имя устройства
     * @return результат работы.
     */
    public boolean isDevice(String deviceName) {
        boolean flag = false;
        try {
            Process p = Runtime.getRuntime().exec(new String[]{adbPath, "-s", deviceName.trim(), "shell", "ls /data/data"});
            p.waitFor();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                if (tmp.toLowerCase().contains("denied") || tmp.toLowerCase().contains("Permission denied")) {
                    flag = true;
                    break;
                }
            }
        }
        catch (Exception ex) {
            System.out.println("Check device available error: " + ex.toString());
        }
        return flag;
    }

    /**
     * Проверка: подключено ли устройство.
     * @param deviceName Имя устройства
     * @return результат работы.
     */
    public boolean isDeviceAvailable(String deviceName) {
        boolean flag = true;
        try {
            Process p = Runtime.getRuntime().exec(
                    isDevice(deviceName)
                            ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c", "ls /"}
                            : new String[]{adbPath, "-s", deviceName.trim(), "shell", "ls /"}
            );
            p.waitFor();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                if (tmp.contains("Error:")) {
                    flag = false;
                    JOptionPane.showMessageDialog(null, tmp, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }
        }
        catch (Exception ex) {
            flag = false;
            System.out.println("Check device available error: " + ex.toString());
        }
        return flag;
    }

    /**
     * Проврека: доступна ли база данных.
     * @param currentFolder Имя базы данных
     * @param deviceName Имя устройства
     * @return результат работы.
     */
    public boolean isDataBaseAvailable(String currentFolder, String deviceName) {
        String fullPath = currentFolder;
        String upFolder = Regex.getNameOfDataBase(currentFolder);
        upFolder = "/" + currentFolder.replace(upFolder, "");
        currentFolder = Regex.getNameOfDataBase(currentFolder);
        boolean flag = false;
        try {
            Process p = Runtime.getRuntime().exec(
                    isDevice(deviceName)
                            ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c", "ls /" + upFolder}
                            : new String[]{adbPath, "-s", deviceName.trim(), "shell", "ls /" + upFolder}
            );
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                if (tmp.equals(currentFolder)) {
                    flag = true;
                    break;
                }
            }
            p.waitFor();
        }
        catch (Exception ex) {
            flag = false;
            System.out.println("Check folder error: " + ex.toString());
        }
        if (!flag) {
            JOptionPane.showMessageDialog(null, "Database isn\'t available:\n " + fullPath, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return flag;

    }

    /**
     * Проверка: доступна ли утилита sqlite3.
     * @param deviceName Имя устройства
     * @return результат работы.
     */
    public boolean isSQLite3Available(String deviceName) {
        boolean flag = false;
        try {
            Process p = Runtime.getRuntime().exec(
                    isDevice(deviceName)
                            ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c", "ls /system/xbin/"}
                            : new String[]{adbPath, "-s", deviceName.trim(), "shell", "ls /system/xbin/"}
            );
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                if (tmp.equalsIgnoreCase("sqlite3")) {
                    flag = true;
                    break;
                }
            }
            p.waitFor();
        }
        catch (Exception ex) {
            System.out.println("Check sqlite3 error: " + ex.toString());
        }
        return flag;
    }

    /**
     * Является ли выбранный элемент папкой.
     * @param currentFolder Имя папки
     * @param deviceName Имя устройства
     * @return результат работы.
     */
    public boolean isFolder(String currentFolder, String deviceName) {
        String upFolder = Regex.getNameOfDataBase(currentFolder);
        upFolder = "/" + currentFolder.replace(upFolder, "");
        currentFolder = Regex.getNameOfDataBase(currentFolder);
        boolean flag = false;
        try {
            Process p = Runtime.getRuntime().exec(
                    isDevice(deviceName)
                            ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c", "ls -F " + upFolder}
                            : new String[]{adbPath, "-s", deviceName.trim(), "shell", "ls -F " + upFolder}
            );
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                if (tmp.contains(currentFolder) && tmp.startsWith("d")) {
                    flag = true;
                    break;
                }
            }
            p.waitFor();
        }
        catch (Exception ex) {
            System.out.println("Check folder error: " + ex.toString());
        }
        return flag;
    }

    /**
     * Проверка: доступно ли подключение к Android Debug Bridge.
     * @return результат работы.
     */
    public boolean checkADBConnection() {
        boolean flag = false;
        try {
            Process p = Runtime.getRuntime().exec(new String[]{adbPath, "devices"});
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String tmp;
            while ((tmp = in.readLine()) != null)
                if (tmp.contains("List of devices attached")) {
                    flag = true;
                    break;
                }
            p.waitFor();
        }
        catch (Exception ex) {
            System.out.println("Check adb error: " + ex.toString());
        }
        return flag;
    }

    /**
     * Получение таблицы по sql - запросу.
     * @param command SQL запрос
     * @param frame Родительский контейнер
     * @param pane Окно с вкладками
     * @return модель для таблицы.
     */
    public DefaultTableModel getTableBySQL(String command, JFrame frame, JTabbedPane pane) {
        command = command.replace("\"", slash);
        String[] cmd = isDevice
                ? new String[]{adbPath, "-s", deviceName, "shell", "su", "-c", "sqlite3 " + dataBaseName + " '" + command + "'"}
                : new String[]{adbPath, "-s", deviceName, "shell", "sqlite3 " + dataBaseName + " '" + command + "'"};
        ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String tmp, row = "";
            boolean flag = false;
            while ((tmp = in.readLine()) != null) {
                if (tmp.contains("Error:")) {
                    JOptionPane.showMessageDialog(frame, tmp, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                else {
                    if (!tmp.isEmpty()) {
                        if (tmp.contains("|") && !tmp.endsWith(")")) {
                            row = tmp;
                            flag = true;
                        } else if (!tmp.contains("|") && flag) {
                            row += tmp;
                        } else if (tmp.contains("|") && flag) {
                            flag = false;
                            list.add(new ArrayList<String>(Arrays.asList(tmp.split("\\|"))));
                            if (!row.isEmpty()) {
                                list.add(new ArrayList<String>(Arrays.asList(row.split("\\|"))));
                                row = "";
                            }
                        } else if (!flag)
                            list.add(new ArrayList<String>(Arrays.asList(tmp.split("\\|"))));
                    }
                }
            }
            p.waitFor();
            if (!row.isEmpty()) {
                list.add(new ArrayList<String>(Arrays.asList(row.split("\\|"))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DefaultTableModel model = null;
        if (list.size() > 0) {
            int i = list.get(0).size();
            ArrayList<String> columns = new ArrayList<String>();
            while (i > 0) {
                columns.add("");
                i--;
            }
            model = new DefaultTableModel(Cast.arrayListTo2DArrayS(list), Cast.arrayListToArray(columns));
            pane.setSelectedIndex(2);
        }

        return model;
    }

    /**
     * Выполнение sql - запроса.
     * @param command SQL - запрос
     * @return результат работы.
     */
    public boolean sqlCommand(String command) {
        command = command.replace("\"", slash);
        return isExecute(isDevice
                        ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c", "sqlite3 " + dataBaseName +" '" + command + "'"}
                        : new String[]{adbPath, "-s", deviceName.trim(), "shell", "sqlite3 " + dataBaseName +" '" + command + "'"}
        );
    }

    /**
     * Выполнение sql - запроса.
     * @param command SQL - запрос
     * @param deviceName Имя устройства
     * @param dataBaseName Имя базы данных
     * @return результат работы.
     */
    public boolean sqlCommand(String command, String deviceName, String dataBaseName) {
        command = command.replace("\"", slash);
        return isExecute(isDevice(deviceName)
                        ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c", "sqlite3 " + dataBaseName +" '" + command + "'"}
                        : new String[]{adbPath, "-s", deviceName.trim(), "shell", "sqlite3 " + dataBaseName +" '" + command + "'"}
        );
    }

    /**
     * Выполнение команды оболочки.
     * @param command Команда
     * @param deviceName Имя устройства
     * @return результат работы.
     */
    public boolean shellCommand(String command, String deviceName) {
        command = command.replace("\"", slash);
        return isExecute(
                isDevice(deviceName)
                        ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c", command}
                        : new String[]{adbPath, "-s", deviceName.trim(), "shell", command}
        );
    }

    /**
     * Перемещение файла.
     * @param what Какой файл
     * @param where Куда переместить
     * @param deviceName Имя устройства
     * @param mode Тип перемещения: true - pull, false - push
     * @return результат работы.
     */
    public boolean pushPullCommand(String what, String where, String deviceName, boolean mode) {
        return isExecute(
                new String[]{adbPath,"-s",deviceName.trim(),mode ? "pull" : "push", what, where}
        );
    }

    /**
     * Выполнение команды.
     * @param cmd Команда
     * @return результат работы.
     */
    public boolean isExecute(String[] cmd) {
        boolean flag = true;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                if (tmp.contains("Error:") || tmp.toLowerCase().contains("unable")) {
                    JOptionPane.showMessageDialog(null, tmp, "Error", JOptionPane.ERROR_MESSAGE);
                    flag = false;
                }
            }
            p.waitFor();
        }
        catch (Exception ex) {
            flag = false;
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return flag;
    }

    /**
     * Получить содержимое папки.
     * @param deviceName Имя устройства
     * @param folder Папка
     * @return содержимое папки.
     */
    public ArrayList<String> getFolderContent(String deviceName, String folder) {
        return execute(
                isDevice(deviceName)
                        ? new String[] {adbPath, "-s", deviceName.trim(), "shell", "su", "-c", "ls /" + folder.trim()}
                        : new String[] {adbPath, "-s", deviceName.trim(), "shell", "ls /" + folder.trim()}
        );
    }

    /**
     * Выполнение команды.
     * @param cmd Команда
     * @return результат работы.
     */
    public ArrayList<String> execute(String[] cmd) {
        ArrayList<String> line = new ArrayList<String>();
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String tmp;
            while ((tmp = in.readLine()) != null)
                if (!tmp.isEmpty())
                    line.add(tmp);

            p.waitFor();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return line;
    }

    /**
     * Получение списка имен элементов базы данных, принадлежащих к определенной категории элементов.
     * @param name Имя категории
     * @return список имен элементов базы данных.
     */
    public ArrayList<String> getNamesOf(String name) {
        return execute(
                isDevice
                        ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                        "sqlite3 " + dataBaseName + " 'SELECT name FROM sqlite_master WHERE type = " + slash + name + slash + "'"
                }
                        : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                        "sqlite3 " + dataBaseName + " 'SELECT name FROM sqlite_master WHERE type = " + slash + name + slash + "'"
                }
        );
    }

    /**
     * Получить количество строк в таблице.
     * @param name Имя таблицы
     * @return количество строк.
     */
    public int getRowCount(String name) {
        return Integer.parseInt(execute(
                isDevice
                        ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                        "sqlite3 " + dataBaseName + " 'SELECT count(*) from " + name + "'"}
                        : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                        "sqlite3 " + dataBaseName + " 'SELECT count(*) from " + name + "'"}
        ).get(0));
    }

    /**
     * Выбрать все записи из таблицы.
     * @param name Имя таблицы
     * @return модель для таблицы.
     */
    public DefaultTableModel selectAllFromTable(String name) {
        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
        ArrayList<String> columns = new ArrayList<String>();
        String[] cmdPragma = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"};
        String[] cmdSelect = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'select * from " + name + " '"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'select * from " + name + " '"};
        for(String st: execute(cmdPragma))
            columns.add(st.split("\\|")[1]);
        for (String st: execute(cmdSelect))
            result.add(new ArrayList<String>(Arrays.asList(st.split("\\|"))));
        return new DefaultTableModel(Cast.arrayListTo2DArrayS(result), columns.toArray());
    }

    /**
     * Выбрать все записи из таблицы с ограничением.
     * @param name Имя таблицы
     * @param start Номер строки, с которого выбираем данные
     * @param end Количество данных
     * @return модель для таблицы.
     */
    public DefaultTableModel selectAllFromTableWithLimit(String name, int start, int end) {
        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
        ArrayList<String> columns = new ArrayList<String>();
        String[] cmdPragma = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"};
        String[] cmdSelect = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'select * from " + name + " LIMIT " + start + ", " + end + " '"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'select * from " + name + " LIMIT " + start + ", " + end + " '"};

        for(String st: execute(cmdPragma))
            columns.add(st.split("\\|")[1]);
        for (String st: execute(cmdSelect))
            result.add(new ArrayList<String>(Arrays.asList(st.split("\\|"))));
        return new DefaultTableModel(Cast.arrayListTo2DArrayS(result), columns.toArray());
    }

    /**
     * Получение всех полей из таблицы.
     * @param name Имя таблицы
     * @return список полей таблицы.
     */
    public Map<String, String> getTypesM(String name) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        String[] cmdPragma = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"};

        for (String st: execute(cmdPragma))
            map.put(st.split("\\|")[1], st.split("\\|")[2]);
        return map;
    }


    /**
     * Получение строки из таблицы.
     * @param sql SQL-запрос
     * @return строки, удовлетворяющие SQL-запросу.
     */
    public ArrayList<String> getRowFromTableBySQL(String sql)
    {
        sql = sql.replace("\"", slash);
        ArrayList<String> list = new ArrayList<String>();
        String[] cmdSQL = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " '" + sql + "'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " '" + sql + "'"};

        for (String st: execute(cmdSQL))
            list.addAll(Arrays.asList(st.split("\\|", -1)));
        return list;
    }

    /**
     * Получение данных из таблицы.
     * @param name Имя таблицы
     * @return список данных.
     */
    public ArrayList<ArrayList<Object>> getDataFromTable(String name) {
        ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
        String[] cmdPragma = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"};

        for(String st: execute(cmdPragma)) {
            String[] ar = st.split("\\|", -1);
            result.add(new ArrayList<Object>(Arrays.asList(
                    ar[1],
                    ar[2],
                    ar[4],
                    ar[3].equals("0") ? "null" : "not null"
            )));
        }
        return result;
    }

    /**
     * Получение информации о поле в таблице.
     * @param tableName Имя таблицы
     * @param dataName Имя поля
     * @return информация о поле.
     */
    public ArrayList<String> getFieldSettingsFromTable(String tableName, String dataName) {
        ArrayList<String> list = new ArrayList<String>();
        String[] cmdPragma = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + tableName + ")'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + tableName + ")'"};
        try {
            Process p = Runtime.getRuntime().exec(cmdPragma);
            p.waitFor();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                String[] ar = tmp.split("\\|", -1);
                if (ar.length >= 2) {
                    if (ar[1].equalsIgnoreCase(dataName)) {
                        list.addAll(Arrays.asList(
                                ar[1], //name
                                ar[2], //type
                                ar[4], //dft_value
                                ar[3].equals("0") ? "null" : "not null"  //notnull
                        ));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Получить информацию о таблице.
     * @param name Имя таблицы
     * @return информация о таблице.
     */
    public ArrayList<String> getDataFieldsNameFromTable(String name) {
        ArrayList<String> result = new ArrayList<String>();
        String[] cmdPragma = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"};

        for(String st: execute(cmdPragma))
            result.add(st.split("\\|", -1)[1]);
        return result;
    }

    /**
     * Получение sql-запроса создания определенного элемента.
     * @param name Имя элемента
     * @return sql-запрос.
     */
    public String getCreateElement(String name) {
        String[] cmd = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'SELECT sql FROM sqlite_master WHERE name = " + slash + name + slash + "'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'SELECT sql FROM sqlite_master WHERE name = " + slash + name + slash + "'"};
        String result = "";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                if (tmp.contains("Error:")) {
                    JOptionPane.showMessageDialog(null, tmp, "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                } else {
                    result += tmp;
                }
            }
            p.waitFor();
        }
        catch (Exception ex) {
            System.out.println("Create element error: " + ex.toString());
        }
        return result;
    }

    /**
     * Получение информации о внешних ключах поля.
     * @param tableName Имя таблицы
     * @param fieldName Имя поля
     * @return список внешних ключей.
     */
    public ArrayList<String> getFieldForeignKeysFromTable(String tableName, String fieldName) {
        ArrayList<String> list = new ArrayList<String>();
        String[] cmdForeignKeyList = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'PRAGMA foreign_key_list(" + tableName + ")'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'PRAGMA foreign_key_list(" + tableName + ")'"};

        try {
            Process p = Runtime.getRuntime().exec(cmdForeignKeyList);
            p.waitFor();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                String[] ar = tmp.split("\\|", -1);
                if (ar.length >= 4) {
                    if (ar[3].equalsIgnoreCase(fieldName)) {
                        list.addAll(Arrays.asList(
                                ar[2], //table
                                ar[4], //to
                                ar[6], //on_delete
                                ar[5]  //on_update
                        ));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Получение списка устройств.
     * @return список устройств.
     */
    public ArrayList<String> getDevices() {
        ArrayList<String> line = new ArrayList<String>();
        try {
            Process p = Runtime.getRuntime().exec(new String[] {adbPath, "devices"});
            p.waitFor();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String tmp;
            while ((tmp = in.readLine()) != null) {
                line.add(tmp.replace("device", ""));
            }
            line.removeAll(Arrays.asList("", null));
            line.remove(0);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return line;
    }

    /**
     * Получение всех полей таблицы.
     * @param name Имя таблицы
     * @return список полей таблицы.
     */
    public ArrayList<Parameter> getTypesP(String name)
    {
        ArrayList<Parameter> list = new ArrayList<Parameter>();
        String[] cmdPragma = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'PRAGMA TABLE_INFO(" + name + ")'"};

        try {
            for (String st: execute(cmdPragma)) {
                Parameter parameter = new Parameter(st.split("\\|")[2], st.split("\\|")[1]);
                parameter.recastType();
                list.add(parameter);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Получение sql-запроса создания таблицы.
     * @param name Имя элемента
     * @return sql-запрос.
     */
    public String getSQLQuery(String name) {
        String[] cmdSQL = isDevice
                ? new String[]{adbPath, "-s", deviceName.trim(), "shell", "su", "-c",
                "sqlite3 " + dataBaseName + " 'SELECT sql FROM sqlite_master WHERE name = " + slash + name + slash + " and type = " + slash + "table" + slash + "'"}
                : new String[]{adbPath, "-s", deviceName.trim(), "shell",
                "sqlite3 " + dataBaseName + " 'SELECT sql FROM sqlite_master WHERE name = " + slash + name + slash + " and type = " + slash + "table" + slash + "'"};

        String result = "";
        for (String st: execute(cmdSQL))
            result += st;
        return result;
    }
}