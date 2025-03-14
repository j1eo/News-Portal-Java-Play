package models;

import java.util.Date;

public class Articulo {
    private Long id;
    private String titulo;
    private String contenido;
    private Date fechaPublicacion;

    public Articulo(Long id, String titulo, String contenido, Date fechaPublicacion) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fechaPublicacion = fechaPublicacion;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getContenido() { return contenido; }
    public Date getFechaPublicacion() { return fechaPublicacion; }
}
