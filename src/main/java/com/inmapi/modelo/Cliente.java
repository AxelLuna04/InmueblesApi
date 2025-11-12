package com.inmapi.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "Cliente", 
    uniqueConstraints = {
        @UniqueConstraint(name = "IX_Cliente_Correo", columnNames = "correo")
    },
    indexes = {
        @Index(name = "IX_Cliente_EmailCambioToken", columnList = "emailCambioToken")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCliente")
    private Integer id;

    @Column(name = "correo", length = 50, nullable = false)
    private String correo;

    @Column(name = "contrasenia", length = 100, nullable = false)
    private String contrasenia;

    @Column(name = "nombreCompleto", length = 100, nullable = false)
    private String nombreCompleto;

    @Column(name = "fechaNacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "presupuesto")
    private Double presupuesto;

    @Column(name = "ubicacionInteres", length = 100)
    private String ubicacionInteres;

    @Column(name = "numeroMiembrosFamilia", length = 10)
    private String numeroMiembrosFamilia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idOcupacion", nullable = true, foreignKey = @ForeignKey(name = "FK_Cliente_Ocupacion"))
    private Ocupacion ocupacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idFotoPerfil", nullable = false, foreignKey = @ForeignKey(name = "FK_Cliente_FotoPerfil"))
    private FotoPerfil fotoPerfil;
    
    @Column(name = "tokenVerificacion", length = 100)
    private String tokenVerificacion;

    @Column(name = "fechaVerificacion")
    private LocalDateTime fechaVerificacion;

    @Column(name = "expiracionToken")
    private LocalDateTime expiracionToken;

    @Column(name = "emailCambioToken", length = 100)
    private String emailCambioToken;

    @Column(name = "emailNuevo", length = 50)
    private String emailNuevo;

    @Column(name = "emailCambioExp")
    private LocalDateTime emailCambioExp;
}
