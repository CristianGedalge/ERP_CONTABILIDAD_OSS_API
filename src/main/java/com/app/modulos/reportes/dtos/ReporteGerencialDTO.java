package com.app.modulos.reportes.dtos;

import java.util.List;
import java.util.Map;

public class ReporteGerencialDTO {
    private Map<String, Object> kpis;
    private List<GerencialTrendDTO> tendencia;
    private List<GerencialShareDTO> participacion;

    public ReporteGerencialDTO() {}

    public ReporteGerencialDTO(Map<String, Object> kpis, List<GerencialTrendDTO> tendencia, List<GerencialShareDTO> participacion) {
        this.kpis = kpis;
        this.tendencia = tendencia;
        this.participacion = participacion;
    }

    public Map<String, Object> getKpis() {
        return kpis;
    }

    public void setKpis(Map<String, Object> kpis) {
        this.kpis = kpis;
    }

    public List<GerencialTrendDTO> getTendencia() {
        return tendencia;
    }

    public void setTendencia(List<GerencialTrendDTO> tendencia) {
        this.tendencia = tendencia;
    }

    public List<GerencialShareDTO> getParticipacion() {
        return participacion;
    }

    public void setParticipacion(List<GerencialShareDTO> participacion) {
        this.participacion = participacion;
    }
}
