package controllers;

import play.mvc.*;
import services.NoticiaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import models.Noticia;

public class NoticiasController extends Controller {

    private final NoticiaService noticiaService;

    public NoticiasController() {
        this.noticiaService = new NoticiaService();
    }

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
