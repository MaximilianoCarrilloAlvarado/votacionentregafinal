package model;

public class Dependientes {
    private final String nombre;
    private final Distrito distrito;
    private final String CURP; // CURP del ciudadano al que depende
    private final String CURPDependiente;

    public Dependientes(String nombre, String CURP, String CURPDependiente, Distrito distrito) {
        this.nombre = nombre == null ? "" : nombre;
        this.CURPDependiente = CURPDependiente == null ? "" : CURPDependiente;
        this.distrito = distrito;
        this.CURP = CURP;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCURP() {
        return CURP;
    }

    public Distrito getDistrito() {
        return distrito;
    }

    public String getCURPDependiente() {
        return CURPDependiente;
    }
}
