package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utilidad simple para obtener conexiones a PostgreSQL y crear el esquema mínimo.
 * Configuración mediante variables de entorno: DB_URL, DB_USER, DB_PASS
 */
public final class Database {
    /**
     * Si es true, la aplicación funciona en modo "in-memory" y evita usar la base de datos.
     * Se puede forzar por variable de entorno `DB_IN_MEMORY=true`.
     * Por defecto está deshabilitado (usar base de datos real).
     */
    public static final boolean IN_MEMORY;
    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/oberver");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String PASS = System.getenv().getOrDefault("DB_PASS", "postgres");

    static {
        String inMem = System.getenv("DB_IN_MEMORY");
        IN_MEMORY = inMem == null ? false : Boolean.parseBoolean(inMem);
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // Driver no disponible en classpath; el usuario deberá añadirlo al ejecutar.
        }
    }

    private Database() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Crea tablas básicas si no existen. */
    public static void initSchema() throws SQLException {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS ciudadanos (curp TEXT PRIMARY KEY, nombre TEXT, distrito TEXT);");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS dependientes (curp_dependiente TEXT PRIMARY KEY, nombre TEXT, curp_tutor TEXT, distrito TEXT);");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS votos (id SERIAL PRIMARY KEY, curp TEXT, distrito TEXT, proyecto TEXT, corredor TEXT, created_at TIMESTAMP DEFAULT now());");
        }
    }
}
