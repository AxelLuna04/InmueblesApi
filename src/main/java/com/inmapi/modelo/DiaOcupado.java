/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "DiaOcupado")
@Getter
@Setter
@NoArgsConstructor
public class DiaOcupado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDiaOcupado")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVendedor", nullable = false, foreignKey = @ForeignKey(name = "FK_DiaOcupado_Vendedor"))
    private Vendedor vendedor;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;
}
