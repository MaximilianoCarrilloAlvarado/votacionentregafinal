package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.Ciudadano;

public class CiudadanoDAO {
    public static boolean save(Ciudadano c) {
        if (c == null) return false;
        // En modo en memoria no persistimos en BD.
        if (Database.IN_MEMORY) return true;
        String sql = "INSERT INTO ciudadanos(curp, nombre, distrito) VALUES (?, ?, ?) ON CONFLICT (curp) DO UPDATE SET nombre = EXCLUDED.nombre, distrito = EXCLUDED.distrito;";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCURP());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getDistrito() == null ? null : c.getDistrito().getClass().getSimpleName());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}
