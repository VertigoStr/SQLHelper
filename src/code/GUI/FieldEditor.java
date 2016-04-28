package code.GUI;

import code.Controller.FieldEditorController;
import code.Utils.Cast;
import code.Utils.Icons;
import code.Utils.Regex;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Окно для создания поля таблицы.
 */
public class FieldEditor {
    /** Контроллер. */
    private FieldEditorController controller;
    /** Имя поля(Label). */
    private final JLabel nameField = new JLabel("Field name:          ");
    /** Тип поля(Label). */
    private final JLabel typeField = new JLabel("Field type:          ");
    /** Ссылка на(Label). */
    private final JLabel referenceField = new JLabel("Reference to:          ");
    /** Поведение при удалении(Label). */
    private final JLabel onDelete = new JLabel("On delete:          ");
    /** Поведение при обновлении(Label). */
    private final JLabel onUpdate = new JLabel("On update:          ");
    /** Значение по умолчанию(Label). */
    private final JLabel defaultValue = new JLabel("Default Value:          ");
    /** Первичный ключ(CheckBox). */
    private final JCheckBox primaryKey = new JCheckBox("Primary Key");
    /** Not null(CheckBox). */
    private final JCheckBox notNull = new JCheckBox("Not Null");
    /** AUTO_INCREMENT(CheckBox). */
    private final JCheckBox autoIncrement = new JCheckBox("Auto Increment");
    /** Уникальное значение(CheckBox). */
    private final JCheckBox unique = new JCheckBox("Unique");
    /** Выбраная строка. */
    private String rowInFocus;
    /** Финальный запрос. */
    private String resultQuery;

    /**
     * Конструктор класса.
     * @param db Имя базы данных
     * @param flag Тип окна: true - полное, false - ограниченное
     * @param tableName Имя таблицы
     */
    public FieldEditor(String db, boolean flag, String tableName)
    {
        controller = new FieldEditorController(db, tableName);
        if (flag)
            initComponents(null);
        else
            initComponentsSimple();
    }

    /**
     * Получение результата работы окна.
     * @return результат работы.
     */
    public String getResult()
    {
        return resultQuery != null ? resultQuery : "";
    }

    /**
     * Конструктор класса.
     * @param db Имя базы данных
     * @param rowInFocus Выбранная строка
     * @param editFieldName Имя, редактируемого элемента
     */
    public FieldEditor(String db, String rowInFocus, String editFieldName)
    {
        controller = new FieldEditorController(db);
        this.rowInFocus = rowInFocus;
        controller.setEditFieldName(editFieldName);
        controller.setRowInFocus(rowInFocus);
        initComponents(null);
    }

    /**
     * Конструктор класса.
     * @param db Имя базы данных
     * @param rowInFocus Выбранная строка
     * @param editFieldName Имя, редактируемого элемента
     * @param parameters Параметры элемента
     */
    public FieldEditor(String db, String rowInFocus, String editFieldName, String parameters)
    {
        controller = new FieldEditorController(db);
        this.rowInFocus = rowInFocus;
        controller.setEditFieldName(editFieldName);
        controller.setRowInFocus(rowInFocus);
        initComponents(parameters);
    }

    /**
     * Создание компонентов для ограниченного окна.
     */
    private void initComponentsSimple() {
        JPanel panel = new JPanel();
        final JDialog dialog = new JDialog();
        dialog.setIconImage(Cast.iconToImage(Icons.Main));
        dialog.setTitle(rowInFocus != null ? "Edit Field" : "Add Field");
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.setLocation(300, 200);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        controller.setDialog(dialog);
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        final JTextField nameTextField = new JTextField();
        final JTextField defaultValueField = new JTextField();
        String typeItems[] = {"INTEGER", "FLOAT", "REAL", "NUMERIC", "BOOLEAN", "TIME", "DATE", "TIMESTAMP", "TEXT", "BLOB"};
        final JComboBox<String> typeBox = new JComboBox<String>(typeItems);
        typeBox.setSelectedIndex(-1);
        typeBox.setEditable(true);

        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");


        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultQuery = controller.addButtonActionListenerSimple(nameTextField, typeBox, defaultValueField, notNull);
                if (resultQuery != null) controller.cancelButtonActionListener();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelButtonActionListener();
            }
        });

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(nameField)
                                        .addComponent(typeField)
                                        .addComponent(defaultValue)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(nameTextField)
                                        .addComponent(typeBox)
                                        .addComponent(defaultValueField)
                                        .addComponent(notNull)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addComponent(addButton)
                                                        .addComponent(cancelButton)
                                        )
                        )
        );
        layout.linkSize(SwingConstants.HORIZONTAL, cancelButton, addButton);
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(nameField)
                                        .addComponent(nameTextField)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(typeField)
                                        .addComponent(typeBox)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(defaultValueField)
                                        .addComponent(defaultValue)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(notNull)
                        ).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(addButton)
                                .addComponent(cancelButton)
                )
        );
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setVisible(true);

    }

    /**
     * Создание компонентов для полного окна.
     * @param parameters Параметры
     */
    private void initComponents(String parameters)
    {
        JPanel panel = new JPanel();
        final JDialog dialog = new JDialog();
        dialog.setIconImage(Cast.iconToImage(Icons.Main));
        dialog.setTitle(rowInFocus != null ? "Edit Field" : "Add Field");
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.setLocation(300, 200);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        controller.setDialog(dialog);
        ArrayList<String> fieldBaseProperties = new ArrayList<String>();
        ArrayList<String> foreignKeysInfo = new ArrayList<String>();
        int []extraProperties = null;
        if (rowInFocus != null) {
            if (parameters != null)
            {
                final String []params = parameters.split("\\|");
                fieldBaseProperties = new ArrayList<String>(Arrays.asList(rowInFocus, params[0], params[1].equals("NOPE") ? "" : params[1]));
                extraProperties = params[2].equals("NOPE") ? null : Regex.getPropertiesOfField(params[2]);
                foreignKeysInfo = new ArrayList<String>(Arrays.asList(params).subList(3, params.length));
            }
            else {
                fieldBaseProperties = controller.getFieldProperties();
                foreignKeysInfo = controller.getForeignKeys();
                extraProperties = controller.getExtraProperties();
            }
        }

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        final JTextField nameTextField = new JTextField();
        String typeItems[] = {"INTEGER", "FLOAT", "REAL", "NUMERIC", "BOOLEAN", "TIME", "DATE", "TIMESTAMP", "TEXT", "BLOB"};
        final JComboBox<String> typeBox = new JComboBox<String>(typeItems);
        typeBox.setSelectedIndex(-1);
        typeBox.setEditable(true);

        String defaultItems[] = {"CURRENT_TIME", "CURRENT_DATE", "CURRENT_TIMESTAMP"};
        final JComboBox<String> defaultBox = new JComboBox<String>(defaultItems);
        defaultBox.setSelectedIndex(-1);
        defaultBox.setEditable(true);

        String onDeleteUpdateItems[] = {"NO ACTION", "RESTRICT", "SET NULL", "SET DEFAULT", "CASCADE"};
        final JComboBox<String> onDeleteBox = new JComboBox<String>(onDeleteUpdateItems);
        onDeleteBox.setSelectedIndex(-1);
        onDeleteBox.setEditable(false);
        onDeleteBox.setEnabled(false);

        final JComboBox<String> onUpdateBox = new JComboBox<String>(onDeleteUpdateItems);
        onUpdateBox.setSelectedIndex(-1);
        onUpdateBox.setEditable(false);
        onUpdateBox.setEnabled(false);

        final JComboBox<String> referenceTableBox = new JComboBox<String>(controller.getNamesOf("table"));
        referenceTableBox.setSelectedIndex(-1);
        referenceTableBox.setEditable(false);

        final JComboBox<String> referenceFieldBox = new JComboBox<String>();
        referenceFieldBox.setEnabled(false);
        referenceTableBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultComboBoxModel model = controller.getComboBoxModel(referenceTableBox.getSelectedItem().toString());
                referenceFieldBox.setModel(model);
                referenceFieldBox.setSelectedIndex(-1);
                referenceFieldBox.setEditable(false);
                referenceFieldBox.setEnabled(true);
                referenceFieldBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onDeleteBox.setEnabled(true);
                        onUpdateBox.setEnabled(true);
                    }
                });
            }
        });

        if (rowInFocus != null) {
            nameTextField.setText(fieldBaseProperties.get(0));
            typeBox.getEditor().setItem(fieldBaseProperties.get(1));
            defaultBox.getEditor().setItem(fieldBaseProperties.get(2));
            if (extraProperties != null)
            {
                if (extraProperties[0] == 1) primaryKey.setSelected(true);
                if (extraProperties[1] == 1) autoIncrement.setSelected(true);
                if (extraProperties[2] == 1) notNull.setSelected(true);
                if (extraProperties[3] == 1) unique.setSelected(true);
            }
            if (foreignKeysInfo.size() != 0)
            {
                referenceTableBox.setSelectedItem(foreignKeysInfo.get(0));
                referenceFieldBox.setSelectedItem(foreignKeysInfo.get(1));
                if (foreignKeysInfo.size() >= 3) {
                    onDeleteBox.setSelectedItem(foreignKeysInfo.get(2));
                }
                if (foreignKeysInfo.size() >= 4) {
                    onUpdateBox.setSelectedItem(foreignKeysInfo.get(3));
                }
            }
            if (parameters == null)
            {
                referenceTableBox.setEnabled(false);
                referenceFieldBox.setEnabled(false);
                onDeleteBox.setEnabled(false);
                onUpdateBox.setEnabled(false);
                autoIncrement.setEnabled(false);
                unique.setEnabled(false);
                primaryKey.setEnabled(false);
                notNull.setEnabled(false);
                defaultBox.setEnabled(false);
                typeBox.setEnabled(false);
                nameTextField.setEnabled(false);
            }
        }

        primaryKey.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (primaryKey.isSelected()) {
                    unique.setEnabled(false);
                    notNull.setSelected(true);
                    notNull.setEnabled(false);

                    typeBox.getEditor().setItem("");
                    typeBox.setSelectedItem("INTEGER");
                    typeBox.setEnabled(false);
                } else {
                    unique.setEnabled(true);
                    notNull.setSelected(false);
                    notNull.setEnabled(true);
                    typeBox.setEnabled(true);
                }
            }
        });
        autoIncrement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (autoIncrement.isSelected()) {
                    unique.setEnabled(false);
                    notNull.setSelected(true);
                    notNull.setEnabled(false);
                    primaryKey.setEnabled(false);
                    primaryKey.setSelected(true);
                    typeBox.setSelectedItem("INTEGER");
                    typeBox.setEnabled(false);
                } else {
                    unique.setEnabled(true);
                    notNull.setSelected(false);
                    notNull.setEnabled(true);
                    primaryKey.setEnabled(true);
                    primaryKey.setSelected(false);
                    typeBox.setEnabled(true);
                }
            }
        });

        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (referenceFieldBox.getSelectedIndex() != -1) {
                    String referenceInfo = "FOREIGN KEY(" + nameTextField.getText() + ") REFERENCES " +
                            referenceTableBox.getSelectedItem().toString() + "(" +
                            referenceFieldBox.getSelectedItem().toString() + ")";
                    if (onDeleteBox.getSelectedIndex() != -1)
                        referenceInfo += " ON DELETE " + onDeleteBox.getSelectedItem().toString();
                    if (onUpdateBox.getSelectedIndex() != -1)
                        referenceInfo += " ON UPDATE " + onUpdateBox.getSelectedItem().toString();
                    resultQuery = controller.addButtonActionListener(nameTextField, typeBox, defaultBox, primaryKey, notNull, autoIncrement, unique, referenceInfo);
                }
                else
                    resultQuery = controller.addButtonActionListener(nameTextField, typeBox, defaultBox, primaryKey, notNull, autoIncrement, unique, null);
                if (resultQuery != null)
                    controller.cancelButtonActionListener();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean result = controller.cancelButtonActionListener();
                if (result) resultQuery = "cancel";
            }
        });

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(nameField)
                                        .addComponent(typeField)
                                        .addComponent(defaultValue)
                                        .addComponent(referenceField)
                                        .addComponent(onDelete)
                                        .addComponent(onUpdate)
                                        .addComponent(primaryKey)
                                        .addComponent(notNull)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(nameTextField)
                                        .addComponent(typeBox)
                                        .addComponent(defaultBox)
                                        .addComponent(referenceTableBox)
                                        .addComponent(referenceFieldBox)
                                        .addComponent(onDeleteBox)
                                        .addComponent(onUpdateBox)
                                        .addComponent(autoIncrement)
                                        .addComponent(unique)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addComponent(addButton)
                                                        .addComponent(cancelButton)
                                        )
                        )
        );
        layout.linkSize(SwingConstants.HORIZONTAL, cancelButton, addButton);
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(nameField)
                                        .addComponent(nameTextField)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(typeField)
                                        .addComponent(typeBox)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(defaultBox)
                                        .addComponent(defaultValue)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(referenceField)
                                        .addComponent(referenceTableBox)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(referenceFieldBox)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(onDelete)
                                        .addComponent(onDeleteBox)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(onUpdate)
                                        .addComponent(onUpdateBox)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(primaryKey)
                                .addComponent(autoIncrement))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(notNull)
                                        .addComponent(unique)
                        ).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(addButton)
                                .addComponent(cancelButton)
                )
        );

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setVisible(true);
    }
}
