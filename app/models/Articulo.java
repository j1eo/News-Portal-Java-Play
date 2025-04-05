package models;

import java.util.Date;

public class Articulo {
    private int idArticulo;
    private int idUsuario;
    private String titulo;
    private String autor;
    private String contenido;
    private String imagen;
    private Date fechaPublicacion;
    private String estado;
    private String categoria;
    private int meGusta;
    private int noMeGusta;
    // Nuevos campos para manejar la interacción del usuario
    private boolean usuarioDioLike;
    private boolean usuarioDioNoMeGusta;
    private boolean puedeDarLike; // Para verificar permisos
    public Articulo() {}

    // Constructor completo actualizado
    public Articulo(int idArticulo, int idUsuario, String titulo, String autor, String contenido, String imagen, 
                    Date fechaPublicacion, String estado, String categoria, int meGusta, int noMeGusta,
                    boolean usuarioDioLike, boolean usuarioDioNoMeGusta) {
        this.idArticulo = idArticulo;
        this.idUsuario = idUsuario;
        this.titulo = titulo;
        this.autor = autor;
        this.contenido = contenido;
        this.imagen = imagen;
        this.fechaPublicacion = fechaPublicacion;
        this.estado = estado;
        this.categoria = categoria;
        this.meGusta = meGusta;
        this.noMeGusta = noMeGusta;
        this.usuarioDioLike = usuarioDioLike;
        this.usuarioDioNoMeGusta = usuarioDioNoMeGusta;
        this.puedeDarLike = true; // Por defecto true, se puede modificar según lógica de negocio
    }

    // Getters y setters
    public int getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(int idArticulo) {
        this.idArticulo = idArticulo;
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

    // Método para incrementar likes
    public void incrementarMeGusta() {
        this.meGusta++;
        this.usuarioDioLike = true;
        // Si tenía dislike, lo quitamos
        if (this.usuarioDioNoMeGusta) {
            this.noMeGusta--;
            this.usuarioDioNoMeGusta = false;
        }
    }

    // Método para incrementar dislikes
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
        // Un usuario no puede dar like a sus propios artículos
        return this.idUsuario != idUsuarioActual && this.puedeDarLike;
    }

    @Override
    public String toString() {
        return "Articulo{" +
                "idArticulo=" + idArticulo +
                ", titulo='" + titulo + '\'' +
                ", meGusta=" + meGusta +
                ", noMeGusta=" + noMeGusta +
                ", usuarioDioLike=" + usuarioDioLike +
                ", usuarioDioNoMeGusta=" + usuarioDioNoMeGusta +
                '}';
    }
}
