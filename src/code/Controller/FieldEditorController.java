package code.Controller;

import code.DataBaseHelper.DBHelper;
import code.DataBaseHelper.DeviceDBHelper;
import code.Utils.Cast;
import code.Utils.Keeper;
import code.Utils.Regex;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Контроллер для окна создания поля таблицы.
 */
public class FieldEditorController {
    /** Экземпляр класса DBHelper, для работы с базой данных, расположенной на компьютере.
     * @see code.DataBaseHelper.DBHelper
     * */
    private DBHelper db;

    /** Имя поля, которое необходимо редактировать. */
    private String editFieldName;

    /** Имя таблицы. */
    private String tableName;
    /** Выбранная строка. */
    private String rowInFocus;
    /** Родительский контейнер. */
    private JDialog dialog;

    /** Переменная, хранящая информацию о том, с какой базой данной ведется работа: с базой, хранящейся на мобильном устройстве,
     * или - на компьютере. */

     private boolean isFromDevice;
    /** Экземпляр класса DeviceDBHelper, для работы с базой данных, расположенной на мобильном устройстве.
     * @see code.DataBaseHelper.DeviceDBHelper
     * */
    private DeviceDBHelper dDB;

    /**
     * Конструктор класса.
     * @param name Имя базы данных
     */
    public FieldEditorController(String name)
    {
        initDB(name);
    }

    /**
     * Конструктор класса.
     * @param name Имя базы данных
     * @param tableName Имя таблицы
     */
    public FieldEditorController(String name, String tableName)
    {
        initDB(name);
        this.tableName = tableName;
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
    public void setDialog(JDialog dialog)
    {
        this.dialog = dialog;
    }

    /**
     * Обработчик нажатия на кнопку cancel: закрывает окно.
     */
    public boolean cancelButtonActionListener()
    {
        dialog.setVisible(false);
        dialog.dispose();
        return true;
    }

    /**
     * Инициализации имени редактируемого поля.
     * @param value Имя поля
     */
    public void setEditFieldName(String value)
    {
        this.editFieldName = value;
    }

    /**
     * Инициализация выбранной строки в TableEditorController.
     * @param value Строки
     * @see code.Controller.TableEditorController
     */
    public void setRowInFocus(String value)
    {
        this.rowInFocus = value;
    }

    /**
     * Возвращает модель для ComboBox, содержащий инфорацию о всех таблицах, с которыми можно установить связь.
     * @param reference Ссылка
     * @return модель для ComboBox.
     */
    public DefaultComboBoxModel getComboBoxModel(String reference)
    {
        String [] referenceFields;
        if (isFromDevice) {
            referenceFields = Cast.arrayListToArray(dDB.getDataFieldsNameFromTable(reference));
        } else {
            db.connect();
            referenceFields = Cast.arrayListToArray(db.getDataFromTable(reference, "name"));
            db.close();
        }
        return new DefaultComboBoxModel(referenceFields);
    }

    /**
     * Получение имени всех элементов указанной группы.
     * @param name Имя группы
     * @return массив имен, входящих в группу.
     */
    public String[] getNamesOf(String name)
    {
        String referenceTables[];
        if (isFromDevice) {
            referenceTables = Cast.arrayListToArray(dDB.getNamesOf(name));
        } else {
            db.connect();
            referenceTables = Cast.arrayListToArray(db.getNamesOf(name));
            db.close();
        }
        return referenceTables;
    }

    /**
     * Получение всех свойств поля.
     * @return список свойств поля.
     */
    public ArrayList<String> getFieldProperties()
    {
        ArrayList<String> fieldBaseProperties;
        if (isFromDevice) {
            fieldBaseProperties = dDB.getFieldSettingsFromTable(editFieldName, rowInFocus);
        } else {
            db.connect();
            fieldBaseProperties = db.getFieldSettingsFromTable(editFieldName, rowInFocus);
            db.close();
        }
        return fieldBaseProperties;
    }

    /**
     * Получение всех внешних ключей для поля.
     * @return список внешних ключей поля.
     */
    public ArrayList<String> getForeignKeys()
    {
        ArrayList<String> foreignKeysInfo;
        if (isFromDevice) {
            foreignKeysInfo = dDB.getFieldForeignKeysFromTable(editFieldName, rowInFocus);
        } else {
            db.connect();
            foreignKeysInfo = db.getFieldForeignKeysFromTable(editFieldName, rowInFocus);
            db.close();
        }
        return foreignKeysInfo;
    }

    /**
     * Получени всех дополнительных свойств поля.
     * @return массив дополнительных свойств поля.
     */
    public int[] getExtraProperties()
    {
        int[] extraProperties;
        if (isFromDevice) {
            extraProperties = Regex.getPropertiesOfFieldI(rowInFocus, dDB.getCreateElement(editFieldName));
        } else {
            db.connect();
            extraProperties = Regex.getPropertiesOfFieldI(rowInFocus, db.getCreateElement(editFieldName));
            db.close();
        }
        return extraProperties;
    }

    /**
     * Обработчик нажатия на кнопку add: добавляет поле в таблицу.
     * @param nameTextField Имя поля
     * @param typeBox Тип поля
     * @param defValue Значение по умолчанию
     * @param notNull Свойства поля
     * @return SQL-запрос на добавление поля.
     */
    public String addButtonActionListenerSimple(JTextField nameTextField, JComboBox<String> typeBox, JTextField defValue, JCheckBox notNull)
    {
        String result = "";
        if (!nameTextField.getText().isEmpty()) result += nameTextField.getText() + " ";
        else{
            JOptionPane.showMessageDialog(dialog, "Input field name!", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        if (typeBox.getSelectedIndex() == -1 && typeBox.getEditor().getItem().toString().isEmpty())
        {
            JOptionPane.showMessageDialog(dialog, "Input field type!", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String type;
        if (typeBox.getSelectedIndex() == -1) {
            type = typeBox.getEditor().getItem().toString();
            result +=  type + " ";
        }
        else {
            type = typeBox.getSelectedItem().toString();
            result += type + " ";
        }

        if (!defValue.getText().isEmpty())
            result += defaultValueEditor(defValue.getText(), type);

        if (notNull.isSelected()) result += "NOT NULL ";

        if (tableName != null) {
            result = "alter table " + tableName + " add column " + result;
            System.out.println(result);
            boolean flag;
            if (isFromDevice)
                flag = alterOnDevice(result);
            else
                flag = alterOnPC(result);
            return flag ? result : null;
        }
        return null;
    }

    /**
     * Добавление поля в таблицу для базы, расположенной на устройстве.
     * @param result SQL - запрос
     * @return результат работы.
     */
    private boolean alterOnDevice(String result) {
        return dDB.sqlCommand(result);
    }

    /**
     * Добавление поля в таблицу для базы, расположенной на компьютере.
     * @param result SQL - запрос
     * @return результат работы.
     */
    private boolean alterOnPC(String result) {
        boolean flag = true;
        try {
            db.connect();
            db.executeQueryBySql(result);
        } catch (SQLException ex) {
            flag = false;
            String exMsg = "Message from MySQL Database";
            String exSqlState = "Exception";
            SQLException mySqlEx = new SQLException(exMsg, exSqlState);
            ex.setNextException(mySqlEx);
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            db.close();
        }
        return flag;
    }

    /**
     * Обработчик нажатия на кнопку add: добавляет поле в таблицу
     * @param nameTextField Имя поля
     * @param typeBox Тип поля
     * @param defaultBox Значение по умолчанию
     * @param primaryKey Первичный ключ
     * @param notNull Нулевое значение
     * @param autoIncrement AUTO_INCREMENT
     * @param unique UNIQUE
     * @param reference Ссылки на другие таблицы
     * @return SQL-запрос на добавление поля в таблицу.
     */
    public String addButtonActionListener(JTextField nameTextField, JComboBox<String> typeBox, JComboBox<String> defaultBox, JCheckBox primaryKey, JCheckBox notNull, JCheckBox autoIncrement, JCheckBox unique, String reference)
    {
        String result = "";
        if (!nameTextField.getText().isEmpty()) result += nameTextField.getText() + " ";
        else{
            JOptionPane.showMessageDialog(dialog, "Input field name!", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        if (typeBox.getSelectedIndex() == -1 && typeBox.getEditor().getItem().toString().isEmpty())
        {
            JOptionPane.showMessageDialog(dialog, "Input field type!", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String type;
        if (typeBox.getSelectedIndex() == -1) {
            type = typeBox.getEditor().getItem().toString();
            result += type + " ";
        }
        else {
            type = typeBox.getSelectedItem().toString();
            result += type + " ";
        }
        if (defaultBox.getSelectedIndex() == -1) {
            String item = defaultBox.getEditor().getItem().toString();
            if (!item.isEmpty())
                result += defaultValueEditor(item, type);
        }
        else
        {
            String item = defaultBox.getSelectedItem().toString();
            if (!item.isEmpty())
                result += defaultValueEditor(item, type);
        }
        if (primaryKey.isSelected()) result += "PRIMARY KEY ";
        if (autoIncrement.isSelected()) result += "AUTOINCREMENT ";
        if (notNull.isSelected()) result += "NOT NULL ";
        if (unique.isSelected()) result += "UNIQUE ";
        if (reference != null)
        {
            result += "," + reference;
        }
        return result;
    }

    /**
     * Проверка: является значение по умолчанию значением времени
     * @param value Строка
     * @return результат работы.
     */
    private boolean defaultValueEqual(String value) {
        return value.contains("CURRENT_TIME") || value.contains("CURRENT_TIMESTAMP") || value.contains("CURRENT_DATE");
    }

    /**
     * Преобразование значения по умолчанию
     * @param value Значение
     * @param type Тип
     * @return преобразованная строка.
     */
    private String defaultValueEditor(String value, String type) {
        value = value.replace("\"", "");
        String res = "";
        if (type.equalsIgnoreCase("INTEGER") || type.equalsIgnoreCase("MEDIUMINT") || type.equalsIgnoreCase("INT2")
                || type.equalsIgnoreCase("INT8") || type.equalsIgnoreCase("INT") || type.equalsIgnoreCase("SMALLINT")
                || type.equalsIgnoreCase("TINYINT") || type.equalsIgnoreCase("BIGINT") || type.equalsIgnoreCase("UNSIGNED BIG INT")
                ||(type.equalsIgnoreCase("NUMERIC"))) {
            res = Regex.getIntegerFromStr(value) + "";
            if (value.contains("-"))
                res = "-" + res;
        }
        else {
            if (type.equalsIgnoreCase("REAL") || type.equalsIgnoreCase("DOUBLE")
                    || type.equalsIgnoreCase("DOUBLE PRECISION") || type.equalsIgnoreCase("FLOAT"))
                res = value.trim().replace(",", ".");
            else
                res += defaultValueEqual(value) ? value: "\"" + value + "\"";
        }
        return "DEFAULT " + res + " ";
    }
}
