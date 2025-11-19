package com.inmapi.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AccesoVendedor", uniqueConstraints = {
    @UniqueConstraint(name = "IX_AccesoVendedor_Unico", columnNames = {"idCliente", "idPublicacion"})
})
@Getter
@Setter
@NoArgsConstructor
public class AccesoVendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAcceso")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente", nullable = false, foreignKey = @ForeignKey(name = "FK_AccesoVendedor_Cliente"))
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPublicacion", nullable = false, foreignKey = @ForeignKey(name = "FK_AccesoVendedor_Publicacion"))
    private Publicacion publicacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTipoPago", nullable = true, foreignKey = @ForeignKey(name = "FK_AccesoVendedor_TipoPago"))
    private TipoPago tipoPago;

    @Column(name = "monto", nullable = false)
    private Double monto;
    
    @Column(name = "fechaPago", nullable = false, updatable = false)
    private LocalDateTime fechaPago;

    @Column(name = "simulacionDatos", length = 255)
    private String simulacionDatos;
}