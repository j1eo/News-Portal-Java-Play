package controllers;

import play.mvc.*;
import services.*;
import models.*;
import javax.inject.Inject;
import views.html.*;

public class UserController extends Controller {
    
    @Inject
    private JwtService jwtService;
    
    @Inject
    private AuthService authService;

    public Result dashboard() {
        // 1. Verificar cookie
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            flash("error", "Debes iniciar sesión");
            return redirect(routes.AuthController.login());
        }

        // 2. Validar token y rol
        try {
            String token = jwtCookie.value();
            String rol = jwtService.obtenerUserRole(token);
            
            if (!"user".equals(rol)) {
                flash("error", "Área solo para usuarios");
                return redirect(routes.AuthController.login());
            }

            // 3. Obtener datos del usuario
            String userId = jwtService.obtenerSubject(token);
            Usuario usuario = (Usuario) authService.getCuentaById(Integer.parseInt(userId));
            
            return ok(views.html.user.dashboard.render(usuario, "UR NEWS", request()));

        } catch (Exception e) {
            flash("error", "Error al cargar datos");
            return redirect(routes.AuthController.login());
        }
    }
    
    public Result miPerfil() {
        // 1. Verificar autenticación
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            return redirect(routes.AuthController.login());
        }
        
        // 2. Obtener usuario
        try {
            String token = jwtCookie.value();
            String userId = jwtService.obtenerSubject(token);
            Usuario usuario = (Usuario) authService.getCuentaById(Integer.parseInt(userId));
            
            // 3. Renderizar vista

            return ok(views.html.user.dashboard.render(usuario, "UR NEWS", request()));
        } catch (Exception e) {
            return redirect(routes.AuthController.login());
        }
    }


   
}