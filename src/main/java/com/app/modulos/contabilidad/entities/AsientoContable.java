package com.app.modulos.contabilidad.entities;

import com.app.modulos.empresa.entities.Empresa;
import com.app.modulos.usuario.entities.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Filter;

@Entity
@Table(name = "asiento_contable")
@Filter(name = "tenantFilter", condition = "id_empresa = :empresaId")
public class AsientoContable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nro_asiento", length = 50)
    private String nroAsiento;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "glosa", nullable = false, length = 255)
    private String glosa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoAsiento estado = EstadoAsiento.BORRADOR;

    @Column(name = "origen_documento", length = 100)
    private String origenDocumento;

    @Column(name = "origen_id")
    private Long origenId;

    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", insertable = false, updatable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_contable_id", nullable = false)
    private PeriodoContable periodoContable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "asientoContable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleAsiento> detalles = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNroAsiento() {
        return nroAsiento;
    }

    public void setNroAsiento(String nroAsiento) {
        this.nroAsiento = nroAsiento;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getGlosa() {
        return glosa;
    }

    public void setGlosa(String glosa) {
        this.glosa = glosa;
    }

    public EstadoAsiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoAsiento estado) {
        this.estado = estado;
    }

    public String getOrigenDocumento() {
        return origenDocumento;
    }

    public void setOrigenDocumento(String origenDocumento) {
        this.origenDocumento = origenDocumento;
    }

    public Long getOrigenId() {
        return origenId;
    }

    public void setOrigenId(Long origenId) {
        this.origenId = origenId;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public PeriodoContable getPeriodoContable() {
        return periodoContable;
    }

    public void setPeriodoContable(PeriodoContable periodoContable) {
        this.periodoContable = periodoContable;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<DetalleAsiento> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleAsiento> detalles) {
        this.detalles = detalles;
        if (detalles != null) {
            for (DetalleAsiento d : detalles) {
                d.setAsientoContable(this);
            }
        }
    }
}
