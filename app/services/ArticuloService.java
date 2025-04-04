package services;

import models.Articulo;
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
}