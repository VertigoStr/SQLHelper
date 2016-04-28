package code.GUI;

import code.Controller.ElementEditorController;
import code.Controller.MainFrameController;
import code.Utils.Cast;
import code.Utils.Icons;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Текстовое поле для ввода sql запроса.
 * Используется для создания триггеров, представления, транзакций.
 */
public class ElementEditor {
    /** Контроллер. */
    private ElementEditorController controller;
    /** Выполняемая функция в данном окне. */
    private String function;
    /** Имя элемента. */
    private String elementName = "";

    /**
     * Конструктор класса.
     * @param dataBaseName Имя базы данных
     * @param function Функция, необходимая к выполнению
     * @param mainFrameController Контроллер класса MainFrameController
     * @see code.Controller.MainFrameController
     */
    public ElementEditor(String dataBaseName, String function, MainFrameController mainFrameController)
    {
        controller = new ElementEditorController(dataBaseName);
        this.function = function;
        initComponents(mainFrameController);
    }

    /**
     * Конструктор класса.
     * @param dataBaseName Имя базы данных
     * @param function Функция, необходимая к выполнению
     * @param elementName Имя элемента
     * @param mainFrameController Контроллер класса MainFrameController
     * @see code.Controller.MainFrameController
     */
    public ElementEditor(String dataBaseName, String function, String elementName, MainFrameController mainFrameController)
    {
        controller = new ElementEditorController(dataBaseName);
        this.elementName = elementName;
        this.function = function;
        initComponents(mainFrameController);
    }

    /**
     * Создание компонентов окна.
     * @param mainFrameController Контроллера класса MainFrameController
     * @see code.Controller.MainFrameController
     */
    private void initComponents(final MainFrameController mainFrameController)
    {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        panel.setPreferredSize(new Dimension(675, 510));

        final JDialog dialog = new JDialog();
        dialog.setIconImage(Cast.iconToImage(Icons.Main));
        dialog.setTitle(function);

        String[] params = function.split(" ");
        function = params[0];
        final String element = params[1];

        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.setLocation(300, 100);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        controller.setJDialog(dialog);
        final JTextArea text = new JTextArea();
        if (!elementName.isEmpty())
        {
            text.setText(controller.getCreateElement(elementName));
        }

        JScrollPane scrollPane = new JScrollPane(text);
        scrollPane.setViewportView(text);

        JButton createButton = new JButton(function);
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (function.equalsIgnoreCase("Edit")) {
                    if (controller.editElement("DROP " + element + " " + elementName)) {
                        controller.createButtonActionListener(text);
                    }
                } else {
                    controller.createButtonActionListener(text);
                }
                mainFrameController.updateTree();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelButtonActionListener();
            }
        });

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(scrollPane)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addComponent(createButton)
                                                        .addComponent(cancelButton)
                                        )
                        )
        );
        layout.linkSize(SwingConstants.HORIZONTAL, cancelButton, createButton);
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPane)
                        ).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(createButton)
                                .addComponent(cancelButton)
                )
        );
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setVisible(true);
    }
}
