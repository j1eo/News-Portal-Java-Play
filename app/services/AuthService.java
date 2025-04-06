package services;

import dataBase.DB_Connection;
import models.*;
import org.mindrot.jbcrypt.BCrypt;
import javax.inject.Inject;
import java.sql.*;
import java.util.Date;

public class AuthService {
    private final DB_Connection dbConnection;

    @Inject
    public AuthService(DB_Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * Autentica un usuario usando nickname/email y contraseña
     */
    public Cuenta autenticarUsuario(String username, String password) throws SQLException {
        String sql = "SELECT c.*, a.ID_Admin, u.ID_Usuario, u.Suscripcion " +
                     "FROM Cuenta c " +
                     "LEFT JOIN Administrador a ON c.ID_Cuenta = a.ID_Cuenta " +
                     "LEFT JOIN Usuario u ON c.ID_Cuenta = u.ID_Cuenta " +
                     "WHERE c.Nickname = ? OR c.Email = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && BCrypt.checkpw(password, rs.getString("Clave_Acceso"))) {
                return mapearCuentaDesdeResultSet(rs);
            }
            return null;
        }
    }

    /**
     * Registra una nueva cuenta completa (en tabla Cuenta y su tabla específica)
     */
    public Cuenta registrarCuentaCompleta(String nombre, String apellidos, String nickname, 
                                        String email, String password, boolean esAdmin, 
                                        String suscripcion) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // Validar que no exista el usuario
            if (existeUsuario(conn, nickname, email)) {
                throw new SQLException("El usuario o email ya están registrados");
            }

            // 1. Registrar en tabla Cuenta
            int idCuenta = insertarCuenta(conn, nombre, apellidos, nickname, email, password);
            
            // 2. Registrar en tabla específica
            Cuenta cuenta;
            if (esAdmin) {
                int idAdmin = insertarAdmin(conn, idCuenta);
                Admin admin = new Admin();
                admin.setIdAdmin(idAdmin);
                cuenta = admin;
            } else {
                int idUsuario = insertarUsuario(conn, idCuenta, suscripcion != null ? suscripcion : "BASICA");
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(idUsuario);
                usuario.setSuscripcion(suscripcion);
                cuenta = usuario;
            }
            
            // Setear datos comunes
            setDatosCuenta(cuenta, idCuenta, nombre, apellidos, nickname, email, password);
            
            conn.commit(); // Confirmar transacción
            return cuenta;
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    /**
     * Obtiene el rol de un usuario (admin/user)
     */
    public String obtenerRolUsuario(int idCuenta) throws SQLException {
        String sql = "SELECT 1 FROM Administrador WHERE ID_Cuenta = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCuenta);
            return stmt.executeQuery().next() ? "admin" : "user";
        }
    }

    /**
     * Verifica si un usuario existe
     */
    public boolean existeUsuario(String nickname, String email) throws SQLException {
        try (Connection conn = dbConnection.getConnection()) {
            return existeUsuario(conn, nickname, email);
        }
    }

    /**
     * Crea usuarios de prueba (admin, normal y premium)
     */
    public void crearUsuariosDePrueba() throws SQLException {
        // Admin
        if (!existeUsuario("admin", "admin@example.com")) {
            registrarCuentaCompleta(
                "Admin", "Sistema", "admin", 
                "admin@example.com", "Admin123", true, null
            );
        }
        
        // Usuario normal
        if (!existeUsuario("usuario", "usuario@example.com")) {
            registrarCuentaCompleta(
                "Usuario", "Normal", "usuario", 
                "usuario@example.com", "Usuario123", false, "BASICA"
            );
        }
        
        // Usuario premium
        if (!existeUsuario("premium", "premium@example.com")) {
            registrarCuentaCompleta(
                "Premium", "Usuario", "premium", 
                "premium@example.com", "Premium123", false, "PREMIUM"
            );
        }
    }

    /**
     * Registra un intento de login en la tabla Login
     */
    public void registrarIntentoLogin(int idCuenta, String ip) throws SQLException {
        String sql = "INSERT INTO Login (ID_Cuenta, Fecha_Hora, IP) VALUES (?, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCuenta);
            stmt.setString(2, ip);
            stmt.executeUpdate();
        }
    }

    // --- Métodos privados auxiliares ---

    private Cuenta mapearCuentaDesdeResultSet(ResultSet rs) throws SQLException {
        Cuenta cuenta;
        
        if (rs.getObject("ID_Admin") != null) {
            Admin admin = new Admin();
            admin.setIdAdmin(rs.getInt("ID_Admin"));
            cuenta = admin;
        } else {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(rs.getInt("ID_Usuario"));
            usuario.setSuscripcion(rs.getString("Suscripcion"));
            cuenta = usuario;
        }
        
        // Campos comunes
        setDatosCuentaDesdeResultSet(cuenta, rs);
        
        return cuenta;
    }

    private void setDatosCuenta(Cuenta cuenta, int idCuenta, String nombre, String apellidos, 
                              String nickname, String email, String password) {
        cuenta.setIdCuenta(idCuenta);
        cuenta.setNombre(nombre);
        cuenta.setApellidos(apellidos);
        cuenta.setNickname(nickname);
        cuenta.setEmail(email);
        cuenta.setClaveAcceso(BCrypt.hashpw(password, BCrypt.gensalt()));
        cuenta.setFechaRegistro(new Date());
    }

    private void setDatosCuentaDesdeResultSet(Cuenta cuenta, ResultSet rs) throws SQLException {
        cuenta.setIdCuenta(rs.getInt("ID_Cuenta"));
        cuenta.setNombre(rs.getString("Nombre"));
        cuenta.setApellidos(rs.getString("Apellidos"));
        cuenta.setNickname(rs.getString("Nickname"));
        cuenta.setEmail(rs.getString("Email"));
        cuenta.setClaveAcceso(rs.getString("Clave_Acceso"));
        cuenta.setFechaRegistro(rs.getDate("Fecha_Registro"));
        cuenta.setFotoPerfil(rs.getString("Foto_Perfil"));
    }

    private boolean existeUsuario(Connection conn, String nickname, String email) throws SQLException {
        String sql = "SELECT 1 FROM Cuenta WHERE Nickname = ? OR Email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nickname);
            stmt.setString(2, email);
            return stmt.executeQuery().next();
        }
    }

    private int insertarCuenta(Connection conn, String nombre, String apellidos, 
                             String nickname, String email, String password) throws SQLException {
        String sql = "INSERT INTO Cuenta (Nombre, Apellidos, Nickname, Email, Clave_Acceso, Fecha_Registro) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellidos);
            stmt.setString(3, nickname);
            stmt.setString(4, email);
            stmt.setString(5, BCrypt.hashpw(password, BCrypt.gensalt()));
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("No se pudo obtener el ID de la cuenta");
            }
        }
    }

    private int insertarAdmin(Connection conn, int idCuenta) throws SQLException {
        String sql = "INSERT INTO Administrador (ID_Cuenta) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, idCuenta);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("No se pudo obtener el ID de administrador");
            }
        }
    }

    private int insertarUsuario(Connection conn, int idCuenta, String suscripcion) throws SQLException {
        String sql = "INSERT INTO Usuario (ID_Cuenta, Suscripcion) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, idCuenta);
            stmt.setString(2, suscripcion);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                throw new SQLException("No se pudo obtener el ID de usuario");
            }
        }
    }
 // Añade estos métodos a tu AuthService existente
    public Cuenta getCuentaById(int idCuenta) throws SQLException {
        String sql = "SELECT c.*, a.ID_Admin, u.ID_Usuario, u.Suscripcion " +
                   "FROM Cuenta c " +
                   "LEFT JOIN Administrador a ON c.ID_Cuenta = a.ID_Cuenta " +
                   "LEFT JOIN Usuario u ON c.ID_Cuenta = u.ID_Cuenta " +
                   "WHERE c.ID_Cuenta = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCuenta);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapearCuentaDesdeResultSet(rs) : null;
        }
    }

    public int contarUsuarios() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Usuario";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int contarAdmins() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Administrador";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    /**
     * Actualiza los datos básicos de una cuenta (nombre, apellidos, email, foto).
     * No actualiza campos sensibles como nickname o contraseña.
     */
    public boolean actualizarCuenta(Cuenta cuenta) throws SQLException {
        String sql = "UPDATE Cuenta SET Nombre = ?, Apellidos = ?, Email = ?, Foto_Perfil = ? WHERE ID_Cuenta = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cuenta.getNombre());
            stmt.setString(2, cuenta.getApellidos());
            stmt.setString(3, cuenta.getEmail());
            stmt.setString(4, cuenta.getFotoPerfil());
            stmt.setInt(5, cuenta.getIdCuenta());
            
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza la contraseña de una cuenta (requiere contraseña actual para validación).
     */
    public boolean actualizarContraseña(int idCuenta, String nuevaContraseña, String contraseñaActual) throws SQLException {
        String sql = "SELECT Clave_Acceso FROM Cuenta WHERE ID_Cuenta = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idCuenta);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && BCrypt.checkpw(contraseñaActual, rs.getString("Clave_Acceso"))) {
                String updateSql = "UPDATE Cuenta SET Clave_Acceso = ? WHERE ID_Cuenta = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, BCrypt.hashpw(nuevaContraseña, BCrypt.gensalt()));
                updateStmt.setInt(2, idCuenta);
                return updateStmt.executeUpdate() > 0;
            }
            return false;
        }
    }
    
    public boolean actualizarSuscripcion(Usuario usuario) throws SQLException {
        // Usamos transacción para asegurar la integridad de los datos
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);  // Iniciar transacción

            // 1. Actualizar la suscripción en la tabla Usuario
            String sqlUsuario = "UPDATE Usuario SET Suscripcion = ? WHERE ID_Usuario = ?";
            try (PreparedStatement stmtUsuario = conn.prepareStatement(sqlUsuario)) {
                stmtUsuario.setString(1, usuario.getSuscripcion());
                stmtUsuario.setInt(2, usuario.getIdUsuario());
                
                int filasAfectadas = stmtUsuario.executeUpdate();
                
                if (filasAfectadas == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Podrías añadir aquí lógica adicional si necesitas actualizar otros datos relacionados
            
            conn.commit();  // Confirmar la transacción
            return true;
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    /**
     * Verifica si el email ya está registrado por otro usuario.
     */
    public boolean existeEmail(String email, int excludeIdCuenta) throws SQLException {
        String sql = "SELECT 1 FROM Cuenta WHERE Email = ? AND ID_Cuenta != ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setInt(2, excludeIdCuenta);
            return stmt.executeQuery().next();
        }
    }
}