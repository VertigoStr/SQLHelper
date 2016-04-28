package code.Utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс, содержащий регулярные выражения для работы с данными.
 */
public class Regex {
    /** Список всех возможных свойств поля.*/
    private static String[] properties_list = {"PRIMARY KEY", "AUTOINCREMENT", "NOT NULL", "UNIQUE"};

    /**
     * Добавление дополнительной косой черты к строке.
     * @param value Строка
     * @return Строка.
     */
    public static String addSlash(String value) {
        return value.replace("\\", "\\\\");
    }

    /**
     * Поиск совпадений в указанном тексте.
     * @param pattern Шаблон
     * @param text Строка
     * @return результат работы.
     */
    public static String getMatch(String pattern, String text) {
        String result = null;
        Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find())
            result = m.group(1);
        return result;
    }

    /**
     * Получение имени базы данных из строки.
     * @param name Строка
     * @return имя базы данных.
     */
    public static String getNameOfDataBase(String name) {
        String[] ar = name.contains(":") ? name.split("\\\\") : name.split("/");
        return ar[ar.length - 1];
    }

    /**
     * Получение числа из строки.
     * @param str Строка
     * @return число.
     */
    public static int getIntegerFromStr(String str)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++)
            if (Character.isDigit(str.charAt(i)))
                result.append(str.charAt(i));
        return Integer.parseInt(result.toString());
    }

    /**
     * Получение описания поля таблицы из строки, на выход - массив свойств поля типа String.
     * @param text SQL - запрос создания таблицы
     * @return массив, содержащий информацию о поле таблицы.
     */
    public static String[] getFieldDescription(String text) {
        String result = "";
        String[] resultArray;
        if (!text.contains("\"")) {
            Matcher m = Pattern.compile("DEFAULT (\\S+) ").matcher(text);
            if (m.find())
                result = m.group(1);
            String[] ar = text.split(" ");
            text = getPropertiesFromString(text).trim();
            resultArray = new String[]{ar[0], ar[1], result, text};
        }
        else {
            System.out.println(text);
            Matcher m = Pattern.compile("\"(.+)\"").matcher(text);
            if (m.find())
                result = m.group(1);
            String string = text.replace("\"" + result + "\"", "");
            System.out.println(string);
            String ar[] = string.split(" ");
            string = getPropertiesFromString(string).trim();
            resultArray = new String[]{ar[0], ar[1], result, string};
        }

        return resultArray;
    }

    /**
     * Получить количество вхождений строки в тексте.
     * @param pattern Шаблон
     * @param text Текст
     * @return количество вхождений.
     */
    public static int getMatches(String pattern, String text)
    {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        int count = 0;
        while (m.find()){
            count +=1;
        }
        return count;
    }

    /**
     * Получение свойств поля таблицы из строки. На выход - массив чисел, определяющий каждое свойство в зависимости от номера в массиве.
     * Если 1 - указанное свойство есть, 0 - нет.
     * @param text SQL - запрос создания таблицы
     * @return массив свойств.
     */
    public static int[] getPropertiesOfField(String text) {
        int[] properties = {0, 0, 0, 0};
        int index = 0;
        for (String str : properties_list) {
            if (getMatches(str, text) >= 1) properties[index] = 1;
            index++;
        }
        return properties;
    }

    /**
     * Получение свойств поля таблицы из строки, на выход - строка, содержащая все свойства, разделенные пробелом.
     * @param text SQL - запрос создания таблицы
     * @return свойства поля.
     */
    public static String getPropertiesFromString(String text) {
        String result = "";
        text = text.toUpperCase();
        for (String st: properties_list)
            if (text.contains(st))
                result += st + " ";
        return result;
    }

    /**
     * Получение свойств поля таблицы из строки для конкретного поля.
     * На выход - массив чисел, определяющий каждое свойство в зависимости от номера в массиве.
     * Если 1 - указанное свойство есть, 0 - нет.
     * @param field Поле
     * @param text SQL - запрос создания таблицы
     * @return массив свойств.
     */
    public static int[] getPropertiesOfFieldI(String field, String text)
    {
        int []properties = {0, 0, 0, 0};
        field = field + "[]| |\"|\']";
        String []textAfterSplit = text.split(",");
        for (String aTextAfterSplit : textAfterSplit) {
            if (getMatches(field, aTextAfterSplit) >= 1) {
                int index = 0;
                for (String str : properties_list) {
                    if (getMatches(str, aTextAfterSplit.toUpperCase()) >= 1) properties[index] = 1;
                    index++;
                }
            }
        }
        return properties;
    }

    /**
     * Разделение строки sql - запроса создания таблицы по полям,
     * в случае, если значение по умолчанию содержит кавычки.
     * @param text SQL - запрос создания таблицы
     * @return разделенный sql-запрос.
     */
    public static String[] splitWithQuote(String text) {
        String[] values;
        if (text.contains("\"")) {
            values = text.split("\" ");
            String sub = values[0] += "\"";
            if (values.length >= 2) {
                if (values[1].split(",").length >= 2)
                    values = new String[]{sub + " " + values[1].split(",")[0], values[1].split(",")[1]};
                else
                    values = new String[]{sub + " " + values[1]};
            }
        }
        else
            values = text.split(" ,");
        return values;
    }

    /**
     * Разделение строки sql - запроса создания таблицы по полям
     * в случае, если значение по умолчанию содержит запятые.
     * @param text SQL - запрос создания таблицы
     * @return список полей.
     */
    public static String[] splitWithComma(String text) {
        String[] values;
        if (text.contains("\"")) {
            ArrayList<String> list = new ArrayList<String>();
            ArrayList<String> quoteString = new ArrayList<String>();
            values = text.split(",");
            int i = 0;
            while (i < values.length) {
                if (getMatches("\"", values[i]) > 1)
                    list.add(values[i]);
                else if (values[i].contains("\"")) {
                    int j = i + 1;
                    if (j < values.length) {
                        while (true) {
                            quoteString.add(values[j]);
                            if(values[j].contains("\""))
                                break;
                            j++;
                        }
                    }
                    String finalString = "";
                    for(String st: quoteString)
                        finalString += st;
                    list.add(values[i] + finalString);
                    quoteString.clear();
                    i = j;
                }
                else
                    list.add(values[i]);
                i++;
            }
            values = Cast.objectToStringArray(list.toArray());
        }
        else
            values = text.split(",");
        return values;
    }

    /**
     * Получение свойств поля таблицы из строки, на выход - строка, содержащая все свойства, разделенные пробелом.
     * @param field Поле
     * @param text SQL - запрос создания таблицы
     * @return строка свойств.
     */
    public static String getPropertiesOfFieldS(String field, String text)
    {
        String properties = "";
        field = field + "[]| |\"|\']";
        String []textAfterSplit = text.split(",");
        for (String aTextAfterSplit : textAfterSplit) {
            if (getMatches(field, aTextAfterSplit) >= 1) {
                for (String str : properties_list) {
                    if (getMatches(str, aTextAfterSplit.toUpperCase()) >= 1) properties += str + ", ";
                }
            }
        }
        return properties.length() != 0 ? properties.substring(0, properties.length() - 2) : "";
    }

    /**
     * Получение информации о внешних ключах и ссылках на другие таблицы
     * из sql-запроса, содержашего запрос создания таблицы.
     * @param text SQL - запрос создания таблицы
     * @return информация о внешних ключах.
     */
    public static String getReferenceInfo(String text)
    {
        String onDeleteResult = "";
        String onUpdateResult = "";
        String reference = "";
        Matcher mTableInfo = Pattern.compile("REFERENCES (.+)").matcher(text);
        Matcher mDel = Pattern.compile("ON DELETE (.+)").matcher(text);
        Matcher mUpd = Pattern.compile("ON UPDATE (.+)").matcher(text);
        if (mTableInfo.find())
            reference = mTableInfo.group(1);
        if (mDel.find())
            onDeleteResult = mDel.group(1);
        if (mUpd.find())
            onUpdateResult = mUpd.group(1);
        if (!onDeleteResult.isEmpty()) {
            reference = reference.replace(" ON DELETE " + onDeleteResult, "");
            reference = reference.replace(")", "");
            reference = reference.replace("(", "|");
            if (!onUpdateResult.isEmpty()) {
                onDeleteResult = onDeleteResult.replace(" ON UPDATE " + onUpdateResult, "");
            }
            return  reference + "|" + onDeleteResult + "|" + onUpdateResult;
        }
        else
        {
            if (!onUpdateResult.isEmpty()) {
                reference = reference.replace(" ON UPDATE " + onUpdateResult, "");
                reference = reference.replace(")", "");
                reference = reference.replace("(", "|");
                return reference + "|" +  "|" + onUpdateResult;
            }
            else {
                System.out.println("Regex" + reference);
                String[] tmp = reference.split("\\(");
                return tmp[0] + "|" + tmp[1].replace(")", "");
            }
        }
    }
}