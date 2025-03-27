package services;

import dataBase.DB_Connection;
import org.mindrot.jbcrypt.BCrypt;
import javax.inject.Inject;
import java.sql.*;

public class AuthService {
    private final DB_Connection dbConnection;

    @Inject
    public AuthService(DB_Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public String autenticarUsuario(String username, String password) throws SQLException {
        String sql = "SELECT Nickname, Clave_Acceso FROM Cuenta WHERE Nickname = ? OR Email = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && BCrypt.checkpw(password, rs.getString("Clave_Acceso"))) {
                return rs.getString("Nickname");
            }
            return null;
        }
    }

    public boolean registrarUsuario(String nombre, String apellidos, String nickname, 
                                  String email, String password) throws SQLException {
        if (existeUsuario(nickname, email)) {
            return false;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO Cuenta (Nombre, Apellidos, Nickname, Email, Clave_Acceso) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nombre);
            stmt.setString(2, apellidos);
            stmt.setString(3, nickname);
            stmt.setString(4, email);
            stmt.setString(5, hashedPassword);
            
            return stmt.executeUpdate() > 0;
        }
    }

    private boolean existeUsuario(String nickname, String email) throws SQLException {
        String sql = "SELECT 1 FROM Cuenta WHERE Nickname = ? OR Email = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nickname);
            stmt.setString(2, email);
            return stmt.executeQuery().next();
        }
    }
    public void crearUsuarioPrueba() {
        String nombre = "Usuario";
        String apellidos = "De Prueba";
        String nickname = "testuser";
        String email = "testuser@example.com";
        String password = "TestPassword123"; // Asegúrate de cambiarlo después

        try {
            if (registrarUsuario(nombre, apellidos, nickname, email, password)) {
                System.out.println("Usuario de prueba creado exitosamente.");
            } else {
                System.out.println("No se pudo crear el usuario de prueba. Puede que ya exista.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
}