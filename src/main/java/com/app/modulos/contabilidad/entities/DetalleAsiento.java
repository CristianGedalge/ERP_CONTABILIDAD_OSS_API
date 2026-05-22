package com.app.modulos.contabilidad.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_asiento")
public class DetalleAsiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_asiento", nullable = false)
	@JsonIgnore
	private AsientoContable asiento;

	@ManyToOne
	@JoinColumn(name = "id_cuenta", nullable = false)
	private CuentaContable cuenta;

	@Column(name = "debe", nullable = false, precision = 18, scale = 2)
	private BigDecimal debe = BigDecimal.ZERO;

	@Column(name = "haber", nullable = false, precision = 18, scale = 2)
	private BigDecimal haber = BigDecimal.ZERO;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AsientoContable getAsiento() {
		return asiento;
	}

	public void setAsiento(AsientoContable asiento) {
		this.asiento = asiento;
	}

	public CuentaContable getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaContable cuenta) {
		this.cuenta = cuenta;
	}

	public BigDecimal getDebe() {
		return debe;
	}

	public void setDebe(BigDecimal debe) {
		this.debe = debe;
	}

	public BigDecimal getHaber() {
		return haber;
	}

	public void setHaber(BigDecimal haber) {
		this.haber = haber;
	}
}
