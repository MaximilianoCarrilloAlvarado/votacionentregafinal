package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dao.ProyectoDAO;
import dao.VotoDAO;
import model.Ciudadano;
import model.Dependientes;
import model.Distrito;

/**
 * VotoController valida y registra votos.
 * Reglas:
 * - Un votante (ciudadano o dependiente) debe estar registrado.
 * - No debe haber votado antes el mismo proyecto/corredor.
 * - Para proyectos de distrito: el proyecto debe pertenecer al distrito del votante y máximo 2 votos.
 * - Para corredores: el corredor debe existir y máximo 1 voto.
 * - Si alguna validación falla, el voto no se registra.
 */
public class VotoController {

	private static class VoteRecord {
		final Set<String> districtProjects = new HashSet<>();
		final Set<String> corredorProjects = new HashSet<>();
	}

	private final Map<String, VoteRecord> records = new HashMap<>();
	private final List<VoteEntry> voteEntries = new ArrayList<>();
	// Pending ballots (curp -> pending selections). Only when a ballot has 2 district projects and 1 corredor
	// will it be persisted atomically.
	private final Map<String, PendingBallot> pending = new HashMap<>();

	private final CiudadanoController ciudadanoController;
	private final DependientesController dependientesController;

	public VotoController(CiudadanoController ciudadanoController, DependientesController dependientesController) {
		this.ciudadanoController = ciudadanoController;
		this.dependientesController = dependientesController;
	}

	// Helper: obtiene el distrito del votante (ciudadano o dependiente) y valida registro
	private Distrito obtenerDistritoVotante(String curp) {
		Ciudadano c = ciudadanoController.obtenerCiudadano(curp);
		if (c != null) return c.getDistrito();
		Dependientes d = dependientesController.obtenerDependiente(curp);
		if (d != null) return d.getDistrito();
		return null;
	}

	private static class PendingBallot {
		final java.util.Set<String> districtProjects = new HashSet<>();
		String corredor = null;
		Distrito distrito = null;
	}

	private boolean estaRegistrado(String curp) {
		return ciudadanoController.obtenerCiudadano(curp) != null || dependientesController.obtenerDependiente(curp) != null;
	}

	/** Registra voto por un proyecto del distrito del votante. */
	public synchronized boolean votarProyecto(String curpVotante, String proyecto) {
		if (curpVotante == null || proyecto == null) return false;
		if (!estaRegistrado(curpVotante)) return false; // no registrado
		// Validar proyecto contra la BD
		Distrito d = obtenerDistritoVotante(curpVotante);
		if (d == null) return false; // sin distrito

		java.util.List<String> proyectosValidos = ProyectoDAO.listByDistrito(d);
		if (proyectosValidos == null || !proyectosValidos.contains(proyecto)) return false; // proyecto no en BD

		// Si ya completó boleta previamente, no puede votar otra vez
		if (records.containsKey(curpVotante)) return false;

		PendingBallot pb = pending.computeIfAbsent(curpVotante, k -> { PendingBallot nb = new PendingBallot(); nb.distrito = d; return nb; });
		// Distrito consistente
		if (pb.distrito != null && !pb.distrito.getClass().equals(d.getClass())) return false;

		if (pb.districtProjects.contains(proyecto)) return false; // ya lo seleccionó en pending
		if (pb.districtProjects.size() >= 2) return false; // ya seleccionó 2 proyectos

		pb.districtProjects.add(proyecto);

		// Si la boleta ya tiene 2 proyectos y corredor -> persistir atómicamente
		if (pb.districtProjects.size() == 2 && pb.corredor != null) {
			boolean saved = VotoDAO.saveBallot(curpVotante, pb.distrito, new java.util.ArrayList<>(pb.districtProjects), pb.corredor);
			if (!saved) {
				// deshacer pending
				pending.remove(curpVotante);
				return false;
			}
			// trasladar a registros completados
			VoteRecord rec = new VoteRecord();
			rec.districtProjects.addAll(pb.districtProjects);
			rec.corredorProjects.add(pb.corredor);
			records.put(curpVotante, rec);
			// guardar entradas individuales en memoria
			for (String proj : pb.districtProjects) {
				voteEntries.add(new VoteEntry(curpVotante, pb.distrito, proj, null));
			}
			voteEntries.add(new VoteEntry(curpVotante, null, null, pb.corredor));
			pending.remove(curpVotante);
		}
		return true;
	}

	/**
	 * Intenta votar por un proyecto y escribe un mensaje resumen al stdout
	 * indicando si el voto fue exitoso o no. La lógica de validación ya
	 * reside en `votarProyecto`.
	 */
	public void votarProyectoYMostrar(String curpVotante, String proyecto) {
		boolean ok = votarProyecto(curpVotante, proyecto);
		System.out.println("Voto '" + proyecto + "' por " + curpVotante + " -> " + (ok ? "OK" : "FAILED"));
	}

	/** Registra voto por un corredor verde. */
	public synchronized boolean votarCorredor(String curpVotante, String corredor) {
		if (curpVotante == null || corredor == null) return false;
		if (!estaRegistrado(curpVotante)) return false;
		// Validar corredor usando la BD (proyectos globales)
		java.util.List<String> corredores = ProyectoDAO.listGlobal();
		if (corredores == null || !corredores.contains(corredor)) return false;

		// Si ya completó boleta previamente, no puede votar otra vez
		if (records.containsKey(curpVotante)) return false;

		PendingBallot pb = pending.computeIfAbsent(curpVotante, k -> new PendingBallot());
		if (pb.corredor != null) return false; // ya seleccionó corredor en pending

		pb.corredor = corredor;
		// If district projects already selected (2) then persist atomically
		if (pb.districtProjects.size() == 2) {
			boolean saved = VotoDAO.saveBallot(curpVotante, pb.distrito, new java.util.ArrayList<>(pb.districtProjects), pb.corredor);
			if (!saved) {
				pending.remove(curpVotante);
				return false;
			}
			VoteRecord rec = new VoteRecord();
			rec.districtProjects.addAll(pb.districtProjects);
			rec.corredorProjects.add(pb.corredor);
			records.put(curpVotante, rec);
			for (String proj : pb.districtProjects) voteEntries.add(new VoteEntry(curpVotante, pb.distrito, proj, null));
			voteEntries.add(new VoteEntry(curpVotante, null, null, pb.corredor));
			pending.remove(curpVotante);
		}
		return true;
	}

	/**
	 * Intenta votar por un corredor y escribe un mensaje resumen al stdout
	 * indicando si el voto fue exitoso o no.
	 */
	public void votarCorredorYMostrar(String curpVotante, String corredor) {
		boolean ok = votarCorredor(curpVotante, corredor);
		System.out.println("Voto corredor '" + corredor + "' por " + curpVotante + " -> " + (ok ? "OK" : "FAILED"));
	}

	/** Devuelve copia de los votos registrados. */
	public synchronized List<VoteEntry> getAllVoteEntries() {
		return List.copyOf(voteEntries);
	}

	/** Representa un voto registrado (proyecto de distrito o corredor). */
	public static class VoteEntry {
		private final String curp;
		private final Distrito distrito; // puede ser null para voto a corredor si no se hereda
		private final String proyecto; // proyecto de distrito (null si fue voto a corredor)
		private final String corredor; // corredor (null si fue voto a proyecto)

		public VoteEntry(String curp, Distrito distrito, String proyecto, String corredor) {
			this.curp = curp;
			this.distrito = distrito;
			this.proyecto = proyecto;
			this.corredor = corredor;
		}

		public String getCurp() { return curp; }
		public Distrito getDistrito() { return distrito; }
		public String getProyecto() { return proyecto; }
		public String getCorredor() { return corredor; }
	}

	/**
	 * Devuelve un mapa: distritoNombre -> (proyecto -> votos)
	 */
	public synchronized Map<String, Map<String, Integer>> getResultadosPorDistrito() {
		Map<String, Map<String, Integer>> res = new HashMap<>();
		for (VoteEntry e : voteEntries) {
			if (e.getProyecto() == null) continue;
			String distrito = e.getDistrito() == null ? "(desconocido)" : e.getDistrito().getClass().getSimpleName();
			res.computeIfAbsent(distrito, k -> new HashMap<>())
				.merge(e.getProyecto(), 1, Integer::sum);
		}
		return res;
	}

	/**
	 * Devuelve un mapa corredor -> votos
	 */
	public synchronized Map<String, Integer> getResultadosPorCorredor() {
		Map<String, Integer> res = new HashMap<>();
		for (VoteEntry e : voteEntries) {
			if (e.getCorredor() == null) continue;
			res.merge(e.getCorredor(), 1, Integer::sum);
		}
		return res;
	}

	/**
	 * Imprime resultados agregados por distrito y por corredor en formato legible.
	 */
	public synchronized void printResultados() {
		System.out.println("==== Resultados por Distrito ====");
		Map<String, Map<String, Integer>> byDistrito = getResultadosPorDistrito();
		if (byDistrito.isEmpty()) {
			System.out.println("(ningún voto de distrito registrado)");
		} else {
			for (String distrito : byDistrito.keySet()) {
				System.out.println("Distrito: " + distrito);
				Map<String, Integer> proyectos = byDistrito.get(distrito);
				proyectos.entrySet().stream()
					.sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))
					.forEach(ent -> System.out.println("  - " + ent.getKey() + ": " + ent.getValue()));
				System.out.println();
			}
		}

		System.out.println("==== Resultados por Corredor ====");
		Map<String, Integer> byCorredor = getResultadosPorCorredor();
		if (byCorredor.isEmpty()) {
			System.out.println("(ningún voto de corredor registrado)");
		} else {
			byCorredor.entrySet().stream()
				.sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))
				.forEach(ent -> System.out.println("  - " + ent.getKey() + ": " + ent.getValue()));
		}
		System.out.println();
	}

	public synchronized boolean yaVotoProyecto(String curpVotante, String proyecto) {
		VoteRecord rec = records.get(curpVotante);
		return rec != null && rec.districtProjects.contains(proyecto);
	}

	public synchronized boolean yaVotoCorredor(String curpVotante, String corredor) {
		VoteRecord rec = records.get(curpVotante);
		return rec != null && rec.corredorProjects.contains(corredor);
	}

}
