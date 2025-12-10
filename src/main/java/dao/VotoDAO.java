package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import model.Distrito;

/**
 * DAO para guardar votos en la tabla `votos`.
 */
public class VotoDAO {
    /** Guarda un voto. Si `distrito` es null, se guarda null en la columna distrito. */
    public static boolean saveVote(String curp, Distrito distrito, String proyecto, String corredor) {
        if (curp == null) return false;
        // Si estamos en modo en memoria, no intentamos persistir en la base de datos.
        if (dao.Database.IN_MEMORY) {
            return true;
        }
        String sql = "INSERT INTO votos(curp, distrito, proyecto, corredor) VALUES (?, ?, ?, ?);";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, curp);
            ps.setString(2, distrito == null ? null : distrito.getClass().getSimpleName());
            ps.setString(3, proyecto);
            ps.setString(4, corredor);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Guarda una boleta completa (dos proyectos de distrito + un corredor) de forma atómica.
     * Inserta tres filas en la tabla `votos` dentro de una transacción.
     */
    public static boolean saveBallot(String curp, Distrito distrito, List<String> proyectos, String corredor) {
        if (curp == null || proyectos == null || proyectos.size() != 2 || corredor == null) return false;
        if (dao.Database.IN_MEMORY) {
            return true;
        }
        String sql = "INSERT INTO votos(curp, distrito, proyecto, corredor) VALUES (?, ?, ?, ?);";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // Insertar los dos proyectos
                for (String proj : proyectos) {
                    ps.setString(1, curp);
                    ps.setString(2, distrito == null ? null : distrito.getClass().getSimpleName());
                    ps.setString(3, proj);
                    ps.setString(4, null);
                    ps.executeUpdate();
                }
                // Insertar el corredor
                ps.setString(1, curp);
                ps.setString(2, null);
                ps.setString(3, null);
                ps.setString(4, corredor);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignore) {}
            }
        }
    }
}
