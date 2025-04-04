package services;

import models.*;
import dataBase.DB_Connection;
import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComentarioService {
    private final DB_Connection dbConnection;

    @Inject
    public ComentarioService(DB_Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public boolean agregarComentarioArticulo(int idUsuario, int idArticulo, String contenido) throws SQLException {
        return agregarComentario(idUsuario, idArticulo, contenido, "ARTICULO");
    }

    public boolean agregarComentarioNoticia(int idUsuario, int idNoticia, String contenido) throws SQLException {
        return agregarComentario(idUsuario, idNoticia, contenido, "NOTICIA");
    }

    private boolean agregarComentario(int idUsuario, int idContenido, String contenido, String tipo) throws SQLException {
        String tabla = tipo.equals("ARTICULO") ? "Comentario_Articulo" : "Comentario_Noticia";
        String columnaId = tipo.equals("ARTICULO") ? "ID_Articulo" : "ID_Noticia";
        
        String sql = String.format(
            "INSERT INTO %s (ID_Usuario, %s, Contenido, Fecha_Creacion, Estado) " +
            "VALUES (?, ?, ?, ?, ?)",
            tabla, columnaId
        );
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idContenido);
            stmt.setString(3, contenido);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.setString(5, "ACTIVO");
            
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Comentario> obtenerComentariosArticulo(int idArticulo) throws SQLException {
        return obtenerComentarios(idArticulo, "ARTICULO");
    }

    public List<Comentario> obtenerComentariosNoticia(int idNoticia) throws SQLException {
        return obtenerComentarios(idNoticia, "NOTICIA");
    }

    private List<Comentario> obtenerComentarios(int idContenido, String tipo) throws SQLException {
        List<Comentario> comentarios = new ArrayList<>();
        String tabla = tipo.equals("ARTICULO") ? "Comentario_Articulo" : "Comentario_Noticia";
        String columnaId = tipo.equals("ARTICULO") ? "ID_Articulo" : "ID_Noticia";
        
        String sql = String.format(
            "SELECT c.*, u.Nombre FROM %s c " +
            "JOIN Usuario u ON c.ID_Usuario = u.ID_Usuario " +
            "WHERE c.%s = ? AND c.Estado = 'ACTIVO' " +
            "ORDER BY c.Fecha_Creacion DESC",
            tabla, columnaId
        );
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idContenido);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Comentario comentario = new Comentario();
                comentario.setIdComentario(rs.getInt("ID_Comentario"));
                comentario.setIdUsuario(rs.getInt("ID_Usuario"));
                comentario.setContenido(rs.getString("Contenido"));
                comentario.setFechaCreacion(rs.getTimestamp("Fecha_Creacion"));
                comentario.setEstado(rs.getString("Estado"));
                
                Usuario usuario = new Usuario();
                usuario.setNombre(rs.getString("Nombre"));
                comentario.setUsuario(usuario);
                
                comentarios.add(comentario);
            }
        }
        return comentarios;
    }
}