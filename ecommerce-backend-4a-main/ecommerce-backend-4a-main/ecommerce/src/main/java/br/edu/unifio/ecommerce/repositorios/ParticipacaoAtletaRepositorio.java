package br.edu.unifio.ecommerce.repositorios;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.unifio.ecommerce.entidades.ParticipacaoAtleta;
public interface ParticipacaoAtletaRepositorio extends JpaRepository<ParticipacaoAtleta, Long> {
    boolean existsByAtletaIdAndTorneioId(Long atletaId, Long torneioId);
    Optional<ParticipacaoAtleta> findByAtletaIdAndTorneioId(Long atletaId, Long torneioId);
    List<ParticipacaoAtleta> findByAtletaId(Long atletaId);
    List<ParticipacaoAtleta> findByTorneioId(Long torneioId);
}
