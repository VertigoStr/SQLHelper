package code.CodeGenerator;

import java.util.ArrayList;

/**
 * Класс, генерирующий содержимое класса.
 */
public class ClassSource {

    /** Имя класса. */
    private String name;

    /** Модификатор доступа. */
    private String modifier;

    /** Имя реализуемого интерфейса. */
    private String implement;

    /** Имя наследуемого класса. */
    private String extend;

    /** Поля класса. */
    private ArrayList<String> fields;

    /** Методы класса. */
    private ArrayList<MethodGenerator> methods;

    /**
     * Конструктор класса.
     * @param name Имя класса
     * @param modifier Тип модификатора: public, private, protected
     */
    public ClassSource(String name, String modifier) {
        this.name = " class " + name;
        this.modifier = modifier;
        this.implement = "";
        this.extend = "";
        fields = new ArrayList<String>();
        methods = new ArrayList<MethodGenerator>();
    }

    /**
     * Добавление метода к классу.
     * @param _methods Код метода
     * @see code.CodeGenerator.MethodGenerator
     */
    public void addMethods(ArrayList<MethodGenerator> _methods)
    {
        methods.addAll(_methods);
    }

    /**
     * Добавление поля к классу.
     * @param modifier Модификатор
     * @param type Тип
     * @param name Имя
     * @param createGetSet Необходимо ли создание методов get и set для поля
     */
    public void addField(String modifier, String type, String name, boolean createGetSet) {
        fields.add("\t" + modifier + " " + type + " " + name + ";");
        if (createGetSet) {
            methods.add(createGetter(type, name));
            methods.add(createSetter(type, name));
        }
    }

    /**
     * Добавление поля со значением по умолчанию.
     * @param modifier Модификатор
     * @param type Тип
     * @param name Имя
     * @param value Значение по умолчанию
     * @param createGetSet Необходимо ли создание методов get и set для поля
     */
    public void addFieldWithDefaultValue(String modifier, String type, String name, String value, boolean createGetSet) {
        fields.add("\t" + modifier + " " + type + " " + name + " = " + value + ";");
        if (createGetSet) {
            methods.add(createGetter(type, name));
            methods.add(createSetter(type, name));
        }
    }

    /**
     * Добавление ключевого слова implements.
     * @param implement Имя интерфейса
     */
    public void setImplements(String implement) {
        this.implement = " implements " + implement;
    }

    /**
     * Добавление ключевого слова extends.
     * @param extend Имя класса - родителя
     */
    public void setExtends(String extend) {
        this.extend = " extends " + extend;
    }

    /**
     * Создание метода get.
     * @param type Тип возвращаемого значения
     * @param name Имя
     * @return код метода get для указанного поля.
     * @see code.CodeGenerator.MethodGenerator
     */
    private MethodGenerator createGetter(String type, String name)
    {
        MethodGenerator mg = new MethodGenerator("public", "get" + name.substring(0, 1).toUpperCase() + name.substring(1), type);
        ArrayList<String> code = new ArrayList<String>();
        code.add("return " + name + ";");
        mg.setCodeOfMethod(code);
        return mg;
    }

    /**
     * Создание метода set.
     * @param type Тип входного значения
     * @param name Имя параметра
     * @return код метода set для указанного поля.
     * @see code.CodeGenerator.MethodGenerator
     */
    private MethodGenerator createSetter(String type, String name)
    {
        ArrayList<Parameter> arg =  new ArrayList<Parameter>();
        arg.add(new Parameter(type, name));
        MethodGenerator mg = new MethodGenerator("public", "set" + name.substring(0, 1).toUpperCase() + name.substring(1), "void", arg);
        ArrayList<String> code = new ArrayList<String>();
        code.add("this." + name + " = " + name + ";");
        mg.setCodeOfMethod(code);
        return mg;
    }

    /**
     * Добавление к входному параметру всей информации о классе.
     * @param sb Переменная, хранящая всю информацию о классе
     */
    public void output(StringBuffer sb) {
        sb.append(modifier).append(name).append(extend).append(implement).append(" {\n");
        for (String st : fields)
            sb.append(st).append("\n");
        sb.append("\n");
        for(MethodGenerator mg: methods)
        {
            mg.output(sb);
            sb.append("\n");
        }
    }
}
