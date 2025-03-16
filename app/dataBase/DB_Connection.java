package dataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB_Connection {
	 private String url;
	    private String username;
	    private String pwd;
	    private Connection connection;

	    // Constructor
	    public DB_Connection(String url, String username, String pwd) {
	        this.url = url;
	        this.username = username;
	        this.pwd = pwd;
	    }

	    // Abrir conexión
	    public void openConnection() throws SQLException {
	        if (this.connection == null || this.connection.isClosed()) {
	            try {
	                Class.forName("com.mysql.cj.jdbc.Driver");
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
