package controllers;

import play.mvc.*;
import models.Articulo;
import java.util.List;
import java.util.Collections; // Añade este import
import javax.inject.Inject;
import services.ArticuloService;
import java.sql.SQLException;

public class HomeController extends Controller {

    private final ArticuloService articuloService;

    @Inject
    public HomeController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    public Result index() {
        // Limpieza adicional si viene de logout
        if (request().getQueryString("noBack") != null) {
            response().discardCookie("jsessionid", "/");
            response().setHeader("Clear-Site-Data", "\"cache\"");
        }
        
        try {
            // Obtener los últimos artículos publicados
            List<Articulo> articulosPublicados = articuloService.obtenerArticulos();
            return ok(views.html.index.render("UR NEWS", articulosPublicados));
        } catch (SQLException e) {
            e.printStackTrace();
            // En caso de error, mostrar página sin artículos
            return ok(views.html.index.render("UR NEWS", Collections.emptyList()));
        }
    }
}