package values;

import java.awt.*;

public class Value {
    public static double featuresPanelSize = 0.04;
    public static double chatListPanelSize = 0.2;
    public static double chatWindowPanelSize = 0.5;
    public static double galleryPanelSize = 0.26;
    static Toolkit toolkit = Toolkit.getDefaultToolkit();
    public static Dimension dimension = toolkit.getScreenSize();
    static Insets insets = toolkit.getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
    public static final int taskBarSize = insets.bottom;
    public static Color onlineStatus = Color.decode("#43c95a");
    public static Color lighter_gray = Color.decode("#404040");
    public static Color bolder_gray = Color.decode("#3c3c3c");
    public static Color bright_red = Color.decode("#ff4d4d");
    public static Color bright_orange = Color.decode("#ffb04d");
    public static Color bright_green = Color.decode("#58C359");
    public static Color sky_blue = Color.decode("#38bdf8");
    public static Color deep_blue = Color.decode("#2a84ff");
    public static Color dark_gray = Color.decode("#282828");
    public static Color message_left = Color.decode("#303030");
    public static Color message_right = Color.decode("#1371ff");
    public static Color unsent_message = Color.decode("#8b8c90");
    public static Color sent_time = Color.decode("#686868");
    public static Color white = Color.decode("#ffffff");
    public static final String app_password = "eonc hgsq xigm tfgv";
    public static final String email = "chatapplication41@gmail.com";
    public static final String url = "jdbc:sqlserver://localhost:1433;" + "databaseName=Application;" + "user=sa;" + "password=160;" + "encrypt=true;" + "trustServerCertificate=true";
    public static final String resources = System.getProperty("user.dir") + "/src/main/resources/";
    public static final String LOGIN = "";
    public static final String REGISTER = "INSERT into Users (FullName, Gender, Dob, Email, Verified, PhoneNumber, Password, Auth_code) VALUES (?, ?, CONVERT(date, ?, 105), ?, ?, ?, ?, ?)";
    public static final String MESSAGE = "";
    public static final String FIND_USER_BY_NAME = "";
    public static final String FIND_MESSAGE = "";
    public static final String EMAIL_EXIST = "SELECT * FROM Users WHERE Email = ?";
    public static final String SERVER_ADDRESS = "https://567b-103-82-39-220.ngrok-free.app";
    public static final int PORT = 1234;
    public static final String SERVER_URL = "https://abdf-14-254-131-170.ngrok-free.app";
    public static final String SERVER = "localhost";
    public static final String DateTimeFormat = "dd/MM/yyyy";
//    public static final String ngrok_token = "2g3QydMakfsej8PGe88Xm4czIzZ_2YYNuqgyJjRPWPHiQzcb8";
}
