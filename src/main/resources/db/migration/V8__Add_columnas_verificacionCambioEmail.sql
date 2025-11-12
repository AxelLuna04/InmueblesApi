ALTER TABLE dbo.Cliente
  ADD emailCambioToken VARCHAR(100) NULL,
      emailNuevo       VARCHAR(50)  NULL,
      emailCambioExp   DATETIME     NULL;

ALTER TABLE dbo.Vendedor
  ADD emailCambioToken VARCHAR(100) NULL,
      emailNuevo       VARCHAR(50)  NULL,
      emailCambioExp   DATETIME     NULL;

CREATE INDEX IX_Cliente_EmailCambioToken  ON dbo.Cliente(emailCambioToken);
CREATE INDEX IX_Vendedor_EmailCambioToken ON dbo.Vendedor(emailCambioToken);