package models;

import java.util.Date;

public class Articulo {
    private int idArticulo;
    private int idMiembro;
    private String titulo;
    private String autor;
    private String contenido;
    private String imagen;
    private Date fechaPublicacion;
    private String estado;
    private String categoria;
    private int meGusta;
    private int noMeGusta;

    // **Constructor completo**
    public Articulo(int idArticulo, int idMiembro, String titulo, String autor, String contenido, String imagen, 
                    Date fechaPublicacion, String estado, String categoria, int meGusta, int noMeGusta) {
        this.idArticulo = idArticulo;
        this.idMiembro = idMiembro;
        this.titulo = titulo;
        this.autor = autor;
        this.contenido = contenido;
        this.imagen = imagen;
        this.fechaPublicacion = fechaPublicacion;
        this.estado = estado;
        this.categoria = categoria;
        this.meGusta = meGusta;
        this.noMeGusta = noMeGusta;
    }

    // Getters y setters
    public int getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(int idArticulo) {
        this.idArticulo = idArticulo;
    }

    public int getIdMiembro() {
        return idMiembro;
    }

    public void setIdMiembro(int idMiembro) {
        this.idMiembro = idMiembro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Date getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Date fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getMeGusta() {
        return meGusta;
    }

    public void setMeGusta(int meGusta) {
        this.meGusta = meGusta;
    }

    public int getNoMeGusta() {
        return noMeGusta;
    }

    public void setNoMeGusta(int noMeGusta) {
        this.noMeGusta = noMeGusta;
    }
}
