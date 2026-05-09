package com.app.modulos.usuario.repositories;

import com.app.modulos.usuario.entities.TokenRevocado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TokenRevocadoRepository extends JpaRepository<TokenRevocado, Long> {
    Optional<TokenRevocado> findByToken(String token);
    boolean existsByToken(String token);
}
