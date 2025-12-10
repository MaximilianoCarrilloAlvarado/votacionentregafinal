package controller;

import dao.CiudadanoDAO;
import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;
import model.Ciudadano;
import model.Distrito;

public class CiudadanoController {
    private final Map<String, Ciudadano> ciudadanos = new LinkedHashMap<>();
    private final LocalDate fechaVotacion = LocalDate.of(2025, 12, 9); // 9 de diciembre de 2025

    public boolean registrarCiudadano(String nombre, String curp, Distrito distrito) {
        if (curp == null || curp.isBlank()) return false;
        if (distrito == null) return false; // no registrar si no tiene distrito
        if (ciudadanos.containsKey(curp)) return false; // CURP duplicado
        if (!esMayorDeEdad(curp)) return false; // exige mayoría de edad

        Ciudadano c = new Ciudadano(nombre, curp, distrito);
        ciudadanos.put(curp, c);
        // Intentar persistir en la base de datos (no bloqueante para la lógica en memoria)
        try {
            CiudadanoDAO.save(c);
        } catch (Exception ex) {
            // ignorar fallos de persistencia por ahora
        }
        return true;
    }

    public Ciudadano obtenerCiudadano(String curp) {
        return ciudadanos.get(curp);
    }

    public java.util.Collection<Ciudadano> getAllCiudadanos() {
        return ciudadanos.values();
    }

    public boolean registrarYMostrar(String nombre, String curp, Distrito distrito) {
        boolean ok = registrarCiudadano(nombre, curp, distrito);
        if (ok) {
            System.out.println("Registrar ciudadano '" + nombre + "' CURP = " + curp + ": se ha registrado correctamente");
            Ciudadano c = obtenerCiudadano(curp);
            if (c != null) {
                System.out.println("  -> " + c.getNombre() + " CURP = " + c.getCURP());
            }
        } else {
            String razonFallo = "";
            if (curp == null || curp.isBlank()) {
                razonFallo = " (CURP inválido o vacío)";
            } else if (ciudadanos.containsKey(curp)) {
                razonFallo = " (CURP duplicado)";
            } else if (!esMayorDeEdad(curp)) {
                razonFallo = " (debe ser mayor de edad - 18 años mínimo)";
            }
            System.out.println("Registrar ciudadano '" + nombre + "' CURP = " + curp + ": no se ha podido registrar" + razonFallo);
        }
        return ok;
    }

    private boolean esMayorDeEdad(String curp) {
        LocalDate nacimiento = obtenerFechaNacimiento(curp);
        if (nacimiento == null) return false;
        return Period.between(nacimiento, fechaVotacion).getYears() >= 18;
    }

    // Lee yymmdd (posiciones 5-10) del CURP y arma la fecha.
    private LocalDate obtenerFechaNacimiento(String curp) {
        if (curp == null || curp.length() < 10) return null;
        try {
            int yy = Integer.parseInt(curp.substring(4, 6));
            int mm = Integer.parseInt(curp.substring(6, 8));
            int dd = Integer.parseInt(curp.substring(8, 10));

            int currentTwoDigits = LocalDate.now().getYear() % 100;
            int century = (yy > currentTwoDigits) ? 1900 : 2000;
            int fullYear = century + yy;

            return LocalDate.of(fullYear, mm, dd);
        } catch (Exception e) {
            return null;
        }
    }
}