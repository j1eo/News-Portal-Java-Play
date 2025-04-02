package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Articulo;
import models.Usuario;
import play.libs.Json;
import play.mvc.*;
import services.ArticuloService;
import services.JwtService;
import controllers.Secured;
import play.mvc.Http.Cookie;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class ArticulosController extends Controller {

    private final ArticuloService articuloService;
    private final JwtService jwtService;

    @Inject
    public ArticulosController(ArticuloService articuloService, JwtService jwtService) {
        this.articuloService = articuloService;
        this.jwtService = jwtService;
    }

    // Método auxiliar para obtener el usuario desde el token
    private Usuario obtenerUsuarioDesdeToken(String token) {
        if (token == null || !jwtService.validarToken(token)) {
            return null;
        }
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(Integer.parseInt(jwtService.obtenerSubject(token)));
        usuario.setNombre(jwtService.obtenerClaim(token, "nombre"));
        usuario.setEmail(jwtService.obtenerClaim(token, "email"));
        return usuario;
    }

    // Obtener token JWT desde las cookies
    private String obtenerTokenJWT() {
        Cookie jwtCookie = request().cookie("jwt");
        return jwtCookie != null ? jwtCookie.value() : null;
    }

    // Métodos actualizados para usar Usuario en lugar de token
    public Result mostrarArticulo(int id) {
        try {
            Articulo articulo = articuloService.obtenerArticuloPorId(id);
            if (articulo == null) {
                return notFound("Artículo no encontrado");
            }

            List<Articulo> relacionados = articuloService.obtenerArticulosRelacionados(
                id, 
                articulo.getCategoria(), 
                3
            );

            Usuario usuario = obtenerUsuarioDesdeToken(obtenerTokenJWT());

            return ok(views.html.articulo.render(
                articulo,
                relacionados,
                usuario,
                "UR NEWS",
                request()
            ));
        } catch (SQLException e) {
            return internalServerError("Error al obtener el artículo");
        }
    }

    public Result listarArticulos() {
        try {
            List<Articulo> articulos = articuloService.obtenerArticulos();
            Usuario usuario = obtenerUsuarioDesdeToken(obtenerTokenJWT());

            return ok(views.html.listaArticulos.render(
                articulos,
                usuario,
                "UR NEWS",
                request()
            ));
        } catch (SQLException e) {
            return internalServerError("Error al obtener artículos");
        }
    }

    @Security.Authenticated(Secured.class)
    public Result mostrarFormularioCreacion() {
        Usuario usuario = obtenerUsuarioDesdeToken(obtenerTokenJWT());
        return ok(views.html.crearArticulo.render(
            usuario,
            "UR NEWS",
            request()
        ));
    }

 // Agrega este método al controlador
    @Security.Authenticated(Secured.class)
    public Result crearArticulo() {
        try {
            String token = obtenerTokenJWT();
            if (token == null || !jwtService.validarToken(token)) {
                return unauthorized("Token inválido");
            }

            // Verificar rol de usuario
            String userRole = jwtService.obtenerUserRole(token);
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
            String autor = json.has("autor") ? json.get("autor").asText() : 
                          jwtService.obtenerClaim(token, "nombre"); // Obtener nombre del token

            // Obtener ID de usuario del token
            int idUsuario = Integer.parseInt(jwtService.obtenerSubject(token));

            boolean creado = articuloService.agregarArticulo(
                idUsuario,
                titulo,
                autor,
                contenido,
                imagen,
                estado,
                categoria,
                0, // meGusta inicial
                0  // noMeGusta inicial
            );

            return creado ? created("Artículo creado") : internalServerError("Error al crear");
            
        } catch (Exception e) {
            return badRequest("Datos inválidos: " + e.getMessage());
        }
    }
    @Security.Authenticated(Secured.class)
    public Result eliminarArticulo(int id) {
        try {
            String token = obtenerTokenJWT();
            if (token == null || !jwtService.validarToken(token)) {
                return unauthorized("Token inválido");
            }

            // Solo ADMIN puede eliminar
            if (!"ADMIN".equals(jwtService.obtenerUserRole(token))) {
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
            
        } catch (SQLException e) {
            return internalServerError(Json.newObject()
                .put("success", false)
                .put("message", "Error al eliminar artículo"));
        }
    }
}