package controllers;

import play.mvc.*;
import services.NoticiaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import models.Noticia;
import javax.inject.Inject;

public class NoticiasController extends Controller {

    private final NoticiaService noticiaService;

    @Inject  // Ahora usa inyección de dependencias
    public NoticiasController(NoticiaService noticiaService) {
        this.noticiaService = noticiaService;
    }

    // Endpoint JSON original para API
    public Result obtenerNoticias() {
        try {
            List<Noticia> noticias = noticiaService.obtenerNoticias();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.valueToTree(noticias);
            return ok(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError("Error al obtener noticias");
        }
    }
}