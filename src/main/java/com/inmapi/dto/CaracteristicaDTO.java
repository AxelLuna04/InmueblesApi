package com.inmapi.dto;

import com.inmapi.modelo.Caracteristica;
import lombok.Data;

@Data
public class CaracteristicaDTO {
    private Integer id;
    private String caracteristica;

    public CaracteristicaDTO(Caracteristica entidad) {
        this.id = entidad.getId();
        this.caracteristica = entidad.getCaracteristica();
    }
}
