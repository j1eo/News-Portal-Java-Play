package controllers;

import models.Articulo;
import play.mvc.Controller;
import play.mvc.Result;
import services.ArticuloService;
import play.libs.Json;

import javax.inject.Inject;
import java.util.List;

public class ArticulosController extends Controller {
    private final ArticuloService articuloService;

    @Inject
    public ArticulosController(ArticuloService articuloService) {
        this.articuloService = articuloService;
    }

    public Result obtenerTodos() {
        List<Articulo> articulos = articuloService.obtenerTodos();
        return ok(Json.toJson(articulos));
    }

    public Result agregarArticulo() {
        Articulo articulo = Json.fromJson(request().body().asJson(), Articulo.class);
        articuloService.agregarArticulo(articulo);
        return created();
    }
}
