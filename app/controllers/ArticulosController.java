package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Articulo;
import services.ArticuloService;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class ArticulosController extends Controller {
    private final ArticuloService articuloService;

    // **Inyección de dependencias en el constructor**
    @Inject
    public ArticulosController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    // **Endpoint para obtener todos los artículos**
    public Result obtenerArticulos() {
        try {
            List<Articulo> articulos = articuloService.obtenerArticulos();
            return ok(Json.toJson(articulos)); // Devuelve los artículos en JSON
        } catch (SQLException e) {
            System.err.println("Error en la base de datos: " + e.getMessage());
            return internalServerError("Error en la base de datos al obtener los artículos.");
        } catch (Exception e) {
            System.err.println("Error desconocido: " + e.getMessage());
            return internalServerError("Error desconocido al obtener los artículos.");
        }
    }

    // **Endpoint para agregar un nuevo artículo**
 // **Endpoint para agregar un nuevo artículo**
    public Result agregarArticulo() {
        try {
            JsonNode json = request().body().asJson(); // Usar request() directamente
            if (json == null) {
                return badRequest("El cuerpo de la solicitud debe ser un JSON.");
            }

            // Extraer los datos del JSON
            String titulo = json.get("titulo").asText();
            String autor = json.get("autor").asText();
            String contenido = json.get("contenido").asText();
            String imagen = json.has("imagen") ? json.get("imagen").asText() : null;
            String estado = json.has("estado") ? json.get("estado").asText() : "PUBLICADO";
            String categoria = json.has("categoria") ? json.get("categoria").asText() : "GENERAL";
            int meGusta = json.has("meGusta") ? json.get("meGusta").asInt() : 0;
            int noMeGusta = json.has("noMeGusta") ? json.get("noMeGusta").asInt() : 0;

            // **Enviar los datos al servicio**
            articuloService.agregarArticulo(titulo, autor, contenido, imagen, estado, categoria, meGusta, noMeGusta);

            return ok(Json.newObject().put("status", "Artículo agregado con éxito."));
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError("Error al agregar el artículo.");
        }
    }

}
