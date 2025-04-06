package controllers;

import play.mvc.*;
import play.data.*;
import services.AuthService;
import services.JwtService;
import java.sql.SQLException;
import javax.inject.Inject;
import views.html.*;
import models.*;

public class AuthController extends Controller {
    private final FormFactory formFactory;
    private final AuthService authService;
    private final JwtService jwtService;

    @Inject
    public AuthController(FormFactory formFactory, AuthService authService, JwtService jwtService) {
        this.formFactory = formFactory;
        this.authService = authService;
        this.jwtService = jwtService;
    }

    // Métodos públicos
    public Result login() {
        String success = flash().get("success");
        String error = flash().get("error");
        return ok(login.render("UR NEWS", success, error));
    }

    public Result register() {
        return ok(register.render("UR NEWS"));
    }

    public Result autenticar() {
        DynamicForm form = formFactory.form().bindFromRequest();
        String username = form.get("username");
        String password = form.get("password");

        try {
            Cuenta cuenta = authService.autenticarUsuario(username, password);
            if (cuenta != null) {
                String ip = request().remoteAddress();
                authService.registrarIntentoLogin(cuenta.getIdCuenta(), ip);
                
                String token = jwtService.generarToken(
                    String.valueOf(cuenta.getIdCuenta()),
                    (cuenta instanceof Admin) ? "admin" : "user"
                );
                
                response().setCookie(
                    Http.Cookie.builder("jwt", token)
                        .withHttpOnly(true)
                        .withSecure(request().secure())
                        .withPath("/")
                        .build()
                );
                
                if (cuenta instanceof Admin) {
                    return redirect(routes.AdminController.dashboard());
                } else {
                    // Cambio clave: Redirigir al dashboard del usuario
                    return redirect(routes.UserController.dashboard());
                }
            }
            flash("error", "Credenciales incorrectas");
        } catch (Exception e) {
            flash("error", "Error en la autenticación");
        }
        return redirect(routes.AuthController.login());
    }

    public Result registrarUsuario() {
        DynamicForm form = formFactory.form().bindFromRequest();
        try {
            Cuenta cuenta = authService.registrarCuentaCompleta(
                form.get("nombre"),
                form.get("apellidos"),
                form.get("nickname"),
                form.get("email"),
                form.get("password"),
                false,
                "BASICA"
            );
            if (cuenta != null) {
                flash("success", "Registro exitoso");
                return redirect(routes.AuthController.login());
            }
        } catch (SQLException e) {
            flash("error", e.getMessage());
        }
        return redirect(routes.AuthController.register());
    }

    public Result logout() {
        // 1. Invalidar la cookie JWT (3 métodos de respaldo)
        response().discardCookie("jwt", "/"); // Método principal
        
        // Refuerzo para navegadores problemáticos
        response().setCookie(
            Http.Cookie.builder("jwt", "invalid")
                .withPath("/")
                .withMaxAge(0)
                .withHttpOnly(true)
                .withSecure(request().secure())
                .build()
        );
        
        // 2. Headers anti-caché
        response().setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response().setHeader("Pragma", "no-cache");
        response().setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
        
        // 3. Invalidar sesión del lado del servidor
        session().clear();
        
        // 4. Redirección con parámetro aleatorio para evitar caché
        return redirect(routes.HomeController.index().url() + "?nocache=" + System.currentTimeMillis());
    }
    // Métodos protegidos para herencia
    protected Cuenta getCurrentAccount() {
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) return null;
        
        try {
            String token = jwtCookie.value();
            if (!jwtService.validarToken(token)) return null;
            
            String idCuenta = jwtService.obtenerSubject(token);
            return authService.getCuentaById(Integer.parseInt(idCuenta));
        } catch (Exception e) {
            return null;
        }
    }

    protected boolean isAdmin() {
        Cuenta cuenta = getCurrentAccount();
        return cuenta != null && cuenta instanceof Admin;
    }

    protected Result redirectToLogin() {
        flash("error", "Debes iniciar sesión");
        return redirect(routes.AuthController.login());
    }
    public Result crearUsuarioPrueba() {
        try {
            authService.crearUsuariosDePrueba();
            flash("success", "Usuarios de prueba creados");
            return redirect(routes.AuthController.login());
        } catch (SQLException e) {
            flash("error", "Error al crear usuarios: " + e.getMessage());
            return redirect(routes.AuthController.register());
        }
    }
}