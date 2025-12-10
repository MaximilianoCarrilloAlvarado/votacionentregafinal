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
        String sql = "INSERT INTO dependientes(curp_dependiente, nombre, curp_tutor, distrito) VALUES (?, ?, ?, ?) ON CONFLICT (curp_dependiente) DO UPDATE SET nombre = EXCLUDED.nombre, curp_tutor = EXCLUDED.curp_tutor, distrito = EXCLUDED.distrito;";
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
