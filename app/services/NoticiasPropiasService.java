package services;

import models.NoticiaPropia;
import play.Logger;
import dataBase.DB_Connection;

import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoticiasPropiasService {
    private final DB_Connection dbConnection;

    @Inject
    public NoticiasPropiasService(DB_Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public List<NoticiaPropia> obtenerNoticias() throws SQLException {
        List<NoticiaPropia> noticias = new ArrayList<>();
        String query = "SELECT n.ID_Noticia, n.ID_Usuario, n.Titulo, n.Autor, n.URL, n.Fuente, " +
                      "n.Descripcion, n.Imagen, n.Contenido, n.Fecha_Publicacion, n.Estado, " +
                      "c.Nombre AS Categoria, n.Me_Gusta, n.No_Me_Gusta " +
                      "FROM Noticia n " +
                      "JOIN Categoria c ON n.Categoria_ID = c.ID_Categoria " +
                      "WHERE n.Estado = 'PUBLICADO' " +
                      "ORDER BY n.Fecha_Publicacion DESC";

        try (Connection connection = dbConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                NoticiaPropia noticia = mapearNoticiaDesdeResultSet(resultSet);
                noticias.add(noticia);
            }
        }
        return noticias;
    }

    public NoticiaPropia obtenerNoticiaPorId(int id) throws SQLException {
        String query = "SELECT n.ID_Noticia, n.ID_Usuario, n.Titulo, n.Autor, n.URL, n.Fuente, " +
                      "n.Descripcion, n.Imagen, n.Contenido, n.Fecha_Publicacion, n.Estado, " +
                      "c.Nombre AS Categoria, n.Me_Gusta, n.No_Me_Gusta " +
                      "FROM Noticia n " +
                      "JOIN Categoria c ON n.Categoria_ID = c.ID_Categoria " +
                      "WHERE n.ID_Noticia = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearNoticiaDesdeResultSet(resultSet);
                }
            }
        }
        return null;
    }

    public List<NoticiaPropia> obtenerNoticiasRelacionadas(int idNoticia, String categoria, int limit) throws SQLException {
        List<NoticiaPropia> relacionados = new ArrayList<>();
        String query = "SELECT n.ID_Noticia, n.Titulo, n.Imagen " +
                      "FROM Noticia n " +
                      "JOIN Categoria c ON n.Categoria_ID = c.ID_Categoria " +
                      "WHERE c.Nombre = ? AND n.ID_Noticia != ? AND n.Estado = 'PUBLICADO' " +
                      "ORDER BY n.Fecha_Publicacion DESC LIMIT ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, categoria);
            statement.setInt(2, idNoticia);
            statement.setInt(3, limit);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    NoticiaPropia noticia = new NoticiaPropia();
                    noticia.setIdNoticia(resultSet.getInt("ID_Noticia"));
                    noticia.setTitulo(resultSet.getString("Titulo"));
                    noticia.setImagen(resultSet.getString("Imagen"));
                    relacionados.add(noticia);
                }
            }
        }
        return relacionados;
    }

    public boolean agregarNoticia(NoticiaPropia noticia) throws SQLException {
        String query = "INSERT INTO Noticia (ID_Usuario, Titulo, Autor, Contenido, Imagen, " +
                      "Fecha_Publicacion, Estado, Categoria_ID, Me_Gusta, No_Me_Gusta, " +
                      "URL, Fuente, Descripcion) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, " +
                      "(SELECT ID_Categoria FROM Categoria WHERE Nombre = ?), ?, ?, ?, ?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, noticia.getIdUsuario());
            statement.setString(2, noticia.getTitulo());
            statement.setString(3, noticia.getAutor());
            statement.setString(4, noticia.getContenido());
            statement.setString(5, noticia.getImagen());
            statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            statement.setString(7, noticia.getEstado());
            statement.setString(8, noticia.getCategoria());
            statement.setInt(9, noticia.getMeGusta());
            statement.setInt(10, noticia.getNoMeGusta());
            statement.setString(11, noticia.getUrl());
            statement.setString(12, noticia.getFuente());
            statement.setString(13, noticia.getDescripcion());

            return statement.executeUpdate() > 0;
        }
    }

    public List<NoticiaPropia> obtenerNoticiasPorUsuario(int idUsuario) throws SQLException {
        List<NoticiaPropia> noticias = new ArrayList<>();
        String query = "SELECT n.ID_Noticia, n.ID_Usuario, n.Titulo, n.Autor, n.URL, n.Fuente, " +
                      "n.Descripcion, n.Imagen, n.Contenido, n.Fecha_Publicacion, n.Estado, " +
                      "c.Nombre AS Categoria, n.Me_Gusta, n.No_Me_Gusta " +
                      "FROM Noticia n " +
                      "JOIN Categoria c ON n.Categoria_ID = c.ID_Categoria " +
                      "WHERE n.Estado = 'PUBLICADO' AND n.ID_Usuario = ? " +
                      "ORDER BY n.Fecha_Publicacion DESC";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setInt(1, idUsuario);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    NoticiaPropia noticia = mapearNoticiaDesdeResultSet(resultSet);
                    noticias.add(noticia);
                }
            }
        }
        return noticias;
    }

    public boolean eliminarNoticia(int idNoticia) throws SQLException {
        String query = "UPDATE Noticia SET Estado = 'ELIMINADO' WHERE ID_Noticia = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idNoticia);
            return statement.executeUpdate() > 0;
        }
    }

    private NoticiaPropia mapearNoticiaDesdeResultSet(ResultSet resultSet) throws SQLException {
        NoticiaPropia noticia = new NoticiaPropia();
        noticia.setIdNoticia(resultSet.getInt("ID_Noticia"));
        noticia.setIdUsuario(resultSet.getInt("ID_Usuario"));
        noticia.setTitulo(resultSet.getString("Titulo"));
        noticia.setAutor(resultSet.getString("Autor"));
        noticia.setUrl(resultSet.getString("URL"));
        noticia.setFuente(resultSet.getString("Fuente"));
        noticia.setDescripcion(resultSet.getString("Descripcion"));
        noticia.setImagen(resultSet.getString("Imagen"));
        noticia.setContenido(resultSet.getString("Contenido"));
        noticia.setFechaPublicacion(resultSet.getDate("Fecha_Publicacion"));
        noticia.setEstado(resultSet.getString("Estado"));
        noticia.setCategoria(resultSet.getString("Categoria"));
        noticia.setMeGusta(resultSet.getInt("Me_Gusta"));
        noticia.setNoMeGusta(resultSet.getInt("No_Me_Gusta"));
        return noticia;
    }
    public boolean actualizarNoticia(NoticiaPropia noticia) throws SQLException {
        String query = "UPDATE Noticia SET Titulo = ?, Autor = ?, URL = ?, Fuente = ?, " +
                      "Descripcion = ?, Imagen = ?, Contenido = ?, Estado = ?, " +
                      "Categoria_ID = (SELECT ID_Categoria FROM Categoria WHERE Nombre = ?) " +
                      "WHERE ID_Noticia = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, noticia.getTitulo());
            statement.setString(2, noticia.getAutor());
            statement.setString(3, noticia.getUrl());
            statement.setString(4, noticia.getFuente());
            statement.setString(5, noticia.getDescripcion());
            statement.setString(6, noticia.getImagen());
            statement.setString(7, noticia.getContenido());
            statement.setString(8, noticia.getEstado());
            statement.setString(9, noticia.getCategoria());
            statement.setInt(10, noticia.getIdNoticia());

            return statement.executeUpdate() > 0;
        }
    }
    
    public boolean darLikeNoticia(int idNoticia, int idUsuario) throws SQLException {
        // Verificar si el usuario es el autor de la noticia
        NoticiaPropia noticia = obtenerNoticiaPorId(idNoticia);
        if (noticia != null && noticia.getIdUsuario() == idUsuario) {
            Logger.debug("Usuario {} intentó dar like a su propia noticia {}", idUsuario, idNoticia);
            return false;
        }

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);

            // 1. Verificar si ya existe una reacción
            String checkQuery = "SELECT Tipo_Like FROM Usuario_Noticia_Like WHERE ID_Usuario = ? AND ID_Noticia = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, idUsuario);
                checkStmt.setInt(2, idNoticia);
                
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    String tipoLike = rs.getString("Tipo_Like");
                    if ("ME_GUSTA".equals(tipoLike)) {
                        return false; // Ya dio like antes
                    } else {
                        // Cambiar de dislike a like
                        return cambiarReaccionNoticia(connection, idNoticia, idUsuario, "NO_ME_GUSTA", "ME_GUSTA");
                    }
                }
            }

            // 2. Registrar nuevo like
            String insertQuery = "INSERT INTO Usuario_Noticia_Like (ID_Usuario, ID_Noticia, Tipo_Like) VALUES (?, ?, 'ME_GUSTA')";
            String updateQuery = "UPDATE Noticia SET Me_Gusta = Me_Gusta + 1 WHERE ID_Noticia = ?";
            
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                 PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                
                // Insertar like
                insertStmt.setInt(1, idUsuario);
                insertStmt.setInt(2, idNoticia);
                if (insertStmt.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
                
                // Actualizar contador
                updateStmt.setInt(1, idNoticia);
                if (updateStmt.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
                
                connection.commit();
                return true;
            }
        }
    }

    private boolean cambiarReaccionNoticia(Connection connection, int idNoticia, int idUsuario, 
                                         String viejoTipo, String nuevoTipo) throws SQLException {
        String updateLikeQuery = "UPDATE Usuario_Noticia_Like SET Tipo_Like = ? WHERE ID_Usuario = ? AND ID_Noticia = ?";
        String decrementQuery = "UPDATE Noticia SET " + 
                              (viejoTipo.equals("ME_GUSTA") ? "Me_Gusta" : "No_Me_Gusta") + 
                              " = " + (viejoTipo.equals("ME_GUSTA") ? "Me_Gusta" : "No_Me_Gusta") + " - 1 " +
                              "WHERE ID_Noticia = ?";
        String incrementQuery = "UPDATE Noticia SET " + 
                              (nuevoTipo.equals("ME_GUSTA") ? "Me_Gusta" : "No_Me_Gusta") + 
                              " = " + (nuevoTipo.equals("ME_GUSTA") ? "Me_Gusta" : "No_Me_Gusta") + " + 1 " +
                              "WHERE ID_Noticia = ?";
        
        try (PreparedStatement updateLikeStmt = connection.prepareStatement(updateLikeQuery);
             PreparedStatement decrementStmt = connection.prepareStatement(decrementQuery);
             PreparedStatement incrementStmt = connection.prepareStatement(incrementQuery)) {
            
            // Actualizar tipo de reacción
            updateLikeStmt.setString(1, nuevoTipo);
            updateLikeStmt.setInt(2, idUsuario);
            updateLikeStmt.setInt(3, idNoticia);
            updateLikeStmt.executeUpdate();
            
            // Decrementar el viejo contador
            decrementStmt.setInt(1, idNoticia);
            decrementStmt.executeUpdate();
            
            // Incrementar el nuevo contador
            incrementStmt.setInt(1, idNoticia);
            incrementStmt.executeUpdate();
            
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public NoticiaPropia obtenerNoticiaPorIdConLikes(int idNoticia, Integer idUsuario) throws SQLException {
        String query = "SELECT n.*, c.Nombre AS Categoria, " +
                     "(SELECT COUNT(*) FROM Usuario_Noticia_Like WHERE ID_Noticia = n.ID_Noticia AND Tipo_Like = 'ME_GUSTA') AS Me_Gusta, " +
                     "(SELECT COUNT(*) FROM Usuario_Noticia_Like WHERE ID_Noticia = n.ID_Noticia AND Tipo_Like = 'NO_ME_GUSTA') AS No_Me_Gusta";
        
        if (idUsuario != null) {
            query += ", EXISTS(SELECT 1 FROM Usuario_Noticia_Like WHERE ID_Noticia = n.ID_Noticia AND ID_Usuario = ? AND Tipo_Like = 'ME_GUSTA') AS UsuarioDioLike, " +
                    "EXISTS(SELECT 1 FROM Usuario_Noticia_Like WHERE ID_Noticia = n.ID_Noticia AND ID_Usuario = ? AND Tipo_Like = 'NO_ME_GUSTA') AS UsuarioDioNoMeGusta";
        } else {
            query += ", false AS UsuarioDioLike, false AS UsuarioDioNoMeGusta";
        }
        
        query += " FROM Noticia n " +
                "LEFT JOIN Categoria c ON n.Categoria_ID = c.ID_Categoria " +
                "WHERE n.ID_Noticia = ?";
        
        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            int paramIndex = 1;
            if (idUsuario != null) {
                statement.setInt(paramIndex++, idUsuario);
                statement.setInt(paramIndex++, idUsuario);
            }
            statement.setInt(paramIndex, idNoticia);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    NoticiaPropia noticia = new NoticiaPropia();
                    noticia.setIdNoticia(rs.getInt("ID_Noticia"));
                    noticia.setIdUsuario(rs.getInt("ID_Usuario"));
                    noticia.setTitulo(rs.getString("Titulo"));
                    noticia.setAutor(rs.getString("Autor"));
                    noticia.setUrl(rs.getString("URL"));
                    noticia.setFuente(rs.getString("Fuente"));
                    noticia.setDescripcion(rs.getString("Descripcion"));
                    noticia.setImagen(rs.getString("Imagen"));
                    noticia.setContenido(rs.getString("Contenido"));
                    noticia.setFechaPublicacion(rs.getDate("Fecha_Publicacion"));
                    noticia.setEstado(rs.getString("Estado"));
                    noticia.setCategoria(rs.getString("Categoria"));
                    noticia.setMeGusta(rs.getInt("Me_Gusta"));
                    noticia.setNoMeGusta(rs.getInt("No_Me_Gusta"));
                    
                    if (idUsuario != null) {
                        noticia.setUsuarioDioLike(rs.getBoolean("UsuarioDioLike"));
                        noticia.setUsuarioDioNoMeGusta(rs.getBoolean("UsuarioDioNoMeGusta"));
                    }
                    
                    return noticia;
                }
            }
        }
        return null;
    }
    public boolean darNoMeGustaNoticia(int idNoticia, int idUsuario) throws SQLException {
        // Verificar si el usuario es el autor de la noticia
        NoticiaPropia noticia = obtenerNoticiaPorId(idNoticia);
        if (noticia != null && noticia.getIdUsuario() == idUsuario) {
            Logger.debug("Usuario {} intentó dar dislike a su propia noticia {}", idUsuario, idNoticia);
            return false;
        }

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);

            // 1. Verificar si ya existe una reacción
            String checkQuery = "SELECT Tipo_Like FROM Usuario_Noticia_Like WHERE ID_Usuario = ? AND ID_Noticia = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, idUsuario);
                checkStmt.setInt(2, idNoticia);
                
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    String tipoLike = rs.getString("Tipo_Like");
                    if ("NO_ME_GUSTA".equals(tipoLike)) {
                        return false; // Ya dio dislike antes
                    } else {
                        // Cambiar de like a dislike
                        return cambiarReaccionNoticia(connection, idNoticia, idUsuario, "ME_GUSTA", "NO_ME_GUSTA");
                    }
                }
            }

            // 2. Registrar nuevo dislike
            String insertQuery = "INSERT INTO Usuario_Noticia_Like (ID_Usuario, ID_Noticia, Tipo_Like) VALUES (?, ?, 'NO_ME_GUSTA')";
            String updateQuery = "UPDATE Noticia SET No_Me_Gusta = No_Me_Gusta + 1 WHERE ID_Noticia = ?";
            
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                 PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                
                // Insertar dislike
                insertStmt.setInt(1, idUsuario);
                insertStmt.setInt(2, idNoticia);
                if (insertStmt.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
                
                // Actualizar contador
                updateStmt.setInt(1, idNoticia);
                if (updateStmt.executeUpdate() == 0) {
                    connection.rollback();
                    return false;
                }
                
                connection.commit();
                return true;
            }
        }
    }
}