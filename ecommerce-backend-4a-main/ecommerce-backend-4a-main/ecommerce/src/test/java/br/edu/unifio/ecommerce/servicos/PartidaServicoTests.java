package br.edu.unifio.ecommerce.servicos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import br.edu.unifio.ecommerce.dtos.*;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.repositorios.*;

@ExtendWith(MockitoExtension.class)
class PartidaServicoTests {

    @Mock private PartidaRepositorio repositorio;
    @Mock private TorneioRepositorio torneios;
    @Mock private EquipeRepositorio equipes;
    @Mock private InscricaoRepositorio inscricoes;
    private PartidaServico servico;
    private PartidaDto dados;

    @BeforeEach
    void preparar() {
        servico = new PartidaServico(repositorio, torneios, equipes, inscricoes, mock(EstatisticaAtletaRepositorio.class));
        dados = new PartidaDto();
        dados.setDataHora(LocalDateTime.of(2030, 1, 10, 15, 0));
        dados.setTorneioId(1L);
        dados.setEquipeAId(10L);
        dados.setEquipeBId(11L);
        dados.setStatus(StatusPartida.AGENDADA);
    }

    private void conferirRejeicao() {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> servico.cadastrar(dados));
        assertEquals(400, erro.getStatusCode().value());
        verify(repositorio, never()).save(any());
    }

    @Test
    void deveImpedirMesmaEquipeNosDoisLados() {
        dados.setEquipeBId(10L);
        conferirRejeicao();
    }

    @Test
    void deveImpedirMesmaEquipeAoAtualizar() {
        when(repositorio.findById(30L)).thenReturn(Optional.of(new Partida()));
        dados.setEquipeBId(10L);
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> servico.atualizar(30L, dados));
        assertEquals(400, erro.getStatusCode().value());
        verify(repositorio, never()).save(any());
    }

    @Test
    void deveImpedirPlacarNegativoDaEquipeA() {
        dados.setPlacarEquipeA(-1);
        conferirRejeicao();
    }

    @Test
    void deveImpedirPlacarNegativoDaEquipeB() {
        dados.setPlacarEquipeB(-1);
        conferirRejeicao();
    }

    @Test
    void deveImpedirFinalizacaoSemPlacares() {
        dados.setStatus(StatusPartida.FINALIZADA);
        conferirRejeicao();
    }

    @Test
    void deveImpedirFinalizacaoComSomenteUmPlacar() {
        dados.setStatus(StatusPartida.FINALIZADA);
        dados.setPlacarEquipeA(0);
        conferirRejeicao();
    }

    @Test
    void devePermitirFinalizacaoComPlacarZero() {
        Torneio torneio = new Torneio();
        torneio.setId(1L);
        Equipe equipeA = new Equipe();
        equipeA.setId(10L);
        Equipe equipeB = new Equipe();
        equipeB.setId(11L);
        Modalidade modalidade = new Modalidade();
        modalidade.setId(1L);
        modalidade.setNome("Futebol");
        torneio.setModalidade(modalidade);
        equipeA.setModalidade(modalidade);
        equipeB.setModalidade(modalidade);
        when(inscricoes.existsByTorneioIdAndEquipeIdAndStatus(eq(1L), anyLong(), eq(StatusInscricao.APROVADA))).thenReturn(true);
        when(torneios.findById(1L)).thenReturn(Optional.of(torneio));
        when(equipes.findById(10L)).thenReturn(Optional.of(equipeA));
        when(equipes.findById(11L)).thenReturn(Optional.of(equipeB));
        when(repositorio.save(any(Partida.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        dados.setStatus(StatusPartida.FINALIZADA);
        dados.setPlacarEquipeA(0);
        dados.setPlacarEquipeB(2);
        PartidaDto resultado = servico.cadastrar(dados);
        assertEquals(StatusPartida.FINALIZADA, resultado.getStatus());
        assertEquals(0, resultado.getPlacarEquipeA());
        assertEquals(2, resultado.getPlacarEquipeB());
        verify(repositorio).save(any(Partida.class));
    }

    @Test
    void deveRetornar404ParaPartidaInexistente() {
        when(repositorio.findById(30L)).thenReturn(Optional.empty());
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> servico.buscarPorId(30L));
        assertEquals(404, erro.getStatusCode().value());
    }

    @Test
    void deveImpedirEquipeSemAprovacaoNaCompeticao() {
        Torneio torneio = new Torneio();
        torneio.setId(1L);
        Equipe equipe = new Equipe();
        equipe.setId(10L);
        when(torneios.findById(1L)).thenReturn(Optional.of(torneio));
        when(equipes.findById(10L)).thenReturn(Optional.of(equipe));
        when(inscricoes.existsByTorneioIdAndEquipeIdAndStatus(1L, 10L, StatusInscricao.APROVADA)).thenReturn(false);
        ResponseStatusException erro = assertThrows(ResponseStatusException.class, () -> servico.cadastrar(dados));
        assertEquals(409, erro.getStatusCode().value());
        verify(repositorio, never()).save(any());
    }

}
