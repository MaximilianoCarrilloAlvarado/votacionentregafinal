package ui;

import model.Ciudadano;
import model.Distrito;

public class ListaProyectosDisplay {

    public static void showFor(Ciudadano c) {
        if (c == null) return;
        Distrito d = c.getDistrito();
        String distritoNombre = d == null ? "(desconocido)" : d.getClass().getSimpleName();
        System.out.println();
        System.out.println("[Proyectos para: " + c.getNombre() + " | Distrito: " + distritoNombre + "]");
        if (d == null) {
            System.out.println("  No hay distrito asignado. No hay proyectos disponibles.");
            return;
        }

        java.util.List<String> proyectos = d.getProyectos();
        if (proyectos == null || proyectos.isEmpty()) {
            System.out.println("  (ninguno)");
        } else {
            for (int i = 0; i < proyectos.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + proyectos.get(i));
            }
        }
        System.out.println();

        // Mostrar también los corredores verdes (lista estática compartida por todos los distritos)
        System.out.println("  Corredores verdes disponibles:");
        java.util.List<String> corredores = Distrito.getCorredores();
        if (corredores == null || corredores.isEmpty()) {
            System.out.println("    (ninguno)");
        } else {
            for (String cor : corredores) {
                System.out.println("    - " + cor);
            }
        }
        System.out.println();
    }
}
