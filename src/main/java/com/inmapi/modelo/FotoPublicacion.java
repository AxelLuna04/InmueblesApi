/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FotoPublicacion")
@Getter
@Setter
@NoArgsConstructor
public class FotoPublicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFotoPublicacion")
    private Integer id;

    @Column(name = "ruta", length = 100, nullable = false)
    private String ruta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPublicacion", nullable = false, foreignKey = @ForeignKey(name = "FK_FotoPublicacion_Publicacion"))
    private Publicacion publicacion;
}
