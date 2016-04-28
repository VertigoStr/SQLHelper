package code.DataBaseHelper;

import code.CodeGenerator.Parameter;
import code.Utils.Cast;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Класс для работы с базой данных, расположенной на компьютере.
 */
public class DBHelper {
    /** Имя базы данных. */
    private String name;
    /** Подключение к базе данных. */
    private Connection connection;
    /** Запрос. */
    private Statement statement;
    /** Результат запроса. */
    private ResultSet resultSet;

    /**
     * Конструтор класса.
     * @param name Имя базы данных
     */
    public DBHelper(String name)
    {
        this.name = name;
        connection = null;
        statement = null;
        resultSet = null;
    }

    /**
     * Возвращает имя базы данных.
     * @return имя базы данных.
     */
    public String getName() {
        return name;
    }

    /**
     * Подключение к базе данных.
     * @return результат работы.
     */
    public boolean connect()
    {
        boolean flag = true;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            flag = false;
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + name);
            statement = connection.createStatement();
        } catch (SQLException e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Закрытие подключения.
     */
    public void close()
    {
        try {
            connection.close();
            statement.close();
            if (resultSet != null)
                resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получение списка имен элементов базы данных, принадлежащих к определенной категории элементов.
     * @param name Имя категории
     * @return список имен.
     */
    public ArrayList<String> getNamesOf(String name)
    {
        ArrayList<String> list = new ArrayList<String>();
        String sql = "SELECT name FROM sqlite_master WHERE type = \"" + name + "\"";
        try {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {
                list.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Удаление элемента.
     * @param name Имя элемента
     * @param type Тип элемента
     * @throws SQLException
     */
    public void dropElement(String name, String type) throws SQLException
    {
        statement.execute("Drop " + type + " " + name);
    }

    /**
     * Получение информации о поле в таблице.
     * @param tableName Имя таблицы
     * @param fieldName Поле
     * @return информация о поле.
     */
    public ArrayList<String> getDataFromTable(String tableName, String fieldName)
    {
        ArrayList<String> list = new ArrayList<String>();
        String sql = "PRAGMA table_info(" + tableName + ")";
        try
        {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                list.add(resultSet.getString(fieldName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return list;
    }

    /**
     * Получение информации о поле в таблице.
     * @param tableName Имя таблицы
     * @param dataName Имя поля
     * @return информация о поле.
     */
    public ArrayList<String> getFieldSettingsFromTable(String tableName, String dataName)
    {
        ArrayList<String> list = new ArrayList<String>();
        String sql = "PRAGMA table_info(" + tableName + ")";
        try
        {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                if (dataName.equals(resultSet.getString("name"))) {
                    list.add(resultSet.getString("name"));
                    list.add(resultSet.getString("type"));
                    list.add(resultSet.getString("dflt_value"));
                    list.add(resultSet.getInt("notnull") == 0 ? "null" : "not null");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Получение информации о внешних ключах поля.
     * @param tableName Имя таблицы
     * @param fieldName Имя поля
     * @return список внешних ключей.
     */
    public ArrayList<String> getFieldForeignKeysFromTable(String tableName, String fieldName)
    {
        ArrayList<String> list = new ArrayList<String>();
        String sql = "PRAGMA foreign_key_list(" + tableName + ")";
        try
        {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                if (fieldName.equals(resultSet.getString("from"))) {
                    list.add(resultSet.getString("table"));
                    list.add(resultSet.getString("to"));
                    list.add(resultSet.getString("on_delete"));
                    list.add(resultSet.getString("on_update"));
                    list.add(resultSet.getString("match"));
                }
            }
        } catch (SQLException e) {
            String exMsg = "Message from MySQL Database";
            String exSqlState = "Exception";
            SQLException mySqlEx = new SQLException(exMsg, exSqlState);
            e.setNextException(mySqlEx);
            if (e.toString().equals("java.sql.SQLException: query does not return ResultSet"))
                System.out.println("Field hasn't references!");
            else
                e.printStackTrace();
        }
        return list;
    }

    /**
     * Получение общей информации о таблице.
     * @param tableName Имя таблицы
     * @return общая информация о таблице.
     */
    public ArrayList<ArrayList<Object>> getDataFromTable(String tableName)
    {
        ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
        String sql = "PRAGMA table_info(" + tableName + ")";
        try
        {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                ArrayList<Object> subresult = new ArrayList<Object>();

                subresult.add(resultSet.getString("name"));
                subresult.add(resultSet.getString("type"));
                subresult.add(resultSet.getString("dflt_value"));
                subresult.add(resultSet.getInt("notnull") == 0 ? "null" : "not null");
                result.add(subresult);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Выполнение запроса, возвращает таблицу.
     * @param sql Запрос
     * @return модель для таблицы.
     * @throws SQLException
     */
    public DefaultTableModel executeQuery(String sql) throws SQLException
    {
        ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
        ArrayList<Object> columns = new ArrayList<Object>();
        resultSet = statement.executeQuery(sql);
        int columnCount = resultSet.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++)
            columns.add(resultSet.getMetaData().getColumnName(i));

        while (resultSet.next()) {
            ArrayList<Object> subresult = new ArrayList<Object>();
            for (int i = 1; i <= columnCount; i++)
                subresult.add(resultSet.getObject(i));
            result.add(subresult);
        }
        return new DefaultTableModel(Cast.arrayListTo2DArray(result), columns.toArray());
    }

    /**
     * Получение всего содержимого таблицы.
     * @param tableName Имя таблицы
     * @return содержимое таблицы в виде модели.
     */
    public DefaultTableModel selectAllFromTable(String tableName)
    {
        ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
        ArrayList<Object> columns = new ArrayList<Object>();
        String sql = "select * from " + tableName + ";";
        try
        {
            resultSet = statement.executeQuery(sql);
            int columnCount = resultSet.getMetaData().getColumnCount();

            for (int i = 1; i <= columnCount; i++)
                columns.add(resultSet.getMetaData().getColumnName(i));

            while (resultSet.next()) {
                ArrayList<Object> subresult = new ArrayList<Object>();
                for (int i = 1; i <= columnCount; i++)
                    subresult.add(resultSet.getObject(i));
                result.add(subresult);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new DefaultTableModel(Cast.arrayListTo2DArray(result), columns.toArray());
    }

    /**
     * Получение sql-запроса создания элемента.
     * @param elementName Имя элемента
     * @return sql-запрос.
     */
    public String getCreateElement(String elementName)
    {
        String result = null;
        String sql = "SELECT sql FROM sqlite_master WHERE name = \"" + elementName + "\"";
        try
        {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {
                result = resultSet.getString("sql");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return  result;
    }

    /**
     * Получение всех полей таблицы.
     * @param name Имя таблицы
     * @return список полей таблицы.
     */
    public Map<String, String> getTypesM(String name)
    {
        Map<String, String> map = new LinkedHashMap<String, String>();
        String sql = "PRAGMA table_info(" + name + ")";
        try
        {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                map.put(resultSet.getString("name"), resultSet.getString("type"));
            }
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return map;
    }

    /**
     * Выполнение запроса.
     * @param sql Запрос
     * @throws SQLException
     */
    public void executeQueryBySql(String sql) throws SQLException
    {
        statement.execute(sql);
    }

    /**
     * Получение строки из запроса.
     * @param sql Запрос
     * @return список строк, удовлетворяющих запросу.
     */
    public ArrayList<String> getRowFromTableBySQL(String sql)
    {
        ArrayList<String> list = new ArrayList<String>();
        try
        {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next())
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    Object object = resultSet.getObject(i);
                    list.add(object != null ? object.toString() : "");
                }
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Получение определенной строки из таблицы.
     * @param name Имя таблицы
     * @param id Номер строки
     * @return содержимое строки.
     */
    public ArrayList<String> getRowFromTable(String name, int id)
    {
        ArrayList<String> list = new ArrayList<String>();
        String sql = "select * from " + name + " where id = " + id;
        try
        {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next())
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++)
                    list.add(resultSet.getObject(i).toString());
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return  list;
    }

    /**
     * Получение всего содержимого таблицы с ограничением в количестве строк.
     * @param tableName Имя таблицы
     * @param start Номер строки, с которого выбираем данные
     * @param end Количество данных
     * @return модель для таблицы.
     */
    public DefaultTableModel selectAllFromTableWithLimit(String tableName, int start, int end)
    {
        ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
        ArrayList<Object> columns = new ArrayList<Object>();
        String sql = "select * from " + tableName + " LIMIT " + start + ", " + end + ";";
        try
        {
            resultSet = statement.executeQuery(sql);
            int columnCount = resultSet.getMetaData().getColumnCount();

            for (int i = 1; i <= columnCount; i++)
                columns.add(resultSet.getMetaData().getColumnName(i));

            while (resultSet.next()) {
                ArrayList<Object> subresult = new ArrayList<Object>();
                for (int i = 1; i <= columnCount; i++)
                    subresult.add(resultSet.getObject(i));
                result.add(subresult);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new DefaultTableModel(Cast.arrayListTo2DArray(result), columns.toArray());
    }

    /**
     * Получить количество строк в таблице.
     * @param name Имя таблицы
     * @return количество строк.
     */
    public int getRowCount(String name)
    {
        int result = 0;
        String sql = "select count(*) from " + name + ";";
        try
        {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next())
                result = resultSet.getInt(1);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Получить все поля таблицы, используя экземпляр класса Parameter.
     * @param name Имя таблицы
     * @return список полей таблицы.
     * @see code.CodeGenerator.Parameter
     */
    public ArrayList<Parameter> getTypesP(String name)
    {
        ArrayList<Parameter> list = new ArrayList<Parameter>();
        String sql = "PRAGMA table_info(" + name + ")";
        try {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Parameter parameter  = new Parameter(resultSet.getString("type"), resultSet.getString("name"));
                parameter.recastType();
                list.add(parameter);
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * Получение sql-запроса создания таблицы.
     * @param name Имя элемента
     * @return sql-запрос.
     */
    public String getSQLQuery(String name)
    {
        String result = "";
        String sql = "SELECT sql FROM sqlite_master WHERE type = \"table\" and name = \"" + name + "\"";
        try {
            resultSet = statement.executeQuery(sql);
            while (resultSet.next())
                result += resultSet.getString("sql").replace("\n", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
