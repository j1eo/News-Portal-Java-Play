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

    @Inject
    public NoticiasController(NoticiaService noticiaService) {
        this.noticiaService = noticiaService;
    }

    // Endpoint para noticias de Milenio
    public Result obtenerNoticiasMilenio() {
        try {
            List<Noticia> noticias = noticiaService.obtenerNoticias();
            return convertirListaAJson(noticias);
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError("Error al obtener noticias de Milenio");
        }
    }

    // Endpoint para noticias de El País
    public Result obtenerNoticiasElPais() {
        try {
            List<Noticia> noticias = noticiaService.obtenerNoticiasElPais();
            return convertirListaAJson(noticias);
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError("Error al obtener noticias de El País");
        }
    }

    // Endpoint combinado (opcional)
    public Result obtenerNoticias() {
        try {
            List<Noticia> noticiasMilenio = noticiaService.obtenerNoticias();
            List<Noticia> noticiasElPais = noticiaService.obtenerNoticiasElPais();
            
            // Combinar ambas listas
            noticiasMilenio.addAll(noticiasElPais);
            
            return convertirListaAJson(noticiasMilenio);
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError("Error al obtener todas las noticias");
        }
    }

    // Método auxiliar para convertir lista a JSON
    private Result convertirListaAJson(List<Noticia> noticias) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.valueToTree(noticias);
        return ok(jsonNode)
                .as("application/json; charset=utf-8");
    }
}