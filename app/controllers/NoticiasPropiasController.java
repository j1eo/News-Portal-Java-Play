package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.NoticiaPropia;
import models.Usuario;
import play.Logger;
import play.libs.Json;
import play.mvc.*;
import services.NoticiasPropiasService;
import services.AuthService;
import services.JwtService;
import controllers.Secured;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class NoticiasPropiasController extends Controller {

    private final NoticiasPropiasService noticiasService;
    private final JwtService jwtService;
    private final AuthService authService;

    @Inject
    public NoticiasPropiasController(NoticiasPropiasService noticiasService, 
                                   JwtService jwtService, 
                                   AuthService authService) {
        this.noticiasService = noticiasService;
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

    public Result mostrarNoticia(int id) {
        try {
            Logger.debug("=== Mostrando noticia ID: " + id + " ===");
            
            NoticiaPropia noticia = noticiasService.obtenerNoticiaPorId(id);
            if (noticia == null) {
                Logger.warn("Noticia no encontrada: " + id);
                return notFound("Noticia no encontrada");
            }

            List<NoticiaPropia> relacionados = noticiasService.obtenerNoticiasRelacionadas(
                id, 
                noticia.getCategoria(), 
                3
            );

            Usuario usuario = obtenerUsuarioCompleto();
            Logger.debug("Usuario obtenido: " + (usuario != null ? usuario.getNombre() : "null"));

            return ok(views.html.noticia.render(
                noticia,
                relacionados,
                usuario,
                "UR NEWS",
                request()
            ));
        } catch (Exception e) {
            Logger.error("Error al mostrar noticia", e);
            return internalServerError("Error al obtener la noticia");
        }
    }

    public Result listarNoticias() {
        try {
            List<NoticiaPropia> noticias = noticiasService.obtenerNoticias();
            Usuario usuario = obtenerUsuarioCompleto();

            return ok(views.html.listaNoticias.render(
                noticias,
                usuario,
                "UR NEWS",
                request()
            ));
        } catch (Exception e) {
            Logger.error("Error al listar noticias", e);
            return internalServerError("Error al obtener noticias");
        }
    }

    @Security.Authenticated(Secured.class)
    public Result mostrarFormularioCreacion() {
        Usuario usuario = obtenerUsuarioCompleto();
        if (usuario == null) {
            return unauthorized("Debes iniciar sesión");
        }

        return ok(views.html.crearNoticia.render(
            usuario,
            "UR NEWS",
            request()
        ));
    }

    @Security.Authenticated(Secured.class)
    public Result crearNoticia() {
        try {
            Usuario usuario = obtenerUsuarioCompleto();
            if (usuario == null) {
                return unauthorized("Token inválido");
            }

            // Verificar rol de usuario
            String userRole = jwtService.obtenerUserRole(request().cookie("jwt").value());
            if (!"user".equals(userRole) && !"admin".equals(userRole)) {
                return forbidden("No tienes permisos para esta acción");
            }

            JsonNode json = request().body().asJson();
            if (json == null) {
                return badRequest("Se esperaba JSON");
            }

            NoticiaPropia noticia = new NoticiaPropia();
            noticia.setIdUsuario(usuario.getIdUsuario());
            noticia.setTitulo(json.get("titulo").asText());
            noticia.setAutor(json.has("autor") ? json.get("autor").asText() : usuario.getNombre());
            noticia.setContenido(json.get("contenido").asText());
            noticia.setImagen(json.has("imagen") ? json.get("imagen").asText() : null);
            noticia.setEstado(json.get("estado").asText());
            noticia.setCategoria(json.get("categoria").asText());
            noticia.setUrl(json.has("url") ? json.get("url").asText() : null);
            noticia.setFuente(json.has("fuente") ? json.get("fuente").asText() : null);
            noticia.setDescripcion(json.has("descripcion") ? json.get("descripcion").asText() : null);
            noticia.setMeGusta(0);
            noticia.setNoMeGusta(0);

            boolean creado = noticiasService.agregarNoticia(noticia);

            return creado ? created("Noticia creada") : internalServerError("Error al crear noticia");
            
        } catch (Exception e) {
            Logger.error("Error al crear noticia", e);
            return badRequest("Datos inválidos: " + e.getMessage());
        }
    }

    @Security.Authenticated(Secured.class)
    public Result eliminarNoticia(int id) {
        try {
            Usuario usuario = obtenerUsuarioCompleto();
            if (usuario == null) {
                return unauthorized("Token inválido");
            }

            // Solo ADMIN puede eliminar
            if (!"admin".equals(jwtService.obtenerUserRole(request().cookie("jwt").value()))) {
                return forbidden("Requiere rol de ADMIN");
            }

            boolean eliminado = noticiasService.eliminarNoticia(id);
            
            if (eliminado) {
                return ok(Json.newObject()
                    .put("success", true)
                    .put("message", "Noticia eliminada"));
            } else {
                return notFound(Json.newObject()
                    .put("success", false)
                    .put("message", "Noticia no encontrada"));
            }
            
        } catch (Exception e) {
            Logger.error("Error al eliminar noticia", e);
            return internalServerError(Json.newObject()
                .put("success", false)
                .put("message", "Error al eliminar noticia"));
        }
    }
}