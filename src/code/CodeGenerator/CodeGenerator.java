package code.CodeGenerator;

import code.Utils.Cast;
import code.Utils.Regex;
import code.Utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс генерирующий java код.
 */
public class CodeGenerator {

    /** Имя управляющего класса. */
    private String mainClassName;

    /** Имя пакета, хранящего сгенерированный код. */
    private String packageName;

    /** Имя базы данных. */
    private String dataBaseName;

    /** Результат работы генератора кода. */
    private boolean result = false;

    /** Сообщение о результате генерации кода. */
    private String resultMessage;

    /** Папка, в которой будет расположен сгенерированный код. */
    private String folder;

    /** Тип косых черт. */
    private String slash;

    /** Список параметров для каждой таблицы. */
    private Map<String, ArrayList<Parameter>> tables;

    /** Список sql - запросов, создающих элементы базы данных. */
    private HashMap<String, ArrayList<String>> sqlQuery;

    /**
     * Конструктор генератора кода.
     * @param name Имя управляющего класса
     * @param packageName Имя папки
     * @param tables Список таблиц
     * @param dataBaseName Путь к базе данных
     * @param folder Имя папки, для расположения кода
     */
    public CodeGenerator(String name, String packageName, Map<String, ArrayList<Parameter>> tables, String dataBaseName, String folder)
    {
        initParams(false, folder, packageName, dataBaseName);
        this.mainClassName = name;
        this.tables = tables;
        generateJava();
    }

    /**
     * Конструктор генератора кода.
     * @param flag С устройства или с компьютера: true - устройство, false - компьютер
     * @param name Имя управляющего класса
     * @param packageName Имя папки
     * @param tables Список таблиц
     * @param dataBaseName Путь к базе данных
     * @param folder Имя папки, для расположения кода
     * @param sqlQuery Список всех sql-запросов создания элементов базы данных
     */
    public CodeGenerator(boolean flag, String name, String packageName, Map<String, ArrayList<Parameter>> tables, String dataBaseName, String folder, HashMap<String, ArrayList<String>> sqlQuery)
    {
        initParams(flag, folder, packageName, dataBaseName);
        this.mainClassName = name;
        this.tables = tables;
        this.sqlQuery = sqlQuery;
        generateAndroidSDK();
    }

    /**
     * Инициализация параметров.
     * @param flag С устройства или с компьютера: true - устройство, false - компьютер
     * @param folder Имя папки, для расположения кода
     * @param packageName Имя папки
     * @param dataBaseName Путь к базе данных
     */
    public void initParams(boolean flag, String folder, String packageName, String dataBaseName) {
        if (flag) {
            this.dataBaseName = Regex.getNameOfDataBase(dataBaseName);
        } else {
            this.dataBaseName = Utils.isWin() ? Regex.addSlash(dataBaseName) : "/" + dataBaseName.replace("\\", "/");
        }
        String subPackage =  Regex.getMatch("com[\\\\/](.+)", folder);
        subPackage = subPackage != null ? "com." + subPackage.replace("\\", ".").replace("/", ".") : "";
        this.packageName = folder.contains("com\\") || folder.contains("com/")
                ? subPackage + "." + packageName
                : packageName;

        if (Utils.isWin()) {
            folder = folder + (folder.endsWith("\\") ? "" : "\\");
            slash = "\\";
        } else {
            folder = folder.replace("\\", "/") + (folder.endsWith("/") ? "" : "/");
            slash = "/";
        }
        this.folder = folder + packageName;
    }

    /**
     * Проверка: закончена ли генерация кода.
     * @return результат работы.
     */
    public boolean isDone() {
        return result;
    }

    /**
     * Получение сообщения о результате генерации.
     * @return результат работы.
     */
    public String getMessage() {
        return resultMessage;
    }

    /**
     * Генерация кода для Android SDK.
     */
    public void generateAndroidSDK() {
        StringBuffer sb = new StringBuffer();
        try {
            if (new File(folder).mkdir()) {

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(folder + slash + mainClassName + ".java"),
                        "UTF-8"
                ));
                bw.write(createMainClassAndroidSDK());
                bw.close();
                bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(folder + slash + mainClassName + "Controller.java"),
                        "UTF-8"
                ));
                bw.write(createControllerAndroidSDK());
                bw.close();

                for (Map.Entry entry: tables.entrySet()) {
                    String tableName = entry.getKey().toString();
                    JavaCodeFile javaCode = new JavaCodeFile(packageName);
                    javaCode.setImports(new ArrayList<String>(Arrays.asList(
                            "android.content.Context",
                            "java.util.ArrayList"
                    )));

                    ClassSource classSource = new ClassSource(tableName, "public");
                    classSource.setExtends(mainClassName + "Controller");
                    classSource.addFieldWithDefaultValue("private final", "String", "TABLE_NAME", "\"" + tableName + "\"", false);

                    ArrayList<Parameter> pList = Cast.objectToArrayList(entry.getValue());
                    String insertFields = "{";
                    String insertValues = "{";
                    String insertAddQuotesFields = "";
                    for (Parameter p: pList) {
                        classSource.addFieldWithDefaultValue("public static final", "String", p.getName(), "\"" + p.getName() + "\"", false);
                        insertFields += tableName + "." + p.getName() + ", ";
                        insertValues += p.getName() + ", ";

                        if (p.getType().equalsIgnoreCase("String")) {
                            insertAddQuotesFields += p.getName() + " = " + p.getName() + " != null ? " + "\"\\\"\" + " + p.getName() + " + \"\\\"\" : null;\n\t\t";
                        }
                    }
                    insertValues = insertValues.substring(0, insertValues.length() - 2) + "};";
                    insertFields = insertFields.substring(0, insertFields.length() - 2) + "};";
                    classSource.addField("private", "Context", "context", false);

                    ArrayList<MethodGenerator> methods = new ArrayList<MethodGenerator>();

                    MethodGenerator mg = new MethodGenerator(tableName, new ArrayList<Parameter>(Arrays.asList(new Parameter("Context", "context"))));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "this.context = context;"
                    )));
                    methods.add(mg);


                    mg = new MethodGenerator("public", "insert", "void", Cast.objectToArrayList(entry.getValue()));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            insertAddQuotesFields,
                            "Object[] values_ar = " + insertValues,
                            "String[] fields_ar = " + insertFields,
                            "String values = \"\", fields = \"\";",
                            "for (int i = 0; i < values_ar.length; i++) {",
                            "if (values_ar[i] != null) {",
                            "values += values_ar[i] + \", \";",
                            "fields += fields_ar[i] + \", \";",
                            "}",
                            "}",
                            "if (!values.isEmpty()) {",
                            "values = values.substring(0, values.length() - 2);",
                            "fields = fields.substring(0, fields.length() - 2);",
                            "super.execute(context, \"INSERT INTO \" + TABLE_NAME + \"(\" + fields + \") values(\" + values + \");\");",
                            "}"
                    )));
                    methods.add(mg);


                    mg = new MethodGenerator("public", "delete", "void",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "whatField"),
                                    new Parameter("String", "whatValue")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "super.delete(context, TABLE_NAME, whatField + \" = \" + whatValue);"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "update", "void",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "whatField"),
                                    new Parameter("String", "whatValue"),
                                    new Parameter("String", "whereField"),
                                    new Parameter("String", "whereValue")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "super.execute(context, \"UPDATE \" + TABLE_NAME + \" set \" + whatField + \" = \\\"\" + whatValue + \"\\\" where \" + whereField + \" = \\\"\" + whereValue + \"\\\";\");"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "select", "ArrayList<ArrayList<String>>",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "fields"),
                                    new Parameter("String", "whatField"),
                                    new Parameter("String", "whatValue"),
                                    new Parameter("String", "sortField"),
                                    new Parameter("String", "sort")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "String query = \"SELECT \";",
                            "query += fields == null ? \" * FROM \" + TABLE_NAME : fields + \" FROM \" + TABLE_NAME;",
                            "query += whatField != null && whatValue != null ? \" WHERE \" + whatField + \" = \\\"\" + whatValue + \"\\\"\" : \"\";",
                            "query += sort != null && sortField != null ? \" order by \" + sortField + \" \" + sort : \"\";",
                            "return super.executeQuery(context, query);"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "getExecuteResult", "ArrayList<ArrayList<String>>",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "query")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "return super.executeQuery(context, query);"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "execute", "void",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "query")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "super.execute(context, query);"
                    )));
                    methods.add(mg);


                    classSource.addMethods(methods);

                    javaCode.addClassSource(classSource);
                    javaCode.output(sb);

                    bw = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(folder + slash + entry.getKey().toString() + ".java"),
                            "UTF-8"
                    ));
                    bw.write(sb.toString());
                    bw.close();
                    sb = new StringBuffer();
                }
                resultMessage = "done";
                result = true;
            }
            else {
                result = false;
                resultMessage = "Package\'ve already created. Input another package name.";
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            resultMessage = ex.getMessage();
            result = false;
        }
    }

    /**
     * Генерация кода для главного класса для Android SDK.
     * @return код главного класса.
     */
    public String createMainClassAndroidSDK() {
        StringBuffer sb = new StringBuffer();
        ArrayList<MethodGenerator> methods = new ArrayList<MethodGenerator>();

        JavaCodeFile javaCodeFile = new JavaCodeFile(packageName);
        javaCodeFile.setImports(new ArrayList<String>(Arrays.asList(
                "android.content.Context",
                "android.database.sqlite.SQLiteDatabase",
                "android.database.sqlite.SQLiteOpenHelper",
                "android.util.Log"
        )));

        ClassSource classSource = new ClassSource(mainClassName, "public");
        classSource.setExtends("SQLiteOpenHelper");
        classSource.addFieldWithDefaultValue("private static final", "String", "DATABASE_NAME", "\"" + dataBaseName + "\"", false);
        classSource.addFieldWithDefaultValue("private static final", "int", "DATABASE_VERSION", "1", false);
        classSource.addFieldWithDefaultValue("private static final", "String", "TAG", mainClassName + ".class.getSimpleName()", false);

        MethodGenerator mg = new MethodGenerator(mainClassName, new ArrayList<Parameter>(Arrays.asList(new Parameter("Context", "context"))));
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "super(context, DATABASE_NAME, null, DATABASE_VERSION);",
                "Log.d(TAG, \"constructor\");"
        )));
        methods.add(mg);

        mg = new MethodGenerator("public", "onCreate", "void", new ArrayList<Parameter>(Arrays.asList(new Parameter("SQLiteDatabase", "db"))));
        mg.setIsOverride(true);
        mg.setCodeOfMethod(getMainClassCreateCode());
        methods.add(mg);

        mg = new MethodGenerator("public", "onUpgrade", "void",
                new ArrayList<Parameter>(Arrays.asList(
                        new Parameter("SQLiteDatabase", "db"),
                        new Parameter("int", "oldVersion"),
                        new Parameter("int", "newVersion")
                )));
        mg.setIsOverride(true);
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "Log.w(TAG, \"Update database from version  \" + oldVersion\n" +
                        "                + \" to \" + newVersion + \", which remove all old records\");",
                "onCreate(db);"
        )));
        methods.add(mg);

        classSource.addMethods(methods);

        javaCodeFile.addClassSource(classSource);
        javaCodeFile.output(sb);

        return sb.toString();
    }

    /**
     * Генерация кода для контроллера главного класса для Android SDK.
     * @return код контроллера главного класса.
     */
    public String createControllerAndroidSDK() {
        StringBuffer sb = new StringBuffer();
        ArrayList<MethodGenerator> methods = new ArrayList<MethodGenerator>();

        JavaCodeFile javaCodeFile = new JavaCodeFile(packageName);
        javaCodeFile.setImports(new ArrayList<String>(Arrays.asList(
                "android.content.Context",
                "android.database.Cursor",
                "android.database.SQLException",
                "android.database.sqlite.SQLiteDatabase",
                "android.database.sqlite.SQLiteException",
                "android.util.Log",
                "java.util.ArrayList"
        )));

        ClassSource classSource = new ClassSource(mainClassName + "Controller", "public");
        classSource.addFieldWithDefaultValue("private final", "String", "TAG", mainClassName + "Controller.class.getSimpleName()", false);



        MethodGenerator mg = new MethodGenerator("protected", "delete", "void",
                new ArrayList<Parameter>(Arrays.asList(
                        new Parameter("Context", "context"),
                        new Parameter("String", "tableName"),
                        new Parameter("String", "statement")
                ))
        );
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "Log.d(TAG, \"delete\");",
                mainClassName + " dbhelper = new " + mainClassName + "(context);",
                "SQLiteDatabase sqliteDB = dbhelper.getWritableDatabase();",
                "sqliteDB.delete(tableName, statement, null);",
                "sqliteDB.close();",
                "dbhelper.close();"
        )));
        methods.add(mg);

        mg = new MethodGenerator("protected", "execute", "void",
                new ArrayList<Parameter>(Arrays.asList(
                        new Parameter("Context", "context"),
                        new Parameter("String", "query")
                ))
        );
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "try {",
                mainClassName + " dbhelper = new " + mainClassName + "(context);",
                "SQLiteDatabase sqliteDB = dbhelper.getWritableDatabase();",
                "sqliteDB.execSQL(query);",
                "sqliteDB.close();",
                "dbhelper.close();",
                "} catch (SQLiteException e) {",
                "Log.e(TAG, \"Failed open database. \", e);",
                "} catch (SQLException e) {",
                "Log.e(TAG, \"Failed to update Names. \", e);",
                "}"
        )));
        methods.add(mg);

        mg = new MethodGenerator(
                "protected",
                "executeQuery",
                "ArrayList<ArrayList<String>>",
                new ArrayList<Parameter>(Arrays.asList(
                        new Parameter("Context", "context"),
                        new Parameter("String", "query"))
                )
        );
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();",
                "DBHelper dbhelper = new DBHelper(context);",
                "SQLiteDatabase sqliteDB = dbhelper.getReadableDatabase();",
                "Cursor c = sqliteDB.rawQuery(query, null);",
                "if (c.moveToFirst()) {",
                "do {",
                "ArrayList<String> subList = new ArrayList<String>();",
                "for (int i = 0; i < c.getColumnCount(); i++) {",
                "subList.add(c.getString(i));",
                "}",
                "list.add(subList);",
                "} while (c.moveToNext());",
                "} else {",
                "Log.d(TAG, \"0 rows\");",
                "}",
                "c.close();",
                "sqliteDB.close();",
                "dbhelper.close();",
                "return list;"
        )));
        methods.add(mg);

        classSource.addMethods(methods);

        javaCodeFile.addClassSource(classSource);
        javaCodeFile.output(sb);

        return sb.toString();
    }

    /**
     * Генерация кода для метода onCreate, содержащего всю информацию о создании элементов базы данных. Случай Android SDK.
     * @return код, содержащий запросы, создающие элементы базы данных.
     */
    private ArrayList<String> getMainClassCreateCode() {
        ArrayList<String> list = new ArrayList<String>();
        for (Map.Entry entry: sqlQuery.entrySet()) {
            String groupName = entry.getKey().toString().toUpperCase();

            ArrayList<String> pList = Cast.objectToStringArrayList(entry.getValue());
            for (String st: pList) {
                String tmp = st.toUpperCase().contains("CREATE " + groupName + " IF NOT EXISTS") ? st : st.replace("CREATE " + groupName, "CREATE " + groupName + " IF NOT EXISTS");

                tmp = groupName.equalsIgnoreCase("trigger") ? tmp.replace("\n", "\t").replace("\r", "\t") : tmp.replace("\n", "").replace("\r", "");
                tmp = "db.execSQL(\"" + tmp.replace("\"", "\\\"") + "\");";
                list.add(tmp);
            }
        }

        return list;
    }

    /**
     * Генерация кода для работы с sqlite-jdbc-3.7.2.
     */
    public void generateJava() {
        StringBuffer sb = new StringBuffer();
        try {
            if (new File(folder).mkdir()) {

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(folder + slash + mainClassName + ".java"),
                        "UTF-8"
                ));
                bw.write(createMainClassJava());
                bw.close();

                for (Map.Entry entry: tables.entrySet()) {
                    String tableName = entry.getKey().toString();
                    JavaCodeFile javaCode = new JavaCodeFile(packageName);
                    javaCode.setImports(new ArrayList<String>(Arrays.asList(
                            "javax.swing.table.DefaultTableModel",
                            "java.util.ArrayList"
                    )));

                    ClassSource classSource = new ClassSource(tableName, "public");
                    classSource.setExtends(mainClassName);
                    classSource.addFieldWithDefaultValue("private final", "String", "TABLE_NAME", "\"" + tableName + "\"", false);

                    ArrayList<Parameter> pList = Cast.objectToArrayList(entry.getValue());
                    String insertFields = "{";
                    String insertValues = "{";
                    String insertAddQuotesFields = "";
                    for (Parameter p: pList) {
                        classSource.addFieldWithDefaultValue("public static final", "String", p.getName(), "\"" + p.getName() + "\"", false);
                        insertFields += tableName + "." + p.getName() + ", ";
                        insertValues += p.getName() + ", ";

                        if (p.getType().equalsIgnoreCase("String")) {
                            insertAddQuotesFields += p.getName() + " = " + p.getName() + " != null ? " + "\"\\\"\" + " + p.getName() + " + \"\\\"\" : null;\n\t\t";
                        }
                    }
                    insertValues = insertValues.substring(0, insertValues.length() - 2) + "};";
                    insertFields = insertFields.substring(0, insertFields.length() - 2) + "};";

                    ArrayList<MethodGenerator> methods = new ArrayList<MethodGenerator>();

                    MethodGenerator mg = new MethodGenerator("private", "prepareSQL", "String",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "fields"),
                                    new Parameter("String", "whatField"),
                                    new Parameter("String", "whatValue"),
                                    new Parameter("String", "sortField"),
                                    new Parameter("String", "sort")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "String query = \"SELECT \";",
                            "query += fields == null ? \" * FROM \" + TABLE_NAME : fields + \" FROM \" + TABLE_NAME;",
                            "query += whatField != null && whatValue != null ? \" WHERE \" + whatField + \" = \\\"\" + whatValue + \"\\\"\" : \"\";",
                            "query += sort != null && sortField != null ? \" order by \" + sortField + \" \" + sort : \"\";",
                            "return query;"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "insert", "void", Cast.objectToArrayList(entry.getValue()));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            insertAddQuotesFields,
                            "Object[] values_ar = " + insertValues,
                            "String[] fields_ar = " + insertFields,
                            "String values = \"\", fields = \"\";",
                            "for (int i = 0; i < values_ar.length; i++) {",
                            "if (values_ar[i] != null) {",
                            "values += values_ar[i] + \", \";",
                            "fields += fields_ar[i] + \", \";",
                            "}",
                            "}",
                            "if (!values.isEmpty()) {",
                            "values = values.substring(0, values.length() - 2);",
                            "fields = fields.substring(0, fields.length() - 2);",
                            "super.execute(\"INSERT INTO \" + TABLE_NAME + \"(\" + fields + \") values(\" + values + \");\");",
                            "}"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "delete", "void",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "whatField"),
                                    new Parameter("String", "whatValue")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "super.execute(\"DELETE from \" + TABLE_NAME + \" where \" + whatField + \" = \" + whatValue + \";\");"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "update", "void",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "whatField"),
                                    new Parameter("String", "whatValue"),
                                    new Parameter("String", "whereField"),
                                    new Parameter("String", "whereValue")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "super.execute(\"UPDATE \" + TABLE_NAME + \" set \" + whatField + \" = \\\"\" + whatValue + \"\\\" where \" + whereField + \" = \\\"\" + whereValue + \"\\\";\");"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "select", "ArrayList<ArrayList<Object>>",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "fields"),
                                    new Parameter("String", "whatField"),
                                    new Parameter("String", "whatValue"),
                                    new Parameter("String", "sortField"),
                                    new Parameter("String", "sort")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "return super.executeQuery(prepareSQL(fields, whatField, whatValue, sortField, sort));"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "getExecuteResult", "ArrayList<ArrayList<Object>>",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "query")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "return super.executeQuery(query);"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "execute", "void",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "query")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "super.execute(query);"
                    )));
                    methods.add(mg);

                    mg = new MethodGenerator("public", "selectToTable", "DefaultTableModel",
                            new ArrayList<Parameter>(Arrays.asList(
                                    new Parameter("String", "fields"),
                                    new Parameter("String", "whatField"),
                                    new Parameter("String", "whatValue"),
                                    new Parameter("String", "sortField"),
                                    new Parameter("String", "sort")
                            )));
                    mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                            "return super.executeQueryToTable(prepareSQL(fields, whatField, whatValue, sortField, sort));"
                    )));
                    methods.add(mg);

                    classSource.addMethods(methods);

                    javaCode.addClassSource(classSource);
                    javaCode.output(sb);

                    bw = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(folder + slash + entry.getKey().toString() + ".java"),
                            "UTF-8"
                    ));
                    bw.write(sb.toString());
                    bw.close();
                    sb = new StringBuffer();
                }
                resultMessage = "done";
                result = true;
            }
            else {
                result = false;
                resultMessage = "Package\'ve already created. Input another package name.";
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            resultMessage = ex.getMessage();
            result = false;
        }
    }

    /**
     * Генерация кода главного класса для работы с sqlite-jdbc-3.7.2.
     * @return код главного класса.
     */
    private String createMainClassJava()
    {
        StringBuffer sb = new StringBuffer();
        ArrayList<MethodGenerator> methods = new ArrayList<MethodGenerator>();

        JavaCodeFile javaCodeFile = new JavaCodeFile(packageName);
        javaCodeFile.setImports(new ArrayList<String>(Arrays.asList(
                "javax.swing.table.DefaultTableModel",
                "java.sql.*",
                "java.util.ArrayList"
        )));

        ClassSource classSource = new ClassSource(mainClassName, "public");
        classSource.addFieldWithDefaultValue("private final", "String", "DATABASE_NAME", "\"" + dataBaseName + "\"", false);
        classSource.addField("private", "Connection", "connection", false);
        classSource.addField("private", "Statement", "statement", false);
        classSource.addField("private", "ResultSet", "resultSet", false);

        MethodGenerator mg = new MethodGenerator(mainClassName, new ArrayList<Parameter>());
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "connection = null;",
                "statement = null;",
                "resultSet = null;"
        )));
        methods.add(mg);

        mg = new MethodGenerator("private", "connect", "void");
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "try {",
                "Class.forName(\"org.sqlite.JDBC\");",
                "} catch (ClassNotFoundException e) {",
                "e.printStackTrace();",
                "}",
                "try {",
                "connection = DriverManager.getConnection(\"jdbc:sqlite:\" + DATABASE_NAME);",
                "statement = connection.createStatement();",
                "} catch (SQLException e) {",
                "e.printStackTrace();",
                "}"
        )));
        methods.add(mg);

        mg = new MethodGenerator("private", "close", "void");
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "try {",
                "connection.close();",
                "statement.close();",
                "if (resultSet != null)",
                "resultSet.close();",
                "} catch (SQLException e) {",
                "e.printStackTrace();",
                "}"
        )));
        methods.add(mg);

        mg = new MethodGenerator("private", "arrayListTo2DArray", "Object[][]", new ArrayList<Parameter>(Arrays.asList(
                new Parameter("ArrayList<ArrayList<Object>>", "list")
        )));
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "Object[][] array = new Object[list.size()][];",
                "for (int i = 0; i < list.size(); i++) {",
                "ArrayList<Object> row = list.get(i);",
                "array[i] = row.toArray(new Object[row.size()]);",
                "}",
                "return array;"
        )));
        methods.add(mg);

        mg = new MethodGenerator("protected", "execute", "void", new ArrayList<Parameter>(Arrays.asList(
                new Parameter("String", "sql")
        )));
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "try {",
                "connect();",
                "statement.execute(sql);",
                "} catch (SQLException e) {",
                "e.printStackTrace();",
                "}",
                "finally {",
                "close();",
                "}"
        )));
        methods.add(mg);

        mg = new MethodGenerator("protected", "executeQueryToTable", "DefaultTableModel", new ArrayList<Parameter>(Arrays.asList(
                new Parameter("String", "sql")
        )));
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();",
                "ArrayList<Object> columns = new ArrayList<Object>();",
                "connect();",
                "try {",
                "resultSet = statement.executeQuery(sql);",
                "int columnCount = resultSet.getMetaData().getColumnCount();",
                "for (int i = 1; i <= columnCount; i++)",
                "columns.add(resultSet.getMetaData().getColumnName(i));",
                "while (resultSet.next()) {",
                "ArrayList<Object> subresult = new ArrayList<Object>();",
                "for (int i = 1; i <= columnCount; i++)",
                "subresult.add(resultSet.getObject(i));",
                "result.add(subresult);",
                "}",
                "} catch (SQLException e) {",
                "e.printStackTrace();",
                "}",
                "close();",
                "return new DefaultTableModel(arrayListTo2DArray(result), columns.toArray());"
        )));
        methods.add(mg);

        mg = new MethodGenerator("protected", "executeQuery", "ArrayList<ArrayList<Object>>", new ArrayList<Parameter>(Arrays.asList(
                new Parameter("String", "sql")
        )));
        mg.setCodeOfMethod(new ArrayList<String>(Arrays.asList(
                "ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();",
                "connect();",
                "try {",
                "resultSet = statement.executeQuery(sql);",
                "int columnCount = resultSet.getMetaData().getColumnCount();",
                "while (resultSet.next()) {",
                "ArrayList<Object> subresult = new ArrayList<Object>();",
                "for (int i = 1; i <= columnCount; i++) {",
                "subresult.add(resultSet.getObject(i));",
                "}",
                "result.add(subresult);",
                "}",
                "} catch (SQLException e){",
                "e.printStackTrace();",
                "}",
                "close();",
                "return result;"
        )));
        methods.add(mg);

        classSource.addMethods(methods);

        javaCodeFile.addClassSource(classSource);
        javaCodeFile.output(sb);

        return sb.toString();
    }
}