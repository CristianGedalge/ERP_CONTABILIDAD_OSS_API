package com.app.modulos.reportes.dtos;

import java.util.List;

public class ReporteQbeQueryDTO {
    private String origen; // VENTAS, COMPRAS, INVENTARIO
    private List<String> columnas;
    private List<ReporteQbeFiltroDTO> filtros;
    private String agruparPor;
    private String ordenarPor;
    private String direccion;

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public List<String> getColumnas() {
        return columnas;
    }

    public void setColumnas(List<String> columnas) {
        this.columnas = columnas;
    }

    public List<ReporteQbeFiltroDTO> getFiltros() {
        return filtros;
    }

    public void setFiltros(List<ReporteQbeFiltroDTO> filtros) {
        this.filtros = filtros;
    }

    public String getAgruparPor() {
        return agruparPor;
    }

    public void setAgruparPor(String agruparPor) {
        this.agruparPor = agruparPor;
    }

    public String getOrdenarPor() {
        return ordenarPor;
    }

    public void setOrdenarPor(String ordenarPor) {
        this.ordenarPor = ordenarPor;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
