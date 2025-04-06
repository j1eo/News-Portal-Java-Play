package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import services.*;
import play.mvc.*;
import play.Logger;
import play.libs.Json;
import play.data.*;
import javax.inject.Inject;
import java.sql.SQLException;
import java.util.*;

public class ComentariosController extends Controller {
    
    private final ComentarioService comentarioService;
    private final AuthService authService;
    private final JwtService jwtService;
    
    @Inject
    public ComentariosController(ComentarioService comentarioService, 
                               AuthService authService,
                               JwtService jwtService) {
        this.comentarioService = comentarioService;
        this.authService = authService;
        this.jwtService = jwtService;
    }
    
    /**
     * Obtiene los comentarios PUBLICADOS de un artículo
     */
    public Result obtenerComentarios(int idArticulo) {
        try {
            List<Comentario> comentarios = comentarioService.obtenerComentariosDeArticulo(idArticulo);
            
            List<Map<String, Object>> response = new ArrayList<>();
            for (Comentario c : comentarios) {
                Map<String, Object> comentarioMap = new HashMap<>();
                comentarioMap.put("id", c.getIdComentario());
                comentarioMap.put("contenido", c.getContenido());
                comentarioMap.put("fecha", c.getFechaCreacion().getTime());
                
                Map<String, Object> usuarioMap = new HashMap<>();
                usuarioMap.put("id", c.getUsuario().getIdUsuario());
                usuarioMap.put("nombre", c.getUsuario().getNombre());
                usuarioMap.put("foto", c.getUsuario().getFotoPerfil());
                
                comentarioMap.put("usuario", usuarioMap);
                response.add(comentarioMap);
            }
            
            return ok(Json.toJson(response));
        } catch (SQLException e) {
            Logger.error("Error al obtener comentarios para artículo " + idArticulo, e);
            return internalServerError(Json.newObject()
                .put("error", "Error al obtener comentarios")
                .put("detalles", e.getMessage()));
        }
    }

    @Security.Authenticated(Secured.class)
    public Result agregarComentario(int idArticulo) {
        try {
            // 1. Verificar token JWT y obtener ID de cuenta
            Http.Cookie jwtCookie = request().cookie("jwt");
            if (jwtCookie == null) {
                return unauthorized(Json.newObject()
                    .put("error", "No se encontró el token de autenticación")
                    .put("code", "missing_token"));
            }

            String token = jwtCookie.value();
            int idCuenta;
            try {
                idCuenta = Integer.parseInt(jwtService.obtenerSubject(token));
            } catch (NumberFormatException e) {
                return unauthorized(Json.newObject()
                    .put("error", "Token inválido")
                    .put("code", "invalid_token"));
            }

            // 2. Validar cuerpo de la solicitud
            JsonNode json = request().body().asJson();
            if (json == null) {
                return badRequest(Json.newObject()
                    .put("error", "Se esperaba un cuerpo JSON")
                    .put("code", "invalid_request"));
            }

            if (!json.has("contenido")) {
                return badRequest(Json.newObject()
                    .put("error", "El campo 'contenido' es requerido")
                    .put("code", "missing_content"));
            }

            String contenido = json.get("contenido").asText().trim();
            if (contenido.isEmpty()) {
                return badRequest(Json.newObject()
                    .put("error", "El comentario no puede estar vacío")
                    .put("code", "empty_content"));
            }

            if (contenido.length() > 500) {
                return badRequest(Json.newObject()
                    .put("error", "El comentario no puede exceder los 500 caracteres")
                    .put("code", "content_too_long"));
            }

            // 3. Intentar agregar el comentario
            boolean exito = comentarioService.agregarComentarioArticulo(idArticulo, idCuenta, contenido);

            if (exito) {
                return ok(Json.newObject()
                    .put("success", true)
                    .put("message", "Comentario agregado exitosamente"));
            } else {
                return internalServerError(Json.newObject()
                    .put("error", "No se pudo agregar el comentario")
                    .put("code", "database_error"));
            }

        } catch (SQLException e) {
            Logger.error("Error de base de datos al agregar comentario", e);
            
            if (e.getMessage().contains("foreign key constraint fails")) {
                return badRequest(Json.newObject()
                    .put("error", "Usuario no encontrado")
                    .put("code", "user_not_found")
                    .put("details", "La cuenta no está asociada a un usuario válido"));
            }

            return internalServerError(Json.newObject()
                .put("error", "Error de base de datos")
                .put("code", "database_error")
                .put("details", e.getMessage()));

        } catch (Exception e) {
            Logger.error("Error inesperado al agregar comentario", e);
            return internalServerError(Json.newObject()
                .put("error", "Error interno del servidor")
                .put("code", "server_error")
                .put("details", e.getMessage()));
        }
    }
    
    @Security.Authenticated(Secured.class)
    public Result eliminarComentario(int idComentario) {
        try {
            // Verificar autenticación y permisos
            String token = request().cookie("jwt").value();
            int idUsuario = Integer.parseInt(jwtService.obtenerSubject(token));
            
            if (!comentarioService.puedeEliminarComentario(idComentario, idUsuario)) {
                return forbidden(Json.newObject()
                    .put("error", "No tienes permiso para eliminar este comentario"));
            }
            
            // Eliminar (marcar como ELIMINADO) el comentario
            boolean exito = comentarioService.eliminarComentario(idComentario);
            
            if (exito) {
                return ok(Json.newObject()
                    .put("success", true)
                    .put("message", "Comentario eliminado"));
            } else {
                return notFound(Json.newObject()
                    .put("error", "Comentario no encontrado"));
            }
        } catch (NumberFormatException e) {
            return unauthorized(Json.newObject()
                .put("error", "Usuario no autenticado"));
        } catch (SQLException e) {
            Logger.error("Error al eliminar comentario " + idComentario, e);
            return internalServerError(Json.newObject()
                .put("error", "Error al eliminar comentario")
                .put("detalles", e.getMessage()));
        }
    }
 // En ComentariosController.java

    /**
     * Obtiene los comentarios PUBLICADOS de una noticia
     */
    public Result obtenerComentariosNoticia(int idNoticia) {
        try {
            List<Comentario> comentarios = comentarioService.obtenerComentariosDeNoticia(idNoticia);
            
            List<Map<String, Object>> response = new ArrayList<>();
            for (Comentario c : comentarios) {
                Map<String, Object> comentarioMap = new HashMap<>();
                comentarioMap.put("id", c.getIdComentario());
                comentarioMap.put("contenido", c.getContenido());
                comentarioMap.put("fecha", c.getFechaCreacion().getTime());
                
                Map<String, Object> usuarioMap = new HashMap<>();
                usuarioMap.put("id", c.getUsuario().getIdUsuario());
                usuarioMap.put("nombre", c.getUsuario().getNombre());
                usuarioMap.put("foto", c.getUsuario().getFotoPerfil());
                
                comentarioMap.put("usuario", usuarioMap);
                response.add(comentarioMap);
            }
            
            return ok(Json.toJson(response))
                .as("application/json; charset=utf-8");
                
        } catch (Exception e) {
            Logger.error("Error al obtener comentarios para noticia " + idNoticia, e);
            return internalServerError(Json.newObject()
                .put("error", "Error al obtener comentarios")
                .put("details", e.getMessage()))
                .as("application/json; charset=utf-8");
        }
    }
    @Security.Authenticated(Secured.class)
    public Result agregarComentarioNoticia(int idNoticia) {
        try {
            // 1. Verificar token JWT y obtener ID de cuenta
            Http.Cookie jwtCookie = request().cookie("jwt");
            if (jwtCookie == null) {
                return unauthorized(Json.newObject()
                    .put("error", "No se encontró el token de autenticación")
                    .put("code", "missing_token"));
            }

            String token = jwtCookie.value();
            int idCuenta;
            try {
                idCuenta = Integer.parseInt(jwtService.obtenerSubject(token));
            } catch (NumberFormatException e) {
                return unauthorized(Json.newObject()
                    .put("error", "Token inválido")
                    .put("code", "invalid_token"));
            }

            // 2. Validar cuerpo de la solicitud
            JsonNode json = request().body().asJson();
            if (json == null) {
                return badRequest(Json.newObject()
                    .put("error", "Se esperaba un cuerpo JSON")
                    .put("code", "invalid_request"));
            }

            if (!json.has("contenido")) {
                return badRequest(Json.newObject()
                    .put("error", "El campo 'contenido' es requerido")
                    .put("code", "missing_content"));
            }

            String contenido = json.get("contenido").asText().trim();
            if (contenido.isEmpty()) {
                return badRequest(Json.newObject()
                    .put("error", "El comentario no puede estar vacío")
                    .put("code", "empty_content"));
            }

            if (contenido.length() > 500) {
                return badRequest(Json.newObject()
                    .put("error", "El comentario no puede exceder los 500 caracteres")
                    .put("code", "content_too_long"));
            }

            // 3. Intentar agregar el comentario
            boolean exito = comentarioService.agregarComentarioNoticia(idNoticia, idCuenta, contenido);

            if (exito) {
                return ok(Json.newObject()
                    .put("success", true)
                    .put("message", "Comentario agregado exitosamente"));
            } else {
                return internalServerError(Json.newObject()
                    .put("error", "No se pudo agregar el comentario")
                    .put("code", "database_error"));
            }

        } catch (SQLException e) {
            Logger.error("Error de base de datos al agregar comentario a noticia", e);
            
            if (e.getMessage().contains("foreign key constraint fails")) {
                return badRequest(Json.newObject()
                    .put("error", "Usuario no encontrado")
                    .put("code", "user_not_found")
                    .put("details", "La cuenta no está asociada a un usuario válido"));
            }

            return internalServerError(Json.newObject()
                .put("error", "Error de base de datos")
                .put("code", "database_error")
                .put("details", e.getMessage()));

        } catch (Exception e) {
            Logger.error("Error inesperado al agregar comentario a noticia", e);
            return internalServerError(Json.newObject()
                .put("error", "Error interno del servidor")
                .put("code", "server_error")
                .put("details", e.getMessage()));
        }
    }

    @Security.Authenticated(Secured.class)
    public Result eliminarComentarioNoticia(int idComentario) {
        try {
            // Verificar autenticación y permisos
            String token = request().cookie("jwt").value();
            int idUsuario = Integer.parseInt(jwtService.obtenerSubject(token));
            
            if (!comentarioService.puedeEliminarComentarioNoticia(idComentario, idUsuario)) {
                return forbidden(Json.newObject()
                    .put("error", "No tienes permiso para eliminar este comentario"));
            }
            
            // Eliminar (marcar como ELIMINADO) el comentario
            boolean exito = comentarioService.eliminarComentarioNoticia(idComentario);
            
            if (exito) {
                return ok(Json.newObject()
                    .put("success", true)
                    .put("message", "Comentario eliminado"));
            } else {
                return notFound(Json.newObject()
                    .put("error", "Comentario no encontrado"));
            }
        } catch (NumberFormatException e) {
            return unauthorized(Json.newObject()
                .put("error", "Usuario no autenticado"));
        } catch (SQLException e) {
            Logger.error("Error al eliminar comentario de noticia " + idComentario, e);
            return internalServerError(Json.newObject()
                .put("error", "Error al eliminar comentario")
                .put("detalles", e.getMessage()));
        }
    }
}