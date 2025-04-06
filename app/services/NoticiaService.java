package services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import models.Noticia;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<Noticia> obtenerNoticiasElPais() throws IOException {
        String url = "https://elpais.com";
        List<Noticia> noticiasElPais = new ArrayList<>();
        int intentos = 3;

        while (intentos > 0) {
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .timeout(15000)
                        .get();

                // Estrategia 1: Buscar por estructura de artículo
                Elements articulos = doc.select("article:has(h2.c_t), article:has(a[href*='elpais.com'])");

                // Estrategia 2: Si no encontramos artículos, probar con otra clase
                if (articulos.isEmpty()) {
                    articulos = doc.select("article.b, article.c, article.card");
                }

                for (Element articulo : articulos) {
                    try {
                        // Título - múltiples estrategias
                        String titulo = "";
                        Element tituloElement = articulo.selectFirst("h2.c_t, h2.c_h, h2.headline, h2.title");
                        if (tituloElement != null) {
                            titulo = tituloElement.text();
                        } else {
                            // Fallback: buscar enlaces que probablemente contengan el título
                            Element enlaceTitulo = articulo.selectFirst("a[href]:has(> h2), a[href]:has(> span)");
                            if (enlaceTitulo != null) {
                                titulo = enlaceTitulo.text();
                            }
                        }

                        // Enlace - múltiples estrategias
                        String enlace = "";
                        Element enlaceElement = articulo.selectFirst("a.c_m_c, a[href*='elpais.com'], a.article-link");
                        if (enlaceElement != null) {
                            enlace = enlaceElement.absUrl("href");
                        }

                        // Solo continuar si tenemos título y enlace
                        if (titulo.isEmpty() || enlace.isEmpty()) {
                            continue;
                        }

                        // Descripción
                        String descripcion = "";
                        Element descElement = articulo.selectFirst("p.c_d, p.summary, p.deck");
                        if (descElement != null) {
                            descripcion = descElement.text();
                        }

                        // Autor - manejar múltiples autores
                        String autor = "";
                        Elements autores = articulo.select("div.c_a a.c_a_a, .author a, .byline a");
                        if (!autores.isEmpty()) {
                            autor = autores.eachText().stream()
                                    .filter(text -> !text.isEmpty())
                                    .collect(Collectors.joining(" / "));
                        } else {
                            // Fallback para autores en span
                            Element autorSpan = articulo.selectFirst("span.author, span.byline");
                            if (autorSpan != null) {
                                autor = autorSpan.text();
                            }
                        }

                        // Imagen - múltiples estrategias
                        String imagen = "";
                        Element imgElement = articulo.selectFirst("img.c_m_e, img.photo, img[src*='imagenes.elpais.com']");
                        if (imgElement != null) {
                            if (imgElement.hasAttr("srcset")) {
                                // Tomar la imagen de mayor resolución del srcset
                                String[] sources = imgElement.attr("srcset").split(",");
                                if (sources.length > 0) {
                                    imagen = sources[sources.length - 1].trim().split(" ")[0];
                                }
                            } else if (imgElement.hasAttr("data-src")) {
                                imagen = imgElement.absUrl("data-src");
                            } else {
                                imagen = imgElement.absUrl("src");
                            }
                        }

                        noticiasElPais.add(new Noticia(
                            titulo.trim(),
                            enlace.trim(),
                            autor.trim(),
                            descripcion.trim(),
                            imagen
                        ));

                    } catch (Exception e) {
                        System.err.println("Error procesando un artículo: " + e.getMessage());
                        continue; // Continuar con el siguiente artículo si hay error
                    }
                }

                // Verificar si obtuvimos resultados
                if (!noticiasElPais.isEmpty()) {
                    break;
                } else {
                    throw new IOException("No se encontraron noticias en el scraping");
                }

            } catch (IOException e) {
                intentos--;
                System.err.println("Intento fallido (" + intentos + " restantes): " + e.getMessage());
                if (intentos == 0) {
                    throw e;
                }
                try {
                    Thread.sleep(10000); // Esperar 10 segundos entre intentos
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return noticiasElPais;
    }
}
