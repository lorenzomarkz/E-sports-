package br.edu.unifio.ecommerce.servicos;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.dtos.*;
import br.edu.unifio.ecommerce.repositorios.*;

@ExtendWith(MockitoExtension.class)
class EstatisticaAtletaServicoTests {
    @Mock EstatisticaAtletaRepositorio repositorio;
    @Mock AtletaRepositorio atletas;
    @Mock PartidaRepositorio partidas;
    @Mock ParticipacaoAtletaServico participacoes;
    @Mock ParticipacaoAtletaRepositorio participacaoRepositorio;
    ParticipacaoAtleta participacao;
    EstatisticaAtletaServico servico;
    EstatisticaAtletaDto dados;
    Atleta atleta;
    Partida partida;
    @BeforeEach void preparar() {
        servico = new EstatisticaAtletaServico(repositorio,atletas,partidas,participacoes,participacaoRepositorio);
        Equipe a = new Equipe(); a.setId(1L);
        Equipe b = new Equipe(); b.setId(2L);
        atleta = new Atleta(); atleta.setId(5L); atleta.setEquipe(a);
        Modalidade modalidade = new Modalidade(); modalidade.setNome("Futebol");
        Torneio torneio = new Torneio(); torneio.setModalidade(modalidade);
        partida = new Partida(); partida.setId(10L); partida.setEquipeA(a); partida.setEquipeB(b); partida.setTorneio(torneio);
        participacao = new ParticipacaoAtleta(); participacao.setId(20L); participacao.setAtleta(atleta);
        participacao.setEquipe(a); participacao.setTorneio(torneio);
        when(participacoes.validarNaPartida(5L,partida)).thenReturn(participacao);
        dados = new EstatisticaAtletaDto(); dados.setAtletaId(5L); dados.setPartidaId(10L); dados.setGols(2);
        when(atletas.findById(5L)).thenReturn(Optional.of(atleta));
        when(partidas.findById(10L)).thenReturn(Optional.of(partida));
    }
    @Test void deveCadastrarGols() {
        when(repositorio.save(any())).thenAnswer(i->i.getArgument(0));
        assertEquals(2, servico.cadastrar(dados).getGols());
    }
    @Test void deveRecusarAtletaDeOutraEquipe() {
        when(participacoes.validarNaPartida(5L,partida)).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.cadastrar(dados)).getStatusCode().value());
        verify(repositorio,never()).save(any());
    }
    @Test void equipeDaParticipacaoDefineOsTotaisMesmoComCadastroAntigoDiferente() {
        atleta.setEquipe(partida.getEquipeB());
        partida.setStatus(StatusPartida.FINALIZADA);partida.setPlacarEquipeA(1);partida.setPlacarEquipeB(10);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.cadastrar(dados)).getStatusCode().value());
    }
    @Test void mesmoAtletaTemGolsEPontosEmParticipacoesSeparadas() {
        when(repositorio.save(any())).thenAnswer(i->i.getArgument(0));
        var futebol = servico.cadastrar(dados);
        var basquete = new Modalidade();basquete.setNome("Basquete");
        var torneio = new Torneio();torneio.setId(200L);torneio.setModalidade(basquete);
        var equipe = new Equipe();equipe.setId(30L);
        var jogo = new Partida();jogo.setId(11L);jogo.setTorneio(torneio);jogo.setEquipeA(equipe);jogo.setEquipeB(partida.getEquipeB());
        jogo.setStatus(StatusPartida.FINALIZADA);jogo.setPlacarEquipeA(5);jogo.setPlacarEquipeB(1);
        var outraParticipacao = new ParticipacaoAtleta();outraParticipacao.setId(21L);outraParticipacao.setAtleta(atleta);outraParticipacao.setEquipe(equipe);outraParticipacao.setTorneio(torneio);
        when(partidas.findById(11L)).thenReturn(Optional.of(jogo));
        when(participacoes.validarNaPartida(5L,jogo)).thenReturn(outraParticipacao);
        dados.setPartidaId(11L);dados.setGols(null);dados.setPontos(4);
        var resultado = servico.cadastrar(dados);
        assertEquals(futebol.getAtletaId(),resultado.getAtletaId());
        assertEquals(20L,futebol.getParticipacaoId());assertEquals(21L,resultado.getParticipacaoId());
        assertEquals(2,futebol.getGols());assertNull(futebol.getPontos());
        assertEquals(4,resultado.getPontos());assertNull(resultado.getGols());
        assertEquals(1L,atleta.getEquipe().getId());
    }
    @Test void deveRecusarValorNegativo() {
        dados.setGols(-1);
        assertEquals(400,assertThrows(ResponseStatusException.class,()->servico.cadastrar(dados)).getStatusCode().value());
    }
    @Test void deveRecusarCampoIncompativel() {
        dados.setPontos(4);
        assertEquals(400,assertThrows(ResponseStatusException.class,()->servico.cadastrar(dados)).getStatusCode().value());
    }
    @Test void deveRecusarDuplicidade() {
        when(repositorio.existsByAtletaIdAndPartidaId(5L,10L)).thenReturn(true);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.cadastrar(dados)).getStatusCode().value());
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
        "Futebol,,gols", "Futsal,,gols", "Handebol,,gols", "Basquete,,pontos",
        "E-sports,Rocket League,gols", "E-sports,Valorant,abates",
        "E-sports,League of Legends,abates", "E-sports,Counter-Strike 2,abates"
    })
    void permiteAssistenciasCompativeis(String modalidade, String jogo, String campo) {
        partida.getTorneio().getModalidade().setNome(modalidade);
        partida.getTorneio().setJogo(jogo);
        dados.setGols(campo.equals("gols") ? 2 : null);
        dados.setPontos(campo.equals("pontos") ? 2 : null);
        dados.setAbates(campo.equals("abates") ? 2 : null);
        dados.setAssistencias(1);
        when(repositorio.save(any())).thenAnswer(i->i.getArgument(0));
        assertEquals(1,servico.cadastrar(dados).getAssistencias());
    }
    @Test void voleiRecusaAssistencias() {
        partida.getTorneio().getModalidade().setNome("Vôlei");
        dados.setGols(null);dados.setPontos(2);dados.setAssistencias(1);
        assertThrows(ResponseStatusException.class,()->servico.cadastrar(dados));
    }
    @Test void recusaSomaDeGolsAcimaDoPlacarFinal() {
        partida.setStatus(StatusPartida.FINALIZADA);partida.setPlacarEquipeA(1);partida.setPlacarEquipeB(0);
        assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.cadastrar(dados)).getStatusCode().value());
    }
    @Test void permiteEstatisticasParciais() {
        partida.setStatus(StatusPartida.FINALIZADA);partida.setPlacarEquipeA(3);partida.setPlacarEquipeB(0);
        when(repositorio.save(any())).thenAnswer(i->i.getArgument(0));
        assertEquals(2,servico.cadastrar(dados).getGols());
    }}
