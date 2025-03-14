package repository;

import models.Articulo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticuloRepository {
    private static final String URL = "jdbc:mysql://localhost:3306/portal_noticias";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public List<Articulo> obtenerTodos() {
        List<Articulo> articulos = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM articulos")) {
            while (rs.next()) {
                articulos.add(new Articulo(rs.getLong("id"), rs.getString("titulo"), rs.getString("contenido"), rs.getDate("fechaPublicacion")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return articulos;
    }

    public void guardar(Articulo articulo) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO articulos (titulo, contenido, fechaPublicacion) VALUES (?, ?, ?)")) {
            stmt.setString(1, articulo.getTitulo());
            stmt.setString(2, articulo.getContenido());
            stmt.setDate(3, new java.sql.Date(articulo.getFechaPublicacion().getTime()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
