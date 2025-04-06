package controllers;

import play.mvc.*;
import services.*;
import models.*;
import javax.inject.Inject;
import views.html.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import play.libs.Json; // Importación añadida
import play.data.DynamicForm;
import play.data.FormFactory;

public class AdminController extends Controller {
    
    @Inject private JwtService jwtService;
    @Inject private AuthService authService;
    @Inject private AdminService adminService;
    @Inject private FormFactory formFactory;
    @Inject private ArticuloService articuloService;
    @Inject private NoticiasPropiasService noticiasPropiasService;


    private boolean isAdmin() {
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) return false;
        try {
            return "admin".equals(jwtService.obtenerUserRole(jwtCookie.value()));
        } catch (Exception e) {
            return false;
        }
    }

    public Result dashboard() {
        if (!isAdmin()) {
            flash("error", "Acceso denegado. Requiere permisos de administrador");
            return redirect(routes.AuthController.login());
        }

        try {
            String userId = jwtService.obtenerSubject(request().cookie("jwt").value());
            Admin admin = (Admin) authService.getCuentaById(Integer.parseInt(userId));
            
            Map<String, Integer> stats = new HashMap<>();
            stats.put("usuarios", adminService.contarUsuarios());
            stats.put("articulos", adminService.contarArticulos());
            stats.put("noticias", adminService.contarNoticias());
            
            return ok(views.html.adminviews.admindashboard.render(
                admin,
                stats,
                "UR NEWS ADMIN",
                request()
            ));
        } catch (Exception e) {
            flash("error", "Error al cargar el panel: " + e.getMessage());
            return redirect(routes.AuthController.login());
        }
    }

    public Result obtenerUsuario(Long id) {
        if (!isAdmin()) return unauthorized();
        
        try {
            Usuario usuario = adminService.obtenerUsuarioPorId(id);
            if (usuario == null) return notFound();
            return ok(Json.toJson(usuario));
        } catch (Exception e) {
            return internalServerError("Error: " + e.getMessage());
        }
    }

    public Result gestionarUsuarios() {
        if (!isAdmin()) return redirect(routes.AuthController.login());
        
        try {
            List<Usuario> usuarios = adminService.obtenerTodosUsuarios();
            return ok(views.html.adminviews.datosusuarios.render(
                usuarios,
                "UR NEWS ADMIN",
                request()
            ));
        } catch (Exception e) {
            flash("error", "Error al cargar usuarios: " + e.getMessage());
            return redirect(routes.AdminController.dashboard());
        }
    }
    public Result gestionarArticulos() {
        if (!isAdmin()) return redirect(routes.AuthController.login());
        
        try {
            List<Articulo> articulos = articuloService.obtenerArticulos();
            return ok(views.html.adminviews.datosarticulos.render(
                articulos,
                "UR NEWS ADMIN",  
                request()
            ));
        } catch (Exception e) {
            flash("error", "Error al cargar artículos: " + e.getMessage());
            return redirect(routes.AdminController.dashboard());
        }
    }
    public Result gestionarNoticias() {
        if (!isAdmin()) return redirect(routes.AuthController.login());
        
        try {
            List<NoticiaPropia> noticias = noticiasPropiasService.obtenerNoticias();
            return ok(views.html.adminviews.datosnoticias.render(
                noticias,
                "UR NEWS ADMIN",  
                request()
            ));
        } catch (Exception e) {
            flash("error", "Error al cargar artículos: " + e.getMessage());
            return redirect(routes.AdminController.dashboard());
        }
    }
  public Result actualizarUsuario(Long id) {
        if (!isAdmin()) return unauthorized();
        
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            Usuario usuario = adminService.obtenerUsuarioPorId(id);
            
            if (usuario == null) return notFound("Usuario no encontrado");
            
            usuario.setNombre(form.get("nombre"));
            usuario.setApellidos(form.get("apellidos"));
            usuario.setEmail(form.get("email"));
            usuario.setSuscripcion(form.get("suscripcion"));
            
            adminService.actualizarUsuario(usuario);
            return ok("Usuario actualizado");
        } catch (Exception e) {
            return badRequest("Error: " + e.getMessage());
        }
    }

    public Result eliminarUsuario(Long id) {
        if (!isAdmin()) return unauthorized();
        
        try {
            adminService.eliminarUsuario(id);
            return ok("Usuario eliminado");
        } catch (Exception e) {
            return badRequest("Error: " + e.getMessage());
        }
    }

    public Result cambiarEstadoArticulo(Long id, String estado) {
        if (!isAdmin()) return unauthorized();
        
        try {
            adminService.cambiarEstadoArticulo(id, estado);
            return ok("Estado actualizado");
        } catch (Exception e) {
            return badRequest("Error: " + e.getMessage());
        }
    }
 // Agregar estos métodos a la clase AdminController

    public Result obtenerArticulo(Long id) {
        if (!isAdmin()) return unauthorized();
        
        try {
            Articulo articulo = articuloService.obtenerArticuloPorId(id.intValue());
            if (articulo == null) return notFound();
            return ok(Json.toJson(articulo));
        } catch (Exception e) {
            return internalServerError("Error: " + e.getMessage());
        }
    }

    public Result crearArticulo() {
        if (!isAdmin()) return unauthorized();
        
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            
            // Obtener ID del usuario admin desde el JWT
            String userId = jwtService.obtenerSubject(request().cookie("jwt").value());
            
            boolean resultado = articuloService.agregarArticulo(
                Integer.parseInt(userId),
                form.get("titulo"),
                form.get("autor"),
                form.get("contenido"),
                form.get("imagen"),
                form.get("estado"),
                form.get("categoria"),
                0,  // meGusta inicial
                0   // noMeGusta inicial
            );
            
            if (resultado) {
                return ok("Artículo creado exitosamente");
            } else {
                return badRequest("Error al crear el artículo");
            }
        } catch (Exception e) {
            return badRequest("Error: " + e.getMessage());
        }
    }

    public Result actualizarArticulo(Long id) {
        if (!isAdmin()) return unauthorized();
        
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            
            Articulo articulo = articuloService.obtenerArticuloPorId(id.intValue());
            if (articulo == null) return notFound("Artículo no encontrado");
            
            // Actualizar campos del artículo
            articulo.setTitulo(form.get("titulo"));
            articulo.setAutor(form.get("autor"));
            articulo.setContenido(form.get("contenido"));
            articulo.setEstado(form.get("estado"));
            
            // Aquí deberías implementar un método actualizarArticulo en ArticuloService
            boolean resultado = articuloService.actualizarArticulo(articulo);
            
            if (resultado) {
                return ok("Artículo actualizado");
            } else {
                return badRequest("Error al actualizar el artículo");
            }
        } catch (Exception e) {
            return badRequest("Error: " + e.getMessage());
        }
    }

    public Result eliminarArticulo(Long id) {
        if (!isAdmin()) return unauthorized();
        
        try {
            boolean resultado = articuloService.eliminarArticulo(id.intValue());
            if (resultado) {
                return ok("Artículo eliminado");
            } else {
                return badRequest("Error al eliminar el artículo");
            }
        } catch (Exception e) {
            return badRequest("Error: " + e.getMessage());
        }
    }
 // Métodos para gestión de noticias
    public Result obtenerNoticia(Long id) {
        if (!isAdmin()) return unauthorized();
        
        try {
            NoticiaPropia noticia = noticiasPropiasService.obtenerNoticiaPorId(id.intValue());
            if (noticia == null) return notFound();
            return ok(Json.toJson(noticia));
        } catch (Exception e) {
            return internalServerError("Error: " + e.getMessage());
        }
    }

    public Result crearNoticia() {
        if (!isAdmin()) return unauthorized();
        
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            
            // Obtener ID del usuario admin desde el JWT
            String userId = jwtService.obtenerSubject(request().cookie("jwt").value());
            
            NoticiaPropia noticia = new NoticiaPropia();
            noticia.setIdUsuario(Integer.parseInt(userId));
            noticia.setTitulo(form.get("titulo"));
            noticia.setAutor(form.get("autor"));
            noticia.setUrl(form.get("url"));
            noticia.setFuente(form.get("fuente"));
            noticia.setDescripcion(form.get("descripcion"));
            noticia.setImagen(form.get("imagen"));
            noticia.setContenido(form.get("contenido"));
            noticia.setEstado(form.get("estado"));
            noticia.setCategoria(form.get("categoria"));
            noticia.setMeGusta(0);
            noticia.setNoMeGusta(0);
            
            boolean resultado = noticiasPropiasService.agregarNoticia(noticia);
            
            if (resultado) {
                return ok("Noticia creada exitosamente");
            } else {
                return badRequest("Error al crear la noticia");
            }
        } catch (Exception e) {
            return badRequest("Error: " + e.getMessage());
        }
    }

    public Result actualizarNoticia(Long id) {
        if (!isAdmin()) return unauthorized();
        
        try {
            DynamicForm form = formFactory.form().bindFromRequest();
            
            NoticiaPropia noticia = noticiasPropiasService.obtenerNoticiaPorId(id.intValue());
            if (noticia == null) return notFound("Noticia no encontrada");
            
            // Actualizar campos de la noticia
            noticia.setTitulo(form.get("titulo"));
            noticia.setAutor(form.get("autor"));
            noticia.setUrl(form.get("url"));
            noticia.setFuente(form.get("fuente"));
            noticia.setDescripcion(form.get("descripcion"));
            noticia.setImagen(form.get("imagen"));
            noticia.setContenido(form.get("contenido"));
            noticia.setEstado(form.get("estado"));
            noticia.setCategoria(form.get("categoria"));
            
            boolean resultado = noticiasPropiasService.actualizarNoticia(noticia);
            
            if (resultado) {
                return ok("Noticia actualizada");
            } else {
                return badRequest("Error al actualizar la noticia");
            }
        } catch (Exception e) {
            return badRequest("Error: " + e.getMessage());
        }
    }

    public Result eliminarNoticia(Long id) {
        if (!isAdmin()) return unauthorized();
        
        try {
            boolean resultado = noticiasPropiasService.eliminarNoticia(id.intValue());
            if (resultado) {
                return ok("Noticia eliminada");
            } else {
                return badRequest("Error al eliminar la noticia");
            }
        } catch (Exception e) {
            return badRequest("Error: " + e.getMessage());
        }
    }
}