package ui;

import controller.CiudadanoController;
import model.Ciudadano;
import model.Distrito;

public class NotificacionDisplay {

    //Notifica a todos los ciudadanos registrados en el controller sobre una votación simulada.
    // Si `distrito` es distinto de null, solo notifica a ciudadanos de ese distrito.

    public static void notificarVotacion(CiudadanoController controller, Distrito distrito, String mensaje) {
        if (controller == null) return;
        for (Ciudadano c : controller.getAllCiudadanos()) {
            if (distrito != null) {
                if (c.getDistrito() == null) continue;
                if (!c.getDistrito().getClass().equals(distrito.getClass())) continue;
            }
            // Mensaje de notificación
            System.out.println("[Notificación de votación] -> " + c.getNombre() + ": " + mensaje);
            // Mostrar los proyectos que ve ese ciudadano (vista)
            ListaProyectosDisplay.showFor(c);
        }
    }

    //Notifica a un solo ciudadano y le muestra su lista de proyectos.

    public static void notificarACiudadano(Ciudadano c, String mensaje) {
        if (c == null) return;
        System.out.println("[Notificación de votación] -> " + c.getNombre() + ": " + mensaje);
        ListaProyectosDisplay.showFor(c);
    }
}
