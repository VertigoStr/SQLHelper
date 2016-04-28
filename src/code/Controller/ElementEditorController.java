package code.Controller;

import code.DataBaseHelper.DBHelper;
import code.DataBaseHelper.DeviceDBHelper;
import code.Utils.Keeper;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Контроллер для текстового поля ввода sql - запроса.
 * Используется для создания триггеров, представления, транзакций.
 */
public class ElementEditorController {
    /** Экземпляр класса DBHelper, для работы с базой данных, расположенной на компьютере.
     * @see code.DataBaseHelper.DBHelper
     * */
    private DBHelper db;

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
     * @param dataBaseName Имя базы данных
     */
    public ElementEditorController(String dataBaseName)
    {
        initDB(dataBaseName);
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
     * Обработчик нажатия на кнопку cancel: закрывает окно.
     */
    public void cancelButtonActionListener()
    {
        dialog.setVisible(false);
        dialog.dispose();
    }

    /**
     * Получение SQL - запроса, создающий указанный элемент.
     * @param name Имя элемента
     * @return SQL-запрос, для указанного элемента.
     */
    public String getCreateElement(String name) {
        if (isFromDevice) {
            name = dDB.getCreateElement(name);
        } else {
            db.connect();
            name = db.getCreateElement(name);
            db.close();
        }

        return name;
    }

    /**
     * Обработчик нажатия на кнопку create: создание элемента.
     * @param text SQL-запрос, создающий элемент
     */
    public void createButtonActionListener(JTextArea text) {
        String query = text.getText().toLowerCase();
        if (isFromDevice ? createOnDevice(query) : createOnPC(query)) {
            Object[] options = {"OK"};
            int n = JOptionPane.showOptionDialog(dialog, "Created!", "Result", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (n == JOptionPane.OK_OPTION) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }

    /**
     * Запрос на изменение элемента.
     * @param query SQL-запрос.
     * @return результат работы.
     */

    public boolean editElement(String query) {
        return isFromDevice ? createOnDevice(query) : createOnPC(query);
    }

    /**
     * Создание элемента для базы данных, расположенной на устройстве.
     * @param query SQL - запрос, создающий код.
     * @return результат работы.
     */
    private boolean createOnDevice(String query) {
        return dDB.sqlCommand(query);
    }

    /**
     * Создание элемента для базы данных, расположенной на компьютере.
     * @param query SQL - запрос, создающий код.
     * @return результат работы.
     */
    private boolean createOnPC(String query) {
        boolean flag = true;
        try {
            db.connect();
            db.executeQueryBySql(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
            String exMsg = "Message from MySQL Database";
            String exSqlState = "Exception";
            SQLException mySqlEx = new SQLException(exMsg, exSqlState);
            ex.setNextException(mySqlEx);
            String result = ex.toString();
            flag = false;
            JOptionPane.showMessageDialog(dialog, result, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            db.close();
        }
        return flag;
    }
}
