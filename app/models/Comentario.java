package models;

import java.util.Date;

public class Comentario {
    private int idComentario;
    private int idUsuario;
    private int idContenido; // Puede ser ID_Articulo o ID_Noticia
    private String tipoContenido; // "ARTICULO" o "NOTICIA"
    private String contenido;
    private Date fechaCreacion;
    private String estado; // "ACTIVO", "ELIMINADO", etc.
    private Usuario usuario; // Para mostrar información del autor

    // Constructor vacío
    public Comentario() {
    }

    // Constructor con parámetros básicos
    public Comentario(int idComentario, int idUsuario, int idContenido, String tipoContenido, 
                     String contenido, Date fechaCreacion, String estado) {
        this.idComentario = idComentario;
        this.idUsuario = idUsuario;
        this.idContenido = idContenido;
        this.tipoContenido = tipoContenido;
        this.contenido = contenido;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
    }

    // Getters y Setters

    public int getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(int idComentario) {
        this.idComentario = idComentario;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdContenido() {
        return idContenido;
    }

    public void setIdContenido(int idContenido) {
        this.idContenido = idContenido;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(String tipoContenido) {
        if (!"ARTICULO".equals(tipoContenido) && !"NOTICIA".equals(tipoContenido)) {
            throw new IllegalArgumentException("Tipo de contenido debe ser 'ARTICULO' o 'NOTICIA'");
        }
        this.tipoContenido = tipoContenido;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido no puede estar vacío");
        }
        this.contenido = contenido;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (!"PUBLICADO".equals(estado) && !"ELIMINADO".equals(estado) && !"PENDIENTE".equals(estado)) {
            throw new IllegalArgumentException("Estado no válido");
        }
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser nulo");
        }
        this.usuario = usuario;
    }

    // Método toString para logging/depuración
    @Override
    public String toString() {
        return "Comentario{" +
                "idComentario=" + idComentario +
                ", idUsuario=" + idUsuario +
                ", idContenido=" + idContenido +
                ", tipoContenido='" + tipoContenido + '\'' +
                ", contenido='" + contenido + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", estado='" + estado + '\'' +
                '}';
    }

    // Método helper para saber si es comentario de artículo
    public boolean esDeArticulo() {
        return "ARTICULO".equals(this.tipoContenido);
    }

    // Método helper para saber si es comentario de noticia
    public boolean esDeNoticia() {
        return "NOTICIA".equals(this.tipoContenido);
    }
}