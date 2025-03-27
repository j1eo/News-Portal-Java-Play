package controllers;
import views.html.*;
import play.mvc.*;

public class AuthController extends Controller {
    public Result login() {
        return ok(login.render("UR NEWS"));
    }
    public Result register() {
        return ok(register.render("UR NEWS"));
    }
}