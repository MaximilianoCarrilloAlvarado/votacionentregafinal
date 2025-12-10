package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.Distrito;

/**
 * DAO para guardar votos en la tabla `votos`.
 */
public class VotoDAO {
    /** Guarda un voto. Si `distrito` es null, se guarda null en la columna distrito. */
    public static boolean saveVote(String curp, Distrito distrito, String proyecto, String corredor) {
        if (curp == null) return false;
        // Si estamos en modo en memoria, no intentamos persistir en la base de datos.
        // Esto permite simular votos completamente en RAM durante el desarrollo.
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
}
