-- init_db.sql
-- Ejecuta este script desde psql (como superusuario) para crear usuario, base y tablas mínimas
-- Uso: psql -U postgres -f init_db.sql

-- 1) Crear el usuario (cliente de la app) y la base de datos
CREATE USER oberver WITH PASSWORD 'postgres';
CREATE DATABASE oberver OWNER oberver;
GRANT ALL PRIVILEGES ON DATABASE oberver TO oberver;

-- 2) Conectarse a la base y crear tablas mínimas (la app también las crea si no existen)
\connect oberver;

-- Tabla de ciudadanos (votantes / tutores)
CREATE TABLE IF NOT EXISTS ciudadanos (
  curp TEXT PRIMARY KEY,
  nombre TEXT NOT NULL,
  distrito TEXT
);

-- Tabla de distritos (para referenciar desde proyectos)
CREATE TABLE IF NOT EXISTS distritos (
  nombre TEXT PRIMARY KEY,
  descripcion TEXT
);

-- Tabla de corredores (corredores verdes)
CREATE TABLE IF NOT EXISTS corredores (
  id SERIAL PRIMARY KEY,
  nombre TEXT NOT NULL UNIQUE
);

-- Tabla de proyectos (cada proyecto puede pertenecer a un distrito y opcionalmente a un corredor)
CREATE TABLE IF NOT EXISTS proyectos (
  id SERIAL PRIMARY KEY,
  curp TEXT, -- CURP del proponente (opcional)
  distrito TEXT REFERENCES distritos(nombre),
  nombre TEXT NOT NULL,
  corredor_id INTEGER REFERENCES corredores(id)
);

-- Tabla de dependientes (vinculada al tutor por CURP)
CREATE TABLE IF NOT EXISTS dependientes (
  curp_dependiente TEXT PRIMARY KEY,
  nombre TEXT NOT NULL,
  curp_tutor TEXT REFERENCES ciudadanos(curp),
  distrito TEXT
);

-- Tabla de votos
CREATE TABLE IF NOT EXISTS votos (
  id SERIAL PRIMARY KEY,
  curp TEXT,
  distrito TEXT,
  proyecto TEXT,
  corredor TEXT,
  created_at TIMESTAMP DEFAULT now()
);

-- Poblar distritos por defecto
INSERT INTO distritos (nombre, descripcion) VALUES
  ('DistritoUno', 'Distrito uno por defecto')
  ON CONFLICT (nombre) DO NOTHING;
INSERT INTO distritos (nombre, descripcion) VALUES
  ('DistritoDos', 'Distrito dos por defecto')
  ON CONFLICT (nombre) DO NOTHING;

-- Poblar corredores por defecto
INSERT INTO corredores (nombre) VALUES
  ('01 - Corredor Verde - Principal') ON CONFLICT (nombre) DO NOTHING,
  ('02 - Corredor Verde - Secundario') ON CONFLICT (nombre) DO NOTHING;

-- Poblar proyectos iniciales (asociados a cada distrito)
-- Obtener ids de corredores si existen
-- Insertar proyectos de DistritoUno
INSERT INTO proyectos (curp, distrito, nombre) VALUES
  (NULL, 'DistritoUno', 'Expansión del Parque') ON CONFLICT DO NOTHING,
  (NULL, 'DistritoUno', 'Mejoras en las calles') ON CONFLICT DO NOTHING,
  (NULL, 'DistritoUno', 'Renovación de bancos') ON CONFLICT DO NOTHING;

-- Insertar proyectos de DistritoDos
INSERT INTO proyectos (curp, distrito, nombre) VALUES
  (NULL, 'DistritoDos', 'Parque Central') ON CONFLICT DO NOTHING,
  (NULL, 'DistritoDos', 'Mejoras de Alumbrado') ON CONFLICT DO NOTHING,
  (NULL, 'DistritoDos', 'Renovación de Plaza') ON CONFLICT DO NOTHING;

-- 3) Confirmación (opcional): listar tablas
\dt

-- Fin del script
