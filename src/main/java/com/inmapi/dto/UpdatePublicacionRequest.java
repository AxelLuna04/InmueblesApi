package com.inmapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdatePublicacionRequest {

    private String titulo;
    private String descripcion;
    private String tipoOperacion;
    private Double precio;
    private Integer numeroHabitaciones;
    private Integer numeroBanosCompletos;
    private Integer numeroExcusados;
    private Integer idTipoInmueble;
    private DireccionDTO direccion;
    private List<Integer> caracteristicasIds;
    private List<Integer> fotosEliminar;
    private Integer portadaId;
}
