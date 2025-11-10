/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ListaCaracteristicas")
@Getter
@Setter
@NoArgsConstructor
public class ListaCaracteristicas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idListaCaracteristica")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTipoInmueble", nullable = false, foreignKey = @ForeignKey(name = "FK_ListaCaracteristicas_TipoInmueble"))
    private TipoInmueble tipoInmueble;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCaracteristica", nullable = false, foreignKey = @ForeignKey(name = "FK_ListaCaracteristicas_Caracteristica"))
    private Caracteristica caracteristica;
}
