import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class TestJsoup {
    public static void main(String[] args) {
        String url = "https://www.bbc.com/news";
        List<String> noticias = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();

            // Usar la clase correcta para los títulos de noticias en BBC
            for (Element noticia : doc.select(".sc-8ea7699c-3.kwWByH")) {
                noticias.add(noticia.text());
            }

            noticias.forEach(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
