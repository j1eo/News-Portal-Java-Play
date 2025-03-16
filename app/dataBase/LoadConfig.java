package dataBase;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class LoadConfig {
//al instanciar dar .properties como parametro
	
	public static Properties loadProperties(String filePath) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo de configuración: " + e.getMessage());
            e.printStackTrace();
        }
        return props;
    }
}
