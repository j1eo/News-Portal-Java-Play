package controllers;

import play.mvc.*;
import play.data.*;
import services.AuthService;

import java.sql.SQLException;

import javax.inject.Inject;
import views.html.*;

public class AuthController extends Controller {
    private final FormFactory formFactory;
    private final AuthService authService;

    @Inject
    public AuthController(FormFactory formFactory, AuthService authService) {
        this.formFactory = formFactory;
        this.authService = authService;
    }

    public Result login() {
        return ok(login.render("UR NEWS"));
    }

    public Result register() {
        return ok(register.render("UR NEWS"));
    }

    public Result autenticar() {
        DynamicForm form = formFactory.form().bindFromRequest();
        String username = form.get("username");
        String password = form.get("password");

        try {
            String nickname = authService.autenticarUsuario(username, password);
            if (nickname != null) {
                session().put("nickname", nickname);
                flash("success", "Bienvenido " + nickname);
                return redirect(routes.HomeController.index());
            }
            flash("error", "Usuario o contraseña incorrectos");
        } catch (SQLException e) {
            flash("error", "Error en la base de datos");
        }
        return redirect(routes.AuthController.login());
    }

    public Result registrarUsuario() {
        DynamicForm form = formFactory.form().bindFromRequest();
        
        try {
            boolean registroExitoso = authService.registrarUsuario(
                form.get("nombre"),
                form.get("apellidos"),
                form.get("nickname"),
                form.get("email"),
                form.get("password")
            );
            
            if (registroExitoso) {
                flash("success", "Registro exitoso. Por favor inicia sesión");
                return redirect(routes.AuthController.login());
            }
            flash("error", "El usuario o email ya existen");
        } catch (SQLException e) {
            flash("error", "Error en el registro");
        }
        return redirect(routes.AuthController.register());
    }

    public Result logout() {
        session().clear();
        flash("info", "Sesión cerrada correctamente");
        return redirect(routes.AuthController.login());
    }
    public Result crearUsuarioPrueba() {
        authService.crearUsuarioPrueba();
        return ok("Usuario de prueba creado. Verifica la base de datos.");
    }

}