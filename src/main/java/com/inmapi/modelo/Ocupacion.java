/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Ocupacion", uniqueConstraints = {
    @UniqueConstraint(name = "IX_Ocupacion_Nombre", columnNames = "nombre")
})
@Getter
@Setter
@NoArgsConstructor
public class Ocupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOcupacion")
    private Integer id;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;
}
