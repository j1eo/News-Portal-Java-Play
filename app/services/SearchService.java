package services;

import models.Articulo;
import models.NoticiaPropia;
import dataBase.DB_Connection;

import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchService {
    
    private final DB_Connection dbConnection;

    @Inject
    public SearchService(DB_Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public SearchResults search(String term) throws SQLException {
        return new SearchResults(
            searchArticulos(term),
            searchNoticias(term)
        );
    }

    private List<Articulo> searchArticulos(String term) throws SQLException {
        List<Articulo> resultados = new ArrayList<>();
        String searchPattern = "%" + term.toLowerCase() + "%";
        
        String query = "SELECT DISTINCT a.*, c.Nombre AS Categoria " +
                      "FROM Articulo a " +
                      "JOIN Categoria c ON a.Categoria_ID = c.ID_Categoria " +
                      "LEFT JOIN Articulo_Etiqueta ae ON a.ID_Articulo = ae.ID_Articulo " +
                      "LEFT JOIN Etiqueta e ON ae.ID_Etiqueta = e.ID_Etiqueta " +
                      "WHERE (LOWER(a.Titulo) LIKE ? OR " +
                      "LOWER(a.Contenido) LIKE ? OR " +
                      "LOWER(e.Nombre) LIKE ?) AND " +
                      "a.Estado = 'PUBLICADO' " +
                      "ORDER BY a.Fecha_Publicacion DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Articulo articulo = new Articulo();
                    articulo.setIdArticulo(rs.getInt("ID_Articulo"));
                    articulo.setTitulo(rs.getString("Titulo"));
                    articulo.setAutor(rs.getString("Autor"));
                    articulo.setContenido(rs.getString("Contenido"));
                    articulo.setImagen(rs.getString("Imagen"));
                    articulo.setFechaPublicacion(rs.getDate("Fecha_Publicacion"));
                    articulo.setCategoria(rs.getString("Categoria"));
                    resultados.add(articulo);
                }
            }
        }
        return resultados;
    }

    private List<NoticiaPropia> searchNoticias(String term) throws SQLException {
        List<NoticiaPropia> resultados = new ArrayList<>();
        String searchPattern = "%" + term.toLowerCase() + "%";
        
        String query = "SELECT DISTINCT n.*, c.Nombre AS Categoria " +
                      "FROM Noticia n " +
                      "JOIN Categoria c ON n.Categoria_ID = c.ID_Categoria " +
                      "LEFT JOIN Noticia_Etiqueta ne ON n.ID_Noticia = ne.ID_Noticia " +
                      "LEFT JOIN Etiqueta e ON ne.ID_Etiqueta = e.ID_Etiqueta " +
                      "WHERE (LOWER(n.Titulo) LIKE ? OR " +
                      "LOWER(n.Contenido) LIKE ? OR " +
                      "LOWER(n.Descripcion) LIKE ? OR " +
                      "LOWER(e.Nombre) LIKE ?) AND " +
                      "n.Estado = 'PUBLICADO' " +
                      "ORDER BY n.Fecha_Publicacion DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
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
                    noticia.setFechaPublicacion(new Date(rs.getTimestamp("Fecha_Publicacion").getTime()));
                    noticia.setEstado(rs.getString("Estado"));
                    noticia.setCategoria(rs.getString("Categoria"));
                    noticia.setMeGusta(rs.getInt("Me_Gusta"));
                    noticia.setNoMeGusta(rs.getInt("No_Me_Gusta"));
                    resultados.add(noticia);
                }
            }
        }
        return resultados;
    }

    public static class SearchResults {
        private final List<Articulo> articulos;
        private final List<NoticiaPropia> noticias;

        public SearchResults(List<Articulo> articulos, List<NoticiaPropia> noticias) {
            this.articulos = articulos;
            this.noticias = noticias;
        }

        public List<Articulo> getArticulos() { return articulos; }
        public List<NoticiaPropia> getNoticias() { return noticias; }
    }
}