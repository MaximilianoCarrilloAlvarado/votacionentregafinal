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
        // H2 upsert using MERGE (we only support H2 now)
        String sql = "MERGE INTO ciudadanos (curp, nombre, distrito) KEY(curp) VALUES (?, ?, ?);";
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
