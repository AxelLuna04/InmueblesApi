/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CaracteristicaSeleccionada")
@Getter
@Setter
@NoArgsConstructor
public class CaracteristicaSeleccionada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCaracteristicaSeleccionada")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCaracteristica", nullable = false, foreignKey = @ForeignKey(name = "FK_CaracteristicaSeleccionada_Caracteristica"))
    private Caracteristica caracteristica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPublicacion", nullable = false, foreignKey = @ForeignKey(name = "FK_CaracteristicaSeleccionada_Publicacion"))
    private Publicacion publicacion;
}
