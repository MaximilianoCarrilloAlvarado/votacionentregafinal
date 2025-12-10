package ui;

import controller.CiudadanoController;
import controller.DependientesController;
import controller.VotoController;
import dao.Database;
import model.Ciudadano;
import model.Distrito;
import model.DistritoDos;
import model.DistritoUno;

public class App {
    public static void main(String[] args) {
        // Inicializar esquema en la base de datos antes de operar
        try {
            Database.initSchema();
            System.out.println("[DB] Esquema inicializado o ya existente.");
            // Sembrar proyectos por defecto desde las clases DistritoUno/Dos si la tabla está vacía
            dao.ProyectoDAO.seedDefaultsIfEmpty();
        } catch (Exception ex) {
            System.out.println("[DB] No se pudo inicializar el esquema: " + ex.getMessage());
        }

        CiudadanoController controller = new CiudadanoController();
        Distrito d1 = new DistritoUno();
        Distrito d2 = new DistritoDos();

        // Paso 1: registrar ciudadanos (casos base)
        controller.registrarYMostrar("Ana", "AEMA800101HDFRRN08", d1);                     // válido
        controller.registrarYMostrar("Luis", "LOPR750215HDFABC01", d2);                    // válido
        controller.registrarYMostrar("Maria", "MARM920630MDFXYZ02", d1);                   // válido
        controller.registrarYMostrar("Max", "AEMA800101HDFRRN08", d2);                     // CURP duplicado -> falla
        controller.registrarYMostrar("Isaac", "ISAU100101HDFQWE04", d2);                   // menor de edad (nac. 2010) -> debe fallar registro
        controller.registrarYMostrar("Chris", "CHRM900101HDFRTY05", null);                 // distrito null -> no se registra

        // Crear controlador de dependientes y casos base
        DependientesController depCtrl = new DependientesController(controller);

        // Dependientes válidos / casos base
        // 1) Dependiente válido para Ana (tutor registrado)
        depCtrl.registrarYMostrar("Diego", "AEMA800101HDFRRN08", "DIEJ100101HDF001");
        // 2) Intento duplicado (mismo CURP dependiente) -> debe fallar
        depCtrl.registrarYMostrar("Diego", "AEMA800101HDFRRN08", "DIEJ100101HDF001");
        // 3) Dependiente con tutor inexistente -> debe fallar
        depCtrl.registrarYMostrar("SinTutor", "NOEX000000HDF00000", "NTUT100101HDF002");

        // 4) Tutor con 3 dependientes ya registrados (Luis): crear tres, cuarto falla
        depCtrl.registrarYMostrar("DepA", "LOPR750215HDFABC01", "LUDP100201HDF010");
        depCtrl.registrarYMostrar("DepB", "LOPR750215HDFABC01", "LUDP100301HDF011");
        depCtrl.registrarYMostrar("DepC", "LOPR750215HDFABC01", "LUDP100401HDF012");
        // cuarto dependiente para el mismo tutor (debe fallar por límite)
        depCtrl.registrarYMostrar("DepD", "LOPR750215HDFABC01", "LUDP100501HDF013");

        // 5) Dependiente fuera de rango de edad (ej. nacido 2000 -> mayor) -> debe fallar
        depCtrl.registrarYMostrar("Mayor", "AEMA800101HDFRRN08", "OLDP000101HDF003");

        // Esto de acá es c1.getDistrito().getProyectos(); solamente que un ciclo for usando displays
        // Paso 2-4: notificar a cada ciudadano y luego mostrar su lista
        for (Ciudadano c : controller.getAllCiudadanos()) {
            // Separador entre ciudadanos
            System.out.println("\n" + "=".repeat(40));

            // Paso 2: notificar (mensaje simulado)
            NotificacionDisplay.notificarACiudadano(c, "Comienza la votación (simulada)");

            // Paso 3 ya lo hace NotificacionDisplay mostrando la lista de proyectos

            // (Se repetirá para el siguiente ciudadano)
        }
        System.out.println();

        // === Simulación de votaciones (no se agregan más archivos; simulamos que funciona) ===
        VotoController votoCtrl = new VotoController(controller, depCtrl);

        // Votantes (CURP): usar algunos ciudadanos existentes registrados en App
        // Ana: "AEMA800101HDFRRN08" (tutor en ejemplos)
        // Luis: "LOPR750215HDFABC01"
        // Isaac y Chris según casos definidos en App

        // Votos de Ana: dos proyectos de su distrito y un corredor
        // Delegar al controlador la acción de votar y la impresión de resultado
        votoCtrl.votarProyectoYMostrar("AEMA800101HDFRRN08", "Expansión del Parque");
        votoCtrl.votarProyectoYMostrar("AEMA800101HDFRRN08", "Mejoras en las calles");
        votoCtrl.votarCorredorYMostrar("AEMA800101HDFRRN08", "01 - Corredor Verde - Principal");

        // Votos de Luis: intenta votar tres proyectos (el tercero debe bloquearse por límite)
        votoCtrl.votarProyectoYMostrar("LOPR750215HDFABC01", "Parque Central");
        votoCtrl.votarProyectoYMostrar("LOPR750215HDFABC01", "Mejoras de Alumbrado");
        votoCtrl.votarProyectoYMostrar("LOPR750215HDFABC01", "Renovación de Plaza"); // simulación: será rechazado por límite

        // Voto duplicado: Ana intenta votar el mismo proyecto otra vez (debe rechazarse)
        votoCtrl.votarProyectoYMostrar("AEMA800101HDFRRN08", "ParqueCentral");

        // Votos de un dependiente (si está registrado): ejemplo con CURP dependiente usado arriba
        votoCtrl.votarProyectoYMostrar("DIEJ100101HDF001", "Parque Central");

        // Mostrar resultados agregados (por distrito y por corredor)
        votoCtrl.printResultados();
    }
}


