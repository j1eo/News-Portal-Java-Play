package services;

import dataBase.DB_Connection;
import models.Articulo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticuloService {
    private final DB_Connection dbConnection;

    public ArticuloService() {
        // Configurar la conexión a la base de datos
        this.dbConnection = new DB_Connection("jdbc:mysql://localhost:3306/newsportal", "root", "");
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
                Date fechaPublicacion = new Date(timestamp.getTime());

                articulos.add(new Articulo(id, titulo, contenido, fechaPublicacion));
            }
        }
        return articulos;
    }

    // Método para agregar un nuevo artículo
    public void agregarArticulo(String titulo, String contenido) throws SQLException {
        String query = "INSERT INTO articulos (titulo, contenido) VALUES (?, ?)";

        try (Connection connection = dbConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, titulo);
            preparedStatement.setString(2, contenido);
            preparedStatement.executeUpdate();
        }
    }
}
