package br.edu.unifio.ecommerce.servicos;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.dtos.*;
import br.edu.unifio.ecommerce.repositorios.*;
import org.springframework.web.server.ResponseStatusException;
class AtletaParticipacaoTests {
 @Test void permiteCadastroSemEquipeParaPosteriorParticipacao() {
  var atletas=mock(AtletaRepositorio.class);var equipes=mock(EquipeRepositorio.class);var estatisticas=mock(EstatisticaAtletaRepositorio.class);
  var servico=new AtletaServico(atletas,equipes,estatisticas);var dto=new AtletaDto();dto.setNome("Atleta teste");dto.setDataNascimento(LocalDate.of(2000,1,1));
  when(atletas.save(any())).thenAnswer(i->i.getArgument(0));
  assertNull(servico.cadastrar(dto).getEquipeId());verifyNoInteractions(equipes);
 }
 @Test void preservaEquipeLegadaQuandoHaEstatisticasAntigas() {
  var atletas=mock(AtletaRepositorio.class);var equipes=mock(EquipeRepositorio.class);var estatisticas=mock(EstatisticaAtletaRepositorio.class);
  var servico=new AtletaServico(atletas,equipes,estatisticas);var equipe=new Equipe();equipe.setId(2L);var atleta=new Atleta();atleta.setId(1L);atleta.setEquipe(equipe);
  when(atletas.findById(1L)).thenReturn(Optional.of(atleta));when(estatisticas.existsByAtletaIdAndParticipacaoIsNull(1L)).thenReturn(true);
  var dto=new AtletaDto();dto.setNome("Atleta teste");dto.setDataNascimento(LocalDate.of(2000,1,1));dto.setEquipeId(3L);
  assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.atualizar(1L,dto)).getStatusCode().value());
  assertEquals(2L,atleta.getEquipe().getId());verify(atletas,never()).save(any());
 }
}
