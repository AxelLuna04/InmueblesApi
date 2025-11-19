CREATE TABLE dbo.AccesoVendedor (
    idAcceso INT IDENTITY(1,1) NOT NULL,
    
    idCliente INT NOT NULL,
    idPublicacion INT NOT NULL,
    
    monto FLOAT NOT NULL,
    fechaPago DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    
    idTipoPago INT NULL,
    simulacionDatos NVARCHAR(255) NULL, 
    
    CONSTRAINT PK_AccesoVendedor PRIMARY KEY CLUSTERED (idAcceso ASC)
);

CREATE UNIQUE INDEX IX_AccesoVendedor_Unico ON dbo.AccesoVendedor (idCliente, idPublicacion);

ALTER TABLE dbo.AccesoVendedor
ADD CONSTRAINT FK_AccesoVendedor_Cliente
FOREIGN KEY (idCliente) REFERENCES dbo.Cliente(idCliente);

ALTER TABLE dbo.AccesoVendedor
ADD CONSTRAINT FK_AccesoVendedor_Publicacion
FOREIGN KEY (idPublicacion) REFERENCES dbo.Publicacion(idPublicacion);

ALTER TABLE dbo.AccesoVendedor
ADD CONSTRAINT FK_AccesoVendedor_TipoPago
FOREIGN KEY (idTipoPago) REFERENCES dbo.TipoPago(idTipoPago);