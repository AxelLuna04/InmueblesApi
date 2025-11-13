/*
  PASO 0: Modificar las columnas de historial para que PERMITAN nulos.
  Esto es necesario antes de poder aplicar 'ON DELETE SET NULL'.
*/
ALTER TABLE dbo.Agenda ALTER COLUMN idArrendador INT NULL;
ALTER TABLE dbo.Agenda ALTER COLUMN idVendedor INT NULL;
ALTER TABLE dbo.Agenda ALTER COLUMN idPublicacion INT NULL;
ALTER TABLE dbo.Contrato ALTER COLUMN idCliente INT NULL;
ALTER TABLE dbo.Contrato ALTER COLUMN idPublicacion INT NULL;
ALTER TABLE dbo.Movimiento ALTER COLUMN idPublicacion INT NULL;
ALTER TABLE dbo.Movimiento ALTER COLUMN idArrendador INT NULL;
GO

/*
  Configuramos la integridad referencial para el borrado de cuentas.
*/

-- === 1. PARTES (Usan CASCADE) ===
-- Si se borra un Vendedor, se borran sus Publicaciones
ALTER TABLE dbo.Publicacion DROP CONSTRAINT FK_Publicacion_Vendedor;
ALTER TABLE dbo.Publicacion
ADD CONSTRAINT FK_Publicacion_Vendedor
FOREIGN KEY (idVendedor) REFERENCES dbo.Vendedor(idVendedor)
ON DELETE CASCADE;
GO

-- Si se borra una Publicacion, se borran sus Fotos
ALTER TABLE dbo.FotoPublicacion DROP CONSTRAINT FK_FotoPublicacion_Publicacion;
ALTER TABLE dbo.FotoPublicacion
ADD CONSTRAINT FK_FotoPublicacion_Publicacion
FOREIGN KEY (idPublicacion) REFERENCES dbo.Publicacion(idPublicacion)
ON DELETE CASCADE;
GO

-- Si se borra una Publicacion, se borran sus Caracteristicas
ALTER TABLE dbo.CaracteristicaSeleccionada DROP CONSTRAINT FK_CaracteristicaSeleccionada_Publicacion;
ALTER TABLE dbo.CaracteristicaSeleccionada
ADD CONSTRAINT FK_CaracteristicaSeleccionada_Publicacion
FOREIGN KEY (idPublicacion) REFERENCES dbo.Publicacion(idPublicacion)
ON DELETE CASCADE;
GO

-- Si se borra un Vendedor, se borra su agenda/disponibilidad
ALTER TABLE dbo.DiaOcupado DROP CONSTRAINT FK_DiaOcupado_Vendedor;
ALTER TABLE dbo.DiaOcupado
ADD CONSTRAINT FK_DiaOcupado_Vendedor
FOREIGN KEY (idVendedor) REFERENCES dbo.Vendedor(idVendedor)
ON DELETE CASCADE;
GO

ALTER TABLE dbo.Disponibilidad DROP CONSTRAINT FK_Disponibilidad_Vendedor;
ALTER TABLE dbo.Disponibilidad
ADD CONSTRAINT FK_Disponibilidad_Vendedor
FOREIGN KEY (idVendedor) REFERENCES dbo.Vendedor(idVendedor)
ON DELETE CASCADE;
GO


-- === 2. HISTORIAL (Usan SET NULL para romper ciclos) ===
-- Si se borra un Cliente, la Agenda se mantiene pero se pone NULL
ALTER TABLE dbo.Agenda DROP CONSTRAINT FK_Agenda_Cliente;
ALTER TABLE dbo.Agenda
ADD CONSTRAINT FK_Agenda_Cliente
FOREIGN KEY (idArrendador) REFERENCES dbo.Cliente(idCliente)
ON DELETE SET NULL;
GO

-- Si se borra un Vendedor, la Agenda NO HACE NADA (la cascada de Publicacion se encarga via SET NULL)
ALTER TABLE dbo.Agenda DROP CONSTRAINT FK_Agenda_Vendedor;
ALTER TABLE dbo.Agenda
ADD CONSTRAINT FK_Agenda_Vendedor
FOREIGN KEY (idVendedor) REFERENCES dbo.Vendedor(idVendedor)
ON DELETE NO ACTION;
GO

-- Si se borra una Publicacion, la Agenda se mantiene pero se pone NULL
ALTER TABLE dbo.Agenda DROP CONSTRAINT FK_Agenda_Publicacion;
ALTER TABLE dbo.Agenda
ADD CONSTRAINT FK_Agenda_Publicacion
FOREIGN KEY (idPublicacion) REFERENCES dbo.Publicacion(idPublicacion)
ON DELETE SET NULL;
GO

-- Si se borra un Cliente o Publicacion, el Contrato se mantiene pero se pone NULL
ALTER TABLE dbo.Contrato DROP CONSTRAINT FK_Contrato_Cliente;
ALTER TABLE dbo.Contrato
ADD CONSTRAINT FK_Contrato_Cliente
FOREIGN KEY (idCliente) REFERENCES dbo.Cliente(idCliente)
ON DELETE SET NULL;
GO

ALTER TABLE dbo.Contrato DROP CONSTRAINT FK_Contrato_Publicacion;
ALTER TABLE dbo.Contrato
ADD CONSTRAINT FK_Contrato_Publicacion
FOREIGN KEY (idPublicacion) REFERENCES dbo.Publicacion(idPublicacion)
ON DELETE SET NULL;
GO

-- Si se borra una Publicacion, el Movimiento se mantiene pero se pone NULL
ALTER TABLE dbo.Movimiento DROP CONSTRAINT FK_Movimiento_Publicacion;
ALTER TABLE dbo.Movimiento
ADD CONSTRAINT FK_Movimiento_Publicacion
FOREIGN KEY (idPublicacion) REFERENCES dbo.Publicacion(idPublicacion)
ON DELETE SET NULL;
GO

-- (Tu V1 no tenía FK para Movimiento->Cliente, la añadimos aquí)
-- Si se borra un Cliente (Arrendador), el Movimiento se mantiene pero se pone NULL
ALTER TABLE dbo.Movimiento
ADD CONSTRAINT FK_Movimiento_Cliente
FOREIGN KEY (idArrendador) REFERENCES dbo.Cliente(idCliente)
ON DELETE SET NULL;
GO