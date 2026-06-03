package com.app.modulos.empresa.services;

import com.app.modulos.empresa.entities.Empresa;
import com.app.modulos.empresa.repositories.EmpresaRepository;
import com.app.modulos.usuario.entities.Usuario;
import com.app.modulos.usuario.repositories.UserRepository;
import com.app.modulos.empresa.entities.Configuracion;
import com.app.modulos.empresa.repositories.ConfiguracionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaService {
	private final EmpresaRepository empresaRepository;
	private final UserRepository userRepository;

	private final ConfiguracionRepository configuracionRepository;

	public EmpresaService(
		EmpresaRepository empresaRepository, 
		UserRepository userRepository,

		ConfiguracionRepository configuracionRepository
	) {
		this.empresaRepository = empresaRepository;
		this.userRepository = userRepository;

		this.configuracionRepository = configuracionRepository;
	}

	public List<Empresa> findAll() {
		return empresaRepository.findByEstadoTrueOrderByIdAsc();
	}

	public Optional<Empresa> findById(Long id) {
		return empresaRepository.findByIdAndEstadoTrue(id);
	}

	@Transactional
	public Empresa save(Empresa empresa) {
		try {
			return  empresaRepository.save(empresa);
			
		} catch (Exception e) {
			throw new RuntimeException("Error al registrar la empresa" + e.getMessage(), e);
		}

	}

	@Transactional
	public Empresa saveConUsuario(Empresa empresa, String adminNombre, String adminEmail, String adminPassword) {
		Empresa saved = empresaRepository.save(empresa);
		try {
			// Creamos una fila de configuración por defecto para esta empresa/tenant
			Configuracion config = new Configuracion();
			config.setIdEmpresa(saved.getId());
			config.setIva(BigDecimal.valueOf(13)); // Bolivia: IVA estándar 13%
			config.setIt(BigDecimal.valueOf(3));   // Bolivia: IT estándar 3%
			config.setMoneda("BOB");
			config.setTipoCambio(BigDecimal.valueOf(6.96));
			config.setEstado(true);
			
			configuracionRepository.save(config);
		} catch (Exception e) {
			throw new RuntimeException("Error al registrar la empresa/usuario, la transacción será revertida: " + e.getMessage(), e);
		}
		return saved;
	}

	public Optional<Usuario> assignEmpresaToUser(Long empresaId, String correo) {
		return userRepository.findByCorreo(correo).map(usuario -> {
			usuario.setIdEmpresa(empresaId);
			return userRepository.save(usuario);
		});
	}

	public Optional<Empresa> update(Long id, Empresa input) {
		return empresaRepository.findById(id).map(existing -> {
			existing.setNombre(input.getNombre());
			existing.setRazonSocial(input.getRazonSocial());
			existing.setNit(input.getNit());
			existing.setDireccion(input.getDireccion());
			existing.setTelefono(input.getTelefono());
			existing.setCorreo(input.getCorreo());
			if (input.getEstado() != null) {
				existing.setEstado(input.getEstado());
			}
			return empresaRepository.save(existing);
		});
	}

	public Optional<Empresa> disable(Long id) {
		return empresaRepository.findById(id).map(existing -> {
			existing.setEstado(false);
			existing.setFechaDelete(LocalDateTime.now());
			return empresaRepository.save(existing);
		});
	}
}
