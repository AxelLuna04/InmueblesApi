ALTER TABLE dbo.Cliente
ADD tokenVerificacion VARCHAR(100) NULL;

ALTER TABLE dbo.Cliente
ADD fechaVerificacion DATETIME NULL;

ALTER TABLE dbo.Cliente
ADD expiracionToken DATETIME NULL;

ALTER TABLE dbo.Vendedor
ADD tokenVerificacion VARCHAR(100) NULL;

ALTER TABLE dbo.Vendedor
ADD fechaVerificacion DATETIME NULL;

ALTER TABLE dbo.Vendedor
ADD expiracionToken DATETIME NULL;