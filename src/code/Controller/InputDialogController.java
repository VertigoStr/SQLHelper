package code.Controller;

import code.DataBaseHelper.DBHelper;
import code.DataBaseHelper.DeviceDBHelper;
import code.Utils.Keeper;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Контроллер для ввода и изменения данных таблицы.
 */
public class InputDialogController {
    /** Экземпляр класса DBHelper, для работы с базой данных, расположенной на компьютере.
     * @see code.DataBaseHelper.DBHelper
     * */
    private DBHelper db;
    /** Экземпляр класса DeviceDBHelper, для работы с базой данных, расположенной на мобильном устройстве.
     * @see code.DataBaseHelper.DeviceDBHelper
     * */
    private DeviceDBHelper dDB;
    /** Родительский контейнер. */
    private JDialog dialog;
    /** Имя таблицы. */
    private String tableName;
    /** SQL - запрос. */
    private String sql;
    /** Переменная, хранящая информацию о том, с какой базой данной ведется работа: с базой, хранящейся на мобильном устройстве,
     * или - на компьютере. */
    private boolean isFromDevice;

    /**
     * Конструктор класса.
     * @param dataBaseName Имя базы данных
     * @param tableName Имя таблицы
     */
    public InputDialogController(String dataBaseName, String tableName)
    {
        this.tableName = tableName;
        sql = null;
        initDB(dataBaseName);
    }

    /**
     * Конструктор класса.
     * @param dataBaseName Имя базы данных
     * @param tableName Имя таблицы
     * @param sql SQL - запрос создания элемента
     */
    public InputDialogController(String dataBaseName, String tableName, String sql)
    {
        this.tableName = tableName;
        this.sql = sql;
        initDB(dataBaseName);
    }

    /**
     * Инициализация базы данных.
     * @param dataBaseName Имя базы данных
     */
    private void initDB(String dataBaseName) {
        String[] params = dataBaseName.split("\\|");
        if (!params[0].isEmpty() && params.length >= 2) {
            dDB = new DeviceDBHelper(Keeper.getSDKLocation(), params[0], params[1]);
            isFromDevice = true;
        }
        else {
            db = new DBHelper(dataBaseName.replace("|", ""));
            isFromDevice = false;
        }
    }

    /**
     * Инициализация родительского контейнера.
     * @param dialog Родительский контейнер
     */
    public void setJDialog(JDialog dialog)
    {
        this.dialog = dialog;
    }

    /**
     * Получение полей.
     * @return список полей.
     */
    public Map<String, String> getFields()
    {
        Map<String, String> map;
        if (isFromDevice) {
            map = dDB.getTypesM(tableName);
        }
        else {
            db.connect();
            map = db.getTypesM(tableName);
            db.close();
        }
        return map;
    }

    /**
     * Задание значений для всех полей таблицы. Необходимо для обновления данных в таблице.
     * @param panel Родительский контейнер
     * @return результат работы.
     */
    public boolean setData(JPanel panel)
    {
        ArrayList<String> list;
        if (!isFromDevice) {
            db.connect();
            list = db.getRowFromTableBySQL("select * " + sql);
            db.close();
        }
        else {
            list = dDB.getRowFromTableBySQL("select * " + sql);
        }
        for (String st: list)
            System.out.println(st);
        int i = 0;
        SimpleDateFormat df = null;
        boolean added = false;
        if (list.size() > 0) {
            for (Component c : panel.getComponents()) {
                if (c instanceof JSpinner) {
                    JSpinner spinner = (JSpinner) c;
                    String type = spinner.getToolTipText();
                    if (type.equalsIgnoreCase("TIMESTAMP"))
                        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    else if (type.equalsIgnoreCase("TIME"))
                        df = new SimpleDateFormat("HH:mm:ss");
                    else if (type.equalsIgnoreCase("DATE"))
                        df = new SimpleDateFormat("yyyy-MM-dd");
                    assert (df != null);
                    try {
                        spinner.getModel().setValue(df.parse(list.get(i)));
                        added = true;
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(dialog, "Can\'t parse DATE format.\nUpdate this row by sql command. ", "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
                if (c instanceof JTextField) {
                    JTextField t = (JTextField) c;
                    t.setText(list.get(i));
                    added = true;
                }
                if (c instanceof TextField) {
                    TextField t = (TextField) c;
                    t.setText(list.get(i));
                    added = true;
                }
                if (added) {
                    i++;
                    added = false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Обработчик нажатия на кнопку insert/update. Добавление изменений в таблицу.
     * @param panel Родительский контейнер
     * @return результат работы.
     */
    public boolean insertButtonListener(JPanel panel)
    {
        String result = "";
        String fields = "";
        for (Component c: panel.getComponents())
        {
            if (c instanceof JSpinner) {
                JSpinner spinner = (JSpinner) c;
                JSpinner.DateEditor de;
                String type = spinner.getToolTipText();

                if (type.equalsIgnoreCase("TIMESTAMP"))
                    de = new JSpinner.DateEditor(spinner, "yyyy-MM-dd HH:mm:ss");
                else if (type.equalsIgnoreCase("TIME"))
                    de = new JSpinner.DateEditor(spinner, "HH:mm:ss");
                else if (type.equalsIgnoreCase("DATE"))
                    de = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
                else
                    break;
                result += "\"" + de.getFormat().format(spinner.getValue()) + "\", ";
                fields += spinner.getName() + ", ";
            }
            if (c instanceof JTextField) {
                String value = ((JTextField) c).getText();
                String type = ((JTextField) c).getToolTipText();
                if ((type.equalsIgnoreCase("REAL") || type.equalsIgnoreCase("DOUBLE")
                        || type.equalsIgnoreCase("DOUBLE PRECISION") || type.equalsIgnoreCase("FLOAT")) && !value.isEmpty()) {
                    result += value.replace(",", ".") + ", ";
                    fields += c.getName() + ", ";
                }
                else {
                    boolean flag = false;
                    for (int i = 0; i < value.length(); i++)
                        if (Character.isLetter(value.charAt(i))) {
                            flag = true;
                            break;
                        }
                    if (!value.isEmpty()) {
                        result += (flag ? "\"" + value + "\"" : value) + ", ";
                        fields += c.getName() + ", ";
                    }
                }
            }
        }

        result = result.length() >= 2 ? result.substring(0, result.length() - 2) : result;
        fields = fields.length() >= 2 ? fields.substring(0, fields.length() - 2) : fields;

        if (isFromDevice)
            insertToDevice(fields, result);
        else
            insertToPC(fields, result);

        return true;
    }

    /**
     * Вставка данных в базу данных, расположенной на устройстве.
     * @param fields Поля
     * @param result Значения
     */
    private void insertToDevice(String fields, String result) {
        if (sql != null)
            dDB.sqlCommand("delete " + sql);
        if (dDB.sqlCommand("INSERT INTO " + tableName + "(" + fields + ") VALUES(" + result + ")")) {
            dialog.dispose();
            dialog.setVisible(false);
        }
    }

    /**
     * Вставка данных в базу данных, расположенной на компьютере.
     * @param fields Поля
     * @param result Значения
     */
    private void insertToPC(String fields, String result) {
        try {
            db.connect();
            if (sql != null)
                db.executeQueryBySql("delete " + sql);
            db.executeQueryBySql("INSERT INTO " + tableName + "(" + fields + ") VALUES(" + result + ")");
            dialog.dispose();
            dialog.setVisible(false);
        } catch (SQLException ex) {
            String exMsg = "Message from MySQL Database";
            String exSqlState = "Exception";
            SQLException mySqlEx = new SQLException(exMsg, exSqlState);
            ex.setNextException(mySqlEx);
            JOptionPane.showMessageDialog(dialog, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        finally {
            db.close();
        }
    }

    /**
     * Обработчик нажатия на кнопку cancel: закрывает окно.
     */
    public void cancelButtonListener()
    {
        dialog.setVisible(false);
        dialog.dispose();
    }
}
