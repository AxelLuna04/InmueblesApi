package com.inmapi.modelo;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;

@Entity
// ----- AQUÍ SE AÑADE LA INFO DEL NUEVO ÍNDICE -----
@Table(name = "Vendedor", 
    uniqueConstraints = {
        @UniqueConstraint(name = "IX_Vendedor_Correo", columnNames = "correo")
    },
    indexes = {
        @Index(name = "IX_Vendedor_EmailCambioToken", columnList = "emailCambioToken") // <-- AÑADIDO
    }
)
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
