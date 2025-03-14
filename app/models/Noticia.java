package models;

public class Noticia {
    private String titulo;
    private String url;
    private String fuente;
    private String descripcion;
    private String imagen;

    public Noticia(String titulo, String url, String fuente, String descripcion, String imagen) {
        this.titulo = titulo;
        this.url = url;
        this.fuente = fuente;
        this.descripcion = descripcion;
        this.imagen = imagen;
    }

    // Getters
    public String getTitulo() { return titulo; }
    public String getUrl() { return url; }
    public String getFuente() { return fuente; }
    public String getDescripcion() { return descripcion; }
    public String getImagen() { return imagen; }
}
