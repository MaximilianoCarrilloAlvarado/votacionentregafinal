package model;

import java.time.LocalDate;
import java.time.Period;

public class Ciudadano {
    private final String nombre;
    private final String CURP;
    private final Distrito distrito;

    public Ciudadano(String nombre, String CURP, Distrito distrito) {
        this.nombre = nombre == null ? "" : nombre;
        this.CURP = CURP == null ? "" : CURP.trim();
        this.distrito = distrito;
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

    /**
     * Extrae la fecha de nacimiento del CURP (posiciones 5-10 con formato yymmdd).
     * Devuelve null si el CURP no tiene formato válido.
     */
    public LocalDate getFechaNacimientoDesdeCurp() {
        if (CURP == null || CURP.length() < 10) return null;
        try {
            int yy = Integer.parseInt(CURP.substring(4, 6));
            int mm = Integer.parseInt(CURP.substring(6, 8));
            int dd = Integer.parseInt(CURP.substring(8, 10));

            int currentTwoDigits = LocalDate.now().getYear() % 100;
            int century = (yy > currentTwoDigits) ? 1900 : 2000;
            int fullYear = century + yy;

            return LocalDate.of(fullYear, mm, dd);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean esMayorDeEdad() {
        LocalDate nacimiento = getFechaNacimientoDesdeCurp();
        if (nacimiento == null) return false;
        return Period.between(nacimiento, LocalDate.now()).getYears() >= 18;
    }
}

