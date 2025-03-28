package controllers;

import play.mvc.*;
import services.*;
import views.html.*;
import models.*;
import javax.inject.Inject;

public class AdminController extends Controller {
    
    @Inject
    private JwtService jwtService;
    
    @Inject
    private AuthService authService;
    
    public Result admin() {
        return ok(admin.render("UR NEWS"));
    }

    public Result dashboard() {
        // Verificación manual del token y rol
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            flash("error", "No autorizado");
            return redirect(routes.AuthController.login());
        }

        try {
            String token = jwtCookie.value();
            String rol = jwtService.obtenerUserRole(token); // "admin" o "user"
            
            if (!"admin".equals(rol)) {
                flash("error", "Solo para administradores");
                return redirect(routes.UserController.dashboard());
            }

            // Si es admin, continuar...
            int totalUsuarios = authService.contarUsuarios();
            return ok(admin.render(String.valueOf(totalUsuarios)));

        } catch (Exception e) {
            flash("error", "Error de autenticación");
            return redirect(routes.AuthController.login());
        }
    }
}