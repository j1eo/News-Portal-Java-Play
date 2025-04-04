package controllers;

import models.*;
import services.*;
import play.mvc.*;
import play.Logger;
import play.data.*;
import play.filters.csrf.RequireCSRFCheck;
import javax.inject.Inject;
import java.sql.SQLException;

public class ComentariosController extends Controller {
    private final ComentarioService comentarioService;
    private final JwtService jwtService;
    private final FormFactory formFactory;

    @Inject
    public ComentariosController(ComentarioService comentarioService, 
                               JwtService jwtService,
                               FormFactory formFactory) {
        this.comentarioService = comentarioService;
        this.jwtService = jwtService;
        this.formFactory = formFactory;
    }

    @Security.Authenticated(Secured.class)
    @RequireCSRFCheck
    public Result agregarComentarioArticulo() {
        return agregarComentario("ARTICULO");
    }

    @Security.Authenticated(Secured.class)
    @RequireCSRFCheck
    public Result agregarComentarioNoticia() {
        return agregarComentario("NOTICIA");
    }

    private Result agregarComentario(String tipo) {
        DynamicForm form = formFactory.form().bindFromRequest();
        
        try {
            int idContenido = Integer.parseInt(form.get("idContenido"));
            String contenido = form.get("contenido");
            
            if (contenido == null || contenido.trim().isEmpty()) {
                flash("error", "El comentario no puede estar vacío");
                return redirect(request().getHeader("referer"));
            }

            String token = request().cookie("jwt").value();
            int idUsuario = Integer.parseInt(jwtService.obtenerSubject(token));
            
            boolean resultado;
            if (tipo.equals("ARTICULO")) {
                resultado = comentarioService.agregarComentarioArticulo(idUsuario, idContenido, contenido.trim());
            } else {
                resultado = comentarioService.agregarComentarioNoticia(idUsuario, idContenido, contenido.trim());
            }
            
            if (resultado) {
                flash("success", "Comentario publicado exitosamente");
            } else {
                flash("error", "Error al publicar comentario");
            }
        } catch (NumberFormatException e) {
            flash("error", "ID de contenido inválido");
        } catch (SQLException e) {
            Logger.error("Error de base de datos al agregar comentario", e);
            flash("error", "Error técnico al guardar comentario");
        } catch (Exception e) {
            Logger.error("Error inesperado al agregar comentario", e);
            flash("error", "Error inesperado");
        }
        
        return redirect(request().getHeader("referer"));
    }
}