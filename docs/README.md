# Diagrama de clases

Este directorio contiene el diagrama de clases (PlantUML) del proyecto.

Archivo:

- `class-diagram.puml` — diagrama UML en formato PlantUML que representa las clases principales, atributos y relaciones (incluye patrón Observer entre `Distrito` y `Ciudadano`).

Cómo generar la imagen (PNG/SVG) en Windows (PowerShell):

1) Usando PlantUML + JAR (requiere Java):

```powershell
# descarga plantuml.jar de https://plantuml.com/download
# ejemplo de uso (genera PNG junto al archivo .puml):
java -jar plantuml.jar docs\class-diagram.puml
```

2) Usando la extensión PlantUML de VS Code:

- Instala "PlantUML" (jebbs.plantuml) y Graphviz si quieres renderizar localmente.
- Abre `docs/class-diagram.puml` y usa el panel de vista previa.

3) Usar el servidor web de PlantUML (online):

- Copia el contenido de `class-diagram.puml` y pégalo en https://plantuml.com/plantuml/ o en algún servicio online para renderizar.

Si quieres que genere y suba la imagen (PNG o SVG) al repositorio, indícamelo y la crearé (nota: necesito que me confirmes si debo usar una librería local o sólo añadir el archivo PNG generado aquí).