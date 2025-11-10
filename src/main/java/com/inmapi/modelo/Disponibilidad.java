/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import java.time.LocalTime;
import lombok.*;

@Entity
@Table(name = "Disponibilidad")
@Getter
@Setter
@NoArgsConstructor
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDisponibilidad")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVendedor", nullable = false, foreignKey = @ForeignKey(name = "FK_Disponibilidad_Vendedor"))
    private Vendedor vendedor;

    @Column(name = "diasDisponibles", length = 7)
    private String diasDisponibles; // p.ej.: 1111100

    @Column(name = "diasNoDisponibles", length = 7)
    private String diasNoDisponibles; // opcional

    @Column(name = "horarioAtencionInicio")
    private LocalTime horarioAtencionInicio;

    @Column(name = "horarioAtencionFin")
    private LocalTime horarioAtencionFin;

    @Column(name = "duracionVisita")
    private Double duracionVisita; // minutos u horas, según definas
}
