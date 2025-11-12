USE[Inmuebles]
GO
/****** Object:  Table [dbo].[Administrador]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Administrador](
	[idAdministrador] [int] IDENTITY(1,1) NOT NULL,
	[nombreUsuario] [nvarchar](50) NOT NULL,
	[correo] [nvarchar](50) NOT NULL,
	[contrasenia] [nvarchar](50) NOT NULL,
 CONSTRAINT [PK_Administrador] PRIMARY KEY CLUSTERED 
(
	[idAdministrador] ASC
),
 CONSTRAINT [IX_Administrador_Correo] UNIQUE NONCLUSTERED 
(
	[correo] ASC
),
 CONSTRAINT [IX_Administrador_NombreUsuario] UNIQUE NONCLUSTERED 
(
	[nombreUsuario] ASC
)
);
GO
/****** Object:  Table [dbo].[Agenda]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Agenda](
	[idAgenda] [int] IDENTITY(1,1) NOT NULL,
	[idVendedor] [int] NOT NULL,
	[idArrendador] [int] NOT NULL,
	[idPublicacion] [int] NOT NULL,
	[fechaSeleccionada] [date] NOT NULL,
	[horaSeleccionada] [time](7) NOT NULL,
 CONSTRAINT [PK_Agenda] PRIMARY KEY CLUSTERED 
(
	[idAgenda] ASC
)
);
GO
/****** Object:  Table [dbo].[Caracteristica]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Caracteristica](
	[idCaracteristica] [int] IDENTITY(1,1) NOT NULL,
	[caracteristica] [nvarchar](50) NOT NULL,
 CONSTRAINT [PK_Caracteristica] PRIMARY KEY CLUSTERED 
(
	[idCaracteristica] ASC
)
);
GO
/****** Object:  Table [dbo].[CaracteristicaSeleccionada]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CaracteristicaSeleccionada](
	[idCaracteristicaSeleccionada] [int] IDENTITY(1,1) NOT NULL,
	[idCaracteristica] [int] NOT NULL,
	[idPublicacion] [int] NOT NULL,
 CONSTRAINT [PK_CaracteristicaSeleccionada] PRIMARY KEY CLUSTERED 
(
	[idCaracteristicaSeleccionada] ASC
)
);
GO
/****** Object:  Table [dbo].[Cliente]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Cliente](
	[idCliente] [int] IDENTITY(1,1) NOT NULL,
	[correo] [nvarchar](50) NOT NULL,
	[contrasenia] [nvarchar](50) NOT NULL,
	[nombreCompleto] [nvarchar](100) NOT NULL,
	[fechaNacimiento] [date] NULL,
	[presupuesto] [float] NULL,
	[ubicacionInteres] [nvarchar](100) NULL,
	[numeroMiembrosFamilia] [nchar](10) NULL,
	[idOcupacion] [int] NOT NULL,
	[idFotoPerfil] [int] NOT NULL,
 CONSTRAINT [PK_Cliente] PRIMARY KEY CLUSTERED 
(
	[idCliente] ASC
),
 CONSTRAINT [IX_Cliente_Correo] UNIQUE NONCLUSTERED 
(
	[correo] ASC
)
);
GO
/****** Object:  Table [dbo].[Contrato]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Contrato](
	[idContrato] [int] IDENTITY(1,1) NOT NULL,
	[idPublicacion] [int] NOT NULL,
	[idCliente] [int] NOT NULL,
	[rutaDocumento] [nvarchar](100) NOT NULL,
	[fechaCarga] [date] NULL,
	[idTipoPago] [int] NOT NULL,
 CONSTRAINT [PK_Contrato] PRIMARY KEY CLUSTERED 
(
	[idContrato] ASC
)
);
GO
/****** Object:  Table [dbo].[DiaOcupado]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DiaOcupado](
	[idDiaOcupado] [int] IDENTITY(1,1) NOT NULL,
	[idVendedor] [int] NOT NULL,
	[fecha] [date] NOT NULL,
 CONSTRAINT [PK_DiaOcupado] PRIMARY KEY CLUSTERED 
(
	[idDiaOcupado] ASC
)
);
GO
/****** Object:  Table [dbo].[Direccion]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Direccion](
	[idDireccion] [int] IDENTITY(1,1) NOT NULL,
	[formatted_address] [nvarchar](510) NOT NULL,
	[line1] [nvarchar](50) NOT NULL,
	[sublocality] [nvarchar](50) NOT NULL,
	[locality] [nvarchar](50) NOT NULL,
	[admin_area2] [nvarchar](50) NULL,
	[admin_area1] [nvarchar](50) NOT NULL,
	[postal_code] [nvarchar](50) NOT NULL,
	[country_code] [nvarchar](50) NOT NULL,
	[lat] [float] NOT NULL,
	[lng] [float] NOT NULL,
	[provider] [nvarchar](50) NULL,
	[provider_place_id] [nchar](10) NULL,
 CONSTRAINT [PK_Direccion] PRIMARY KEY CLUSTERED 
(
	[idDireccion] ASC
)
);
GO
/****** Object:  Table [dbo].[Disponibilidad]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Disponibilidad](
	[idDisponibilidad] [int] IDENTITY(1,1) NOT NULL,
	[idVendedor] [int] NOT NULL,
	[diasDisponibles] [varchar](7) NULL,
	[diasNoDisponibles] [varchar](7) NULL,
	[horarioAtencionInicio] [time](7) NULL,
	[horarioAtencionFin] [time](7) NULL,
	[duracionVisita] [float] NULL,
 CONSTRAINT [PK_Disponibilidad] PRIMARY KEY CLUSTERED 
(
	[idDisponibilidad] ASC
)
);
GO
/****** Object:  Table [dbo].[FotoPerfil]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[FotoPerfil](
	[idFotoPerfil] [int] IDENTITY(1,1) NOT NULL,
	[ruta] [nvarchar](100) NOT NULL,
 CONSTRAINT [PK_FotoPerfil] PRIMARY KEY CLUSTERED 
(
	[idFotoPerfil] ASC
)
);
GO
/****** Object:  Table [dbo].[FotoPublicacion]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[FotoPublicacion](
	[idFotoPublicacion] [int] IDENTITY(1,1) NOT NULL,
	[ruta] [nvarchar](100) NOT NULL,
	[idPublicacion] [int] NOT NULL,
 CONSTRAINT [PK_FotoPublicacion] PRIMARY KEY CLUSTERED 
(
	[idFotoPublicacion] ASC
)
);
GO
/****** Object:  Table [dbo].[ListaCaracteristicas]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ListaCaracteristicas](
	[idListaCaracteristica] [int] IDENTITY(1,1) NOT NULL,
	[idTipoInmueble] [int] NOT NULL,
	[idCaracteristica] [int] NOT NULL,
 CONSTRAINT [PK_ListaCaracteristicas] PRIMARY KEY CLUSTERED 
(
	[idListaCaracteristica] ASC
)
);
GO
/****** Object:  Table [dbo].[Movimiento]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Movimiento](
	[idMovimiento] [int] IDENTITY(1,1) NOT NULL,
	[idPublicacion] [int] NOT NULL,
	[tipoMovimiento] [nvarchar](50) NOT NULL,
	[fecha] [date] NOT NULL,
	[idArrendador] [int] NULL,
 CONSTRAINT [PK_Movimiento] PRIMARY KEY CLUSTERED 
(
	[idMovimiento] ASC
)
);
GO
/****** Object:  Table [dbo].[Ocupacion]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Ocupacion](
	[idOcupacion] [int] IDENTITY(1,1) NOT NULL,
	[nombre] [nvarchar](50) NOT NULL,
 CONSTRAINT [PK_Ocupacion] PRIMARY KEY CLUSTERED 
(
	[idOcupacion] ASC
),
 CONSTRAINT [IX_Ocupacion_Nombre] UNIQUE NONCLUSTERED 
(
	[nombre] ASC
)
);
GO
/****** Object:  Table [dbo].[Publicacion]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Publicacion](
	[idPublicacion] [int] IDENTITY(1,1) NOT NULL,
	[idVendedor] [int] NOT NULL,
	[titulo] [nvarchar](50) NOT NULL,
	[descripcion] [nvarchar](200) NOT NULL,
	[precio] [float] NOT NULL,
	[numeroHabitaciones] [int] NULL,
	[numeroBanosCompletos] [int] NULL,
	[numeroExcusados] [int] NULL,
	[idTipoInmueble] [int] NOT NULL,
	[idDireccion] [int] NOT NULL,
 CONSTRAINT [PK_Publicacion] PRIMARY KEY CLUSTERED 
(
	[idPublicacion] ASC
)
);
GO
/****** Object:  Table [dbo].[TipoInmueble]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TipoInmueble](
	[idTipoInmueble] [int] IDENTITY(1,1) NOT NULL,
	[tipo] [nvarchar](50) NOT NULL,
 CONSTRAINT [PK_TipoInmueble] PRIMARY KEY CLUSTERED 
(
	[idTipoInmueble] ASC
)
);
GO
/****** Object:  Table [dbo].[TipoPago]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TipoPago](
	[idTipoPago] [int] IDENTITY(1,1) NOT NULL,
	[tipoPago] [nvarchar](50) NOT NULL,
 CONSTRAINT [PK_TipoPago] PRIMARY KEY CLUSTERED 
(
	[idTipoPago] ASC
)
);
GO
/****** Object:  Table [dbo].[Vendedor]    Script Date: 10/29/2025 3:16:19 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Vendedor](
	[idVendedor] [int] IDENTITY(1,1) NOT NULL,
	[nombreCompleto] [nvarchar](100) NOT NULL,
	[correo] [nvarchar](50) NOT NULL,
	[contrasenia] [nvarchar](50) NOT NULL,
	[telefono] [nchar](10) NULL,
	[idFotoPerfil] [int] NOT NULL,
 CONSTRAINT [PK_Vendedor] PRIMARY KEY CLUSTERED 
(
	[idVendedor] ASC
),
 CONSTRAINT [IX_Vendedor_Correo] UNIQUE NONCLUSTERED 
(
	[correo] ASC
)
);
GO
ALTER TABLE [dbo].[Agenda]  WITH CHECK ADD  CONSTRAINT [FK_Agenda_Cliente] FOREIGN KEY([idArrendador])
REFERENCES [dbo].[Cliente] ([idCliente])
GO
ALTER TABLE [dbo].[Agenda] CHECK CONSTRAINT [FK_Agenda_Cliente]
GO
ALTER TABLE [dbo].[Agenda]  WITH CHECK ADD  CONSTRAINT [FK_Agenda_Publicacion] FOREIGN KEY([idPublicacion])
REFERENCES [dbo].[Publicacion] ([idPublicacion])
GO
ALTER TABLE [dbo].[Agenda] CHECK CONSTRAINT [FK_Agenda_Publicacion]
GO
ALTER TABLE [dbo].[Agenda]  WITH CHECK ADD  CONSTRAINT [FK_Agenda_Vendedor] FOREIGN KEY([idVendedor])
REFERENCES [dbo].[Vendedor] ([idVendedor])
GO
ALTER TABLE [dbo].[Agenda] CHECK CONSTRAINT [FK_Agenda_Vendedor]
GO
-- ESTA ES LA CORRECCIÓN:
ALTER TABLE [dbo].[CaracteristicaSeleccionada]  WITH CHECK ADD  CONSTRAINT [FK_CaracteristicaSeleccionada_Caracteristica] FOREIGN KEY([idCaracteristica])
REFERENCES [dbo].[Caracteristica] ([idCaracteristica])
GO
ALTER TABLE [dbo].[CaracteristicaSeleccionada] CHECK CONSTRAINT [FK_CaracteristicaSeleccionada_Caracteristica]
GO
-- ESTS ES LA CORRECCIÓN:
ALTER TABLE [dbo].[CaracteristicaSeleccionada]  WITH CHECK ADD  CONSTRAINT [FK_CaracteristicaSeleccionada_Publicacion] FOREIGN KEY([idPublicacion])
REFERENCES [dbo].[Publicacion] ([idPublicacion])
GO
ALTER TABLE [dbo].[CaracteristicaSeleccionada] CHECK CONSTRAINT [FK_CaracteristicaSeleccionada_Publicacion]
GO
ALTER TABLE [dbo].[Cliente]  WITH CHECK ADD  CONSTRAINT [FK_Cliente_FotoPerfil] FOREIGN KEY([idFotoPerfil])
REFERENCES [dbo].[FotoPerfil] ([idFotoPerfil])
GO
ALTER TABLE [dbo].[Cliente] CHECK CONSTRAINT [FK_Cliente_FotoPerfil]
GO
ALTER TABLE [dbo].[Cliente]  WITH CHECK ADD  CONSTRAINT [FK_Cliente_Ocupacion] FOREIGN KEY([idOcupacion])
REFERENCES [dbo].[Ocupacion] ([idOcupacion])
GO
ALTER TABLE [dbo].[Cliente] CHECK CONSTRAINT [FK_Cliente_Ocupacion]
GO
ALTER TABLE [dbo].[Contrato]  WITH CHECK ADD  CONSTRAINT [FK_Contrato_Cliente] FOREIGN KEY([idCliente])
REFERENCES [dbo].[Cliente] ([idCliente])
GO
ALTER TABLE [dbo].[Contrato] CHECK CONSTRAINT [FK_Contrato_Cliente]
GO
ALTER TABLE [dbo].[Contrato]  WITH CHECK ADD  CONSTRAINT [FK_Contrato_Publicacion] FOREIGN KEY([idPublicacion])
REFERENCES [dbo].[Publicacion] ([idPublicacion])
GO
ALTER TABLE [dbo].[Contrato] CHECK CONSTRAINT [FK_Contrato_Publicacion]
GO
ALTER TABLE [dbo].[Contrato]  WITH CHECK ADD  CONSTRAINT [FK_Contrato_TipoPago] FOREIGN KEY([idTipoPago])
REFERENCES [dbo].[TipoPago] ([idTipoPago])
GO
ALTER TABLE [dbo].[Contrato] CHECK CONSTRAINT [FK_Contrato_TipoPago]
GO
ALTER TABLE [dbo].[DiaOcupado]  WITH CHECK ADD  CONSTRAINT [FK_DiaOcupado_Vendedor] FOREIGN KEY([idVendedor])
REFERENCES [dbo].[Vendedor] ([idVendedor])
GO
ALTER TABLE [dbo].[DiaOcupado] CHECK CONSTRAINT [FK_DiaOcupado_Vendedor]
GO
ALTER TABLE [dbo].[Disponibilidad]  WITH CHECK ADD  CONSTRAINT [FK_Disponibilidad_Vendedor] FOREIGN KEY([idVendedor])
REFERENCES [dbo].[Vendedor] ([idVendedor])
GO
ALTER TABLE [dbo].[Disponibilidad] CHECK CONSTRAINT [FK_Disponibilidad_Vendedor]
GO
ALTER TABLE [dbo].[FotoPublicacion]  WITH CHECK ADD  CONSTRAINT [FK_FotoPublicacion_Publicacion] FOREIGN KEY([idPublicacion])
REFERENCES [dbo].[Publicacion] ([idPublicacion])
GO
ALTER TABLE [dbo].[FotoPublicacion] CHECK CONSTRAINT [FK_FotoPublicacion_Publicacion]
GO
ALTER TABLE [dbo].[ListaCaracteristicas]  WITH CHECK ADD  CONSTRAINT [FK_ListaCaracteristicas_Caracteristica] FOREIGN KEY([idCaracteristica])
REFERENCES [dbo].[Caracteristica] ([idCaracteristica])
GO
ALTER TABLE [dbo].[ListaCaracteristicas] CHECK CONSTRAINT [FK_ListaCaracteristicas_Caracteristica]
GO
ALTER TABLE [dbo].[ListaCaracteristicas]  WITH CHECK ADD  CONSTRAINT [FK_ListaCaracteristicas_TipoInmueble] FOREIGN KEY([idTipoInmueble])
REFERENCES [dbo].[TipoInmueble] ([idTipoInmueble])
GO
ALTER TABLE [dbo].[ListaCaracteristicas] CHECK CONSTRAINT [FK_ListaCaracteristicas_TipoInmueble]
GO
ALTER TABLE [dbo].[Movimiento]  WITH CHECK ADD  CONSTRAINT [FK_Movimiento_Publicacion] FOREIGN KEY([idPublicacion])
REFERENCES [dbo].[Publicacion] ([idPublicacion])
GO
ALTER TABLE [dbo].[Movimiento] CHECK CONSTRAINT [FK_Movimiento_Publicacion]
GO
ALTER TABLE [dbo].[Publicacion]  WITH CHECK ADD  CONSTRAINT [FK_Publicacion_Direccion] FOREIGN KEY([idDireccion])
REFERENCES [dbo].[Direccion] ([idDireccion])
GO
ALTER TABLE [dbo].[Publicacion] CHECK CONSTRAINT [FK_Publicacion_Direccion]
GO
ALTER TABLE [dbo].[Publicacion]  WITH CHECK ADD  CONSTRAINT [FK_Publicacion_Inmueble] FOREIGN KEY([idTipoInmueble])
REFERENCES [dbo].[TipoInmueble] ([idTipoInmueble])
GO
ALTER TABLE [dbo].[Publicacion] CHECK CONSTRAINT [FK_Publicacion_Inmueble]
GO
ALTER TABLE [dbo].[Publicacion]  WITH CHECK ADD  CONSTRAINT [FK_Publicacion_Vendedor] FOREIGN KEY([idVendedor])
REFERENCES [dbo].[Vendedor] ([idVendedor])
GO
ALTER TABLE [dbo].[Publicacion] CHECK CONSTRAINT [FK_Publicacion_Vendedor]
GO
ALTER TABLE [dbo].[Vendedor]  WITH CHECK ADD  CONSTRAINT [FK_Vendedor_FotoPerfil] FOREIGN KEY([idFotoPerfil])
REFERENCES [dbo].[FotoPerfil] ([idFotoPerfil])
GO
ALTER TABLE [dbo].[Vendedor] CHECK CONSTRAINT [FK_Vendedor_FotoPerfil]
GO