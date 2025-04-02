package controllers;

import play.mvc.*;
import play.mvc.Http.*;
import services.JwtService;

import javax.inject.Inject;

public class Secured extends Security.Authenticator {
    private final JwtService jwtService;

    @Inject
    public Secured(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String getUsername(Context ctx) {
        // Forma compatible con Play antiguo
        Cookie jwtCookie = ctx.request().cookie("jwt");
        if (jwtCookie == null) {
            return null;
        }
        
        String token = jwtCookie.value();
        if (!jwtService.validarToken(token)) {
            return null;
        }
        return jwtService.obtenerSubject(token);
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return unauthorized("Acceso no autorizado");
    }
}