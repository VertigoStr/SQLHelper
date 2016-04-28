package code.Controller;

import code.DataBaseHelper.DBHelper;
import code.DataBaseHelper.DeviceDBHelper;
import code.GUI.FieldEditor;
import code.Utils.Cast;
import code.Utils.Keeper;
import code.Utils.Regex;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Контроллер для окна создания таблицы.
 */
public class TableEditorController {
    /** Имя базы данных. */
    private String dataBaseName;
    /** Экземпляр класса DBHelper, для работы с базой данных, расположенной на компьютере.
     * @see code.DataBaseHelper.DBHelper
     * */
    private DBHelper db;
    /** Выбранная строка в таблице. */
    private String rowInFocus;
    /** Имя таблицы. */
    private String name;
    /** Родительский контейнер. */
    private JDialog dialog;
    /** Общий запрос создания таблицы. */
    private ArrayList<String> query;
    /** Внешние ключи, содержащиеся в запросе. */
    private ArrayList<String> foreignKeysArray;
    /** Контроллер класса MainFrameController.
     * @see code.Controller.MainFrameController
     * */
    private MainFrameController mfc;
    /** Модель для таблицы, отображающая информацию о полях текущей таблицы. */
    private DefaultTableModel tableModel;
    /** Таблица, отображающая информацию о полях текущей таблицы. */
    private JTable table;
    /** Выбранная строка в таблице. */
    private int clickedRow = -1;
    /** Переменная, хранящая информацию о том создается ли новая таблица или редактируется уже созданная. */
    private boolean flag = false;
    /** Переменная, хранящая информацию о том, с какой базой данной ведется работа: с базой, хранящейся на мобильном устройстве,
     * или - на компьютере. */
    private boolean isFromDevice;
    /** Экземпляр класса DeviceDBHelper, для работы с базой данных, расположенной на мобильном устройстве.
     * @see code.DataBaseHelper.DeviceDBHelper
     * */
    private DeviceDBHelper dDB;

    /**
     * Конструктор класса.
     * @param dataBaseName Имя базы данных
     * @param name Имя таблицы
     * @param mfc Контроллер класса MainFrameController
     * @see code.Controller.MainFrameController
     */
    public TableEditorController(String dataBaseName, String name, MainFrameController mfc)
    {
        if (name.equals("Create Table")) flag = true;
        this.dataBaseName = dataBaseName;
        initDB(dataBaseName);
        this.name = name;
        this.mfc = mfc;
        query = new ArrayList<String>();
        foreignKeysArray = new ArrayList<String>();
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
     * @param value Родительский контейнер
     */
    public void setDialog(JDialog value)
    {
        this.dialog = value;
    }

    /**
     * Инициализация модели таблицы, которая содержит всю информацию о полях таблицы.
     * @param value Модель таблицы
     */
    public void setTableModel(DefaultTableModel value)
    {
        this.tableModel = value;
    }

    /**
     * Инициализация таблицы.
     * @param value Таблица
     */
    public void setTable(JTable value)
    {
        this.table = value;
    }

    /**
     * Обработчик нажатия на кнопку add: добавляет новое поле в таблицу,
     * используя графический интерфейс класса FieldEditor.
     * @see code.GUI.FieldEditor
     */
    public void addFieldActionListener() {
        if (!flag) {
            if (!new FieldEditor(dataBaseName, false, name).getResult().isEmpty())
                table.setModel(tableModel = getDataForTable());
        }
        else
            addToArray(new FieldEditor(dataBaseName, true, null).getResult());
    }

    /**
     * Конструктор запроса, создающий таблицу: добавляет новое поле к общей команде.
     * @param tempResult Текущее созданное поле
     */
    private void addToArray(String tempResult) {
        if (!tempResult.isEmpty() && !tempResult.equals("cancel")) {
            String[] result = Regex.splitWithQuote(tempResult);
            if (!result[0].isEmpty()) {
                if (flag) query.add(result[0] + "\n, ");
                tableModel.addRow(Regex.getFieldDescription(result[0]));
            }
            if (result.length >= 2)
                if (!result[1].isEmpty())
                    if (flag) foreignKeysArray.add(result[1] + "\n, ");

        }
    }

    /**
     * Редактирование общей команды, создающей таблицу.
     * @param str Строка
     * @param index Индекс
     */
    private void editInArray(String str, int index)
    {
        String []result = Regex.splitWithQuote(str);
        if (!result[0].isEmpty()) {
            query.remove(clickedRow);
            query.add(result[0] + "\n, ");
            tableModel.removeRow(clickedRow);
            tableModel.addRow(Regex.getFieldDescription(result[0]));
        }
        if (result.length >= 2)
            if (!result[1].isEmpty()) {
                foreignKeysArray.remove(index);
                foreignKeysArray.add(result[1] + "\n, ");
            }
    }


    /**
     * Обработчик, отвечающий за редактирование поля.
     */
    public void editFieldActionListener()
    {
        if (clickedRow != -1) {
            if (flag) {
                String editInfo = "";
                editInfo += table.getValueAt(clickedRow, 1).toString() + "|";
                if (!table.getValueAt(clickedRow, 2).toString().isEmpty())
                    editInfo += table.getValueAt(clickedRow, 2).toString() + "|";
                else
                    editInfo += "NOPE|";
                if (!table.getValueAt(clickedRow, 3).toString().isEmpty() && table.getValueAt(clickedRow, 3).toString() != null)
                    editInfo += table.getValueAt(clickedRow, 3).toString() + "|";
                else
                    editInfo += "NOPE|";
                int foreignKeyIndex = -1;
                String sub = "";
                for (int i = 0; i < foreignKeysArray.size(); i++)
                    if (Regex.getMatches(rowInFocus + "[)]", foreignKeysArray.get(i)) >= 1) {
                        foreignKeyIndex = i;
                        sub = Regex.getReferenceInfo(foreignKeysArray.get(i));
                    }
                editInfo += sub;

                String tempResult = new FieldEditor(dataBaseName, rowInFocus, name, editInfo).getResult();
                if (!tempResult.equals("cancel"))
                    editInArray(tempResult, foreignKeyIndex);
            }
            else
                new FieldEditor(dataBaseName, rowInFocus, name);
        }
    }

    /**
     * Обработчик, отвечающий за удаление поля.
     */
    public void deleteFieldActionListener()
    {
        if (flag) {
            if (clickedRow != -1) {
                int n = JOptionPane.showConfirmDialog(dialog, "Delete field " + rowInFocus + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(clickedRow);
                    query.remove(clickedRow);
                    for (String str : foreignKeysArray)
                        if (Regex.getMatches(rowInFocus + "[)]", str) >= 1)
                            foreignKeysArray.remove(str);
                }

            }
        }
    }

    /**
     * Получение свойств полей таблицы.
     * @return модель для таблицы.
     */
    public DefaultTableModel getDataForTable()
    {
        String[] columnNames = {"Field Name", "Field Type", "Default Value", "Properties"};
        String []res;
        Object[][] data;
        if (isFromDevice) {
            data = Cast.arrayListTo2DArray(dDB.getDataFromTable(name));
            res = Regex.splitWithComma(dDB.getCreateElement(name));
        }
        else {
            db.connect();
            data = Cast.arrayListTo2DArray(db.getDataFromTable(name));
            res = Regex.splitWithComma(db.getCreateElement(name));
            db.close();
        }

        for (int i = 0; i < data.length; i++)
        {
            data[i][3] = Regex.getPropertiesOfFieldS(data[i][0].toString(), res[i]);
        }
        return new DefaultTableModel(data, columnNames);
    }

    /**
     * Обработчик нажатия на ячейку таблицы.
     * @param e MouseEvent
     */
    public void tableMouseAdapter(MouseEvent e)
    {
        clickedRow = table.rowAtPoint(e.getPoint());
        int col = table.columnAtPoint(e.getPoint());

        if (clickedRow > -1 && col > -1) {
            rowInFocus = table.getValueAt(clickedRow, 0).toString();
            if (e.getClickCount() == 2 && !flag)
                new FieldEditor(dataBaseName, rowInFocus, name);
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

    /**
     * Обработчик создания таблицы.
     * @param tableName Имя таблицы
     */
    public void createTableButtonListener(String tableName) {
        if (flag) {
            if (tableName.isEmpty())
                JOptionPane.showMessageDialog(dialog, "Enter table name!", "Error", JOptionPane.ERROR_MESSAGE);
            else {
                String resultQuery = "";
                String foreignKeys = "";

                if (query.size() >= 1) {
                    for (String str : query)
                        resultQuery += str;
                    resultQuery = resultQuery.substring(0, resultQuery.length() - 2);

                    resultQuery = "CREATE TABLE " + tableName + "(\n  " + resultQuery;
                    if (foreignKeysArray.size() >= 1) {
                        for (String str : foreignKeysArray)
                            foreignKeys += str;

                        foreignKeys = foreignKeys.substring(0, foreignKeys.length() - 2);
                        resultQuery += ", " + foreignKeys;
                    }
                    resultQuery += ");";
                }
                System.out.println(resultQuery);
                if (isFromDevice)
                    insertToDevice(resultQuery);
                else
                    insertToPC(resultQuery);
                mfc.updateTree();
                System.out.println("Create table!");
            }
        } else {
            if (!name.equals(tableName)) {
                String sql = "ALTER TABLE " + name + " RENAME TO " + tableName;
                if (isFromDevice)
                    insertToDevice(sql);
                else
                    insertToPC(sql);

                mfc.updateTree();
                System.out.println("Edit table!");
            }
            else
            {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }

    /**
     * Вставка таблицы в базу данных, расположенной на устройстве.
     * @param sql SQL - запрос, создающий таблицу
     */
    private void insertToDevice(String sql) {
        if (dDB.sqlCommand(sql)) {
            dialog.dispose();
            dialog.setVisible(false);
        }
    }

    /**
     * Вставка таблицы в базу данных, расположенной на компьютере.
     * @param sql SQL - запрос, создающий таблицу
     */
    private void insertToPC(String sql) {
        try {
            db.connect();
            db.executeQueryBySql(sql);
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
}
