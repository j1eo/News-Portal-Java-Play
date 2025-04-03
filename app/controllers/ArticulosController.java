package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Articulo;
import models.Usuario;
import play.Logger;
import play.libs.Json;
import play.mvc.*;
import services.ArticuloService;
import services.AuthService;
import services.JwtService;
import controllers.Secured;
import play.mvc.Http.Cookie;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class ArticulosController extends Controller {

    private final ArticuloService articuloService;
    private final JwtService jwtService;
    private final AuthService authService;

    @Inject
    public ArticulosController(ArticuloService articuloService, JwtService jwtService, AuthService authService) {
        this.articuloService = articuloService;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    // Método para obtener usuario completo (igual que en UserController)
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

    // Método para mostrar artículo
    public Result mostrarArticulo(int id) {
        try {
            Logger.debug("=== Mostrando artículo ID: " + id + " ===");
            
            Articulo articulo = articuloService.obtenerArticuloPorId(id);
            if (articulo == null) {
                Logger.warn("Artículo no encontrado: " + id);
                return notFound("Artículo no encontrado");
            }

            List<Articulo> relacionados = articuloService.obtenerArticulosRelacionados(
                id, 
                articulo.getCategoria(), 
                3
            );

            Usuario usuario = obtenerUsuarioCompleto();
            Logger.debug("Usuario obtenido: " + (usuario != null ? usuario.getNombre() : "null"));

            return ok(views.html.articulo.render(
                articulo,
                relacionados,
                usuario,
                "UR NEWS",
                request()
            ));
        } catch (Exception e) {
            Logger.error("Error al mostrar artículo", e);
            return internalServerError("Error al obtener el artículo");
        }
    }

    // Método para listar artículos
    public Result listarArticulos() {
        try {
            List<Articulo> articulos = articuloService.obtenerArticulos();
            Usuario usuario = obtenerUsuarioCompleto();

            return ok(views.html.listaArticulos.render(
                articulos,
                usuario,
                "UR NEWS",
                request()
            ));
        } catch (Exception e) {
            Logger.error("Error al listar artículos", e);
            return internalServerError("Error al obtener artículos");
        }
    }

    @Security.Authenticated(Secured.class)
    public Result mostrarFormularioCreacion() {
        Usuario usuario = obtenerUsuarioCompleto();
        if (usuario == null) {
            return unauthorized("Debes iniciar sesión");
        }

        return ok(views.html.crearArticulo.render(
            usuario,
            "UR NEWS",
            request()
        ));
    }

    @Security.Authenticated(Secured.class)
    public Result crearArticulo() {
        try {
            Usuario usuario = obtenerUsuarioCompleto();
            if (usuario == null) {
                return unauthorized("Token inválido");
            }

            // Verificar rol de usuario
            String userRole = jwtService.obtenerUserRole(request().cookie("jwt").value());
            if (!"AUTOR".equals(userRole) && !"ADMIN".equals(userRole)) {
                return forbidden("No tienes permisos para esta acción");
            }

            JsonNode json = request().body().asJson();
            if (json == null) {
                return badRequest("Se esperaba JSON");
            }

            // Obtener datos del formulario
            String titulo = json.get("titulo").asText();
            String contenido = json.get("contenido").asText();
            String imagen = json.has("imagen") ? json.get("imagen").asText() : null;
            String categoria = json.get("categoria").asText();
            String estado = json.get("estado").asText();
            String autor = json.has("autor") ? json.get("autor").asText() : usuario.getNombre();

            boolean creado = articuloService.agregarArticulo(
                usuario.getIdUsuario(),
                titulo,
                autor,
                contenido,
                imagen,
                estado,
                categoria,
                0, // meGusta inicial
                0  // noMeGusta inicial
            );

            return creado ? created("Artículo creado") : internalServerError("Error al crear artículo");
            
        } catch (Exception e) {
            Logger.error("Error al crear artículo", e);
            return badRequest("Datos inválidos: " + e.getMessage());
        }
    }

    @Security.Authenticated(Secured.class)
    public Result eliminarArticulo(int id) {
        try {
            Usuario usuario = obtenerUsuarioCompleto();
            if (usuario == null) {
                return unauthorized("Token inválido");
            }

            // Solo ADMIN puede eliminar
            if (!"ADMIN".equals(jwtService.obtenerUserRole(request().cookie("jwt").value()))) {
                return forbidden("Requiere rol de ADMIN");
            }

            boolean eliminado = articuloService.eliminarArticulo(id);
            
            if (eliminado) {
                return ok(Json.newObject()
                    .put("success", true)
                    .put("message", "Artículo eliminado"));
            } else {
                return notFound(Json.newObject()
                    .put("success", false)
                    .put("message", "Artículo no encontrado"));
            }
            
        } catch (Exception e) {
            Logger.error("Error al eliminar artículo", e);
            return internalServerError(Json.newObject()
                .put("success", false)
                .put("message", "Error al eliminar artículo"));
        }
    }
}