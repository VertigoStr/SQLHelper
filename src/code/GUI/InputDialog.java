package code.GUI;

import code.Controller.InputDialogController;
import code.Utils.InputCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Диалог для ввода и изменения данных в таблице.
 */
public class InputDialog {
    /** Переменная, отвечающая за проверкой: закончена ли работа окна. */
    private boolean done = false;
    /** Контроллер. */
    private InputDialogController controller;

    /**
     * Конструктор класса.
     * @param tableName Имя таблицы
     * @param dbName Имя базы данных
     */
    public InputDialog(String tableName, String dbName)
    {
        controller = new InputDialogController(dbName, tableName);
        initComponents(false);
    }

    /**
     * Конструктор класса.
     * @param tableName Имя таблицы
     * @param dbName Имя базы данных
     * @param sql SQL - запрос
     */
    public InputDialog(String tableName, String dbName, String sql)
    {
        controller = new InputDialogController(dbName, tableName, sql);
        initComponents(true);
    }

    /**
     * Проверка: завершена ли работа окна.
     * @return результат работы.
     */
    public boolean isDone()
    {
        return done;
    }

    /**
     * Создание компонентов окна.
     * @param isUpdate Тип операции, выполняемой окном: true - обновление, false - вставка
     */
    private void initComponents(boolean isUpdate)
    {
        final JDialog dialog = new JDialog();
        dialog.setIconImage(new ImageIcon("src\\res\\icon16x16\\S-icon.png").getImage());
        final JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        controller.setJDialog(dialog);
        Map<String, String> map = controller.getFields();

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        GroupLayout.ParallelGroup hSubGroup1 =  layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup hSubGroup2 =  layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        for (Map.Entry entry: map.entrySet())
        {
            JLabel field = new JLabel(entry.getKey().toString());
            String fieldType = entry.getValue().toString();
            Component type;
            if (fieldType.equalsIgnoreCase("TIMESTAMP") || fieldType.equalsIgnoreCase("DATETIME")) {
                type = InputCreator.getTimeStampBox(0);
            }
            else if (fieldType.equalsIgnoreCase("TIME")) {
                type = InputCreator.getTimeStampBox(1);
            }
            else if (fieldType.equalsIgnoreCase("DATE")) {
                type = InputCreator.getTimeStampBox(2);
            }
            else
            {
                type = new JTextField();
                ((JTextField) type).setToolTipText(fieldType);
                ((JTextField) type).setBorder(BorderFactory.createLoweredBevelBorder());
            }
            type.setName(field.getText());
            hSubGroup1.addComponent(field);
            hSubGroup2.addComponent(type);
            vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(field)
                            .addComponent(type)
            );
        }

        JButton insert = new JButton(isUpdate ? "Update" : "Insert");
        JButton cancel = new JButton("Cancel");
        hSubGroup2.addGroup(layout.createSequentialGroup().addComponent(insert).addComponent(cancel));
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(hSubGroup1)
                        .addGroup(hSubGroup2)

        );
        layout.setVerticalGroup(vGroup
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(insert)
                                        .addComponent(cancel)
                        )
        );
        boolean flag = true;

        if (isUpdate)
            if (!controller.setData(panel)) {
                flag = false;
                JOptionPane.showMessageDialog(dialog, "Something went wrong in reading row.\nUpdate this row by sql-query.", "Error", JOptionPane.ERROR_MESSAGE);
                controller.cancelButtonListener();
            }

        if (flag) {
            insert.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    done = controller.insertButtonListener(panel);
                }
            });
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    controller.cancelButtonListener();
                }
            });
            dialog.setTitle("Input");
            dialog.setModal(true);
            dialog.setContentPane(panel);
            dialog.setLocation(200, 150);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.pack();
            dialog.setVisible(true);
        }
    }
}
