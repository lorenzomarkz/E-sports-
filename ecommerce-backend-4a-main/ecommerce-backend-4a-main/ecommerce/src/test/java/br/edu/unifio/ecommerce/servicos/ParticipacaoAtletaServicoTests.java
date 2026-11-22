package br.edu.unifio.ecommerce.servicos;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.dtos.*;
import br.edu.unifio.ecommerce.repositorios.*;

@ExtendWith(MockitoExtension.class)
class ParticipacaoAtletaServicoTests {
 @Mock ParticipacaoAtletaRepositorio repositorio;
 @Mock AtletaRepositorio atletas;
 @Mock EquipeRepositorio equipes;
 @Mock TorneioRepositorio torneios;
 @Mock InscricaoRepositorio inscricoes;
 @Mock PartidaRepositorio partidas;
 @Mock EstatisticaAtletaRepositorio estatisticas;
 @InjectMocks ParticipacaoAtletaServico servico;
 ParticipacaoAtleta preparar(long torneioId, long equipeId, long modalidadeId) {
  var m=new Modalidade();m.setId(modalidadeId);
  var t=new Torneio();t.setId(torneioId);t.setModalidade(m);
  var e=new Equipe();e.setId(equipeId);e.setModalidade(m);
  var a=new Atleta();a.setId(1L);
  var p=new ParticipacaoAtleta();p.setAtleta(a);p.setEquipe(e);p.setTorneio(t);return p;
 }
 ParticipacaoAtletaDto dados(ParticipacaoAtleta p) {
  var d=new ParticipacaoAtletaDto();d.setAtletaId(p.getAtleta().getId());d.setEquipeId(p.getEquipe().getId());d.setTorneioId(p.getTorneio().getId());return d;
 }
 void cadastro(ParticipacaoAtleta p) {
  when(atletas.findById(1L)).thenReturn(Optional.of(p.getAtleta()));
  when(equipes.findById(p.getEquipe().getId())).thenReturn(Optional.of(p.getEquipe()));
  when(torneios.findById(p.getTorneio().getId())).thenReturn(Optional.of(p.getTorneio()));
 }
 void aprovado(ParticipacaoAtleta p,boolean valor) {
  when(inscricoes.existsByTorneioIdAndEquipeIdAndStatus(p.getTorneio().getId(),p.getEquipe().getId(),StatusInscricao.APROVADA)).thenReturn(valor);
 }
 Partida partida(ParticipacaoAtleta p) {
  var j=new Partida();j.setId(10L);j.setTorneio(p.getTorneio());j.setEquipeA(p.getEquipe());var b=new Equipe();b.setId(99L);j.setEquipeB(b);return j;
 }
 @Test void mesmoAtletaEmDuasModalidadesSemDuplicarCadastro() {
  var futsal=preparar(2,3,4);var basquete=preparar(5,6,7);
  basquete.setAtleta(futsal.getAtleta());
  when(repositorio.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));
  cadastro(futsal);aprovado(futsal,true);var a=servico.cadastrar(dados(futsal));
  cadastro(basquete);aprovado(basquete,true);var b=servico.cadastrar(dados(basquete));
  assertEquals(a.getAtletaId(),b.getAtletaId());assertNotEquals(a.getTorneioId(),b.getTorneioId());assertNotEquals(a.getEquipeId(),b.getEquipeId());
  verify(atletas,never()).save(any());assertNull(futsal.getAtleta().getEquipe());
 }
 @Test void bloqueiaDuplicataMesmoEmOutraEquipe() {
  var p=preparar(2,3,4);cadastro(p);when(repositorio.existsByAtletaIdAndTorneioId(1L,2L)).thenReturn(true);
  assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.cadastrar(dados(p))).getStatusCode().value());
 }
 @Test void bloqueiaModalidadesIncompativeis() {
  var p=preparar(2,3,4);var m=new Modalidade();m.setId(9L);p.getEquipe().setModalidade(m);cadastro(p);
  assertThrows(ResponseStatusException.class,()->servico.cadastrar(dados(p)));verify(repositorio,never()).saveAndFlush(any());
 }
 @Test void exigeInscricaoAprovada() {
  var p=preparar(2,3,4);cadastro(p);aprovado(p,false);
  assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.cadastrar(dados(p))).getStatusCode().value());
 }
 @Test void cadastroAntigoNaoAutorizaPartida() {
  var p=preparar(2,3,4);p.getAtleta().setEquipe(p.getEquipe());
  assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.validarNaPartida(1L,partida(p))).getStatusCode().value());
 }
 @Test void participacaoEmOutroTorneioNaoAutorizaPartida() {
  var p=preparar(2,3,4);var outro=preparar(5,3,4);
  when(repositorio.findByAtletaIdAndTorneioId(1L,5L)).thenReturn(Optional.empty());
  assertThrows(ResponseStatusException.class,()->servico.validarNaPartida(1L,partida(outro)));
 }
 @Test void equipeForaDoConfrontoNaoPodeRegistrar() {
  var p=preparar(2,3,4);var outra=preparar(2,8,4);
  when(repositorio.findByAtletaIdAndTorneioId(1L,2L)).thenReturn(Optional.of(outra));
  assertThrows(ResponseStatusException.class,()->servico.validarNaPartida(1L,partida(p)));
 }
 @Test void aprovacaoRevogadaImpedeEstatisticas() {
  var p=preparar(2,3,4);when(repositorio.findByAtletaIdAndTorneioId(1L,2L)).thenReturn(Optional.of(p));aprovado(p,false);
  assertThrows(ResponseStatusException.class,()->servico.validarNaPartida(1L,partida(p)));
 }
 @Test void listaSomenteParticipantesAprovadosDoConfronto() {
  var p=preparar(2,3,4);var outra=preparar(2,8,4);var j=partida(p);
  when(partidas.findById(10L)).thenReturn(Optional.of(j));when(repositorio.findByTorneioId(2L)).thenReturn(List.of(p,outra));aprovado(p,true);
  var lista=servico.listar(10L);assertEquals(1,lista.size());assertEquals(3L,lista.getFirst().getEquipeId());
 }
 @Test void preservaParticipacaoComEstatisticas() {
  var p=preparar(2,3,4);when(repositorio.findById(8L)).thenReturn(Optional.of(p));when(estatisticas.existsByParticipacaoId(8L)).thenReturn(true);
  assertThrows(ResponseStatusException.class,()->servico.excluir(8L));verify(repositorio,never()).delete(any());
 }
}
