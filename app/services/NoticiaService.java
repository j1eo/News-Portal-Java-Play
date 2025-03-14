package services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import models.Noticia;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoticiaService {

    public List<Noticia> obtenerNoticias() throws IOException {
        String url = "https://www.milenio.com";
        List<Noticia> noticias = new ArrayList<>();
        int intentos = 3;

        while (intentos > 0) {
            try {
                // Conectar a la página con un User-Agent válido
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
                        .timeout(10000)
                        .get();

                // Seleccionar los artículos
                Elements articulos = doc.select("article");

                for (Element articulo : articulos) {
                    String titulo = articulo.selectFirst("h2 a") != null ? articulo.selectFirst("h2 a").text() : "";
                    String enlace = articulo.selectFirst("h2 a") != null ? articulo.selectFirst("h2 a").absUrl("href") : "";
                    String descripcion = articulo.selectFirst("li.sn-base-bottom-text-growable__sec span, div.sn-base-centered__section span") != null
                            ? articulo.selectFirst("li.sn-base-bottom-text-growable__sec span, div.sn-base-centered__section span").text()
                            : "";
                    String autor = articulo.selectFirst("li.sn-base-bottom-text-growable__author span, div.sn-base-centered__author span") != null
                            ? articulo.selectFirst("li.sn-base-bottom-text-growable__author span, div.sn-base-centered__author span").text()
                            : "";
                    String imagen = articulo.selectFirst("img") != null ? articulo.selectFirst("img").absUrl("src") : "";

                    noticias.add(new Noticia(titulo, enlace, autor, descripcion, imagen));
                }

                // Si el scraping tiene éxito, sal del bucle
                break;

            } catch (IOException e) {
                intentos--;
                if (intentos == 0) {
                    throw e; // Lanza la excepción si no hay más intentos
                }
                try {
                    Thread.sleep(5000); // Espera antes de reintentar
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        return noticias;
    }
}
