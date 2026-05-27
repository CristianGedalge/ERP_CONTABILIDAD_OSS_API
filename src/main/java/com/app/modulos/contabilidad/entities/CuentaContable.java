package com.app.modulos.contabilidad.entities;

import com.app.modulos.empresa.entities.Empresa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Filter;

@Entity
@Table(name = "cuenta_contable")
@Filter(name = "tenantFilter", condition = "id_empresa = :empresaId")
public class CuentaContable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoCuenta tipo;

    @Column(name = "nivel", nullable = false)
    private Integer nivel = 1;

    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Column(name = "id_empresa", nullable = false)
    private Long idEmpresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", insertable = false, updatable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_padre_id")
    private CuentaContable cuentaPadre;

    @OneToMany(mappedBy = "cuentaPadre", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CuentaContable> subCuentas = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoCuenta getTipo() {
        return tipo;
    }

    public void setTipo(TipoCuenta tipo) {
        this.tipo = tipo;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
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

    public CuentaContable getCuentaPadre() {
        return cuentaPadre;
    }

    public void setCuentaPadre(CuentaContable cuentaPadre) {
        this.cuentaPadre = cuentaPadre;
    }

    public List<CuentaContable> getSubCuentas() {
        return subCuentas;
    }

    public void setSubCuentas(List<CuentaContable> subCuentas) {
        this.subCuentas = subCuentas;
    }
}
