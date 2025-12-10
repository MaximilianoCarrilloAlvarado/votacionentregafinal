package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.Dependientes;

public class DependientesDAO {
    public static boolean save(Dependientes d) {
        if (d == null) return false;
        // En modo en memoria no persistimos en BD.
        if (Database.IN_MEMORY) return true;
        // H2 upsert using MERGE (we only support H2 now)
        String sql = "MERGE INTO dependientes (curp_dependiente, nombre, curp_tutor, distrito) KEY(curp_dependiente) VALUES (?, ?, ?, ?);";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getCURPDependiente());
            ps.setString(2, d.getNombre());
            ps.setString(3, d.getCURP());
            ps.setString(4, d.getDistrito() == null ? null : d.getDistrito().getClass().getSimpleName());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}
