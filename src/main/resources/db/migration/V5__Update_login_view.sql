DROP VIEW V_Usuarios_Login;
GO

CREATE VIEW V_Usuarios_Login AS
SELECT 
    idAdministrador AS id, 
    correo, 
    contrasenia, 
    'ADMIN' AS rol,
    GETDATE() AS fechaVerificacion
FROM dbo.Administrador

UNION ALL

SELECT 
    idCliente AS id, 
    correo, 
    contrasenia, 
    'CLIENTE' AS rol,
    fechaVerificacion
FROM dbo.Cliente

UNION ALL

SELECT 
    idVendedor AS id, 
    correo, 
    contrasenia, 
    'VENDEDOR' AS rol,
    fechaVerificacion
FROM dbo.Vendedor;
GO