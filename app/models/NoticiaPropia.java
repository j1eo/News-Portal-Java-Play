package models;

import java.util.Date;

public class NoticiaPropia extends Noticia {
    private int idNoticia;
    private int idMiembro;
    private String contenido;
    private Date fechaPublicacion;
    private String estado;
    private String categoria;
    private int meGusta;
    private int noMeGusta;

    public NoticiaPropia(String titulo, String url, String fuente, String descripcion, String imagen) {
        super(titulo, url, fuente, descripcion, imagen);
    }

    // Getters adicionales
    public int getIdNoticia() { return idNoticia; }
    public String getCategoria() { return categoria; }
    public int getMeGusta() { return meGusta; }
    public int getNoMeGusta() { return noMeGusta; }
    public Date getFechaPublicacion() { return fechaPublicacion; }

    // Setters
    public void setIdNoticia(int idNoticia) { this.idNoticia = idNoticia; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public void setFechaPublicacion(Date fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setMeGusta(int meGusta) { this.meGusta = meGusta; }
    public void setNoMeGusta(int noMeGusta) { this.noMeGusta = noMeGusta; }
}