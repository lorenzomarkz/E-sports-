package br.edu.unifio.ecommerce.repositorios;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.unifio.ecommerce.entidades.EstatisticaAtleta;

public interface EstatisticaAtletaRepositorio extends JpaRepository<EstatisticaAtleta, Long> {
    boolean existsByAtletaIdAndParticipacaoIsNull(Long atletaId);
    boolean existsByPartidaId(Long partidaId);
    boolean existsByParticipacaoId(Long participacaoId);
    List<EstatisticaAtleta> findByAtletaId(Long atletaId);
    List<EstatisticaAtleta> findByPartidaId(Long partidaId);
    boolean existsByAtletaIdAndPartidaId(Long atletaId, Long partidaId);
}
