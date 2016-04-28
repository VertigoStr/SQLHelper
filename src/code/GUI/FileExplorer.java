package code.GUI;
import code.Controller.FileExplorerController;
import code.Utils.Keeper;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Просмотр файлов на мобильном устройстве.
 */
public class FileExplorer {
    /** Контроллер. */
    private FileExplorerController controller;
    /** Имя выбранного файла. */
    private String name;
    /** Имя базы данных. */
    private String dbName;
    /** Имя выполняемой команды. */
    private String title;

    /**
     * Конструктор класса.
     * @param parent Родительский контейнер
     * @param name Имя базы данных
     * @param title Тип операции
     */
    public FileExplorer(JFrame parent, String name, String title) {

        controller = new FileExplorerController(Keeper.getSDKLocation());
        this.dbName = name;
        this.title = title;
        initComponents(parent);
    }

    /**
     * Конструктор класса.
     * @param parent Родительский контейнер
     */
    public FileExplorer(JFrame parent) {
        controller = new FileExplorerController(Keeper.getSDKLocation());
        title = "Open";
        initComponents(parent);
    }

    /**
     * Получить результат работы окна.
     * @return результат работы.
     */
    public String getResult() {
        return name;
    }

    /**
     * Создание компонентов окна.
     * @param parent Родительский контейнер
     */
    private void initComponents(JFrame parent) {
        JPanel panel = new JPanel();
        final JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setIconImage(parent.getIconImage());
        dialog.setResizable(false);
        dialog.setContentPane(panel);
        dialog.setLocation(200, 150);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        controller.setDialog(dialog);

        JScrollPane tree = initTreePanel();

        JButton createDataBaseButton = new JButton();
        JButton okButton = new JButton("Open");

        boolean flag = false;
        if (title.equals("Open")) {
            createDataBaseButton.setVisible(false);
            createDataBaseButton.setEnabled(false);
        }
        if (title.equals("Create")) {
            okButton.setEnabled(false);
            okButton.setVisible(false);

            createDataBaseButton.setIcon(UIManager.getIcon("FileChooser.newFolderIcon"));
            createDataBaseButton.setToolTipText("Create new database file");
        }
        if (title.equals("Push")) {
            flag = true;
            okButton.setEnabled(false);
            okButton.setVisible(false);

            createDataBaseButton.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
            createDataBaseButton.setToolTipText("Push database file");
        }
        if (title.equals("Pull")) {
            flag = false;
            okButton.setEnabled(false);
            okButton.setVisible(false);

            createDataBaseButton.setIcon(UIManager.getIcon("FileChooser.upFolderIcon"));
            createDataBaseButton.setToolTipText("Pull database file");
        }
        final boolean mode = flag;
        createDataBaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (title.equals("Pull") || title.equals("Push")) {
                    controller.PushPullListener(mode, dbName);
                } else {
                    if ((name = controller.newDataBaseListener()) != null) {
                        dialog.setVisible(false);
                        dialog.dispose();
                    }
                }
            }
        });

        okButton.setToolTipText("Usually database file can be founded in this directory: " +
                "/data/data/<your.package.name>/databases/file");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((name = controller.openListener()) != null) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelListener();
            }
        });

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        String st = "";
        for (int i = 0; i < 75; i++)
            st += " ";
        JLabel label = new JLabel(st);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(tree)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addComponent(createDataBaseButton)
                                                        .addComponent(label)
                                                        .addComponent(okButton)
                                                        .addComponent(cancelButton)
                                        )
                        )
        );
        layout.linkSize(SwingConstants.HORIZONTAL, cancelButton, okButton);
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(tree)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(createDataBaseButton)
                                        .addComponent(label)
                                        .addComponent(okButton)
                                        .addComponent(cancelButton)
                        )
        );

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setVisible(true);
    }

    /**
     * Создание дерева элементов.
     * @return дерево элементов.
     */
    private JScrollPane initTreePanel()
    {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Devices");
        DefaultMutableTreeNode category;
        ArrayList<String> list = controller.getDevices();
        for (String listOfCategory : list) {
            category = new DefaultMutableTreeNode(listOfCategory);
            top.add(category);
        }

        final JTree tree = new JTree(new DefaultTreeModel(top));
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getClosedIcon());
        tree.setCellRenderer(renderer);
        controller.setTree(tree);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e)
            {
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    TreePath path = tree.getPathForLocation ( e.getX (), e.getY () );
                    Rectangle pathBounds = tree.getUI ().getPathBounds ( tree, path );
                    if ( pathBounds != null && pathBounds.contains ( e.getX (), e.getY () ) ) {
                        assert path != null;
                        if (e.getClickCount() == 1)
                            controller.treeListener(path);
                    }
                }
            }
        });
        return new JScrollPane(tree);
    }
}