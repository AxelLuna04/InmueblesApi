CREATE INDEX IX_Cliente_Token ON dbo.Cliente(tokenVerificacion);
CREATE INDEX IX_Vendedor_Token ON dbo.Vendedor(tokenVerificacion);

ALTER TABLE dbo.FotoPerfil ALTER COLUMN ruta VARCHAR(255) NOT NULL;
