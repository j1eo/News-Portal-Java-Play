package dataBase;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DB_Connection {
    private String url;
    private String username;
    private String pwd;
    private Connection connection;

    // Constructor
    public DB_Connection(String propertiesPath) {
        try {
            Properties props = new Properties();
            // Cargar las propiedades del archivo .properties
            FileInputStream fis = new FileInputStream(propertiesPath);
            props.load(fis);

            // Asignar valores desde el archivo properties
            this.url = props.getProperty("DB_URL");
            this.username = props.getProperty("DB_USER");
            this.pwd = props.getProperty("DB_PASS");
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar las propiedades de la base de datos", e);
        }
    }

    // Abrir conexión
    public void openConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver"); // Cargar el driver de MySQL
                this.connection = DriverManager.getConnection(url, username, pwd);
                System.out.println("Conexión abierta con éxito.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Error al cargar el driver de MySQL", e);
            }
        }
    }

    // Cerrar conexión
    public void closeConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            try {
                this.connection.close();
                System.out.println("Conexión cerrada con éxito.");
            } catch (SQLException e) {
                throw new SQLException("Error al cerrar la conexión", e);
            }
        }
    }

    // Getter para obtener la conexión
    public Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed()) {
            openConnection();
        }
        return this.connection;
    }
}
