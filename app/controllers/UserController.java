package controllers;

import play.mvc.*;
import play.data.*;
import services.*;
import models.*;
import javax.inject.Inject;
import views.html.*;
import java.util.List;
import java.net.URL;
import play.Logger;
import play.filters.csrf.CSRF;

public class UserController extends Controller {
    
    @Inject private JwtService jwtService;
    @Inject private AuthService authService;
    @Inject private ArticuloService articuloService;
    @Inject private FormFactory formFactory;

    // Método dashboard
    public Result dashboard() {
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            flash("error", "Debes iniciar sesión");
            return redirect(routes.AuthController.login());
        }

        try {
            String token = jwtCookie.value();
            String rol = jwtService.obtenerUserRole(token);
            
            if (!"user".equals(rol)) {
                flash("error", "Área solo para usuarios");
                return redirect(routes.AuthController.login());
            }

            String userId = jwtService.obtenerSubject(token);
            Usuario usuario = (Usuario) authService.getCuentaById(Integer.parseInt(userId));
            List<Articulo> articulosPublicados = articuloService.obtenerArticulos();
            
            return ok(views.html.user.dashboard.render(
                usuario, 
                articulosPublicados,
                "UR NEWS", 
                request()
            ));

        } catch (Exception e) {
            flash("error", "Error al cargar datos");
            return redirect(routes.AuthController.login());
        }
    }

    // Método miPerfil
    public Result miPerfil() {
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            return redirect(routes.AuthController.login());
        }
        
        try {
            String token = jwtCookie.value();
            String userId = jwtService.obtenerSubject(token);
            Usuario usuario = (Usuario) authService.getCuentaById(Integer.parseInt(userId));
            List<Articulo> articulosUsuario = articuloService.obtenerArticulosPorUsuario(usuario.getIdUsuario());
            
            return ok(views.html.user.dashboard.render(
                usuario,
                articulosUsuario,
                "UR NEWS",
                request()
            ));
        } catch (Exception e) {
            return redirect(routes.AuthController.login());
        }
    }

    // Método mostrarFormularioEdicion
    public Result mostrarFormularioEdicion() {
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            flash("error", "Debes iniciar sesión");
            return redirect(routes.AuthController.login());
        }

        try {
            String token = jwtCookie.value();
            String userId = jwtService.obtenerSubject(token);
            Usuario usuario = (Usuario) authService.getCuentaById(Integer.parseInt(userId));
            
            return ok(views.html.user.editProfile.render(
                usuario,
                "UR NEWS",
                request()
            ));

        } catch (Exception e) {
            flash("error", "Error al cargar formulario");
            return redirect(routes.UserController.dashboard());
        }
    }

    public Result actualizarPerfil() {
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            flash("error", "Sesión expirada");
            return redirect(routes.AuthController.login());
        }

        try {
            String token = jwtCookie.value();
            String userId = jwtService.obtenerSubject(token);
            Usuario usuario = (Usuario) authService.getCuentaById(Integer.parseInt(userId));
            
            DynamicForm form = formFactory.form().bindFromRequest();
            
            // Validación de email único
            String nuevoEmail = form.get("email");
            if (!usuario.getEmail().equals(nuevoEmail) && authService.existeEmail(nuevoEmail, usuario.getIdCuenta())) {
                flash("error", "El email ya está registrado");
                return redirect(routes.UserController.mostrarFormularioEdicion());
            }

            // Actualizar campos
            usuario.setNombre(form.get("nombre"));
            usuario.setApellidos(form.get("apellidos"));
            usuario.setEmail(nuevoEmail);
            
            // Actualizar foto si se proporcionó URL
            String urlImagen = form.get("urlImagen");
            if (urlImagen != null && !urlImagen.isEmpty()) {
                usuario.setFotoPerfil(urlImagen);
            }

            if (authService.actualizarCuenta(usuario)) {
                flash("success", "Perfil actualizado");
                return redirect(routes.UserController.miPerfil());
            } else {
                throw new Exception("Error al guardar cambios");
            }

        } catch (Exception e) {
            flash("error", "Error: " + e.getMessage());
            return redirect(routes.UserController.mostrarFormularioEdicion());
        }
    }
}