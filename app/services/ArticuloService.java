package services;

import models.Articulo;
import play.Logger;
import dataBase.DB_Connection;

import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticuloService {
    private final DB_Connection dbConnection;

    @Inject
    public ArticuloService(DB_Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public List<Articulo> obtenerArticulos() throws SQLException {
        List<Articulo> articulos = new ArrayList<>();
        String query = "SELECT a.ID_Articulo, a.ID_Usuario, a.Titulo, a.Autor, " +
                      "a.Contenido, a.Imagen, a.Fecha_Publicacion, a.Estado, " +
                      "c.Nombre AS Categoria, a.Me_Gusta, a.No_Me_Gusta " +
                      "FROM Articulo a " +
                      "JOIN Categoria c ON a.Categoria_ID = c.ID_Categoria " +
                      "WHERE a.Estado = 'PUBLICADO' " +
                      "ORDER BY a.Fecha_Publicacion DESC";

        try (Connection connection = dbConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Articulo articulo = new Articulo();
                articulo.setIdArticulo(resultSet.getInt("ID_Articulo"));
                articulo.setIdUsuario(resultSet.getInt("ID_Usuario"));
                articulo.setTitulo(resultSet.getString("Titulo"));
                articulo.setAutor(resultSet.getString("Autor"));
                articulo.setContenido(resultSet.getString("Contenido"));
                articulo.setImagen(resultSet.getString("Imagen"));
                articulo.setFechaPublicacion(resultSet.getDate("Fecha_Publicacion"));
                articulo.setEstado(resultSet.getString("Estado"));
                articulo.setCategoria(resultSet.getString("Categoria"));
                articulo.setMeGusta(resultSet.getInt("Me_Gusta"));
                articulo.setNoMeGusta(resultSet.getInt("No_Me_Gusta"));
                
                articulos.add(articulo);
            }
        }
        return articulos;
    }

    public Articulo obtenerArticuloPorId(int id) throws SQLException {
        String query = "SELECT a.ID_Articulo, a.ID_Usuario, a.Titulo, a.Autor, " +
                      "a.Contenido, a.Imagen, a.Fecha_Publicacion, a.Estado, " +
                      "c.Nombre AS Categoria, a.Me_Gusta, a.No_Me_Gusta " +
                      "FROM Articulo a " +
                      "JOIN Categoria c ON a.Categoria_ID = c.ID_Categoria " +
                      "WHERE a.ID_Articulo = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Articulo articulo = new Articulo();
                    articulo.setIdArticulo(resultSet.getInt("ID_Articulo"));
                    articulo.setIdUsuario(resultSet.getInt("ID_Usuario"));
                    articulo.setTitulo(resultSet.getString("Titulo"));
                    articulo.setAutor(resultSet.getString("Autor"));
                    articulo.setContenido(resultSet.getString("Contenido"));
                    articulo.setImagen(resultSet.getString("Imagen"));
                    articulo.setFechaPublicacion(resultSet.getDate("Fecha_Publicacion"));
                    articulo.setEstado(resultSet.getString("Estado"));
                    articulo.setCategoria(resultSet.getString("Categoria"));
                    articulo.setMeGusta(resultSet.getInt("Me_Gusta"));
                    articulo.setNoMeGusta(resultSet.getInt("No_Me_Gusta"));
                    return articulo;
                }
            }
        }
        return null;
    }

    public List<Articulo> obtenerArticulosRelacionados(int idArticulo, String categoria, int limit) throws SQLException {
        List<Articulo> relacionados = new ArrayList<>();
        String query = "SELECT a.ID_Articulo, a.Titulo, a.Imagen " +
                      "FROM Articulo a " +
                      "JOIN Categoria c ON a.Categoria_ID = c.ID_Categoria " +
                      "WHERE c.Nombre = ? AND a.ID_Articulo != ? AND a.Estado = 'PUBLICADO' " +
                      "ORDER BY a.Fecha_Publicacion DESC LIMIT ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, categoria);
            statement.setInt(2, idArticulo);
            statement.setInt(3, limit);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Articulo articulo = new Articulo();
                    articulo.setIdArticulo(resultSet.getInt("ID_Articulo"));
                    articulo.setTitulo(resultSet.getString("Titulo"));
                    articulo.setImagen(resultSet.getString("Imagen"));
                    relacionados.add(articulo);
                }
            }
        }
        return relacionados;
    }

    public boolean agregarArticulo(int idUsuario, String titulo, String autor, String contenido, 
                                  String imagen, String estado, String categoria, 
                                  int meGusta, int noMeGusta) throws SQLException {
        String query = "INSERT INTO Articulo (ID_Usuario, Titulo, Autor, Contenido, Imagen, " +
                      "Fecha_Publicacion, Estado, Categoria_ID, Me_Gusta, No_Me_Gusta) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, " +
                      "(SELECT ID_Categoria FROM Categoria WHERE Nombre = ?), ?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idUsuario);
            statement.setString(2, titulo);
            statement.setString(3, autor);
            statement.setString(4, contenido);
            statement.setString(5, imagen);
            statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            statement.setString(7, estado);
            statement.setString(8, categoria);
            statement.setInt(9, meGusta);
            statement.setInt(10, noMeGusta);

            return statement.executeUpdate() > 0;
        }
    }
 // En ArticuloService.java
    public List<Articulo> obtenerArticulosPorUsuario(int idUsuario) throws SQLException {
        List<Articulo> articulos = new ArrayList<>();
        String query = "SELECT a.ID_Articulo, a.ID_Usuario, a.Titulo, a.Autor, " +
                      "a.Contenido, a.Imagen, a.Fecha_Publicacion, a.Estado, " +
                      "c.Nombre AS Categoria, a.Me_Gusta, a.No_Me_Gusta " +
                      "FROM Articulo a " +
                      "JOIN Categoria c ON a.Categoria_ID = c.ID_Categoria " +
                      "WHERE a.Estado = 'PUBLICADO' AND a.ID_Usuario = ? " +
                      "ORDER BY a.Fecha_Publicacion DESC";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            statement.setInt(1, idUsuario);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Articulo articulo = new Articulo();
                    articulo.setIdArticulo(resultSet.getInt("ID_Articulo"));
                    articulo.setIdUsuario(resultSet.getInt("ID_Usuario"));
                    articulo.setTitulo(resultSet.getString("Titulo"));
                    articulo.setAutor(resultSet.getString("Autor"));
                    articulo.setContenido(resultSet.getString("Contenido"));
                    articulo.setImagen(resultSet.getString("Imagen"));
                    articulo.setFechaPublicacion(resultSet.getDate("Fecha_Publicacion"));
                    articulo.setEstado(resultSet.getString("Estado"));
                    articulo.setCategoria(resultSet.getString("Categoria"));
                    articulo.setMeGusta(resultSet.getInt("Me_Gusta"));
                    articulo.setNoMeGusta(resultSet.getInt("No_Me_Gusta"));
                    articulos.add(articulo);
                }
            }
        }
        return articulos;
    }

    public boolean eliminarArticulo(int idArticulo) throws SQLException {
        String query = "UPDATE Articulo SET Estado = 'ELIMINADO' WHERE ID_Articulo = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idArticulo);
            return statement.executeUpdate() > 0;
        }
    }
    public boolean actualizarArticulo(Articulo articulo) throws SQLException {
        String query = "UPDATE Articulo SET Titulo = ?, Autor = ?, Contenido = ?, " +
                      "Estado = ?, Categoria_ID = (SELECT ID_Categoria FROM Categoria WHERE Nombre = ?) " +
                      "WHERE ID_Articulo = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, articulo.getTitulo());
            statement.setString(2, articulo.getAutor());
            statement.setString(3, articulo.getContenido());
            statement.setString(4, articulo.getEstado());
            statement.setString(5, articulo.getCategoria());
            statement.setInt(6, articulo.getIdArticulo());

            return statement.executeUpdate() > 0;
        }
    }
 // Método para dar like a un artículo
    public boolean darLikeArticulo(int idArticulo, int idUsuario) throws SQLException {
        // Verificar si el artículo existe
        Articulo articulo = obtenerArticuloPorId(idArticulo);
        if (articulo == null) {
            Logger.error("Artículo no encontrado: {}", idArticulo);
            return false;
        }
        
        // Verificar si el usuario es el autor
        if (articulo.getIdUsuario() == idUsuario) {
            Logger.debug("Usuario {} intentó dar like a su propio artículo {}", idUsuario, idArticulo);
            return false;
        }

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);

            // Verificar reacción existente
            String checkQuery = "SELECT Tipo_Like FROM Usuario_Articulo_Like " +
                              "WHERE ID_Usuario = ? AND ID_Articulo = ?";
            
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, idUsuario);
                checkStmt.setInt(2, idArticulo);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String tipoLike = rs.getString("Tipo_Like");
                        if ("ME_GUSTA".equals(tipoLike)) {
                            Logger.debug("Usuario {} ya dio like al artículo {}", idUsuario, idArticulo);
                            return false;
                        } else {
                            // Cambiar de dislike a like
                            Logger.debug("Cambiando dislike a like para usuario {} en artículo {}", idUsuario, idArticulo);
                            return cambiarReaccion(connection, idArticulo, idUsuario, "NO_ME_GUSTA", "ME_GUSTA");
                        }
                    }
                }
            }

            // Registrar nuevo like
            Logger.debug("Registrando nuevo like para usuario {} en artículo {}", idUsuario, idArticulo);
            String insertQuery = "INSERT INTO Usuario_Articulo_Like " +
                               "(ID_Usuario, ID_Articulo, Tipo_Like) " +
                               "VALUES (?, ?, 'ME_GUSTA')";
            
            String updateQuery = "UPDATE Articulo SET Me_Gusta = Me_Gusta + 1 " +
                               "WHERE ID_Articulo = ?";
            
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                 PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                
                insertStmt.setInt(1, idUsuario);
                insertStmt.setInt(2, idArticulo);
                int filasInsertadas = insertStmt.executeUpdate();
                
                if (filasInsertadas == 0) {
                    Logger.error("No se insertó el like en la tabla Usuario_Articulo_Like");
                    connection.rollback();
                    return false;
                }
                
                updateStmt.setInt(1, idArticulo);
                int filasActualizadas = updateStmt.executeUpdate();
                
                if (filasActualizadas == 0) {
                    Logger.error("No se actualizó el contador de likes en la tabla Articulo");
                    connection.rollback();
                    return false;
                }
                
                connection.commit();
                Logger.debug("Like registrado exitosamente");
                return true;
            } catch (SQLException e) {
                connection.rollback();
                Logger.error("Error SQL al registrar like", e);
                throw e;
            }
        }
    }

    // Método para dar "no me gusta" a un artículo
    public boolean darNoMeGustaArticulo(int idArticulo, int idUsuario) throws SQLException {
        // Verificar si el artículo existe
        Articulo articulo = obtenerArticuloPorId(idArticulo);
        if (articulo == null) {
            Logger.error("Artículo no encontrado: {}", idArticulo);
            return false;
        }
        
        // Verificar si el usuario es el autor
        if (articulo.getIdUsuario() == idUsuario) {
            Logger.debug("Usuario {} intentó dar no me gusta a su propio artículo {}", idUsuario, idArticulo);
            return false;
        }

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);

            // Verificar reacción existente
            String checkQuery = "SELECT Tipo_Like FROM Usuario_Articulo_Like " +
                              "WHERE ID_Usuario = ? AND ID_Articulo = ?";
            
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, idUsuario);
                checkStmt.setInt(2, idArticulo);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String tipoLike = rs.getString("Tipo_Like");
                        if ("NO_ME_GUSTA".equals(tipoLike)) {
                            Logger.debug("Usuario {} ya dio no me gusta al artículo {}", idUsuario, idArticulo);
                            return false;
                        } else {
                            // Cambiar de like a no me gusta
                            Logger.debug("Cambiando like a no me gusta para usuario {} en artículo {}", idUsuario, idArticulo);
                            return cambiarReaccion(connection, idArticulo, idUsuario, "ME_GUSTA", "NO_ME_GUSTA");
                        }
                    }
                }
            }

            // Registrar nuevo no me gusta
            Logger.debug("Registrando nuevo no me gusta para usuario {} en artículo {}", idUsuario, idArticulo);
            String insertQuery = "INSERT INTO Usuario_Articulo_Like " +
                               "(ID_Usuario, ID_Articulo, Tipo_Like) " +
                               "VALUES (?, ?, 'NO_ME_GUSTA')";
            
            String updateQuery = "UPDATE Articulo SET No_Me_Gusta = No_Me_Gusta + 1 " +
                               "WHERE ID_Articulo = ?";
            
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                 PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                
                insertStmt.setInt(1, idUsuario);
                insertStmt.setInt(2, idArticulo);
                int filasInsertadas = insertStmt.executeUpdate();
                
                if (filasInsertadas == 0) {
                    Logger.error("No se insertó el no me gusta en la tabla Usuario_Articulo_Like");
                    connection.rollback();
                    return false;
                }
                
                updateStmt.setInt(1, idArticulo);
                int filasActualizadas = updateStmt.executeUpdate();
                
                if (filasActualizadas == 0) {
                    Logger.error("No se actualizó el contador de no me gusta en la tabla Articulo");
                    connection.rollback();
                    return false;
                }
                
                connection.commit();
                Logger.debug("No me gusta registrado exitosamente");
                return true;
            } catch (SQLException e) {
                connection.rollback();
                Logger.error("Error SQL al registrar no me gusta", e);
                throw e;
            }
        }
    }

    // Método auxiliar para cambiar entre like y dislike
    private boolean cambiarReaccion(Connection connection, int idArticulo, int idUsuario, 
                                  String viejoTipo, String nuevoTipo) throws SQLException {
        // Actualizar el tipo de reacción
        String updateLikeQuery = "UPDATE Usuario_Articulo_Like SET Tipo_Like = ? " +
                               "WHERE ID_Usuario = ? AND ID_Articulo = ?";
        
        // Actualizar contadores en el artículo
        String decrementQuery = "UPDATE Articulo SET " + 
                              (viejoTipo.equals("ME_GUSTA") ? "Me_Gusta" : "No_Me_Gusta") + 
                              " = " + (viejoTipo.equals("ME_GUSTA") ? "Me_Gusta" : "No_Me_Gusta") + " - 1 " +
                              "WHERE ID_Articulo = ?";
        
        String incrementQuery = "UPDATE Articulo SET " + 
                              (nuevoTipo.equals("ME_GUSTA") ? "Me_Gusta" : "No_Me_Gusta") + 
                              " = " + (nuevoTipo.equals("ME_GUSTA") ? "Me_Gusta" : "No_Me_Gusta") + " + 1 " +
                              "WHERE ID_Articulo = ?";
        
        try (PreparedStatement updateLikeStmt = connection.prepareStatement(updateLikeQuery);
             PreparedStatement decrementStmt = connection.prepareStatement(decrementQuery);
             PreparedStatement incrementStmt = connection.prepareStatement(incrementQuery)) {
            
            updateLikeStmt.setString(1, nuevoTipo);
            updateLikeStmt.setInt(2, idUsuario);
            updateLikeStmt.setInt(3, idArticulo);
            updateLikeStmt.executeUpdate();
            
            decrementStmt.setInt(1, idArticulo);
            decrementStmt.executeUpdate();
            
            incrementStmt.setInt(1, idArticulo);
            incrementStmt.executeUpdate();
            
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    // Método para obtener artículos con información de likes del usuario
    public List<Articulo> obtenerArticulosConLikesUsuario(Integer idUsuario) throws SQLException {
        List<Articulo> articulos = new ArrayList<>();
        String query = "SELECT a.ID_Articulo, a.ID_Usuario, a.Titulo, a.Autor, " +
                      "a.Contenido, a.Imagen, a.Fecha_Publicacion, a.Estado, " +
                      "c.Nombre AS Categoria, a.Me_Gusta, a.No_Me_Gusta ";

        if (idUsuario != null) {
            query += ", EXISTS(SELECT 1 FROM Usuario_Articulo_Like ual " +
                    "WHERE ual.ID_Articulo = a.ID_Articulo AND ual.ID_Usuario = ? AND ual.Tipo_Like = 'ME_GUSTA') AS usuarioDioLike, " +
                    "EXISTS(SELECT 1 FROM Usuario_Articulo_Like ual " +
                    "WHERE ual.ID_Articulo = a.ID_Articulo AND ual.ID_Usuario = ? AND ual.Tipo_Like = 'NO_ME_GUSTA') AS usuarioDioNoMeGusta ";
        } else {
            query += ", false AS usuarioDioLike, false AS usuarioDioNoMeGusta ";
        }

        query += "FROM Articulo a " +
                "JOIN Categoria c ON a.Categoria_ID = c.ID_Categoria " +
                "WHERE a.Estado = 'PUBLICADO' " +
                "ORDER BY a.Fecha_Publicacion DESC";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (idUsuario != null) {
                statement.setInt(1, idUsuario);
                statement.setInt(2, idUsuario);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Articulo articulo = new Articulo();
                    // ... (llenar los datos del artículo)
                    articulo.setUsuarioDioLike(resultSet.getBoolean("usuarioDioLike"));
                    articulo.setUsuarioDioNoMeGusta(resultSet.getBoolean("usuarioDioNoMeGusta"));
                    articulos.add(articulo);
                }
            }
        }
        return articulos;
    }

    // Método para obtener un artículo con información de likes del usuario
    public Articulo obtenerArticuloPorIdConLikes(int id, Integer idUsuario) throws SQLException {
        String query = "SELECT a.ID_Articulo, a.ID_Usuario, a.Titulo, a.Autor, " +
                      "a.Contenido, a.Imagen, a.Fecha_Publicacion, a.Estado, " +
                      "c.Nombre AS Categoria, a.Me_Gusta, a.No_Me_Gusta ";

        if (idUsuario != null) {
            query += ", EXISTS(SELECT 1 FROM Usuario_Articulo_Like ual " +
                    "WHERE ual.ID_Articulo = a.ID_Articulo AND ual.ID_Usuario = ? AND ual.Tipo_Like = 'ME_GUSTA') AS usuarioDioLike, " +
                    "EXISTS(SELECT 1 FROM Usuario_Articulo_Like ual " +
                    "WHERE ual.ID_Articulo = a.ID_Articulo AND ual.ID_Usuario = ? AND ual.Tipo_Like = 'NO_ME_GUSTA') AS usuarioDioNoMeGusta ";
        } else {
            query += ", false AS usuarioDioLike, false AS usuarioDioNoMeGusta ";
        }

        query += "FROM Articulo a " +
                "JOIN Categoria c ON a.Categoria_ID = c.ID_Categoria " +
                "WHERE a.ID_Articulo = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            int paramIndex = 1;
            if (idUsuario != null) {
                statement.setInt(paramIndex++, idUsuario);
                statement.setInt(paramIndex++, idUsuario);
            }
            statement.setInt(paramIndex, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Articulo articulo = new Articulo();
                    articulo.setIdArticulo(resultSet.getInt("ID_Articulo"));
                    articulo.setIdUsuario(resultSet.getInt("ID_Usuario"));
                    articulo.setTitulo(resultSet.getString("Titulo"));
                    articulo.setAutor(resultSet.getString("Autor"));
                    articulo.setContenido(resultSet.getString("Contenido"));
                    articulo.setImagen(resultSet.getString("Imagen"));
                    articulo.setFechaPublicacion(resultSet.getDate("Fecha_Publicacion"));
                    articulo.setEstado(resultSet.getString("Estado"));
                    articulo.setCategoria(resultSet.getString("Categoria"));
                    articulo.setMeGusta(resultSet.getInt("Me_Gusta"));
                    articulo.setNoMeGusta(resultSet.getInt("No_Me_Gusta"));
                    if (idUsuario != null) {
                        articulo.setUsuarioDioLike(resultSet.getBoolean("usuarioDioLike"));
                        articulo.setUsuarioDioNoMeGusta(resultSet.getBoolean("usuarioDioNoMeGusta"));
                    }
                    return articulo;
                }
            }
        }
        return null;
    }
}