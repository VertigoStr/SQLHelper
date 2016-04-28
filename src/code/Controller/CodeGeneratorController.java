package code.Controller;

import code.CodeGenerator.CodeGenerator;
import code.CodeGenerator.Parameter;
import code.DataBaseHelper.DBHelper;
import code.DataBaseHelper.DeviceDBHelper;
import code.Utils.Keeper;

import javax.swing.*;
import java.util.*;

/**
 * Контроллер для генерации кода.
 */
public class CodeGeneratorController {
    /** Экземпляр класса DBHelper, для работы с базой данных, расположенной на компьютере.
     * @see code.DataBaseHelper.DBHelper
     * */
    private DBHelper db;
    /** Экземпляр класса DeviceDBHelper, для работы с базой данных, расположенной на мобильном устройстве.
     * @see code.DataBaseHelper.DeviceDBHelper
     * */
    private DeviceDBHelper dDB;
    /** Переменная, хранящая информацию о том, с какой базой данной ведется работа: с базой, хранящейся на мобильном устройстве,
     * или - на компьютере. */
    private boolean isFromDevice;

    /** Родительский контейнер. */
    private JDialog dialog;

    /** Имя базы данных. */
    private String name;

    /** Список выбранных таблиц. */
    private ArrayList<JCheckBox> boxes;

    /** Папка, в которой будет храниться сгенерированный код. */
    private JTextField folderField;

    /**
     * Конструктор класса.
     * @param name Имя базы данных
     */
    public CodeGeneratorController(String name)
    {
        initDB(name);
    }


    /**
     * Инициализация базы данных.
     * @param dataBaseName Имя базы данных
     */
    private void initDB(String dataBaseName) {
        String[] params = dataBaseName.split("\\|");
        if (!params[0].isEmpty() && params.length >= 2) {
            this.name = params[1];
            dDB = new DeviceDBHelper(Keeper.getSDKLocation(), params[0], params[1]);
            isFromDevice = true;
        }
        else {
            this.name = dataBaseName;
            db = new DBHelper(dataBaseName.replace("|", ""));
            isFromDevice = false;
        }
    }

    /**
     * Инициализация родительского контейнера.
     * @param dialog Родительский контейнер
     */
    public void setDialog(JDialog dialog)
    {
        this.dialog = dialog;
    }

    /**
     * Получение информации о CheckBox'ах, расположенных на графическом интерефейсе.
     * @param boxes Список CheckBox'ов
     */
    public void setBoxes(ArrayList<JCheckBox> boxes)
    {
        this.boxes = boxes;
    }

    /**
     * Получение информации о папки из JTextField, расположенном на графическом интерфейсе.
     * @param field JTextField
     */
    public void setFolderField(JTextField field) {
        this.folderField = field;
    }

    /**
     * Обработчик нажатия на кнопку newFolder: отвечает за поиск новой папки, используя проводник. Результат сохраняется в
     * переменную folderField.
     * @param parent Родительский котейнер
     */
    public void newFolderListener(JDialog parent) {
        JFileChooser fileChooser = new JFileChooser(folderField != null ? folderField.getText() : "");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showDialog(parent, "Apply") == JFileChooser.APPROVE_OPTION)
            folderField.setText(fileChooser.getSelectedFile().getPath());

    }

    /**
     * Обработчик нажатия на кнопку defaultFolder: значение переменной folderField возвращается к значению по умолчанию.
     * @param defFolder Путь
     */
    public void defaultFolderListener(String defFolder) {
        folderField.setText(defFolder);
    }

    /**
     * Обработчик нажатия на кнопку generate: отвечает за генерацию кода.
     * @param mainClassName Имя управляющего класса
     * @param packageName Имя папки
     * @param type Тип генерируемого кода
     */
    public void generateListener(String mainClassName, String packageName, String type, ArrayList<JCheckBox> otherParams)
    {
        boolean flag = !type.equals("Java");
        boolean dataBaseNameFlag = !isFromDevice && type.equals("Java Android SDK");

        Map<String, ArrayList<Parameter>> tableInfo = new LinkedHashMap<String, ArrayList<Parameter>>();
        ArrayList<String> sqlQuery = new ArrayList<String>();
        if (boxes.size() != 0)
        {
            if (isFromDevice) {
                for (JCheckBox box : boxes)
                    if (box.isSelected()) {
                        tableInfo.put(box.getText(), dDB.getTypesP(box.getText()));
                        sqlQuery.add(dDB.getSQLQuery(box.getText()));
                    }
            } else {
                db.connect();
                for (JCheckBox box : boxes) {
                    if (box.isSelected() && flag) {
                        tableInfo.put(box.getText(), db.getTypesP(box.getText()));
                        sqlQuery.add(db.getSQLQuery(box.getText()));
                    }
                    else if (box.isSelected())
                        tableInfo.put(box.getText(), db.getTypesP(box.getText()));
                }
                db.close();
            }


            HashMap<String, ArrayList<String>> queries = new LinkedHashMap<String, ArrayList<String>>();
            queries.put("table", sqlQuery);
            for (JCheckBox box: otherParams){
                if (box.isSelected()) {
                    ArrayList<String> list = new ArrayList<String>();
                    for (String element: getNamesOf(box.getName())) {
                        if (!element.startsWith("sqlite_") && !element.startsWith("android_")) {
                            if (isFromDevice) {
                                list.add(dDB.getCreateElement(element));
                            } else {
                                db.connect();
                                list.add(db.getCreateElement(element));
                                db.close();
                            }
                        }
                    }
                    queries.put(box.getName(), list);
                }
            }


            CodeGenerator codeGenerator = isFromDevice || flag
                    ? new CodeGenerator(dataBaseNameFlag, mainClassName, packageName, tableInfo, name, folderField.getText(), queries)
                    : new CodeGenerator(mainClassName, packageName, tableInfo, name, folderField.getText());
            if (codeGenerator.isDone()) {
                JOptionPane.showMessageDialog(dialog, "Java code was generated!", "Done", JOptionPane.INFORMATION_MESSAGE);
                cancelListener();
            }
            else
                JOptionPane.showMessageDialog(dialog, codeGenerator.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        else
            JOptionPane.showMessageDialog(dialog, "No tables were founded!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Обработчик нажатия на кнопку cancel: закрывает окно генерации кода.
     */
    public void cancelListener()
    {
        dialog.setVisible(false);
        dialog.dispose();
    }

    /**
     * Получение списка имен элементов из базы данных
     * @param name имя группы элементов
     * @return список имен, входящих в заданную группу
     */
    public ArrayList<String> getNamesOf(String name) {
        ArrayList<String> list;
        if (isFromDevice) {
            list = dDB.getNamesOf(name);
        } else {
            db.connect();
            list = db.getNamesOf(name);
            db.close();
        }
        return list;
    }

    /**
     * Получение списка таблиц из базы данных.
     * @return список таблиц.
     */
    public ArrayList<String> getTablesName()
    {
        ArrayList<String> list;
        if (isFromDevice) {
            list = dDB.getNamesOf("table");
        } else {
            db.connect();
            list = db.getNamesOf("table");
            db.close();
        }
        list.removeAll(Arrays.asList("sqlite_sequence", "android_metadata", null));
        return list;
    }

}
