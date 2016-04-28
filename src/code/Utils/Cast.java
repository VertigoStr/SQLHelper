package code.Utils;

import code.CodeGenerator.Parameter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Класс для преобразования типов.
 */
public class Cast {
    /**
     * Преобразование объекта к списку, состоящему из экземпляров класса String.
     * @param obj Объект.
     * @return преобразованный список.
     */
    public static ArrayList<String> objectToStringArrayList(Object obj) {
        ArrayList<String> result = new ArrayList<String>();
        if (obj instanceof ArrayList<?>) {
            ArrayList<?> al = (ArrayList<?>) obj;
            if (al.size() > 0)
                for (Object ob: al)
                    if (ob instanceof String)
                        result.add(ob.toString());
        }
        return result;
    }


    /**
     * Преобразование объекта к списку, состоящему из экземпляров класса Parameter.
     * @see code.CodeGenerator.Parameter
     * @param obj Объект
     * @return преобразованный список.
     */
    public static ArrayList<Parameter> objectToArrayList(Object obj) {
        ArrayList<Parameter> result = new ArrayList<Parameter>();
        if (obj instanceof ArrayList<?>) {
            ArrayList<?> al = (ArrayList<?>) obj;
            if (al.size() > 0)
                for (Object ob: al)
                    if (ob instanceof Parameter)
                        result.add((Parameter) ob);
        }
        return result;
    }

    /**
     * Преобразование списка с вложенным списком, состоящим из экземпляров класса Object, к двумерному массиву типа Object.
     * @param list Вложенный список типа Object
     * @return двумерный массив.
     */
    public static Object[][] arrayListTo2DArray(ArrayList<ArrayList<Object>> list)
    {
        Object[][] array = new Object[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            ArrayList<Object> row = list.get(i);
            array[i] = row.toArray(new Object[row.size()]);
        }
        return array;
    }

    /**
     * Преобразование списка с вложенным списком, состоящим из экземпляров класса String, к двумерному массиву типа String.
     * @param list Вложенный список типа String
     * @return двумерный массив.
     */
    public static String[][] arrayListTo2DArrayS(ArrayList<ArrayList<String>> list)
    {
        String[][] array = new String[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            ArrayList<String> row = list.get(i);
            array[i] = row.toArray(new String[row.size()]);
        }
        return array;
    }

    /**
     * Преобразование массива тип Object к массиву типа String.
     * @param ob Массив типа Object
     * @return массив.
     */
    public static String[] objectToStringArray(Object[] ob) {
        String[] st = new String[ob.length];
        for (int i = 0; i < ob.length; i++)
            st[i] = ob[i].toString();
        return st;
    }

    /**
     * Преобразование списка типа String к массиву типа String.
     * @param list ArrayList
     * @return массив.
     */
    public static String[] arrayListToArray(ArrayList<String> list)
    {
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Преобразование иконки к изображению.
     * @param icon Иконка.
     * @return изображение.
     */
    public static Image iconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon)icon).getImage();
        }
        else {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
    }
}

