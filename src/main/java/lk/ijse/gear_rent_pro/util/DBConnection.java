package lk.ijse.gear_rent_pro.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String PROPS = "/database.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = DBConnection.class.getResourceAsStream(PROPS)) {
            Properties p = new Properties();
            p.load(in);
            url = p.getProperty("jdbc.url");
            user = p.getProperty("jdbc.user");
            password = p.getProperty("jdbc.password");
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
