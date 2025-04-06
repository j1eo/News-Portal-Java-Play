package services;

import dataBase.DB_Connection;
import models.Comentario;
import models.Usuario;
import models.Cuenta;
import play.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class ComentarioService {

    private final DB_Connection dbConnection;
    private final AuthService authService;

    @Inject
    public ComentarioService(DB_Connection dbConnection, AuthService authService) {
        this.dbConnection = dbConnection;
        this.authService = authService;
    }

    /**
     * Obtiene todos los comentarios PUBLICADOS de un artículo con información del usuario
     */
    public List<Comentario> obtenerComentariosDeArticulo(int idArticulo) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Comentario> comentarios = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT ca.ID_Comentario, ca.ID_Articulo, ca.ID_Usuario, " +
                       "ca.Contenido, ca.Fecha_Creacion, ca.Estado, " +
                       "u.ID_Usuario as user_id, u.ID_Cuenta, c.Nombre as user_nombre, " +
                       "c.Foto_Perfil as user_foto " +
                       "FROM Comentario_Articulo ca " +
                       "JOIN Usuario u ON ca.ID_Usuario = u.ID_Usuario " +
                       "JOIN Cuenta c ON u.ID_Cuenta = c.ID_Cuenta " +
                       "WHERE ca.ID_Articulo = ? AND ca.Estado = 'PUBLICADO' " +
                       "ORDER BY ca.Fecha_Creacion DESC";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idArticulo);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Comentario comentario = new Comentario();
                comentario.setIdComentario(rs.getInt("ID_Comentario"));
                comentario.setIdUsuario(rs.getInt("ID_Usuario"));
                comentario.setIdContenido(rs.getInt("ID_Articulo"));
                comentario.setTipoContenido("ARTICULO");
                comentario.setContenido(rs.getString("Contenido"));
                comentario.setFechaCreacion(rs.getTimestamp("Fecha_Creacion"));
                comentario.setEstado("PUBLICADO"); // Forzamos el estado válido
                
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("user_id"));
                usuario.setIdCuenta(rs.getInt("ID_Cuenta"));
                usuario.setNombre(rs.getString("user_nombre"));
                usuario.setFotoPerfil(rs.getString("user_foto"));
                
                comentario.setUsuario(usuario);
                comentarios.add(comentario);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        return comentarios;
    }

    /**
     * Agrega un nuevo comentario a un artículo con estado PUBLICADO
     */
    public boolean agregarComentarioArticulo(int idArticulo, int idCuenta, String contenido) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement getUserStmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            
            // 1. Obtener el ID_Usuario a partir del ID_Cuenta
            String getUserSql = "SELECT ID_Usuario FROM Usuario WHERE ID_Cuenta = ?";
            getUserStmt = conn.prepareStatement(getUserSql);
            getUserStmt.setInt(1, idCuenta);
            rs = getUserStmt.executeQuery();
            
            if (!rs.next()) {
                throw new SQLException("No existe un usuario vinculado a esta cuenta");
            }
            int idUsuario = rs.getInt("ID_Usuario");
            
            // 2. Insertar el comentario con el ID_Usuario correcto
            String sql = "INSERT INTO Comentario_Articulo " +
                       "(ID_Articulo, ID_Usuario, Contenido, Fecha_Creacion, Estado) " +
                       "VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'PUBLICADO')";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idArticulo);
            stmt.setInt(2, idUsuario);  // Usamos el ID_Usuario obtenido
            stmt.setString(3, contenido);

            return stmt.executeUpdate() > 0;
        } finally {
            // Cerrar todos los recursos
            if (rs != null) rs.close();
            if (getUserStmt != null) getUserStmt.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Marca un comentario como ELIMINADO
     */
    public boolean eliminarComentario(int idComentario) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            String sql = "UPDATE Comentario_Articulo SET Estado = 'ELIMINADO' WHERE ID_Comentario = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idComentario);
            
            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Verifica si un usuario puede eliminar un comentario
     */
    public boolean puedeEliminarComentario(int idComentario, int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            
            // 1. Verificar si el usuario es administrador
            if (authService.obtenerRolUsuario(idUsuario).equals("admin")) {
                return true;
            }

            // 2. Verificar si es el autor del comentario
            String sql = "SELECT 1 FROM Comentario_Articulo " +
                        "WHERE ID_Comentario = ? AND ID_Usuario = ? AND Estado = 'PUBLICADO'";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idComentario);
            stmt.setInt(2, idUsuario);
            
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Obtiene un comentario por su ID
     */
    public Comentario obtenerComentarioPorId(int idComentario) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT * FROM Comentario_Articulo WHERE ID_Comentario = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idComentario);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Comentario comentario = new Comentario();
                comentario.setIdComentario(rs.getInt("ID_Comentario"));
                comentario.setIdUsuario(rs.getInt("ID_Usuario"));
                comentario.setIdContenido(rs.getInt("ID_Articulo"));
                comentario.setTipoContenido("ARTICULO");
                comentario.setContenido(rs.getString("Contenido"));
                comentario.setFechaCreacion(rs.getTimestamp("Fecha_Creacion"));
                comentario.setEstado(rs.getString("Estado"));
                return comentario;
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
 // En ComentarioService.java

    /**
     * Obtiene todos los comentarios PUBLICADOS de una noticia con información del usuario
     */
    public List<Comentario> obtenerComentariosDeNoticia(int idNoticia) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Comentario> comentarios = new ArrayList<>();

        try {
            conn = dbConnection.getConnection();
            String sql = "SELECT cn.ID_Comentario, cn.ID_Noticia, cn.ID_Usuario, " +
                       "cn.Contenido, cn.Fecha_Creacion, cn.Estado, " +
                       "u.ID_Usuario as user_id, u.ID_Cuenta, c.Nombre as user_nombre, " +
                       "c.Foto_Perfil as user_foto " +
                       "FROM Comentario_Noticia cn " +
                       "JOIN Usuario u ON cn.ID_Usuario = u.ID_Usuario " +
                       "JOIN Cuenta c ON u.ID_Cuenta = c.ID_Cuenta " +
                       "WHERE cn.ID_Noticia = ? AND cn.Estado = 'PUBLICADO' " +
                       "ORDER BY cn.Fecha_Creacion DESC";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idNoticia);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Comentario comentario = new Comentario();
                comentario.setIdComentario(rs.getInt("ID_Comentario"));
                comentario.setIdUsuario(rs.getInt("ID_Usuario"));
                comentario.setIdContenido(rs.getInt("ID_Noticia"));
                comentario.setTipoContenido("NOTICIA");
                comentario.setContenido(rs.getString("Contenido"));
                comentario.setFechaCreacion(rs.getTimestamp("Fecha_Creacion"));
                comentario.setEstado("PUBLICADO");
                
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("user_id"));
                usuario.setIdCuenta(rs.getInt("ID_Cuenta"));
                usuario.setNombre(rs.getString("user_nombre"));
                usuario.setFotoPerfil(rs.getString("user_foto"));
                
                comentario.setUsuario(usuario);
                comentarios.add(comentario);
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        return comentarios;
    }

    /**
     * Agrega un nuevo comentario a una noticia con estado PUBLICADO
     */
    public boolean agregarComentarioNoticia(int idNoticia, int idCuenta, String contenido) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement getUserStmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            
            // 1. Obtener el ID_Usuario a partir del ID_Cuenta
            String getUserSql = "SELECT ID_Usuario FROM Usuario WHERE ID_Cuenta = ?";
            getUserStmt = conn.prepareStatement(getUserSql);
            getUserStmt.setInt(1, idCuenta);
            rs = getUserStmt.executeQuery();
            
            if (!rs.next()) {
                throw new SQLException("No existe un usuario vinculado a esta cuenta");
            }
            int idUsuario = rs.getInt("ID_Usuario");
            
            // 2. Insertar el comentario con el ID_Usuario correcto
            String sql = "INSERT INTO Comentario_Noticia " +
                       "(ID_Noticia, ID_Usuario, Contenido, Fecha_Creacion, Estado) " +
                       "VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'PUBLICADO')";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idNoticia);
            stmt.setInt(2, idUsuario);
            stmt.setString(3, contenido);

            return stmt.executeUpdate() > 0;
        } finally {
            if (rs != null) rs.close();
            if (getUserStmt != null) getUserStmt.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Marca un comentario de noticia como ELIMINADO
     */
    public boolean eliminarComentarioNoticia(int idComentario) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            String sql = "UPDATE Comentario_Noticia SET Estado = 'ELIMINADO' WHERE ID_Comentario = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idComentario);
            
            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Verifica si un usuario puede eliminar un comentario de noticia
     */
    public boolean puedeEliminarComentarioNoticia(int idComentario, int idUsuario) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            
            // 1. Verificar si el usuario es administrador
            if (authService.obtenerRolUsuario(idUsuario).equals("admin")) {
                return true;
            }

            // 2. Verificar si es el autor del comentario
            String sql = "SELECT 1 FROM Comentario_Noticia " +
                        "WHERE ID_Comentario = ? AND ID_Usuario = ? AND Estado = 'PUBLICADO'";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idComentario);
            stmt.setInt(2, idUsuario);
            
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
}