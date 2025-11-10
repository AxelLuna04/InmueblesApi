/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inmapi.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "Administrador", uniqueConstraints = {
    @UniqueConstraint(name = "IX_Administrador_Correo", columnNames = "correo"),
    @UniqueConstraint(name = "IX_Administrador_NombreUsuario", columnNames = "nombreUsuario")
})
@Getter
@Setter
@NoArgsConstructor
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAdministrador")
    private Integer id;

    @Column(name = "nombreUsuario", length = 50, nullable = false)
    private String nombreUsuario;

    @Column(name = "correo", length = 50, nullable = false)
    private String correo;

    @Column(name = "contrasenia", length = 100, nullable = false)
    private String contrasenia;
}
