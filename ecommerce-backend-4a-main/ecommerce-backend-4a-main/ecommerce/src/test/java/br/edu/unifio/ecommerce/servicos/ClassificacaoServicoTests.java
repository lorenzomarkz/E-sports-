package br.edu.unifio.ecommerce.servicos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.repositorios.*;

class ClassificacaoServicoTests {
    private final TorneioRepositorio torneios = mock(TorneioRepositorio.class);
    private final InscricaoRepositorio inscricoes = mock(InscricaoRepositorio.class);
    private final PartidaRepositorio partidas = mock(PartidaRepositorio.class);
    private final ClassificacaoServico servico = new ClassificacaoServico(torneios, inscricoes, partidas);

    private Torneio preparar(String esporte, String jogo, int a, int b) {
        Modalidade modalidade = new Modalidade(); modalidade.setNome(esporte);
        Torneio torneio = new Torneio(); torneio.setId(1L); torneio.setModalidade(modalidade); torneio.setJogo(jogo);
        Equipe equipeA = new Equipe(); equipeA.setId(1L); equipeA.setNome("Equipe A");
        Equipe equipeB = new Equipe(); equipeB.setId(2L); equipeB.setNome("Equipe B");
        Partida partida = new Partida(); partida.setId(1L); partida.setEquipeA(equipeA); partida.setEquipeB(equipeB);
        partida.setTorneio(torneio); partida.setDataHora(LocalDateTime.of(2030,1,1,10,0));
        partida.setStatus(StatusPartida.FINALIZADA); partida.setPlacarEquipeA(a); partida.setPlacarEquipeB(b);
        when(torneios.findById(1L)).thenReturn(Optional.of(torneio));
        when(partidas.buscarPorTorneio(1L)).thenReturn(List.of(partida));
        return torneio;
    }

    @ParameterizedTest
    @CsvSource({"Futebol,3,0", "Futsal,3,0", "Handebol,3,0", "Basquete,2,1"})
    void calculaPontosConformeEsporte(String esporte, int vencedor, int perdedor) {
        preparar(esporte, null, 2, 1);
        var tabela = servico.consultar(1L).getClassificacao();
        assertEquals(vencedor, tabela.get(0).getPontos());
        assertEquals(perdedor, tabela.get(1).getPontos());
        assertEquals(1, tabela.get(0).getVitorias());
        assertEquals(1, tabela.get(1).getDerrotas());
    }

    @Test void empateNoFutebolValeUmPonto() {
        preparar("Futebol", null, 1, 1);
        var tabela = servico.consultar(1L).getClassificacao();
        assertTrue(tabela.stream().allMatch(l -> l.getPontos() == 1 && l.getEmpates() == 1));
    }

    @ParameterizedTest
    @CsvSource({"Vôlei,", "E-sports,League of Legends", "E-sports,Valorant", "E-sports,Counter-Strike 2", "E-sports,Rocket League"})
    void usaVitoriasSemPontosDeFutebol(String esporte, String jogo) {
        preparar(esporte, jogo, 3, 1);
        var tabela = servico.consultar(1L).getClassificacao();
        assertNull(tabela.get(0).getPontos());
        assertEquals(1, tabela.get(0).getVitorias());
        assertEquals(1, tabela.get(1).getDerrotas());
    }

    @Test void preservaResultadoInvalidoMasNaoPontua() {
        preparar("Basquete", null, 2, 2);
        var resultado = servico.consultar(1L);
        assertEquals(1, resultado.getResultados().size());
        assertTrue(resultado.getClassificacao().isEmpty());
        assertNotNull(resultado.getAviso());
    }

    @Test void separaProximosDosFinalizados() {
        preparar("Futebol", null, 2, 1);
        var partida = partidas.buscarPorTorneio(1L).getFirst();
        partida.setStatus(StatusPartida.AGENDADA);
        var resultado = servico.consultar(1L);
        assertEquals(1, resultado.getProximos().size());
        assertTrue(resultado.getResultados().isEmpty());
        assertTrue(resultado.getClassificacao().isEmpty());
    }

    @Test void freeFireFicaParaDepois() {
        preparar("E-sports", "Free Fire", 0, 0);
        assertFalse(servico.consultar(1L).isDisponivel());
    }

    @Test void inexistenteRetorna404() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> servico.consultar(99L)).getStatusCode().value());
    }
}
