/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "Vendedor", uniqueConstraints = {
    @UniqueConstraint(name = "IX_Vendedor_Correo", columnNames = "correo")
})
@Getter
@Setter
@NoArgsConstructor
public class Vendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVendedor")
    private Integer id;

    @Column(name = "nombreCompleto", length = 100, nullable = false)
    private String nombreCompleto;

    @Column(name = "correo", length = 50, nullable = false)
    private String correo;

    @Column(name = "contrasenia", length = 100, nullable = false)
    private String contrasenia;

    @Column(name = "telefono", length = 10)
    private String telefono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idFotoPerfil", nullable = false, foreignKey = @ForeignKey(name = "FK_Vendedor_FotoPerfil"))
    private FotoPerfil fotoPerfil;

    @OneToMany(mappedBy = "vendedor")
    private List<Publicacion> publicaciones = new ArrayList<>();
}
