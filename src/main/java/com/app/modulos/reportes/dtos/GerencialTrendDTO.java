package com.app.modulos.reportes.dtos;

import java.math.BigDecimal;

public class GerencialTrendDTO {
    private String etiqueta;
    private BigDecimal valor;

    public GerencialTrendDTO() {}

    public GerencialTrendDTO(String etiqueta, BigDecimal valor) {
        this.etiqueta = etiqueta;
        this.valor = valor;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
