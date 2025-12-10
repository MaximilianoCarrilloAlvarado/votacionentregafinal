package model;

import java.util.ArrayList;
import java.util.List;

public abstract class Distrito {
    protected List<String> proyectos = new ArrayList<>();
    public static final List<String> corredores = List.of(
        "01 - Corredor Verde - Principal",
        "02 - Corredor Verde - Secundario"
    );

    public List<String> getProyectos() {
        return proyectos;
    }

    public static List<String> getCorredores() {
        return corredores;
    }
}

