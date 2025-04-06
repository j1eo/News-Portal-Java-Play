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
import java.util.Map;

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
                flash("error", "Debes iniciar sesión");
                return redirect(routes.AuthController.login());
            }

            // Obtener rol del usuario
            String userRole = jwtService.obtenerUserRole(request().cookie("jwt").value());
            
            // Procesar el formulario
            Http.RequestBody body = request().body();
            Map<String, String[]> formData = body.asFormUrlEncoded();

            NoticiaPropia noticia = new NoticiaPropia();
            noticia.setIdUsuario(usuario.getIdUsuario());
            noticia.setTitulo(formData.get("titulo")[0]);
            noticia.setAutor(formData.containsKey("autor") ? formData.get("autor")[0] : usuario.getNombre());
            noticia.setContenido(formData.get("contenido")[0]);
            noticia.setImagen(formData.containsKey("imagen") ? formData.get("imagen")[0] : null);
            noticia.setEstado(formData.get("estado")[0]);
            noticia.setCategoria(formData.get("categoria")[0]);
            noticia.setUrl(formData.containsKey("url") ? formData.get("url")[0] : null);
            noticia.setFuente(formData.containsKey("fuente") ? formData.get("fuente")[0] : null);
            noticia.setDescripcion(formData.containsKey("descripcion") ? formData.get("descripcion")[0] : null);
            noticia.setMeGusta(0);
            noticia.setNoMeGusta(0);

            boolean creado = noticiasService.agregarNoticia(noticia);

            if (creado) {
                flash("success", "Noticia creada exitosamente");
                // Redirección diferente para admin
                if ("admin".equals(userRole)) {
                    return redirect(routes.AdminController.gestionarNoticias());
                }
                return redirect(routes.NoticiasPropiasController.listarNoticias());
            } else {
                flash("error", "Error al crear la noticia");
                return redirect(routes.NoticiasPropiasController.mostrarFormularioCreacion());
            }
            
        } catch (Exception e) {
            Logger.error("Error al crear noticia", e);
            flash("error", "Error: " + e.getMessage());
            return badRequest();
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
    public Result verificarRol() {
        try {
            Http.Cookie jwtCookie = request().cookie("jwt");
            if (jwtCookie == null) {
                return unauthorized();
            }
            
            String role = jwtService.obtenerUserRole(jwtCookie.value());
            return ok(Json.newObject().put("role", role));
        } catch (Exception e) {
            return internalServerError();
        }
    }
    
    @Security.Authenticated(Secured.class)
    public Result darLikeNoticiaPropia(int idNoticia) {
        try {
            Usuario usuario = obtenerUsuarioCompleto();
            if (usuario == null) {
                Logger.error("Intento de like sin usuario autenticado");
                return unauthorized(Json.newObject()
                    .put("success", false)
                    .put("message", "Debes iniciar sesión para esta acción"));
            }

            Logger.debug("Intentando registrar like - Noticia: {}, Usuario: {}", idNoticia, usuario.getIdUsuario());
            
            // Verificar si es el autor de la noticia
            NoticiaPropia noticia = noticiasService.obtenerNoticiaPorId(idNoticia);
            if (noticia != null && noticia.getIdUsuario() == usuario.getIdUsuario()) {
                return badRequest(Json.newObject()
                    .put("success", false)
                    .put("message", "No puedes dar like a tu propia noticia"));
            }

            boolean exito = noticiasService.darLikeNoticia(idNoticia, usuario.getIdUsuario());
            
            if (exito) {
                NoticiaPropia noticiaActualizada = noticiasService.obtenerNoticiaPorIdConLikes(idNoticia, usuario.getIdUsuario());
                return ok(Json.newObject()
                    .put("success", true)
                    .put("message", "Like registrado")
                    .put("nuevoTotalLikes", noticiaActualizada.getMeGusta())
                    .put("nuevoTotalDislikes", noticiaActualizada.getNoMeGusta())
                    .put("usuarioDioLike", noticiaActualizada.isUsuarioDioLike())
                    .put("usuarioDioNoMeGusta", noticiaActualizada.isUsuarioDioNoMeGusta()));
            } else {
                // Verificar si ya existe un like
                boolean yaDioLike = noticiasService.obtenerNoticiaPorIdConLikes(idNoticia, usuario.getIdUsuario())
                    .isUsuarioDioLike();
                    
                if (yaDioLike) {
                    return badRequest(Json.newObject()
                        .put("success", false)
                        .put("message", "Ya has dado like a esta noticia"));
                }
                
                return badRequest(Json.newObject()
                    .put("success", false)
                    .put("message", "No se pudo registrar el like"));
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
    @Security.Authenticated(Secured.class)
    public Result darNoMeGustaNoticiaPropia(int idNoticia) {
        try {
            Usuario usuario = obtenerUsuarioCompleto();
            if (usuario == null) {
                Logger.error("Intento de dislike sin usuario autenticado");
                return unauthorized(Json.newObject()
                    .put("success", false)
                    .put("message", "Debes iniciar sesión para esta acción"));
            }

            Logger.debug("Intentando registrar dislike - Noticia: {}, Usuario: {}", idNoticia, usuario.getIdUsuario());
            
            // Verificar si es el autor de la noticia
            NoticiaPropia noticia = noticiasService.obtenerNoticiaPorId(idNoticia);
            if (noticia != null && noticia.getIdUsuario() == usuario.getIdUsuario()) {
                return badRequest(Json.newObject()
                    .put("success", false)
                    .put("message", "No puedes dar dislike a tu propia noticia"));
            }

            boolean exito = noticiasService.darNoMeGustaNoticia(idNoticia, usuario.getIdUsuario());
            
            if (exito) {
                NoticiaPropia noticiaActualizada = noticiasService.obtenerNoticiaPorIdConLikes(idNoticia, usuario.getIdUsuario());
                return ok(Json.newObject()
                    .put("success", true)
                    .put("message", "Dislike registrado")
                    .put("nuevoTotalLikes", noticiaActualizada.getMeGusta())
                    .put("nuevoTotalDislikes", noticiaActualizada.getNoMeGusta())
                    .put("usuarioDioLike", noticiaActualizada.isUsuarioDioLike())
                    .put("usuarioDioNoMeGusta", noticiaActualizada.isUsuarioDioNoMeGusta()));
            } else {
                // Verificar si ya existe un dislike
                boolean yaDioDislike = noticiasService.obtenerNoticiaPorIdConLikes(idNoticia, usuario.getIdUsuario())
                    .isUsuarioDioNoMeGusta();
                    
                if (yaDioDislike) {
                    return badRequest(Json.newObject()
                        .put("success", false)
                        .put("message", "Ya has dado dislike a esta noticia"));
                }
                
                return badRequest(Json.newObject()
                    .put("success", false)
                    .put("message", "No se pudo registrar el dislike"));
            }
        } catch (SQLException e) {
            Logger.error("Error de base de datos al registrar dislike", e);
            return internalServerError(Json.newObject()
                .put("success", false)
                .put("message", "Error en la base de datos"));
        } catch (Exception e) {
            Logger.error("Error inesperado al registrar dislike", e);
            return internalServerError(Json.newObject()
                .put("success", false)
                .put("message", "Error interno del servidor"));
        }
    }
    
}