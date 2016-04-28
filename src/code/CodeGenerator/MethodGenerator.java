package code.CodeGenerator;

import java.util.ArrayList;

/**
 * Класс, генерирующий методы.
 */
public class MethodGenerator {

    /** Модификатор метода. */
    private String modifier;
    /** Имя метода. */
    private String name;
    /** Возвращаемое значение метода. */
    private String returnValue;
    /** Необходимо ли добавление аннотации Override. */
    private boolean isOverride;
    /** Код метода. */
    private ArrayList<String> code;
    /** Параметры метода. */
    private ArrayList<Parameter> parameters;

    /**
     * Конструктор класса.
     * @param modifier Модификатор
     * @param name Имя
     * @param returnValue Возвращаемое значение
     */
    public MethodGenerator(String modifier, String name, String returnValue)
    {
        this.modifier = "\t" + modifier + " ";
        this.name = name;
        this.returnValue = returnValue + " ";
        code = new ArrayList<String>();
        parameters = new ArrayList<Parameter>();
    }

    /**
     * Конструктор класса.
     * @param modifier Модификатор
     * @param name Метод
     * @param returnValue Возвращаемое значение
     * @param parameters Список параметров
     * @see code.CodeGenerator.Parameter
     */
    public MethodGenerator(String modifier, String name, String returnValue, ArrayList<Parameter> parameters)
    {
        this.modifier = "\t" + modifier + " ";
        this.name = name;
        this.returnValue = returnValue + " ";
        code = new ArrayList<String>();
        this.parameters = parameters;
    }

    /**
     * Конструктор класса.
     * @param name Имя
     * @param parameters Список параметров
     * @see code.CodeGenerator.Parameter
     */
    public MethodGenerator(String name, ArrayList<Parameter> parameters)
    {
        this.modifier = "\tpublic" + " ";
        this.returnValue = "";
        this.name = name;
        code = new ArrayList<String>();
        this.parameters = parameters;
    }

    /**
     * Создание кода метода.
     * @param code Код метода
     */
    public void setCodeOfMethod(ArrayList<String> code)
    {
        for (String st: code)
            this.code.add("\t\t" + st);
    }

    /**
     * Добавление аннотации Override.
     * @param flag boolean
     */
    public void setIsOverride(boolean flag) {
        this.isOverride = flag;
    }

    /**
     * Добавление к входному параметру всей информации о методе.
     * @param sb Переменная, хранящая всю информацию о классе
     */
    public void output(StringBuffer sb)
    {
        sb.append(isOverride ? "\t@Override\n" : "").append(modifier).append(returnValue).append(name).append("(");
        if (!parameters.isEmpty())
        {
            for (int i = 0; i < parameters.size(); i++)
            {
                sb.append(parameters.get(i).toString());
                sb.append(i == (parameters.size() - 1) ? "" : ", ");
            }
        }
        sb.append(") {\n");

        String tabulation = "";
        for (String st: code)
        {
            if (st.contains("{") && st.contains("}") && st.contains(";")) {
                sb.append(st).append("\n").append(tabulation);
            } else if (st.contains("{") && st.contains("}")) {
                tabulation = tabulation.length() > 1 ? tabulation.substring(0, tabulation.length() - 1) : "";
                sb.append(tabulation).append(st).append("\n").append(tabulation).append("\t");
            }
            else {
                if (st.contains("{")) {
                    tabulation += "\t";
                    sb.append(tabulation.substring(0, tabulation.length() - 1)).append(st).append("\n");
                } else {
                    if (st.contains("}"))
                        tabulation = tabulation.length() > 1 ? tabulation.substring(0, tabulation.length() - 1) : "";
                    sb.append(tabulation).append(st).append("\n");
                }
            }
        }
        sb.append("\t}\n");
    }
}
