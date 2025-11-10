/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TipoPago")
@Getter
@Setter
@NoArgsConstructor
public class TipoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTipoPago")
    private Integer id;

    @Column(name = "tipoPago", length = 50, nullable = false)
    private String tipoPago;
}
