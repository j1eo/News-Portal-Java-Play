package services;

import models.Articulo;
import dataBase.DB_Connection;

import javax.inject.Inject;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArticuloService {
    private final DB_Connection dbConnection;

    @Inject
    public ArticuloService(DB_Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

 // **Obtener todos los artículos**
    public List<Articulo> obtenerArticulos() throws SQLException {
        List<Articulo> articulos = new ArrayList<>();
        String query = "SELECT articulo.ID_Articulo, articulo.ID_Usuario, articulo.Titulo, articulo.Autor, " +
                       "articulo.Contenido, articulo.Imagen, articulo.Fecha_Publicacion, articulo.Estado, " +
                       "categoria.Nombre AS Categoria, articulo.Me_Gusta, articulo.No_Me_Gusta " +
                       "FROM articulo " +
                       "JOIN categoria ON articulo.Categoria_ID = categoria.ID_Categoria";

        try (Connection connection = dbConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                articulos.add(new Articulo(
                    (int) resultSet.getLong("ID_Articulo"),              // idArticulo
                    (int) resultSet.getLong("ID_Usuario"),               // idMiembro
                    resultSet.getString("Titulo"),                      // titulo
                    resultSet.getString("Autor"),                       // autor
                    resultSet.getString("Contenido"),                   // contenido
                    resultSet.getString("Imagen"),                      // imagen
                    resultSet.getDate("Fecha_Publicacion"),             // fechaPublicacion
                    resultSet.getString("Estado"),                      // estado
                    resultSet.getString("Categoria"),                   // categoria (ahora el nombre)
                    resultSet.getInt("Me_Gusta"),                       // meGusta
                    resultSet.getInt("No_Me_Gusta")                     // noMeGusta
                ));
            }
        }
        
        return articulos;
    }

    // **Guardar un nuevo artículo en la base de datos**
    public void agregarArticulo(String titulo, String autor, String contenido, String imagen, String estado, String categoria, int meGusta, int noMeGusta) throws SQLException {
        String query = "INSERT INTO articulos (idMiembro, titulo, autor, contenido, imagen, fechaPublicacion, estado, categoria, meGusta, noMeGusta) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, 1);  // **idMiembro temporal** (ajustar según lógica de usuario)
            stmt.setString(2, titulo);
            stmt.setString(3, autor);
            stmt.setString(4, contenido);
            stmt.setString(5, imagen);
            stmt.setDate(6, new java.sql.Date(System.currentTimeMillis())); // Fecha actual
            stmt.setString(7, estado);
            stmt.setString(8, categoria);
            stmt.setInt(9, meGusta);
            stmt.setInt(10, noMeGusta);

            stmt.executeUpdate();
        }
    }

    // **Eliminar un artículo por su ID**
    public void eliminarArticulo(int id) throws SQLException {
        String query = "DELETE FROM articulos WHERE id = ?";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
