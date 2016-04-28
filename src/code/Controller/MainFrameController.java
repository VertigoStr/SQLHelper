package code.Controller;

import code.DataBaseHelper.DBHelper;
import code.DataBaseHelper.DeviceDBHelper;
import code.GUI.ElementEditor;
import code.GUI.InputDialog;
import code.GUI.TableEditor;
import code.Utils.InputCreator;
import code.Utils.Keeper;
import code.Utils.Regex;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * Контроллер главного окна.
 * Управление элементами базы данных.
 */
public class MainFrameController {
    /** Экземпляр класса DBHelper, для работы с базой данных, расположенной на компьютере.
     * @see code.DataBaseHelper.DBHelper
     * */
    private DBHelper db;
    /** Экземпляр класса DeviceDBHelper, для работы с базой данных, расположенной на мобильном устройстве.
     * @see code.DataBaseHelper.DeviceDBHelper
     * */
    private DeviceDBHelper dDB;
    /** Родительский контейнер. */
    private JFrame frame;
    /** Дерево элементов. */
    private JTree tree;
    /** Имя базы данных. */
    private String dataBaseName;
    /** Имя выбранной таблицы. */
    private String selectedTableName = "";
    /** Текущая страница. */
    private int page;
    /** Выбранная строка. */
    private int selectedRow = -1;
    /** Максимальное количество страниц. */
    private int maxPage;
    /** Ограничение по количество выводимых строк таблицы. */
    private int limit;
    /** Проверка перелистывания страницы. */
    private boolean doubleCheck = false;
    /** Текстовое поле, хранящее номер текущей страницы. */
    private JTextField pageField;
    /** Таблица выбора. */
    private JTable selectedTable;
    /** Таблица результатов. */
    private JTable resultSetTable;
    /** Выбор ограничения на количество выводимых строк. */
    private JComboBox<String> limiter;
    /** Переменная, хранящая информацию о том, с какой базой данной ведется работа: с базой, хранящейся на мобильном устройстве,
     * или - на компьютере. */
    private boolean isFromDevice;
    /** Имя мобильного устройства. */
    private String deviceName;

    /**
     * Конструктор класса.
     * @param dataBaseName Имя базы данных
     */
    public MainFrameController(String dataBaseName)
    {
        this.dataBaseName = dataBaseName;
        db = new DBHelper(dataBaseName);
        this.deviceName = "|";
        page = 0;
        maxPage = 0;
        limit = -1;
        isFromDevice = false;
    }

    /**
     * Конструктор класса.
     * @param dataBaseName Имя базы данных
     * @param deviceName Имя устройства
     */
    public MainFrameController(String dataBaseName, String deviceName) {
        this.dataBaseName = dataBaseName;
        this.deviceName = deviceName + "|";
        dDB = new DeviceDBHelper(Keeper.getSDKLocation(), deviceName, dataBaseName);
        db = null;
        page = 0;
        maxPage = 0;
        limit = -1;
        isFromDevice = true;
    }

    /**
     * Получение имени базы данных.
     * @return имя базы данных.
     */
    public String getDataBaseName()
    {
        return deviceName.equals("|") ? dataBaseName : deviceName + dataBaseName;
    }

    /**
     * Инициализация родительского контейнера.
     * @param frame Родительский контейнер
     */
    public void setFrame(JFrame frame)
    {
        this.frame = frame;
    }

    /**
     * Инициализация дерева элементов.
     * @param tree Дерево элементов
     */
    public void setTree(JTree tree)
    {
        this.tree = tree;
    }

    /**
     * Обновление дерева элементов.
     */
    public void updateTree()
    {
        tree.setModel(createNodes());
    }

    /**
     * Инициализация выбранной таблицы.
     * @param table Выбранная таблица
     */
    public void setSelectedTable(JTable table)
    {
        this.selectedTable = table;
    }

    /**
     * Инициализация поля для ввода страниц.
     * @param field Поле для ввода страниц
     */
    public void setPageField(JTextField field)
    {
        this.pageField = field;
    }

    /**
     * Инициализация таблицы результата.
     * @param table Таблица результата
     */
    public void setResultSetTable(JTable table)
    {
        this.resultSetTable = table;
    }

    /**
     * Обработчик закрытия окна.
     * @param e WindowEvent
     */
    public void windowCloseListener(WindowEvent e)
    {
        e.getWindow().dispose();
    }

    /**
     * Инициализация ограничителя вывода строк в таблице.
     * @param limiter Ограничитель вывода строк
     */
    public void setLimiter(JComboBox<String> limiter)
    {
        this.limiter = limiter;
    }

    /**
     * Обработчик перехода к следующим окнам.
     * @param e ActionEvent
     * @param elementInFocus Выбранный элемент
     * @param parentElementOfFocusElement Родительский элемент выбранного элемента
     * @param listOfCategories Лист категорий элементов
     */
    public void popUpMenuActionListenerCreateEdit(ActionEvent e, final String elementInFocus, final String parentElementOfFocusElement, final String[] listOfCategories) {
        final String actionCommand = e.getActionCommand();

        if (elementInFocus == null && parentElementOfFocusElement == null) {
            //Create
            if (!actionCommand.equals("Create " + listOfCategories[0])) {
                System.out.println("1");
                new ElementEditor(deviceName + dataBaseName, actionCommand, this);
            }
            //Edit
            else {
                System.out.println("2");
                new TableEditor(deviceName + dataBaseName, actionCommand, this);
            }
        } else {
            //Create Table Block
            assert elementInFocus != null;
            if (elementInFocus.equals("Tables")) {
                System.out.println("3");
                new TableEditor(deviceName + dataBaseName, actionCommand, this);
            }

            if (parentElementOfFocusElement.equals("Tables")) {
                System.out.println("4");
                //Create
                if (actionCommand.equals("Create " + listOfCategories[0])) {
                    System.out.println("5");
                    new TableEditor(deviceName + dataBaseName, actionCommand, this);
                }
                //Edit
                else {
                    System.out.println("6");
                    if (actionCommand.equals("Edit " + listOfCategories[0]))
                        new TableEditor(deviceName + dataBaseName, actionCommand, elementInFocus, this);
                    else
                        new ElementEditor(deviceName + dataBaseName, actionCommand, this);
                }
            }
            //Other Blocks
            if (!elementInFocus.equals("Tables") && !parentElementOfFocusElement.equals("Tables")) {
                //Create
                System.out.println("7");
                if (!actionCommand.equals("Create " + listOfCategories[0]) && actionCommand.contains("Create ")) {
                    new ElementEditor(deviceName + dataBaseName, actionCommand, this);
                }
                //Edit
                else {
                    System.out.println("9");
                    if (!actionCommand.equals("Create " + listOfCategories[0]))
                        new ElementEditor(deviceName + dataBaseName, actionCommand, elementInFocus, this);
                    else
                        new TableEditor(deviceName + dataBaseName, actionCommand, this);
                }
            }
        }
    }

    /**
     * Обработчик удаления элемента в базе данных.
     * @param e ActionEvent
     * @param elementInFocus Выбранный элемент
     */
    public void popUpMenuActionListenerDelete(ActionEvent e, String elementInFocus)
    {
        int n = JOptionPane.showConfirmDialog(frame, e.getActionCommand() + " " + elementInFocus + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getParentPath().getLastPathComponent();
            int index = node.getParent().getIndex(node);
            if (isFromDevice)
                deleteTreeElementFromDevice(elementInFocus, e.getActionCommand());
            else
                deleteTreeElementFromPC(elementInFocus, e.getActionCommand());
            if (Regex.getMatches("Table", e.getActionCommand()) > 0)
                selectedTable.setModel(new DefaultTableModel());
            updateTree();
            tree.expandRow(index + 1);
        }
    }


    /**
     * Удаление элемента из базы данных, расположенной на устройстве.
     * @param elementInFocus Выбранный элемент
     * @param actionCommand Тип элемента
     */
    private void deleteTreeElementFromDevice(String elementInFocus, String actionCommand) {
        dDB.sqlCommand("Drop " + actionCommand.split(" ")[1].toLowerCase() + " " + elementInFocus);
    }

    /**
     * Удаление элемента из базы данных, расположенной на компьютере.
     * @param elementInFocus Выбранный элемент
     * @param actionCommand Тип элемента
     */
    private void deleteTreeElementFromPC(String elementInFocus, String actionCommand) {
        try {
            db.connect();
            db.dropElement(elementInFocus, actionCommand.split(" ")[1].toLowerCase());
        } catch (SQLException ex) {
            ex.printStackTrace();
            String exMsg = "Message from MySQL Database";
            String exSqlState = "Exception";
            SQLException mySqlEx = new SQLException(exMsg, exSqlState);
            ex.setNextException(mySqlEx);
            String result = ex.toString();
            JOptionPane.showMessageDialog(frame, result, "Error", JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            db.close();
        }

    }

    /**
     * Обработчик добавления новой строки в таблицу.
     */
    public void addNewRowButtonListener()
    {

        if (!selectedTableName.isEmpty()) {
            if (new InputDialog(selectedTableName, deviceName + dataBaseName).isDone()) {
                initLimit();
                reloadMaxPage();
                pageFieldCheck();
                setTableModel();
            }
        }
        else
            JOptionPane.showMessageDialog(frame, "Select table!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Обработчик обновления строки в таблице.
     */
    public void updateRowButtonListener()
    {
        if (selectedRow != -1 && !selectedTableName.isEmpty()) {
            String result = " from " + selectedTableName + " where ";
            for (int i = 0; i < selectedTable.getModel().getColumnCount(); i++) {
                if (selectedTable.getValueAt(selectedRow, i) != null) {
                    String value = selectedTable.getValueAt(selectedRow, i).toString();
                    if (!value.isEmpty())
                        result += selectedTable.getModel().getColumnName(i) + " = \"" + value + "\" and ";

                }
            }
            result = result.substring(0, result.length() - 4);

            if (new InputDialog(selectedTableName, deviceName + dataBaseName, result).isDone()) {
                initLimit();
                reloadMaxPage();
                pageFieldCheck();
                setTableModel();
            }
        }
        else
            JOptionPane.showMessageDialog(frame, "Select row or table!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Обработчик удаления строки из таблицы.
     */
    public void deleteRowButtonListener()
    {
        if (selectedRow != -1 && !selectedTableName.isEmpty()) {
            int n = JOptionPane.showConfirmDialog(frame, "Delete this row?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                String result = "delete from " + selectedTableName + " where ";
                for (int i = 0; i < selectedTable.getModel().getColumnCount(); i++) {
                    Object value = selectedTable.getValueAt(selectedRow, i);
                    if (value != null)
                        if (!value.toString().isEmpty())
                            result += selectedTable.getModel().getColumnName(i) + " = \"" + value.toString() + "\" and ";
                }
                result = result.substring(0, result.length() - 4);
                if (isFromDevice)
                    deleteFromDevice(result);
                else
                    deleteFromPC(result);
            }
        }
        else
            JOptionPane.showMessageDialog(frame, "Select row or table!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Удаление строки из таблицы из базы данных, расположенной на устройстве.
     * @param result SQL - запрос
     */
    public void deleteFromDevice(String result) {
        if (dDB.sqlCommand(result)) {
            initLimit();
            reloadMaxPage();
            pageFieldCheck();
            setTableModel();
        }
    }

    /**
     * Удаление строки из таблицы из базы данных, расположенной на компьютере.
     * @param result SQL - запрос
     */
    public void deleteFromPC(String result) {
        try {
            db.connect();
            db.executeQueryBySql(result);
        } catch (SQLException ex) {
            String exMsg = "Message from MySQL Database";
            String exSqlState = "Exception";
            SQLException mySqlEx = new SQLException(exMsg, exSqlState);
            ex.setNextException(mySqlEx);
            JOptionPane.showMessageDialog(frame, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            db.close();
            initLimit();
            reloadMaxPage();
            pageFieldCheck();
            setTableModel();
        }
    }

    /**
     * Обработчик выбора ячейки в таблице.
     * @param e MouseEvent
     */
    public void tableMouseListener(MouseEvent e) {
        int row = selectedTable.rowAtPoint(e.getPoint());
        int col = selectedTable.columnAtPoint(e.getPoint());
        selectedRow = row;
        if (e.getClickCount() == 2)
            if (row > -1 && col > -1) {
                Object cell;
                if ((cell = selectedTable.getValueAt(row, col)) != null) {
                    InputCreator.textDialog(cell.toString());
                }
            }
    }

    /**
     * Обработчик изменения ораничения вывода количества строк в таблице.
     */
    public void limitBoxListener() {
        page = 1;
        initLimit();
        reloadMaxPage();
        pageFieldCheck();
        setTableModel();
    }

    /**
     * Обработчик выбора таблицы для отображения.
     * @param e MouseEvent
     */
    public void treePanelMouseListener(MouseEvent e)
    {
        TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
        if (tp != null) {
            String current = tp.getLastPathComponent().toString();
            if (!current.equals(tree.getModel().getRoot().toString())) {
                String parent = tp.getParentPath().getLastPathComponent().toString();
                if (parent.equals("Tables")) {
                    selectedTableName = current;
                    initLimit();
                    reloadMaxPage();
                    pageField.setText("1");
                    page = 1;
                    setTableModel();
                }
            }
        }
    }

    /**
     * Обработчик нажатия на кнопку create: создание sql-запроса.
     * @param text Текст запроса
     * @param pane Окно с вкладками
     */
    public void createSqlQueryButtonListener(JTextArea text, JTabbedPane pane) {
        //String query = text.getText();
        for (String query : text.getText().split(";")) {
            if (!query.isEmpty()) {
                if (query.toLowerCase().contains("begin") || query.toLowerCase().contains("end")) {
                    JOptionPane.showMessageDialog(frame, "Transaction not supported!", "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                else {
                    if (!isFromDevice)
                        resultSetExecuteOnPC(pane, query);
                    else {
                        DefaultTableModel model = dDB.getTableBySQL(query, frame, pane);
                        if (model != null)
                            resultSetTable.setModel(model);
                        else
                            updateTree();
                    }
                }
            }
        }
    }

    /**
     * Вывод содержимого результат запроса на таблицу для базы данных, расположенной на компьютере.
     * @param pane Окно с вкладками
     * @param query Запрос
     */
    public void resultSetExecuteOnPC(JTabbedPane pane, String query) {
        int count = Regex.getMatches("select|pragma", query.toLowerCase());
        try {
            db.connect();
            if (count != 0) {
                DefaultTableModel dtModel = db.executeQuery(query);
                resultSetTable.setModel(dtModel);
                pane.setSelectedIndex(2);
            } else {
                db.executeQueryBySql(query);
                updateTree();
            }
        } catch (SQLException ex) {
            String exMsg = "Message from MySQL Database";
            String exSqlState = "Exception";
            SQLException mySqlEx = new SQLException(exMsg, exSqlState);
            ex.setNextException(mySqlEx);
            String result = ex.toString();
            if (result.equals("java.sql.SQLException: query does not return ResultSet"))
                JOptionPane.showMessageDialog(frame, "Table hasn't foreign keys!", "Error", JOptionPane.ERROR_MESSAGE);
            else {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, result, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            db.close();
        }
    }

    /**
     * Обработчик выбора ячеек на таблице результатов.
     * @param e MouseEvent
     */
    public void resultTableMouseListener(MouseEvent e) {
        int row = resultSetTable.rowAtPoint(e.getPoint());
        int col = resultSetTable.columnAtPoint(e.getPoint());
        if (e.getClickCount() == 2)
            if (row > -1 && col > -1) {
                Object cell;
                if ((cell = resultSetTable.getValueAt(row, col)) != null) {
                    InputCreator.textDialog(cell.toString());
                }
            }
    }

    /**
     * Создание узлов для дерева элементов.
     * @return модель для дерева.
     */
    public DefaultTreeModel createNodes() {
        return isFromDevice ? createNodesFromDevice() : createNodesFromPC();
    }

    /**
     * Создание узлов для дерева элементов для базы данных, расположенной на устройстве.
     * @return модель для дерева.
     */
    private DefaultTreeModel createNodesFromDevice() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new File(dataBaseName).getName().split("\\.")[0].toUpperCase());
        DefaultMutableTreeNode category;
        DefaultMutableTreeNode element;
        ArrayList<String> listOfElements;
        String[] listOfCategories = {"Tables", "Indexes", "Views", "Triggers"};
        String[] listOfSearchElement = {"table", "index", "view", "trigger"};

        for (int i = 0; i < listOfSearchElement.length; i++) {
            category = new DefaultMutableTreeNode(listOfCategories[i]);
            top.add(category);
            listOfElements = dDB.getNamesOf(listOfSearchElement[i]);
            for (String listOfElement : listOfElements) {
                element = new DefaultMutableTreeNode(listOfElement);
                category.add(element);
            }
            listOfElements.clear();
        }

        return new DefaultTreeModel(top);

    }

    /**
     * Создание узлов для дерева элементов для базы данных, расположенной на компьютере.
     * @return модель для дерева.
     */
    private DefaultTreeModel createNodesFromPC() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new File(dataBaseName).getName().split("\\.")[0].toUpperCase());
        DefaultMutableTreeNode category;
        DefaultMutableTreeNode element;
        ArrayList<String> listOfElements;
        String[] listOfCategories = {"Tables", "Indexes", "Views", "Triggers"};
        String[] listOfSearchElement = {"table", "index", "view", "trigger"};

        db.connect();
        for (int i = 0; i < listOfSearchElement.length; i++) {
            category = new DefaultMutableTreeNode(listOfCategories[i]);
            top.add(category);
            listOfElements = db.getNamesOf(listOfSearchElement[i]);
            for (String listOfElement : listOfElements) {
                element = new DefaultMutableTreeNode(listOfElement);
                category.add(element);
            }
            listOfElements.clear();
        }
        db.close();

        return new DefaultTreeModel(top);
    }


    /**
     * Обработчик, отвечающий за получение информации о номере текущей страницы.
     */
    public void pageFieldListener()
    {
        page = Regex.getIntegerFromStr(pageField.getText());
        pageFieldCheck();
        doubleCheck = true;
        setTableModel();
    }

    /**
     * Обработчик нажатия на кнопку left в выборе страницы.
     */
    public void leftPageButton() {
        page = Regex.getIntegerFromStr(pageField.getText()) - 1;
        pageFieldCheck();
        doubleCheck = true;
        setTableModel();
    }

    /**
     * Обработчик нажатия на кнопку right в выборе страницы.
     */
    public void rightPageButton() {
        page = Regex.getIntegerFromStr(pageField.getText()) + 1;
        pageFieldCheck();
        doubleCheck = true;
        setTableModel();
    }

    /**
     * Отображение выбранной таблицы с учетом номера страницы и ограничения.
     */
    private void setTableModel()
    {
        if (!selectedTableName.isEmpty()) {
            if (limit != -1) {
                page = page - 1 < 0 ? page : page - 1;
                int start = page * limit;
                int end = limit;
                DefaultTableModel dtModel;
                if (isFromDevice) {
                    dtModel = limit == -2
                            ? dDB.selectAllFromTable(selectedTableName)
                            : dDB.selectAllFromTableWithLimit(selectedTableName, start, end);
                }
                else {
                    db.connect();
                    dtModel = limit == -2
                            ? db.selectAllFromTable(selectedTableName)
                            : db.selectAllFromTableWithLimit(selectedTableName, start, end);
                    db.close();
                }
                selectedTable.setModel(dtModel);
            }
        }
        else {
            JOptionPane.showMessageDialog(frame, "Select table!", "Error", JOptionPane.ERROR_MESSAGE);
            if (!doubleCheck)
                limiter.setSelectedIndex(0);
            doubleCheck = false;
        }
    }

    /**
     * Проверка: не выходит ли текущий номер страницы за границы.
     */
    private void pageFieldCheck()
    {
        pageField.setText(page + "");
        if (page > maxPage)
        {
            page = maxPage;
            pageField.setText(maxPage + "");
        }
        if (page < 1)
        {
            page = 1;
            pageField.setText(1 + "");
        }
    }

    /**
     * Получение значение в поле ограничения номера страниц.
     */
    private void initLimit()
    {
        String selectedItem = limiter.getSelectedItem().toString().split(": ")[1];
        limit = selectedItem.equals("MAX") ? -2 : Integer.parseInt(selectedItem);
    }

    /**
     * Пересчет максимального номера страниц.
     */
    private void reloadMaxPage()
    {
        if (!selectedTableName.isEmpty()) {
            if (isFromDevice) {
                maxPage = dDB.getRowCount(selectedTableName);
            }
            else {
                db.connect();
                maxPage = db.getRowCount(selectedTableName);
                db.close();
            }
            int mod = maxPage % (limit == -2 ? maxPage : limit);
            maxPage /= limit == -2 ? maxPage : limit;
            maxPage += mod >= 1 ? 1 : mod;
        }
    }
}