CREATE VIEW V_Usuarios_Login AS
SELECT 
    idAdministrador AS id, 
    correo, 
    contrasenia, 
    'ADMIN' AS rol
FROM dbo.Administrador
UNION ALL
SELECT 
    idCliente AS id, 
    correo, 
    contrasenia, 
    'CLIENTE' AS rol
FROM dbo.Cliente
UNION ALL
SELECT 
    idVendedor AS id, 
    correo, 
    contrasenia, 
    'VENDEDOR' AS rol
FROM dbo.Vendedor;