/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "Publicacion")
@Getter
@Setter
@NoArgsConstructor
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPublicacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVendedor", nullable = false, foreignKey = @ForeignKey(name = "FK_Publicacion_Vendedor"))
    private Vendedor vendedor;

    @Column(name = "titulo", length = 50, nullable = false)
    private String titulo;

    @Column(name = "descripcion", length = 200, nullable = false)
    private String descripcion;

    @Column(name = "precio", nullable = false)
    private Double precio;

    @Column(name = "numeroHabitaciones")
    private Integer numeroHabitaciones;

    @Column(name = "numeroBanosCompletos")
    private Integer numeroBanosCompletos;

    @Column(name = "numeroExcusados")
    private Integer numeroExcusados;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTipoInmueble", nullable = false, foreignKey = @ForeignKey(name = "FK_Publicacion_Inmueble"))
    private TipoInmueble tipoInmueble;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDireccion", nullable = false, foreignKey = @ForeignKey(name = "FK_Publicacion_Direccion"))
    private Direccion direccion;

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FotoPublicacion> fotos = new ArrayList<>();

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaracteristicaSeleccionada> caracteristicas = new ArrayList<>();

    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "PENDIENTE";

    @Column(name = "creadoEn", nullable = false, updatable = false)
    @CreationTimestamp
    private java.time.LocalDateTime creadoEn;
}
