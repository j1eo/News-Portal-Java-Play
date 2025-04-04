package services;

import models.NoticiaPropia;
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
}