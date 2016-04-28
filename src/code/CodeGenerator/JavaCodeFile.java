package code.CodeGenerator;

import java.util.ArrayList;

/**
 * Класс, генерирующий зависимости генерируемого класса.
 */
public class JavaCodeFile {
    /** Имя пакета, содержащего генерируемый класс. */
    private String packageName;
    /** Список зависимостей. */
    private ArrayList<String> imports;
    /** Содержимое класса. */
    private ClassSource classSource;

    /**
     * Конструктор класса.
     * @param packageName Имя папки
     */
    public JavaCodeFile(String packageName)
    {
        this.packageName = packageName.isEmpty() ? "" : "package " + packageName + ";\n\n";
        imports = new ArrayList<String>();
    }

    /**
     * Создание списка зависимостей.
     * @param imports Список зависимостей
     */
    public void setImports(ArrayList<String> imports)
    {
        this.imports = imports;
    }

    /**
     * Добавление кода содержимого класса.
     * @param classSource Содержимое класса
     * @see code.CodeGenerator.ClassSource
     */
    public void addClassSource(ClassSource classSource)
    {
        this.classSource = classSource;
    }
    /**
     * Добавление к входному параметру всей информации о классе.
     * @param sb Переменная, хранящая всю информацию о классе
     */
    public void output(StringBuffer sb)
    {
        try {
            sb.append(packageName);
            for (String st: imports)
            {
                sb.append("import ").append(st).append(";");
                sb.append("\n");
            }
            sb.append("\n");
            classSource.output(sb);
            sb.append("}");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
