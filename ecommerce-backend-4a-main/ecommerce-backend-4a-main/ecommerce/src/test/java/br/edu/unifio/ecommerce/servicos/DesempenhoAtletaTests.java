package br.edu.unifio.ecommerce.servicos;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.repositorios.*;

@ExtendWith(MockitoExtension.class)
class DesempenhoAtletaTests {
 @Mock EstatisticaAtletaRepositorio repositorio;
 @Mock AtletaRepositorio atletas;
 @Mock PartidaRepositorio partidas;
 @Mock ParticipacaoAtletaServico participacoes;
 @Mock ParticipacaoAtletaRepositorio participacaoRepositorio;
 @InjectMocks EstatisticaAtletaServico servico;
 Atleta atleta;
 @BeforeEach void preparar(){atleta=new Atleta();atleta.setId(1L);atleta.setNome("Atleta teste");when(atletas.findById(1L)).thenReturn(Optional.of(atleta));}
 ParticipacaoAtleta participacao(long id,String nome,String jogo){
  var m=new Modalidade();m.setNome(nome);var t=new Torneio();t.setId(id);t.setNome("Copa "+id);t.setModalidade(m);t.setJogo(jogo);
  var e=new Equipe();e.setId(id);e.setNome("Equipe "+id);var p=new ParticipacaoAtleta();p.setId(id);p.setAtleta(atleta);p.setEquipe(e);p.setTorneio(t);return p;
 }
 EstatisticaAtleta registro(long id,ParticipacaoAtleta p,int valor){
  var adversaria=new Equipe();adversaria.setId(99L);adversaria.setNome("Adversária");
  var partida=new Partida();partida.setId(id);partida.setTorneio(p.getTorneio());partida.setEquipeA(p.getEquipe());partida.setEquipeB(adversaria);
  partida.setDataHora(java.time.LocalDateTime.of(2030,1,1,8,0).plusDays(id));
  var r=new EstatisticaAtleta();r.setId(id);r.setAtleta(atleta);r.setPartida(partida);r.setParticipacao(p);r.setGols(valor);r.setPontos(valor);r.setAbates(valor);return r;
 }
 @Test void somaDoisMaisUmSemContarPartidasSemEstatistica(){
  var p=participacao(10,"Futsal",null);var a=registro(1,p,2);var b=registro(2,p,1);
  when(participacaoRepositorio.findByAtletaId(1L)).thenReturn(List.of(p));when(repositorio.findByAtletaId(1L)).thenReturn(List.of(a,b));
  var c=servico.desempenho(1L).getCompeticoes().getFirst();
  assertEquals(3L,c.getTotais().get("gols"));assertNull(c.getTotais().get("assistencias"));assertEquals(2,c.getPartidasComEstatisticas());
  assertEquals(2L,c.getHistorico().getFirst().getPartidaId());verifyNoInteractions(partidas);
 }
 @Test void separaCompeticoesMesmoNaMesmaModalidade(){
  var a=participacao(10,"Futsal",null);var b=participacao(20,"Futsal",null);
  when(repositorio.findByAtletaId(1L)).thenReturn(List.of(registro(1,a,2),registro(2,b,9)));
  var c=servico.desempenho(1L).getCompeticoes();assertEquals(2,c.size());assertEquals(2L,c.get(0).getTotais().get("gols"));assertEquals(9L,c.get(1).getTotais().get("gols"));
 }
 @Test void naoMisturaFutsalBasqueteEValorant(){
  var a=participacao(10,"Futsal",null);var b=participacao(20,"Basquete",null);var c=participacao(30,"E-sports","Valorant");
  when(repositorio.findByAtletaId(1L)).thenReturn(List.of(registro(1,a,3),registro(2,b,20),registro(3,c,12)));
  var grupos=servico.desempenho(1L).getCompeticoes();assertEquals(3,grupos.size());
  assertEquals(3L,grupos.get(0).getTotais().get("gols"));assertFalse(grupos.get(0).getTotais().containsKey("pontos"));
  assertEquals(20L,grupos.get(1).getTotais().get("pontos"));assertEquals(12L,grupos.get(2).getTotais().get("abates"));assertFalse(grupos.get(2).getTotais().containsKey("gols"));
 }
 @Test void preservaLegadoSemAtribuirParticipacao(){
  var p=participacao(10,"Futsal",null);var r=registro(1,p,2);r.setParticipacao(null);
  when(repositorio.findByAtletaId(1L)).thenReturn(List.of(r));var c=servico.desempenho(1L).getCompeticoes().getFirst();
  assertTrue(c.getHistorico().getFirst().isLegado());assertTrue(c.getEquipes().isEmpty());assertEquals(2L,c.getTotais().get("gols"));assertNull(r.getParticipacao());verify(repositorio,never()).save(any());
 }
 @Test void participacaoSemRegistrosNaoIndicaPartidaJogada(){
  when(participacaoRepositorio.findByAtletaId(1L)).thenReturn(List.of(participacao(10,"Vôlei",null)));
  var c=servico.desempenho(1L).getCompeticoes().getFirst();assertEquals(0,c.getPartidasComEstatisticas());assertTrue(c.getHistorico().isEmpty());assertNull(c.getTotais().get("pontos"));
 }
 @Test void novaConsultaRefleteMudancaSemCacheENaoDuplicaPartida(){
  var p=participacao(10,"Futsal",null);var r=registro(1,p,2);
  when(repositorio.findByAtletaId(1L)).thenReturn(List.of(r,r));assertEquals(2L,servico.desempenho(1L).getCompeticoes().getFirst().getTotais().get("gols"));
  r.setGols(4);var c=servico.desempenho(1L).getCompeticoes().getFirst();assertEquals(4L,c.getTotais().get("gols"));assertEquals(1,c.getPartidasComEstatisticas());
 }
 @ParameterizedTest @CsvSource({"Futebol,,gols","Futsal,,gols","Handebol,,gols","Basquete,,pontos","Vôlei,,pontos","E-sports,Rocket League,gols","E-sports,League of Legends,abates","E-sports,Valorant,abates","E-sports,Counter-Strike 2,abates"})
 void camposApropriados(String modalidade,String jogo,String campo){
  var p=participacao(10,modalidade,jogo);var r=registro(1,p,5);r.setAssistencias(2);
  when(repositorio.findByAtletaId(1L)).thenReturn(List.of(r));var totais=servico.desempenho(1L).getCompeticoes().getFirst().getTotais();
  assertEquals(5L,totais.get(campo));assertEquals(modalidade.equals("Vôlei")?Set.of(campo):Set.of(campo,"assistencias"),totais.keySet());
 }
 @Test void atletaInexistenteRetorna404(){when(atletas.findById(1L)).thenReturn(Optional.empty());assertEquals(404,assertThrows(ResponseStatusException.class,()->servico.desempenho(1L)).getStatusCode().value());}
}
