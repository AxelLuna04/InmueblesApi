CREATE INDEX IX_Publicacion_TipoOperacion ON dbo.Publicacion(tipoOperacion);
CREATE INDEX IX_Publicacion_Estado_CreadoEn ON dbo.Publicacion(estado, creadoEn DESC);
CREATE INDEX IX_Publicacion_Precio ON dbo.Publicacion(precio);
CREATE INDEX IX_Publicacion_Tipo ON dbo.Publicacion(idTipoInmueble);
CREATE INDEX IX_FotoPublicacion_Portada ON dbo.FotoPublicacion(idPublicacion, esPortada);
