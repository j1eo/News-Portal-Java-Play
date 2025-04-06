package controllers;

import play.mvc.*;
import models.Articulo;
import models.NoticiaPropia;
import java.util.List;
import java.util.Collections;
import javax.inject.Inject;
import services.ArticuloService;
import services.NoticiasPropiasService;
import java.sql.SQLException;

public class HomeController extends Controller {

    private final ArticuloService articuloService;
    private final NoticiasPropiasService noticiasService;

    @Inject
    public HomeController(ArticuloService articuloService, NoticiasPropiasService noticiasService) {
        this.articuloService = articuloService;
        this.noticiasService = noticiasService;
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
            
            // Obtener las noticias más recientes (1 principal + 2 secundarias)
            List<NoticiaPropia> noticiasRecientes = noticiasService.obtenerNoticias();
            
            NoticiaPropia noticiaPrincipal = null;
            List<NoticiaPropia> noticiasSecundarias = Collections.emptyList();
            
            if (!noticiasRecientes.isEmpty()) {
                noticiaPrincipal = noticiasRecientes.get(0);
                if (noticiasRecientes.size() > 2) {
                    noticiasSecundarias = noticiasRecientes.subList(1, 3);
                }
            }
            
            return ok(views.html.index.render(
                "UR NEWS", 
                articulosPublicados,
                noticiaPrincipal,
                noticiasSecundarias
            ));
        } catch (SQLException e) {
            e.printStackTrace();
            // En caso de error, mostrar página sin artículos ni noticias
            return ok(views.html.index.render(
                "UR NEWS", 
                Collections.emptyList(),
                null,
                Collections.emptyList()
            ));
        }
    }
}