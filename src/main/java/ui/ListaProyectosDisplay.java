package ui;

import dao.ProyectoDAO;
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

        java.util.List<String> proyectos = ProyectoDAO.listByDistrito(d);
        // Fallback: si la tabla proyectos no tiene datos para este distrito, usar lista en código
        if (proyectos == null || proyectos.isEmpty()) {
            proyectos = d.getProyectos();
        }
        if (proyectos == null || proyectos.isEmpty()) {
            System.out.println("  (ninguno)");
        } else {
            for (int i = 0; i < proyectos.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + proyectos.get(i));
            }
        }
        System.out.println();

        // Mostrar también los corredores verdes: leer desde la BD (proyectos globales) con fallback a la lista estática
        System.out.println("  Corredores verdes disponibles:");
        java.util.List<String> corredores = ProyectoDAO.listGlobal();
        if (corredores == null || corredores.isEmpty()) {
            corredores = Distrito.getCorredores();
        }
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
