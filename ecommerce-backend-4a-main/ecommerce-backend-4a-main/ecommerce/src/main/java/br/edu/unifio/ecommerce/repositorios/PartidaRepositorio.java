package br.edu.unifio.ecommerce.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.unifio.ecommerce.entidades.Partida;

public interface PartidaRepositorio extends JpaRepository<Partida, Long> {
    long countByStatus(br.edu.unifio.ecommerce.entidades.StatusPartida status);

    @Query("select p from Partida p where p.torneio.id = :torneioId")
    List<Partida> buscarPorTorneio(@Param("torneioId") Long torneioId);
}

