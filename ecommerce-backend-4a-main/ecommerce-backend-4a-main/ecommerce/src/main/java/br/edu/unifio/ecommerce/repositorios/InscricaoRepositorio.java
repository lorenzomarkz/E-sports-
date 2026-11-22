package br.edu.unifio.ecommerce.repositorios;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.unifio.ecommerce.entidades.Inscricao;

public interface InscricaoRepositorio extends JpaRepository<Inscricao, Long> {
    boolean existsByTorneioIdAndEquipeIdAndStatus(Long torneioId, Long equipeId, br.edu.unifio.ecommerce.entidades.StatusInscricao status);
    @Query("select count(distinct i.equipe.id) from Inscricao i where i.status = :status")
    long contarEquipesPorStatus(@Param("status") br.edu.unifio.ecommerce.entidades.StatusInscricao status);
    boolean existsByEquipeId(Long equipeId);

    @Query("select i from Inscricao i where i.torneio.id = :torneioId")
    List<Inscricao> buscarPorTorneio(@Param("torneioId") Long torneioId);
}

