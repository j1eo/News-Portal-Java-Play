package services;

import models.Articulo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dataBase.DB_Connection;

public class ArticuloService {
    private final DB_Connection dbConnection;

    public ArticuloService() {
        // Ruta correcta al archivo .properties
        this.dbConnection = new DB_Connection(".properties");
    }

    // Método para obtener todos los artículos
    public List<Articulo> obtenerArticulos() throws SQLException {
        List<Articulo> articulos = new ArrayList<>();
        String query = "SELECT * FROM articulos";

        try (Connection connection = dbConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String titulo = resultSet.getString("titulo");
                String contenido = resultSet.getString("contenido");
                Timestamp timestamp = resultSet.getTimestamp("fecha_publicacion");
                Date fechaPublicacion = timestamp != null ? new Date(timestamp.getTime()) : null;

                articulos.add(new Articulo(id, titulo, contenido, fechaPublicacion));
            }
        }
        return articulos;
    }

    // Método para agregar un nuevo artículo
    public void agregarArticulo(String titulo, String contenido) throws SQLException {
        String query = "INSERT INTO articulos (titulo, contenido, fecha_publicacion) VALUES (?, ?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Configurar los parámetros para la consulta
            preparedStatement.setString(1, titulo);
            preparedStatement.setString(2, contenido);
            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Fecha actual

            preparedStatement.executeUpdate();
        }
    }
}
