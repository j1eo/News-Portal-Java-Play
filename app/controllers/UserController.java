package controllers;

import play.mvc.*;
import play.data.*;
import services.*;
import models.*;
import javax.inject.Inject;
import views.html.*;
import java.util.List;
import java.util.Collections;
import java.net.URL;
import java.sql.SQLException;

import play.Logger;
import play.filters.csrf.CSRF;

public class UserController extends Controller {
    
    @Inject private JwtService jwtService;
    @Inject private AuthService authService;
    @Inject private ArticuloService articuloService;
    @Inject private NoticiasPropiasService noticiasService;
    @Inject private FormFactory formFactory;
 // Método dashboard actualizado
    public Result dashboard() {
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            return redirect(routes.HomeController.index());
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
            
            // Obtener artículos y noticias como en HomeController
            List<Articulo> articulosPublicados = articuloService.obtenerArticulos();
            List<NoticiaPropia> noticiasRecientes = noticiasService.obtenerNoticias();
            
            NoticiaPropia noticiaPrincipal = null;
            List<NoticiaPropia> noticiasSecundarias = Collections.emptyList();
            
            if (!noticiasRecientes.isEmpty()) {
                noticiaPrincipal = noticiasRecientes.get(0);
                if (noticiasRecientes.size() > 2) {
                    noticiasSecundarias = noticiasRecientes.subList(1, 3);
                }
            }
            
            return ok(views.html.user.dashboard.render(
                usuario, 
                articulosPublicados,
                noticiaPrincipal,
                noticiasSecundarias,
                "UR NEWS", 
                request()
            ));

        } catch (Exception e) {
            flash("error", "Error al cargar datos");
            return redirect(routes.AuthController.login());
        }
    }

    // Método miPerfil actualizado
    public Result miPerfil() {
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            return redirect(routes.AuthController.login());
        }
        
        try {
            String token = jwtCookie.value();
            String userId = jwtService.obtenerSubject(token);
            Usuario usuario = (Usuario) authService.getCuentaById(Integer.parseInt(userId));
            
            // Obtener artículos del usuario y noticias como en HomeController
            List<Articulo> articulosUsuario = articuloService.obtenerArticulosPorUsuario(usuario.getIdUsuario());
            List<NoticiaPropia> noticiasRecientes = noticiasService.obtenerNoticias();
            
            NoticiaPropia noticiaPrincipal = null;
            List<NoticiaPropia> noticiasSecundarias = Collections.emptyList();
            
            if (!noticiasRecientes.isEmpty()) {
                noticiaPrincipal = noticiasRecientes.get(0);
                if (noticiasRecientes.size() > 2) {
                    noticiasSecundarias = noticiasRecientes.subList(1, 3);
                }
            }
            
            return ok(views.html.user.dashboard.render(
                usuario,
                articulosUsuario,
                noticiaPrincipal,
                noticiasSecundarias,
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
    public Result mostrarFormularioSuscripcion() {
    	 Http.Cookie jwtCookie = request().cookie("jwt");
         if (jwtCookie == null) {
             flash("error", "Debes iniciar sesión");
             return redirect(routes.AuthController.login());
         }

         try {
             String token = jwtCookie.value();
             String userId = jwtService.obtenerSubject(token);
             Usuario usuario = (Usuario) authService.getCuentaById(Integer.parseInt(userId));
             
             return ok(views.html.user.suscripcion.render(
                 usuario,
                 "UR NEWS",
                 request()
             ));

         } catch (Exception e) {
             flash("error", "Error al cargar formulario");
             return redirect(routes.UserController.dashboard());
         }
    }
    public Result actualizarSuscripcion() {
        // 1. Verificar sesión
        Http.Cookie jwtCookie = request().cookie("jwt");
        if (jwtCookie == null) {
            flash("error", "Debes iniciar sesión para cambiar tu suscripción");
            return redirect(routes.AuthController.login());
        }

        try {
            // 2. Obtener usuario actual
            String token = jwtCookie.value();
            String userId = jwtService.obtenerSubject(token);
            Usuario usuario = (Usuario) authService.getCuentaById(Integer.parseInt(userId));
            
            if (usuario == null) {
                flash("error", "Usuario no encontrado");
                return redirect(routes.AuthController.login());
            }

            // 3. Procesar formulario
            DynamicForm form = formFactory.form().bindFromRequest();
            String nuevaSuscripcion = form.get("suscripcion");

            // 4. Validar suscripción
            if (!"BASICA".equals(nuevaSuscripcion) && !"PREMIUM".equals(nuevaSuscripcion)) {
                flash("error", "Tipo de suscripción no válido");
                // Corrección aquí: Usar la vista correctamente
                return ok(views.html.user.suscripcion.render(usuario, "UR NEWS", request()));
            }

            // 5. Validar datos de pago para Premium
            if ("PREMIUM".equals(nuevaSuscripcion)) {
                if (!validarDatosPago(form)) {
                    // Corrección aquí también
                    return ok(views.html.user.suscripcion.render(usuario, "UR NEWS", request()));
                }
            }

            // 6. Actualizar suscripción
            String suscripcionActual = usuario.getSuscripcion();
            if (!nuevaSuscripcion.equals(suscripcionActual)) {
                usuario.setSuscripcion(nuevaSuscripcion);
                if (!authService.actualizarSuscripcion(usuario)) {
                    throw new RuntimeException("No se pudo actualizar la suscripción");
                }
            }

            // 7. Redirigir con mensaje de éxito
            flash("success", "Suscripción actualizada correctamente");
            return redirect(routes.UserController.dashboard());

        } catch (NumberFormatException e) {
            flash("error", "ID de usuario inválido");
            return redirect(routes.AuthController.login());
        } catch (SQLException e) {
            flash("error", "Error al conectar con la base de datos");
            return redirect(routes.UserController.mostrarFormularioSuscripcion());
        } catch (Exception e) {
            flash("error", "Error: " + e.getMessage());
            return redirect(routes.UserController.mostrarFormularioSuscripcion());
        }
    }

    // Método auxiliar para validar datos de pago
    private boolean validarDatosPago(DynamicForm form) {
        String cardNumber = form.get("cardNumber");
        String expiryDate = form.get("expiryDate");
        String cvv = form.get("cvv");
        String cardName = form.get("cardName");
        
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            flash("error", "Número de tarjeta inválido (16 dígitos requeridos)");
            return false;
        }
        
        if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            flash("error", "Formato de fecha inválido (MM/AA)");
            return false;
        }
        
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            flash("error", "CVV inválido (3-4 dígitos requeridos)");
            return false;
        }
        
        if (cardName == null || cardName.trim().isEmpty()) {
            flash("error", "Nombre en la tarjeta es requerido");
            return false;
        }
        
        return true;
    }
}