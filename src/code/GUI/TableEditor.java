package code.GUI;

import code.Controller.MainFrameController;
import code.Controller.TableEditorController;
import code.Utils.Cast;
import code.Utils.Icons;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

/**
 * Окно для создания таблицы.
 */
public class TableEditor {
    /** Имя таблицы(Label). */
    private JLabel tableName = new JLabel("Table name:          ");
    /** Тип команды. */
    private String name;
    /** Имя таблицы. */
    private String editTableName = "";
    /** Контроллер. */
    private TableEditorController controller;

    /**
     * Конструктор класса.
     * @param dataBaseName Имя базы данных
     * @param name Тип команды(Создание или редактирование)
     * @param elementName Имя таблицы
     * @param mfc Контроллер класса MainFrameController
     * @see code.Controller.MainFrameController
     */
    public TableEditor(String dataBaseName, String name, String elementName, MainFrameController mfc)
    {
        this.name = name;
        this.editTableName = elementName;
        controller = new TableEditorController(dataBaseName, elementName, mfc);
        initComponents();
    }

    /**
     * Конструктор класса.
     * @param dataBaseName Имя базы данных
     * @param name Тип команды(Создание или редактирование)
     * @param mfc Контроллер класса MainFrameController
     * @see code.Controller.MainFrameController
     */
    public TableEditor(String dataBaseName, String name, MainFrameController mfc)
    {
        this.name = name;
        controller = new TableEditorController(dataBaseName, name, mfc);
        initComponents();
    }

    /**
     * Создание компонентов окна.
     */
    private void initComponents() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(675, 510));
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        GroupLayout panelLayout = new GroupLayout(panel);
        panelLayout.setAutoCreateGaps(true);
        panelLayout.setAutoCreateContainerGaps(true);
        panel.setLayout(panelLayout);

        final JDialog dialog = new JDialog();
        dialog.setIconImage(Cast.iconToImage(Icons.Main));
        dialog.setTitle(name);
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.setLocation(300, 100);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        controller.setDialog(dialog);

        JPanel tableField = new JPanel();
        tableField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        GroupLayout layout = new GroupLayout(tableField);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        tableField.setLayout(layout);

        final JTextField tableNameTextField = new JTextField();
        tableNameTextField.setText(editTableName.isEmpty() ? "" : editTableName);
        JButton createTable = new JButton(name);
        JButton cancelButton = new JButton("Cancel");
        createTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.createTableButtonListener(tableNameTextField.getText());
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelButtonListener();
            }
        });

        panelLayout.setHorizontalGroup(
                panelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(panelLayout.createSequentialGroup()
                                        .addComponent(tableName)
                                        .addComponent(tableNameTextField)
                        )
                        .addComponent(tableField)
                        .addGroup(panelLayout.createSequentialGroup()
                                        .addComponent(createTable)
                                        .addComponent(cancelButton)
                        )
        );

        panelLayout.linkSize(SwingConstants.HORIZONTAL, createTable, cancelButton);

        panelLayout.setVerticalGroup(
                panelLayout.createSequentialGroup()
                        .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(tableName)
                                        .addComponent(tableNameTextField)
                        )
                        .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(tableField))
                        .addGroup(panelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(createTable)
                                        .addComponent(cancelButton)
                        )
        );

        String[] columnNames = {"Field Name", "Field Type", "Default Value", "Properties"};
        Object[][] data = {};
        DefaultTableModel model = editTableName.isEmpty() ? new DefaultTableModel(data, columnNames) : controller.getDataForTable();

        final JTable table = new JTable() {
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        table.setShowGrid(true);
        table.setGridColor(Color.black);
        controller.setTableModel(model);
        table.setModel(model);
        controller.setTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setViewportView(table);
        table.setPreferredScrollableViewportSize(new Dimension(500, 500));
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                controller.tableMouseAdapter(e);
            }
        });
        JButton addField = new JButton("Add Field");
        addField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.addFieldActionListener();
            }
        });
        JButton editField = new JButton("Edit Field");
        editField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.editFieldActionListener();
            }
        });
        JButton deleteField = new JButton("Delete Field");
        deleteField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.deleteFieldActionListener();
            }
        });

        if (editTableName.isEmpty()) {
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(scrollPane)
                            .addGroup(layout.createSequentialGroup()
                                            .addComponent(addField)
                                            .addComponent(editField)
                                            .addComponent(deleteField)
                            )
            );

            layout.linkSize(SwingConstants.HORIZONTAL, editField, addField);
            layout.linkSize(SwingConstants.HORIZONTAL, addField, deleteField);

            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(scrollPane))
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addComponent(addField)
                                    .addComponent(editField)
                                    .addComponent(deleteField))
            );
        }
        else
        {
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(scrollPane)
                            .addGroup(layout.createSequentialGroup()
                                            .addComponent(addField)
                            )
            );

            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(scrollPane))
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addComponent(addField))
            );
        }
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setVisible(true);
    }
}
