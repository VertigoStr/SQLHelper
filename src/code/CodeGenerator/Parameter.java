package code.CodeGenerator;

/**
 * Класс, работающий с полями таблицы.
 */
public class Parameter {
    /** Имя параметра. */
    private String name;
    /** Тип параметра. */
    private String type;

    /**
     * Конструктор класса.
     * @param type Тип
     * @param name Имя
     */
    public Parameter(String type, String name)
    {
        this.type = type;
        this.name = name;
    }

    /**
     * Получение имени.
     * @return имя.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Получение типа.
     * @return тип.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Преобразование к строке.
     * @return строка, содержащая все поля класса.
     */
    public String toString()
    {
        return type + " " + name;
    }

    /**
     * Преобразование типов SQLite к типам Java.
     */
    public void recastType()
    {
        if (type.equalsIgnoreCase("INTEGER") || type.equalsIgnoreCase("MEDIUMINT") || type.equalsIgnoreCase("INT2")
                || type.equalsIgnoreCase("INT8") || type.equalsIgnoreCase("INT") || type.equalsIgnoreCase("SMALLINT")
                || type.equalsIgnoreCase("TINYINT"))
            type = "Integer";
        if (type.equalsIgnoreCase("BIGINT") || type.equalsIgnoreCase("UNSIGNED BIG INT"))
            type = "Long";
        if (type.toLowerCase().contains("CHARACTER".toLowerCase())
                || type.toLowerCase().contains("VARCHAR".toLowerCase())
                || type.toLowerCase().contains("VARYING CHARACTER".toLowerCase())
                || type.toLowerCase().contains("NCHAR".toLowerCase())
                || type.toLowerCase().contains("NATIVE CHARACTER".toLowerCase())
                || type.toLowerCase().contains("NVARCHAR".toLowerCase())
                || type.toLowerCase().contains("TEXT".toLowerCase())
                || type.toLowerCase().contains("CLOB".toLowerCase())
                )
            type = "String";
        if (type.equalsIgnoreCase("BLOB"))
            type = "String";
        if (type.equalsIgnoreCase("REAL") || type.equalsIgnoreCase("DOUBLE") || type.equalsIgnoreCase("DOUBLE PRECISION"))
            type = "Double";
        if (type.equalsIgnoreCase("FLOAT"))
            type = "Float";
        if (type.equalsIgnoreCase("DATE") || type.equalsIgnoreCase("DATETIME") || type.equalsIgnoreCase("TIMESTAMP") || type.equalsIgnoreCase("TIME"))
            type = "String";
        if (type.equalsIgnoreCase("BOOLEAN"))
            type = "Boolean";
        if (type.equalsIgnoreCase("DECIMAL"))
            type = "String";
        if (type.equalsIgnoreCase("NUMERIC"))
            type = "Integer";
    }
}
