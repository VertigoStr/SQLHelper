import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import code.GUI.MainFrame;

import javax.swing.*;

/**
 * Начальный класс.
 */
public class Start extends AnAction {
    private final MainFrame frame = new MainFrame();
    /**
     * Слушатель, ожидающий нажатия на иконку плагина.
     * @param e AnActionEvent.
     */
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if (project != null) {
                    frame.run(project.getBasePath());
                }
            }
        });
    }
}
