package services;

import dataBase.DB_Connection;
import models.*;
import javax.inject.Inject;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class AdminService {
    private final DB_Connection dbConnection;

    @Inject
    public AdminService(DB_Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public int contarUsuarios() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Usuario")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int contarArticulos() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Articulo")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int contarNoticias() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Noticia")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<Usuario> obtenerTodosUsuarios() throws SQLException {
        String sql = "SELECT u.*, c.* FROM Usuario u JOIN Cuenta c ON u.ID_Cuenta = c.ID_Cuenta";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<Usuario> usuarios = new ArrayList<>();
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
            return usuarios;
        }
    }

    public Usuario obtenerUsuarioPorId(Long id) throws SQLException {
        String sql = "SELECT u.*, c.* FROM Usuario u JOIN Cuenta c ON u.ID_Cuenta = c.ID_Cuenta WHERE u.ID_Usuario = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapearUsuario(rs) : null;
        }
    }

    public boolean actualizarUsuario(Usuario usuario) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Actualizar cuenta
            String sqlCuenta = "UPDATE Cuenta SET Nombre = ?, Apellidos = ?, Email = ? WHERE ID_Cuenta = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCuenta)) {
                stmt.setString(1, usuario.getNombre());
                stmt.setString(2, usuario.getApellidos());
                stmt.setString(3, usuario.getEmail());
                stmt.setInt(4, usuario.getIdCuenta());
                stmt.executeUpdate();
            }
            
            // Actualizar usuario
            String sqlUsuario = "UPDATE Usuario SET Suscripcion = ? WHERE ID_Usuario = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUsuario)) {
                stmt.setString(1, usuario.getSuscripcion());
                stmt.setInt(2, usuario.getIdUsuario());
                stmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    public boolean eliminarUsuario(Long id) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            
            int idCuenta;
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT ID_Cuenta FROM Usuario WHERE ID_Usuario = ?")) {
                stmt.setLong(1, id);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) return false;
                idCuenta = rs.getInt("ID_Cuenta");
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM Usuario WHERE ID_Usuario = ?")) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM Cuenta WHERE ID_Cuenta = ?")) {
                stmt.setInt(1, idCuenta);
                stmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    public boolean cambiarEstadoArticulo(Long id, String estado) throws SQLException {
        String sql = "UPDATE Articulo SET Estado = ? WHERE ID_Articulo = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("ID_Usuario"));
        usuario.setIdCuenta(rs.getInt("ID_Cuenta"));
        usuario.setNombre(rs.getString("Nombre"));
        usuario.setApellidos(rs.getString("Apellidos"));
        usuario.setEmail(rs.getString("Email"));
        usuario.setSuscripcion(rs.getString("Suscripcion"));
        return usuario;
    }
}