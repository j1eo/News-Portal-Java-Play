package models;

import java.util.Date;

public class NoticiaPropia {
    private int idNoticia;
    private int idUsuario;
    private String titulo;
    private String autor;
    private String url;
    private String fuente;
    private String descripcion;
    private String imagen;
    private String contenido;
    private Date fechaPublicacion;
    private String estado;
    private String categoria;
    private int meGusta;
    private int noMeGusta;
    // Campos para manejar interacción del usuario
    private boolean usuarioDioLike;
    private boolean usuarioDioNoMeGusta;
    private boolean puedeDarLike; // Para verificar permisos

    public NoticiaPropia() {}

    // Constructor completo actualizado
    public NoticiaPropia(int idNoticia, int idUsuario, String titulo, String autor, String url, 
                        String fuente, String descripcion, String imagen, String contenido,
                        Date fechaPublicacion, String estado, String categoria, 
                        int meGusta, int noMeGusta, boolean usuarioDioLike, 
                        boolean usuarioDioNoMeGusta) {
        this.idNoticia = idNoticia;
        this.idUsuario = idUsuario;
        this.titulo = titulo;
        this.autor = autor;
        this.url = url;
        this.fuente = fuente;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.contenido = contenido;
        this.fechaPublicacion = fechaPublicacion;
        this.estado = estado;
        this.categoria = categoria;
        this.meGusta = meGusta;
        this.noMeGusta = noMeGusta;
        this.usuarioDioLike = usuarioDioLike;
        this.usuarioDioNoMeGusta = usuarioDioNoMeGusta;
        this.puedeDarLike = true; // Por defecto true, se puede modificar según lógica de negocio
    }

    // Getters y setters básicos
    public int getIdNoticia() {
        return idNoticia;
    }

    public void setIdNoticia(int idNoticia) {
        this.idNoticia = idNoticia;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
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

    // Getters y setters para interacción de usuario
    public boolean isUsuarioDioLike() {
        return usuarioDioLike;
    }

    public void setUsuarioDioLike(boolean usuarioDioLike) {
        this.usuarioDioLike = usuarioDioLike;
    }

    public boolean isUsuarioDioNoMeGusta() {
        return usuarioDioNoMeGusta;
    }

    public void setUsuarioDioNoMeGusta(boolean usuarioDioNoMeGusta) {
        this.usuarioDioNoMeGusta = usuarioDioNoMeGusta;
    }

    public boolean isPuedeDarLike() {
        return puedeDarLike;
    }

    public void setPuedeDarLike(boolean puedeDarLike) {
        this.puedeDarLike = puedeDarLike;
    }

    // Métodos para manejo de likes/dislikes
    public void incrementarMeGusta() {
        this.meGusta++;
        this.usuarioDioLike = true;
        // Si tenía dislike, lo quitamos
        if (this.usuarioDioNoMeGusta) {
            this.noMeGusta--;
            this.usuarioDioNoMeGusta = false;
        }
    }

    public void incrementarNoMeGusta() {
        this.noMeGusta++;
        this.usuarioDioNoMeGusta = true;
        // Si tenía like, lo quitamos
        if (this.usuarioDioLike) {
            this.meGusta--;
            this.usuarioDioLike = false;
        }
    }

    // Método para verificar si el usuario puede interactuar
    public boolean puedeInteractuar(int idUsuarioActual) {
        // Un usuario no puede dar like a sus propias noticias
        return this.idUsuario != idUsuarioActual && this.puedeDarLike;
    }

    @Override
    public String toString() {
        return "NoticiaPropia{" +
                "idNoticia=" + idNoticia +
                ", titulo='" + titulo + '\'' +
                ", meGusta=" + meGusta +
                ", noMeGusta=" + noMeGusta +
                ", usuarioDioLike=" + usuarioDioLike +
                ", usuarioDioNoMeGusta=" + usuarioDioNoMeGusta +
                '}';
    }
}