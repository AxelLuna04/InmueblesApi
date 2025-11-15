ALTER TABLE dbo.Publicacion ADD estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';
ALTER TABLE dbo.Publicacion ADD creadoEn DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME();

ALTER TABLE dbo.FotoPublicacion ADD esPortada BIT NOT NULL DEFAULT 0;

CREATE INDEX IX_Publicacion_Estado ON dbo.Publicacion(estado);