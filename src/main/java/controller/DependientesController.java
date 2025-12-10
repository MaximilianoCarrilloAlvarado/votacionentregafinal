package controller;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;
import model.Ciudadano;
import model.Dependientes;
import model.Distrito;
import dao.DependientesDAO;

public class DependientesController {
    private final Map<String, Dependientes> dependientes = new LinkedHashMap<>();
    private final LocalDate fechaVotacion = LocalDate.of(2025, 12, 9); // 9 de diciembre de 2025
    private CiudadanoController ciudadanoController; // referencia para obtener tutor

    public DependientesController(CiudadanoController ciudadanoController) {
        this.ciudadanoController = ciudadanoController;
    }

    public boolean registrarDependiente(String nombre, String curpCiudadano, String curpDependiente) {
        if (curpDependiente == null || curpDependiente.isBlank()) return false;
        if (dependientes.containsKey(curpDependiente)) return false; // CURP dependiente ya registrado
        if (curpCiudadano == null || curpCiudadano.isBlank()) return false;
        if (!esEdadValida(curpDependiente)) return false; // exige rango 10-17 años

        // Obtener ciudadano tutor y validar que exista
        Ciudadano tutor = ciudadanoController.obtenerCiudadano(curpCiudadano);
        if (tutor == null) return false; // ciudadano tutor no existe

        // Contar dependientes actuales del tutor
        int dependientesDelTutor = contarDependientesDelTutor(curpCiudadano);
        if (dependientesDelTutor >= 3) return false; // máximo 3 dependientes por tutor

        // Obtener distrito del ciudadano tutor
        Distrito distritoHeredado = tutor.getDistrito();

        Dependientes d = new Dependientes(nombre, curpCiudadano, curpDependiente, distritoHeredado);
        dependientes.put(curpDependiente, d);
        try {
            DependientesDAO.save(d);
        } catch (Exception ex) {
            // ignorar errores de persistencia por ahora
        }
        return true;
    }

    private int contarDependientesDelTutor(String curpCiudadano) {
        int count = 0;
        for (Dependientes d : dependientes.values()) {
            if (d.getCURP().equals(curpCiudadano)) {
                count++;
            }
        }
        return count;
    }

    public Dependientes obtenerDependiente(String curpDependiente) {
        return dependientes.get(curpDependiente);
    }

    public java.util.Collection<Dependientes> getAllDependientes() {
        return dependientes.values();
    }

    public boolean registrarYMostrar(String nombre, String curpCiudadano, String curpDependiente) {
        boolean ok = registrarDependiente(nombre, curpCiudadano, curpDependiente);
        if (ok) {
            System.out.println("Registrar dependiente '" + nombre + "' CURP = " + curpDependiente + ": se ha registrado correctamente");
            Dependientes d = obtenerDependiente(curpDependiente);
            if (d != null) {
                System.out.println("  -> " + d.getNombre() + " CURP = " + d.getCURPDependiente());
            }
        } else {
            String razonFallo = "";
            if (curpDependiente == null || curpDependiente.isBlank()) {
                razonFallo = " (CURP inválido o vacío)";
            } else if (dependientes.containsKey(curpDependiente)) {
                razonFallo = " (CURP dependiente ya registrado)";
            } else if (curpCiudadano == null || curpCiudadano.isBlank()) {
                razonFallo = " (CURP tutor inválido o vacío)";
            } else if (ciudadanoController.obtenerCiudadano(curpCiudadano) == null) {
                razonFallo = " (ciudadano tutor no existe)";
            } else if (contarDependientesDelTutor(curpCiudadano) >= 3) {
                razonFallo = " (el tutor ya tiene 3 dependientes registrados)";
            } else if (!esEdadValida(curpDependiente)) {
                razonFallo = " (debe tener entre 10 y 17 años)";
            }
            System.out.println("Registrar dependiente '" + nombre + "' CURP = " + curpDependiente + ": no se ha podido registrar" + razonFallo);
        }
        return ok;
    }

    private boolean esEdadValida(String curp) {
        LocalDate nacimiento = obtenerFechaNacimiento(curp);
        if (nacimiento == null) return false;
        int age = Period.between(nacimiento, fechaVotacion).getYears();
        return age >= 10 && age <= 17;
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
