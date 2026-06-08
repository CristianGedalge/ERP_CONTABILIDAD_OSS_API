package com.app.modulos.usuario.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_revocados")
public class TokenRevocado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    public TokenRevocado() {}

    public TokenRevocado(String token, LocalDateTime fechaExpiracion) {
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
}
