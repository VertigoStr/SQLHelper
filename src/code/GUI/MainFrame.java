package code.GUI;

import code.Controller.JMenuController;
import code.Controller.MainFrameController;
import code.Utils.Cast;
import code.Utils.Icons;
import code.Utils.Utils;
import code.Utils.Keeper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Главное окно.
 * Управление элементами базы данных.
 */
public class MainFrame {
    /** Контроллер. */
    private MainFrameController controller;
    /** Родительский контейнер. */
    private JFrame main;

    /**
     * Конструктор класса.
     */
    public MainFrame()
    {
        controller = null;
    }

    /**
     * Метод для запуска главного окна
     * @param defaultFolderName Имя папки для хранения генерируемого кода по умолчанию
     */
    public void run(String defaultFolderName) {
        if (main == null || !main.isDisplayable()) {
            main = new JFrame("SQL Helper");
            main.setSize(800, 500);
            main.setLocation(100, 100);
            main.setVisible(true);
            main.setIconImage(Cast.iconToImage(Icons.Main));
            main.setJMenuBar(createMenuBar(defaultFolderName));
            main.getContentPane().add(initComponents(null));
        } else {
            main.toFront();
        }
    }

    /**
     * Создание компонентов окна.
     * @param name Имя базы данных
     * @return компоненты окна, помещенные в Panel.
     */
    private JPanel initComponents(String name)
    {
        JPanel panel = new JPanel();
        JScrollPane treeElements;
        if (name != null) {
            String [] params = name.split("\t");
            controller = params.length == 2
                    ? new MainFrameController(params[1], params[0])
                    : new MainFrameController(name);
            main.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    controller.windowCloseListener(e);
                }
            });
            controller.setFrame(main);
            treeElements = initTreePanel();
        }
        else {
            main.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            treeElements = initTreePanelSimple();
        }

        JTabbedPane workOut = initTabbedPane();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(treeElements, 150, 200, 250)
                        )
                        .addContainerGap(10, 15)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(workOut)
                        )
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(treeElements)
                                        .addComponent(workOut)
                        )
        );
        return panel;
    }

    /**
     * Создание меню.
     * @param defaultFolderName Имя папки для хранения генерируемого кода по умолчанию
     * @return меню.
     */
    private JMenuBar createMenuBar(final String defaultFolderName)
    {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("Open");
        menu.setMnemonic(KeyEvent.VK_O);

        final JMenu menuRecent = new JMenu("Recent");
        updateMenu(menuRecent);

        JMenuItem item = new JMenuItem("from PC");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update(JMenuController.openListener(main), menuRecent);
            }
        });
        menu.add(item);

        item = new JMenuItem("from Device");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Utils.checkSDK(main, defaultFolderName))
                    update(JMenuController.openFromDevice(main), menuRecent);
            }
        });
        menu.add(item);
        menuBar.add(menu);

        menu = new JMenu("Create");
        menu.setMnemonic(KeyEvent.VK_C);

        item = new JMenuItem("on PC");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update(JMenuController.createListener(main), menuRecent);
            }
        });
        menu.add(item);

        item = new JMenuItem("on Device");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Utils.checkSDK(main, defaultFolderName))
                    update(JMenuController.createOnDevice(main, defaultFolderName), menuRecent);
            }
        });
        menu.add(item);
        menuBar.add(menu);

        menuRecent.setMnemonic(KeyEvent.VK_R);
        menuBar.add(menuRecent);

        menu = new JMenu("Generate Code");
        menu.setMnemonic(KeyEvent.VK_G);
        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (controller != null)
                    new CodeGeneratorDialog(controller.getDataBaseName(), defaultFolderName);
                else
                    JOptionPane.showMessageDialog(main, "It seems no database was opened.\nPlease, open database you need.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        menuBar.add(menu);

        menu = new JMenu("Push\\Pull");
        item = new JMenuItem("Push    ");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller != null) {
                    if (Utils.checkSDK(main, defaultFolderName))
                        JMenuController.pushOnDevice(main, controller.getDataBaseName());
                } else
                    JOptionPane.showMessageDialog(main, "It seems no database was opened.\nPlease, open database you need.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        menu.add(item);

        item = new JMenuItem("Pull");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Utils.checkSDK(main, defaultFolderName))
                    JMenuController.pullFromDevice(main, defaultFolderName);
            }
        });
        menu.add(item);
        menuBar.add(menu);

        return menuBar;
    }

    /**
     * Управление обновлением контента окна.
     * @param result Имя базы данных
     * @param menuRecent Элемент меню
     */
    private void update(String result, JMenu menuRecent) {
        if (result != null && !result.isEmpty()) {
            Keeper.push(result);
            updateMenu(menuRecent);
            updateContent(result);
        }
    }

    /**
     * Обновление контента окна.
     * @param result Имя базы данных
     */
    private void updateContent(String result)
    {
        if (Utils.isExist(main, result)) {
            main.getContentPane().removeAll();
            main.getContentPane().revalidate();
            main.getContentPane().repaint();
            main.getContentPane().add(initComponents(result));
        }
    }

    /**
     * Обновление списка недавних файлов.
     * @param menuRecent Элемент меню
     */
    private void updateMenu(JMenu menuRecent)
    {
        menuRecent.removeAll();
        for (final JMenuItem item : Keeper.getRecentFilesAsItems()) {
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String result = item.getText();
                    if (result != null)
                        updateContent(result);
                }
            });
            menuRecent.add(item);
        }
    }

    /**
     * Создание окна с вкладками.
     * @return окно с вкладками.
     */
    private JTabbedPane initTabbedPane()
    {
        final JTabbedPane tabbedPane = new JTabbedPane();
        try {
            // tabbed pane №1 - Table View
            JComponent panel1 = new JPanel();
            final JTable selectedTable = new JTable()
            {
                public boolean isCellEditable(int rowIndex, int colIndex)
                {
                    return false;
                }
            };
            selectedTable.setShowGrid(true);
            selectedTable.setGridColor(Color.black);

            JScrollPane scrollPane = new JScrollPane(selectedTable);
            scrollPane.setViewportView(selectedTable);
            selectedTable.setPreferredScrollableViewportSize(new Dimension(500, 500));
            selectedTable.getTableHeader().setReorderingAllowed(false);
            selectedTable.setFillsViewportHeight(true);

            GroupLayout layout = new GroupLayout(panel1);
            panel1.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            JButton addNewRowButton = new JButton("Add Row");
            JButton deleteRowButton = new JButton("Delete Row");
            JButton updateRowButton = new JButton("Update Row");
            final JTextField numberPage = new JTextField();
            numberPage.setText("1");


            BasicArrowButton rightPage = new BasicArrowButton(BasicArrowButton.EAST);
            BasicArrowButton leftPage = new BasicArrowButton(BasicArrowButton.WEST);
            String[] limits = {"Limit: 1", "Limit: 10", "Limit: 20", "Limit: 50", "Limit: 100", "Limit: MAX"};
            JComboBox<String> showLimit = new JComboBox<String>(limits);
            showLimit.setSelectedIndex(1);
            showLimit.setEditable(false);

            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(scrollPane)
                            .addGroup(layout.createSequentialGroup()
                                            .addComponent(leftPage, 25, 25, 25)
                                            .addComponent(numberPage, 25, 25, 25)
                                            .addComponent(rightPage, 25, 25, 25)
                                            .addComponent(showLimit, 125, 125, 125)
                                            .addComponent(addNewRowButton, 100, 100, 100)
                                            .addComponent(updateRowButton, 100, 100, 100)
                                            .addComponent(deleteRowButton, 100, 100, 100)
                            )
            );
            layout.linkSize(SwingConstants.HORIZONTAL, addNewRowButton, deleteRowButton);
            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(scrollPane)
                            )
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(leftPage, 25, 25, 25)
                                            .addComponent(numberPage, 25, 25, 25)
                                            .addComponent(rightPage, 25, 25, 25)
                                            .addComponent(showLimit, 25, 25, 25)
                                            .addComponent(addNewRowButton, 25, 25, 25)
                                            .addComponent(updateRowButton, 25, 25, 25)
                                            .addComponent(deleteRowButton, 25, 25, 25)
                            )
            );
            tabbedPane.add("Table", panel1);

            // tabbed pane №2 - Field for sql query
            JComponent panel2 = new JPanel();
            final JTextArea queryTextArea = new JTextArea();
            JScrollPane queryScrollPane = new JScrollPane(queryTextArea);
            queryScrollPane.setViewportView(queryTextArea);


            JButton createButton = new JButton("Create");
            createButton.setToolTipText("Press F9 to execute command");

            GroupLayout sqlQueryLayout = new GroupLayout(panel2);
            panel2.setLayout(sqlQueryLayout);
            sqlQueryLayout.setAutoCreateGaps(true);
            sqlQueryLayout.setAutoCreateContainerGaps(true);

            sqlQueryLayout.setHorizontalGroup(
                    sqlQueryLayout.createSequentialGroup()
                            .addGroup(sqlQueryLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addComponent(queryScrollPane)
                                            .addGroup(sqlQueryLayout.createSequentialGroup()
                                                            .addComponent(createButton, 100, 100, 100)
                                            )
                            )
            );
            sqlQueryLayout.setVerticalGroup(
                    sqlQueryLayout.createSequentialGroup()
                            .addGroup(sqlQueryLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(queryScrollPane)
                            ).addGroup(sqlQueryLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(createButton, 25, 25, 25)
                    )
            );
            tabbedPane.add("SQL Query", panel2);



            final JTable resultSetTable = new JTable()
            {
                public boolean isCellEditable(int rowIndex, int colIndex)
                {
                    return false;
                }
            };
            resultSetTable.setShowGrid(true);
            resultSetTable.setGridColor(Color.black);
            JScrollPane resultSetScrollPane = new JScrollPane(resultSetTable);
            resultSetScrollPane.setViewportView(resultSetTable);
            resultSetTable.setPreferredScrollableViewportSize(new Dimension(500, 500));
            resultSetTable.getTableHeader().setReorderingAllowed(false);
            resultSetTable.setFillsViewportHeight(true);
            tabbedPane.add("Result", resultSetScrollPane);
            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

            //initialize actions
            if (controller != null)
            {
                controller.setSelectedTable(selectedTable);
                selectedTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        controller.tableMouseListener(e);
                    }
                });

                controller.setResultSetTable(resultSetTable);
                resultSetTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        controller.resultTableMouseListener(e);
                    }
                });
                AbstractAction createButtonPressed = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.createSqlQueryButtonListener(queryTextArea, tabbedPane);
                    }
                };
                createButton.addActionListener(createButtonPressed);
                createButton.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).
                        put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "F9");
                createButton.getActionMap().put("F9", createButtonPressed);

                rightPage.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.rightPageButton();
                    }
                });
                leftPage.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.leftPageButton();
                    }
                });
                addNewRowButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.addNewRowButtonListener();
                    }
                });
                deleteRowButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.deleteRowButtonListener();
                    }
                });
                updateRowButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.updateRowButtonListener();
                    }
                });

                controller.setPageField(numberPage);
                numberPage.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.pageFieldListener();
                    }
                });
                showLimit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.limitBoxListener();
                    }
                });
                controller.setLimiter(showLimit);
            }


        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return tabbedPane;
    }

    /**
     * Создание дерева элементов без элементов (содержит только категории).
     * @return дерево элементов.
     */
    private JScrollPane initTreePanelSimple()
    {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Simple");
        DefaultMutableTreeNode category;
        String[] listOfCategories = {"Tables", "Indexes", "Views", "Triggers"};
        for (String listOfCategory : listOfCategories) {
            category = new DefaultMutableTreeNode(listOfCategory);
            top.add(category);
        }

        JTree tree = new JTree(new DefaultTreeModel(top));
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getClosedIcon());
        tree.setCellRenderer(renderer);
        return new JScrollPane(tree);
    }

    /**
     * Создание дерева элементов.
     * @return дерево элементов.
     */
    private JScrollPane initTreePanel()
    {
        final String[] listOfCategories = {"Table", "Index", "View", "Trigger"};
        final JTree tree = new JTree();
        final DefaultTreeModel root = controller.createNodes();
        tree.setModel(root);
        controller.setTree(tree);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getClosedIcon());
        tree.setCellRenderer(renderer);
        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    JPopupMenu menu;
                    TreePath path = tree.getPathForLocation ( e.getX (), e.getY () );
                    Rectangle pathBounds = tree.getUI ().getPathBounds ( tree, path );
                    if ( pathBounds != null && pathBounds.contains ( e.getX (), e.getY () ) ) {
                        assert path != null;
                        menu = getPopupMenu(listOfCategories,
                                path.getLastPathComponent().toString(),
                                path.getParentPath().getLastPathComponent().toString(),
                                true);
                    }
                    else
                    {
                        menu = getPopupMenu(listOfCategories,
                                null,
                                null,
                                false);
                    }
                    menu.show(tree, e.getX(), e.getY());
                }
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    controller.treePanelMouseListener(e);
                }
            }
        });

        return new JScrollPane(tree);
    }

    /**
     * Создание всплывающего меню.
     * @param listOfCategories Лист категорий элементов базы данных
     * @param elementInFocus Выбранный элемент в дереве элементов
     * @param parentElementOfFocusElement Родительский элемент выбранного элемента в дереве элементов
     * @param inFocus Нажатие было произведено на каком - либо узле или нет
     * @return всплывающее меню.
     */
    private JPopupMenu getPopupMenu(final String[] listOfCategories, final String elementInFocus, final String parentElementOfFocusElement, boolean inFocus)
    {
        JPopupMenu menu = new JPopupMenu();

        ActionListener actionListenerCreateEdit = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.popUpMenuActionListenerCreateEdit(e, elementInFocus, parentElementOfFocusElement, listOfCategories);
            }
        };

        ActionListener actionDelete = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.popUpMenuActionListenerDelete(e, elementInFocus);
            }
        };
        JMenuItem item;
        for (int i = 0; i < listOfCategories.length; i++)
        {
            if (inFocus) {
                if (parentElementOfFocusElement.contains(listOfCategories[i]) && elementInFocus != null) {
                    item = new JMenuItem("Create " + listOfCategories[i]);
                    item.addActionListener(actionListenerCreateEdit);
                    menu.add(item);
                    item = new JMenuItem("Edit " + listOfCategories[i]);
                    item.addActionListener(actionListenerCreateEdit);
                    menu.add(item);
                    item = new JMenuItem("Delete " + listOfCategories[i]);
                    item.addActionListener(actionDelete);
                    menu.add(item);
                }
                else
                {
                    item = new JMenuItem("Create " + listOfCategories[i]);
                    item.addActionListener(actionListenerCreateEdit);
                    menu.add(item);
                }
            }
            else
            {
                item = new JMenuItem("Create " + listOfCategories[i]);
                item.addActionListener(actionListenerCreateEdit);
                menu.add(item);
            }
            if (i != listOfCategories.length - 1) menu.addSeparator();
        }
        return menu;
    }
}
