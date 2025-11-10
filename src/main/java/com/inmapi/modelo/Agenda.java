/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.*;

@Entity
@Table(name = "Agenda")
@Getter
@Setter
@NoArgsConstructor
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAgenda")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVendedor", nullable = false, foreignKey = @ForeignKey(name = "FK_Agenda_Vendedor"))
    private Vendedor vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idArrendador", nullable = false, foreignKey = @ForeignKey(name = "FK_Agenda_Cliente"))
    private Cliente arrendador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPublicacion", nullable = false, foreignKey = @ForeignKey(name = "FK_Agenda_Publicacion"))
    private Publicacion publicacion;

    @Column(name = "fechaSeleccionada", nullable = false)
    private LocalDate fechaSeleccionada;

    @Column(name = "horaSeleccionada", nullable = false)
    private LocalTime horaSeleccionada;
}
