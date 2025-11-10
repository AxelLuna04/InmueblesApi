package com.inmapi.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "V_Usuarios_Login")
@Getter
public class UsuarioLoginView {

    @Id
    private Integer id;

    private String correo;
    
    private String contrasenia;
    
    private String rol;
    
    private LocalDateTime fechaVerificacion;
}
