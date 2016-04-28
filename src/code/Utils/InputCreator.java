package code.Utils;

import code.DataBaseHelper.DeviceDBHelper;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;

/**
 * Класс, создающий поля для ввода.
 */
public class InputCreator {
    /**
     * Диалог для ввода данных, предназначенных для создания базы данных на мобильном устройстве.
     * @param folder Имя папки, в которой будет создана база данных
     * @param deviceName Имя устройства
     * @param dDB Экземпляр класса DeviceDBHelper
     * @return результат ввода.
     * @see code.DataBaseHelper.DeviceDBHelper
     */
    public static String getInputDialog(final String folder, final String deviceName, final DeviceDBHelper dDB) {
        final JPanel panel = new JPanel();
        final JTextField text = new JTextField();
        text.setToolTipText("Input database file name");
        text.setPreferredSize(new Dimension(300, 25));

        JLabel packageInfo = new JLabel("If folder database has not created then it will be created automaticaly.");
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(text)
                                        .addComponent(packageInfo)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addComponent(createButton)
                                                        .addComponent(cancelButton)
                                        )
                        )
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(text)
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(packageInfo)
                        ).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(createButton)
                                .addComponent(cancelButton)
                )
        );

        final JDialog dialog = new JDialog();
        dialog.setIconImage(Cast.iconToImage(Icons.Main));
        dialog.setTitle("Create database");
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.setLocation(200, 150);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final ArrayList<String> list = new ArrayList<String>();
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String txt;
                txt = (txt = Regex.getMatch("[/](.+)$", text.getText().replace("\\", "/"))) != null ? txt : text.getText();
                if (txt.endsWith(".db") || txt.endsWith(".s3db")) {
                    try {
                        String fol = folder;
                        if (!(folder + "/").toLowerCase().contains("/databases/")) {
                            fol = folder + "/databases";
                            dDB.shellCommand("mkdir " + fol, deviceName);
                        }
                        fol += "/" + txt;
                        boolean flag = dDB.sqlCommand("create table val(id int)", deviceName, fol);
                        if (flag)
                            flag = dDB.sqlCommand("drop table val", deviceName, fol);
                        if (flag)  {
                            list.add(fol);
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error in format of database, it must be \"db\" or \"s3db\"", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        dialog.pack();
        dialog.setVisible(true);
        return list.size() >= 1 ? list.get(0) : null;
    }

    /**
     * Создание JSpinner для работы с датой.
     * @param i тип данных: 0 - TimeStamp, 1 - Time, 2 - Date
     * @return контейнер для работы с датой.
     */
    public static JSpinner getTimeStampBox(int i)
    {
        /**
         *  0 - TimeStamp
         *  1 - Time
         *  2 - Date
         */
        JSpinner timeSpinner = new JSpinner( new SpinnerDateModel() );
        timeSpinner.setToolTipText(i == 0 ? "TIMESTAMP" : i == 1 ? "TIME" : i == 2 ? "DATE" : "");
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner,
                i == 0 ? "yyyy-MM-dd HH:mm:ss" : i == 1 ? "HH:mm:ss" : i == 2 ? "yyyy-MM-dd" : "");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setValue(new Date());
        return timeSpinner;
    }

    /**
     * Текстовый диалог для подробного просмотра текста.
     * @param valueInFocus Текст
     */
    public static void textDialog(final String valueInFocus) {
        final JPanel panel = new JPanel();
        final JTextArea text = new JTextArea();
        text.setText(valueInFocus);
        text.setEditable(false);
        text.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        JScrollPane scrollPane = new JScrollPane(text);
        scrollPane.setViewportView(text);
        scrollPane.setPreferredSize(new Dimension(500, 250));

        JButton confirmButton = new JButton("Confirm");

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(scrollPane)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addComponent(confirmButton)
                                        )
                        )
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPane)
                        ).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(confirmButton)
                )
        );

        final JDialog dialog = new JDialog();
        dialog.setIconImage(Cast.iconToImage(Icons.Main));
        dialog.setTitle("Memo");
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.setLocation(200, 150);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        dialog.pack();
        dialog.setVisible(true);
    }
}