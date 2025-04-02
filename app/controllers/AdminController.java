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
                "Gestión de Usuarios",
                request()
            ));
        } catch (Exception e) {
            flash("error", "Error al cargar usuarios: " + e.getMessage());
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
}