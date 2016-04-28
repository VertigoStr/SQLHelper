package code.GUI;

import code.Controller.CodeGeneratorController;
import code.Utils.Cast;
import code.Utils.Icons;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Окно управления генерацией программного кода.
 */
public class CodeGeneratorDialog {
    /** Контроллер. */
    private CodeGeneratorController controller;

    /**
     * Конструктор класса.
     * @param name Имя базы данных
     * @param defaultFolderName Имя папки для хранения по умолчанию
     */
    public CodeGeneratorDialog(String name, String defaultFolderName)
    {
        controller = new CodeGeneratorController(name);
        initComponents(defaultFolderName);
    }

    /**
     * Создание компонентов окна.
     * @param defaultFolderName Имя папки для хранения по умолчанию
     */
    private void initComponents(final String defaultFolderName)
    {
        final JDialog dialog = new JDialog();
        dialog.setIconImage(Cast.iconToImage(Icons.Main));
        controller.setDialog(dialog);
        JPanel panel = new JPanel();
        JScrollPane listOfTables = getListOfTables();

        final JPanel subPanel = new JPanel();
        GroupLayout sublayout = new GroupLayout(subPanel);
        subPanel.setLayout(sublayout);
        sublayout.setAutoCreateGaps(true);
        sublayout.setAutoCreateContainerGaps(true);
        GroupLayout.SequentialGroup vGroup = sublayout.createSequentialGroup();
        GroupLayout.ParallelGroup hGroup = sublayout.createParallelGroup(GroupLayout.Alignment.LEADING);

        String[] elements = {"indexes", "views", "triggers"};
        String[] listOfCategories = {"index", "view", "trigger"};
        int i = 0;
        final ArrayList<JCheckBox> elementsList = new ArrayList<JCheckBox>();
        for (String st: elements) {
            JCheckBox box = new JCheckBox("Creating " + st + " in the onCreate() method?");
            box.setVisible(true);
            box.setSelected(false);
            box.setName(listOfCategories[i]);
            elementsList.add(box);
            i++;
            hGroup.addComponent(box);
            vGroup.addGroup(sublayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(box));
        }
        sublayout.setHorizontalGroup(sublayout.createSequentialGroup().addGroup(hGroup));
        sublayout.setVerticalGroup(vGroup);
        subPanel.setVisible(false);
        subPanel.setBorder(BorderFactory.createLineBorder(JBColor.BLACK));

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel packageNameLabel = new JLabel("Set package name:");
        JLabel classNameLabel = new JLabel("Set main class name:");
        JLabel folderNameLabel = new JLabel("Set folder path:");
        JLabel setType = new JLabel("Set type of code:");

        String[] params = new String[] {"Java", "Java Android SDK"};
        final JComboBox<String> types = new JComboBox<String>(params);
        types.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (types.getSelectedItem().toString().equalsIgnoreCase("Java Android SDK")) {
                    subPanel.setVisible(true);
                    dialog.repaint();
                    dialog.revalidate();
                    dialog.pack();
                } else {
                    subPanel.setVisible(false);
                    dialog.repaint();
                    dialog.revalidate();
                    dialog.pack();
                }
            }
        });
        types.setSelectedIndex(0);
        types.setEditable(false);

        final JTextField packageName = new JTextField("DBHelper");
        final JTextField className = new JTextField("DBHelper");
        final JTextField folderName = new JTextField(defaultFolderName);
        controller.setFolderField(folderName);

        JButton newFolder = new JButton("Set folder");
        newFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.newFolderListener(dialog);
            }
        });
        JButton defaultFolder = new JButton("Default");
        defaultFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.defaultFolderListener(defaultFolderName);
            }
        });
        JButton generate = new JButton("Generate");
        generate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.generateListener(className.getText(), packageName.getText(), types.getSelectedItem().toString(), elementsList);
            }
        });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelListener();
            }
        });

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(listOfTables)
                                        .addComponent(subPanel)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(folderNameLabel)
                                                                        .addComponent(packageNameLabel)
                                                                        .addComponent(classNameLabel)
                                                                        .addComponent(setType)
                                                        )
                                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(folderName)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                        .addComponent(newFolder)
                                                                                        .addComponent(defaultFolder)
                                                                        )
                                                                        .addComponent(packageName)
                                                                        .addComponent(className)
                                                                        .addComponent(types)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                        .addComponent(generate)
                                                                                        .addComponent(cancel)
                                                                        )
                                                        )
                                        )
                        )
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(listOfTables)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(subPanel)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(folderNameLabel)
                                        .addComponent(folderName)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(newFolder)
                                        .addComponent(defaultFolder)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(packageNameLabel)
                                        .addComponent(packageName)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(classNameLabel)
                                        .addComponent(className)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(setType)
                                        .addComponent(types)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(generate)
                                        .addComponent(cancel)
                        )
        );

        dialog.setTitle("Code Generator");
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.setLocation(300, 200);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }

    /**
     * Создание панели, содержащей список таблиц.
     * @return панель, содержащая список таблиц.
     */
    private JScrollPane getListOfTables()
    {
        JPanel panel = new JPanel();
        ArrayList<String> list = controller.getTablesName();
        ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setViewportView(panel);

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        if (list.size() == 0) {
            JLabel label = new JLabel("No tables were founded.\nCreate some tables and try to generate code again.");
            layout.setHorizontalGroup(
                    layout.createSequentialGroup()
                            .addComponent(label)
            );
            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addComponent(label)
            );
        }

        if (list.size() >= 3) {
            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
            GroupLayout.ParallelGroup hSubGroup1 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
            GroupLayout.ParallelGroup hSubGroup2 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
            GroupLayout.ParallelGroup hSubGroup3 = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

            for (int i = 0; i < list.size() - 2; i += 3) {
                JCheckBox box1 = new JCheckBox(list.get(i));
                box1.setSelected(true);
                JCheckBox box2 = new JCheckBox(list.get(i + 1));
                box2.setSelected(true);
                JCheckBox box3 = new JCheckBox(list.get(i + 2));
                box3.setSelected(true);

                checkBoxes.add(box1);
                checkBoxes.add(box2);
                checkBoxes.add(box3);

                hSubGroup1.addComponent(box1);
                hSubGroup2.addComponent(box2);
                hSubGroup3.addComponent(box3);

                vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(box1)
                                .addComponent(box2)
                                .addComponent(box3)
                );
            }

            if (list.size() % 3 == 2)
            {
                JCheckBox box1 = new JCheckBox(list.get(list.size() - 2));
                box1.setSelected(true);
                JCheckBox box2 = new JCheckBox(list.get(list.size() - 1));
                box2.setSelected(true);

                checkBoxes.add(box1);
                checkBoxes.add(box2);
                hSubGroup1.addComponent(box1);
                hSubGroup2.addComponent(box2);

                vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(box1)
                                .addComponent(box2)
                );
            }

            if (list.size() % 3 == 1)
            {
                JCheckBox box1 = new JCheckBox(list.get(list.size() - 1));
                box1.setSelected(true);

                checkBoxes.add(box1);
                hSubGroup1.addComponent(box1);

                vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(box1)
                );
            }

            layout.setHorizontalGroup(
                    layout.createSequentialGroup()
                            .addGroup(hSubGroup1)
                            .addGroup(hSubGroup2)
                            .addGroup(hSubGroup3)
            );
            layout.setVerticalGroup(vGroup);
        }

        if (list.size() <= 2 && list.size() > 0) {
            String labelText = "";
            for (int i = 0; i < 100; i++)
                labelText += " ";
            JLabel label = new JLabel(labelText);
            GroupLayout.SequentialGroup vG = layout.createSequentialGroup();
            GroupLayout.ParallelGroup hG =  layout.createParallelGroup(GroupLayout.Alignment.LEADING);
            for (String st : list)
            {
                JCheckBox box = new JCheckBox(st);
                box.setSelected(true);
                checkBoxes.add(box);

                hG.addComponent(box);
                vG.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(box)
                );
            }
            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addGroup(vG)
                            .addComponent(label)
            );
            layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(hG).addComponent(label));
        }
        controller.setBoxes(checkBoxes);
        return scrollPane;
    }
}
