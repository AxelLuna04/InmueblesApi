-- 1. Insertar Tipos de Inmueble
INSERT INTO TipoInmueble (tipo)
VALUES 
    ('Casa'),
    ('Departamento'),
    ('Cuarto'),
    ('Oficina'),
    ('Terreno');

-- 2. Insertar Características
INSERT INTO Caracteristica (caracteristica)
VALUES 
    ('Luz'),
    ('Agua'),
    ('Gas'),
    ('Cable'),
    ('Internet'),
    ('Drenaje'),
    ('Seguridad'),
    ('Estacionamiento'),
    ('Balcon'),
    ('Jardin');

-- 3. Insertar Relaciones en ListaCaracteristicas
INSERT INTO ListaCaracteristicas (idTipoInmueble, idCaracteristica)
VALUES 
    -- Casa (ID 1)
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
    -- Departamento (ID 2)
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 10),
    -- Cuarto (ID 3)
    (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 9),
    -- Oficina (ID 4)
    (4, 1), (4, 2), (4, 5), (4, 6), (4, 7), (4, 8), (4, 9),
    -- Terreno (ID 5)
    (5, 1), (5, 2), (5, 6), (5, 7);

-- 4. Modificar la tabla Direccion para permitir nulos
ALTER TABLE Direccion
ALTER COLUMN line1 NVARCHAR(50) NULL;

ALTER TABLE Direccion
ALTER COLUMN sublocality NVARCHAR(50) NULL;

ALTER TABLE Direccion
ALTER COLUMN locality NVARCHAR(50) NULL;

ALTER TABLE Direccion
ALTER COLUMN admin_area1 NVARCHAR(50) NULL;

-- 5. Insertar Tipo de pago
INSERT INTO TipoPago (tipoPago) VALUES ('Tarjeta');