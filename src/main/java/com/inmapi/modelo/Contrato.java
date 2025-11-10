/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "Contrato")
@Getter
@Setter
@NoArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idContrato")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPublicacion", nullable = false, foreignKey = @ForeignKey(name = "FK_Contrato_Publicacion"))
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente", nullable = false, foreignKey = @ForeignKey(name = "FK_Contrato_Cliente"))
    private Cliente cliente;

    @Column(name = "rutaDocumento", length = 100, nullable = false)
    private String rutaDocumento;

    @Column(name = "fechaCarga")
    private LocalDate fechaCarga;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTipoPago", nullable = false, foreignKey = @ForeignKey(name = "FK_Contrato_TipoPago"))
    private TipoPago tipoPago;
}
