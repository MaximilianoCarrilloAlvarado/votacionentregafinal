package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Distrito;
import model.DistritoDos;
import model.DistritoUno;
import model.Proyecto;

public class ProyectoDAO {
    public static boolean save(Proyecto p) {
        if (p == null) return false;
        // Use MERGE keyed on (nombre, distrito) because we have UNIQUE(nombre,distrito)
        String sql = "MERGE INTO proyectos (nombre, distrito, descripcion, activo) KEY(nombre, distrito) VALUES (?, ?, ?, ?);";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDistrito() == null ? null : p.getDistrito().getClass().getSimpleName());
            ps.setString(3, p.getDescripcion());
            ps.setBoolean(4, p.isActivo());
            ps.executeUpdate();
            // Retrieve id
            String q = "SELECT id FROM proyectos WHERE nombre = ? AND (distrito = ? OR (distrito IS NULL AND ? IS NULL)) LIMIT 1";
            try (PreparedStatement pst = conn.prepareStatement(q)) {
                pst.setString(1, p.getNombre());
                pst.setString(2, p.getDistrito() == null ? null : p.getDistrito().getClass().getSimpleName());
                pst.setString(3, p.getDistrito() == null ? null : p.getDistrito().getClass().getSimpleName());
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) p.setId(rs.getLong("id"));
                }
            }
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public static List<String> listByDistrito(Distrito distrito) {
        List<String> out = new ArrayList<>();
        if (distrito == null) return out;
        String sql = "SELECT nombre FROM proyectos WHERE distrito = ? AND activo = TRUE";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, distrito.getClass().getSimpleName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("nombre"));
            }
        } catch (SQLException ex) {
            // ignore
        }
        return out;
    }

    /**
     * Lista proyectos globales (distrito IS NULL), por ejemplo los corredores.
     */
    public static List<String> listGlobal() {
        List<String> out = new ArrayList<>();
        String sql = "SELECT nombre FROM proyectos WHERE distrito IS NULL AND activo = TRUE";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(rs.getString("nombre"));
        } catch (SQLException ex) {
            // ignore
        }
        return out;
    }

    public static int countAll() {
        String sql = "SELECT COUNT(*) as c FROM proyectos";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("c");
        } catch (SQLException ex) {
            // ignore
        }
        return 0;
    }

    public static void seedDefaultsIfEmpty() {
        if (countAll() > 0) return;
        // seed from DistritoUno and DistritoDos and corredores
        Distrito d1 = new DistritoUno();
        Distrito d2 = new DistritoDos();
        for (String p : d1.getProyectos()) {
            save(new Proyecto(p, d1, null));
        }
        for (String p : d2.getProyectos()) {
            save(new Proyecto(p, d2, null));
        }
        // seed corredores as global projects
        for (String c : Distrito.getCorredores()) {
            save(new Proyecto(c, null, null));
        }
    }
}
