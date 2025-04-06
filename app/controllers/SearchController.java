package controllers;

import models.Usuario;
import models.NoticiaPropia;
import play.Logger;
import play.mvc.*;
import services.SearchService;
import services.AuthService;
import services.JwtService;

import javax.inject.Inject;
import java.sql.SQLException;

public class SearchController extends Controller {
    
    private final SearchService searchService;
    private final JwtService jwtService;
    private final AuthService authService;

    @Inject
    public SearchController(SearchService searchService,
                          JwtService jwtService,
                          AuthService authService) {
        this.searchService = searchService;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    private Usuario obtenerUsuarioCompleto() {
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            return null;
        }

        try {
            String token = jwtCookie.value();
            String userId = jwtService.obtenerSubject(token);
            return (Usuario) authService.getCuentaById(Integer.parseInt(userId));
        } catch (Exception e) {
            Logger.error("Error al obtener usuario completo", e);
            return null;
        }
    }

    public Result search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return badRequest("Término de búsqueda vacío");
        }

        try {
            SearchService.SearchResults resultados = searchService.search(q);
            Usuario usuario = obtenerUsuarioCompleto();

            return ok(views.html.searchResults.render(
                resultados.getArticulos(),
                resultados.getNoticias(),
                usuario,
                "Resultados para: " + q,
                "UR NEWS",
                request()
            ));
        } catch (SQLException e) {
            Logger.error("Error en búsqueda", e);
            return internalServerError("Error al realizar la búsqueda");
        }
    }
}