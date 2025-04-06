package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import models.Articulo;
import models.Comentario;
import models.Usuario;
import play.Logger;
import play.libs.Json;
import play.mvc.*;
import services.ArticuloService;
import services.ComentarioService;
import services.AuthService;
import services.JwtService;
import controllers.Secured;
import play.mvc.Http.Cookie;
import play.data.Form;
import play.data.DynamicForm;
import play.data.FormFactory;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

public class ArticulosController extends Controller {
    private final ArticuloService articuloService;
    private final ComentarioService comentarioService;
    private final JwtService jwtService;
    private final AuthService authService;
    private final FormFactory formFactory;

    @Inject
    public ArticulosController(ArticuloService articuloService, 
                             ComentarioService comentarioService,
                             JwtService jwtService,
                             AuthService authService,
                             FormFactory formFactory) {
        this.articuloService = articuloService;
        this.comentarioService = comentarioService;
        this.jwtService = jwtService;
        this.authService = authService;
        this.formFactory = formFactory;
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
            
            Usuario usuario = obtenerUsuarioCompleto();
            Articulo articulo;
            
            if (usuario != null) {
                articulo = articuloService.obtenerArticuloPorIdConLikes(id, usuario.getIdUsuario());
            } else {
                articulo = articuloService.obtenerArticuloPorId(id);
            }

            if (articulo == null) {
                Logger.warn("Artículo no encontrado: " + id);
                return notFound("Artículo no encontrado");
            }

            List<Articulo> relacionados = articuloService.obtenerArticulosRelacionados(
                id, 
                articulo.getCategoria(), 
                3
            );

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
                return unauthorized("Debes iniciar sesión");
            }

            // Obtener datos del formulario (ahora de form-urlencoded)
            DynamicForm form = formFactory.form().bindFromRequest();
            
            String titulo = form.get("titulo");
            String contenido = form.get("contenido");
            String imagen = form.get("imagen");
            String categoria = form.get("categoria");
            String estado = form.get("estado");
            
            if (titulo == null || contenido == null || categoria == null) {
                return badRequest("Faltan campos requeridos");
            }

            // Establecer valores por defecto si son nulos
            if (imagen == null) imagen = "";
            if (estado == null) estado = "PUBLICADO";

            boolean creado = articuloService.agregarArticulo(
                usuario.getIdUsuario(),
                titulo,
                usuario.getNombre(), // Autor es el usuario actual
                contenido,
                imagen,
                estado,
                categoria,
                0, // meGusta inicial
                0  // noMeGusta inicial
            );

            if (creado) {
            	flash("success", "Articulo creado exitosamente");
                return redirect(routes.ArticulosController.listarArticulos());
            } else {
                return badRequest("Error al crear el artículo");
            }
            
        } catch (Exception e) {
            Logger.error("Error al crear artículo", e);
            return badRequest("Error: " + e.getMessage());
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
            if (!"admin".equals(jwtService.obtenerUserRole(request().cookie("jwt").value()))) {
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
 // Método para manejar likes
    @Security.Authenticated(Secured.class)
    public Result darLike(int idArticulo) {
        try {
            Usuario usuario = obtenerUsuarioCompleto();
            if (usuario == null) {
                Logger.error("Intento de like sin usuario autenticado");
                return unauthorized(Json.newObject()
                    .put("success", false)
                    .put("message", "Debes iniciar sesión para esta acción"));
            }

            Logger.debug("Intentando registrar like - Artículo: {}, Usuario: {}", idArticulo, usuario.getIdUsuario());
            
            boolean exito = articuloService.darLikeArticulo(idArticulo, usuario.getIdUsuario());
            
            if (exito) {
                Logger.debug("Like registrado exitosamente");
                return ok(Json.newObject()
                    .put("success", true)
                    .put("message", "Like registrado")
                    .put("nuevoTotal", articuloService.obtenerArticuloPorId(idArticulo).getMeGusta()));
            } else {
                // Obtener más información sobre el fallo
                Articulo articulo = articuloService.obtenerArticuloPorId(idArticulo);
                if (articulo != null && articulo.getIdUsuario() == usuario.getIdUsuario()) {
                    Logger.debug("Usuario intentó dar like a su propio artículo");
                    return badRequest(Json.newObject()
                        .put("success", false)
                        .put("message", "No puedes dar like a tu propio artículo"));
                }
                
                // Verificar si ya existe un like
                boolean yaDioLike = articuloService.obtenerArticuloPorIdConLikes(idArticulo, usuario.getIdUsuario())
                    .isUsuarioDioLike();
                    
                if (yaDioLike) {
                    Logger.debug("Usuario ya había dado like anteriormente");
                    return badRequest(Json.newObject()
                        .put("success", false)
                        .put("message", "Ya has dado like a este artículo"));
                }
                
                Logger.warn("No se pudo registrar el like por razones desconocidas");
                return badRequest(Json.newObject()
                    .put("success", false)
                    .put("message", "No se pudo registrar el like. Intenta nuevamente"));
            }
        } catch (SQLException e) {
            Logger.error("Error de base de datos al registrar like", e);
            return internalServerError(Json.newObject()
                .put("success", false)
                .put("message", "Error en la base de datos"));
        } catch (Exception e) {
            Logger.error("Error inesperado al registrar like", e);
            return internalServerError(Json.newObject()
                .put("success", false)
                .put("message", "Error interno del servidor"));
        }
    }
    // Método para manejar dislikes
    @Security.Authenticated(Secured.class)
    public Result darNoMeGusta(int idArticulo) {
        try {
            Usuario usuario = obtenerUsuarioCompleto();
            if (usuario == null || usuario.getIdUsuario() <= 0) {
                Logger.error("Intento de no me gusta sin usuario autenticado");
                return unauthorized(Json.newObject()
                    .put("success", false)
                    .put("message", "Debes iniciar sesión para esta acción"));
            }

            Logger.debug("Intentando registrar no me gusta - Artículo: {}, Usuario: {}", idArticulo, usuario.getIdUsuario());
            
            boolean exito = articuloService.darNoMeGustaArticulo(idArticulo, usuario.getIdUsuario());
            
            if (exito) {
                Logger.debug("No me gusta registrado exitosamente");
                return ok(Json.newObject()
                    .put("success", true)
                    .put("message", "No me gusta registrado")
                    .put("nuevoTotal", articuloService.obtenerArticuloPorId(idArticulo).getNoMeGusta()));
            } else {
                // Obtener más información sobre el fallo
                Articulo articulo = articuloService.obtenerArticuloPorId(idArticulo);
                if (articulo != null && articulo.getIdUsuario() == usuario.getIdUsuario()) {
                    Logger.debug("Usuario intentó dar no me gusta a su propio artículo");
                    return badRequest(Json.newObject()
                        .put("success", false)
                        .put("message", "No puedes dar no me gusta a tu propio artículo"));
                }
                
                // Verificar si ya existe un no me gusta
                boolean yaDioNoMeGusta = articuloService.obtenerArticuloPorIdConLikes(idArticulo, usuario.getIdUsuario())
                    .isUsuarioDioNoMeGusta();
                    
                if (yaDioNoMeGusta) {
                    Logger.debug("Usuario ya había dado no me gusta anteriormente");
                    return badRequest(Json.newObject()
                        .put("success", false)
                        .put("message", "Ya has dado no me gusta a este artículo"));
                }
                
                Logger.warn("No se pudo registrar el no me gusta por razones desconocidas");
                return badRequest(Json.newObject()
                    .put("success", false)
                    .put("message", "No se pudo registrar el no me gusta. Intenta nuevamente"));
            }
        } catch (SQLException e) {
            Logger.error("Error de base de datos al registrar no me gusta", e);
            return internalServerError(Json.newObject()
                .put("success", false)
                .put("message", "Error en la base de datos"));
        } catch (Exception e) {
            Logger.error("Error inesperado al registrar no me gusta", e);
            return internalServerError(Json.newObject()
                .put("success", false)
                .put("message", "Error interno del servidor"));
        }
    }
}