package com.app.modulos.reportes.dtos;

import java.math.BigDecimal;

public class GerencialShareDTO {
    private String nombre;
    private BigDecimal valor;

    public GerencialShareDTO() {}

    public GerencialShareDTO(String nombre, BigDecimal valor) {
        this.nombre = nombre;
        this.valor = valor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
