package controllers;

import models.Articulo;
import services.ArticuloService;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.sql.SQLException;
import java.util.List;

public class ArticulosController extends Controller {
    private final ArticuloService articuloService;

    // Constructor: inicializa el servicio
    public ArticulosController() {
        this.articuloService = new ArticuloService();
    }

    // Endpoint para obtener todos los artículos
    public Result obtenerArticulos() {
        try {
            List<Articulo> articulos = articuloService.obtenerArticulos();
            return ok(Json.toJson(articulos)); // Devuelve los artículos en formato JSON
        } catch (SQLException e) {
            System.err.println("Error en la base de datos: " + e.getMessage());
            return internalServerError("Error en la base de datos al obtener los artículos");
        } catch (Exception e) {
            System.err.println("Error desconocido: " + e.getMessage());
            return internalServerError("Error desconocido al obtener los artículos");
        }
    }


    // Endpoint para agregar un nuevo artículo
    public Result agregarArticulo() {
        try {
            String titulo = request().body().asJson().get("titulo").asText();
            String contenido = request().body().asJson().get("contenido").asText();
            articuloService.agregarArticulo(titulo, contenido);
            return ok(Json.newObject().put("status", "Artículo agregado con éxito"));
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError("Error al agregar el artículo");
        }
    }
}