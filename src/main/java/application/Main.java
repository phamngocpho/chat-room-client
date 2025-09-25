package application;

import features.CustomNotification;
import features.FormsManager;
import features.notifications.popup.GlassPanePopup;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import panels.ClientPanel;
import raven.toast.Notifications;
import values.Value;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    public Main() {
        GlassPanePopup.install(this);
        init();
    }

    private void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowOpened(WindowEvent e) {
//                Notifications.getInstance().show(Notifications.Type.SUCCESS, "test");
//            }
//        });
        setSize((int) Value.dimension.getWidth(), (int) (Value.dimension.getHeight() - Value.taskBarSize));
//        System.out.println(Value.dimension);
        Notifications.getInstance().setJFrame(this);
        CustomNotification customNotification = new CustomNotification();
        customNotification.setJFrame(this);
        setLocationRelativeTo(null);
        setContentPane(new ClientPanel());
        setMinimumSize(new Dimension((int) (Value.dimension.getWidth() / 2), (int) Value.dimension.getHeight() * 3 / 5));
        FormsManager.getInstance().initApplication(this);
    }

    public static void main(String[] args) {
        FlatRobotoFont.install();
        FlatMacDarkLaf.setup();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 15));
        EventQueue.invokeLater(() -> new Main().setVisible(true));

//        FormsManager.getInstance().showForm(new sth);
    }
}