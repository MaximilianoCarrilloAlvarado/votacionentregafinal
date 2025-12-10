package model;

public class Proyecto {
    private long id;
    private String nombre;
    private Distrito distrito;
    private String descripcion;
    private boolean activo = true;

    public Proyecto() {}

    public Proyecto(String nombre, Distrito distrito, String descripcion) {
        this.nombre = nombre;
        this.distrito = distrito;
        this.descripcion = descripcion;
        this.activo = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Distrito getDistrito() {
        return distrito;
    }

    public void setDistrito(Distrito distrito) {
        this.distrito = distrito;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
