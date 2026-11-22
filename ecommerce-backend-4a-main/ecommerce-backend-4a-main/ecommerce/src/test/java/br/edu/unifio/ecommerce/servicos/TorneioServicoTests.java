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
class TorneioServicoTests {

    @Mock private TorneioRepositorio repositorio;
    @Mock private ModalidadeRepositorio modalidades;
    @Mock private PartidaRepositorio partidas;
    @Mock private InscricaoRepositorio inscricoes;
    private TorneioServico servico;

    @BeforeEach
    void preparar() {
        servico = new TorneioServico(repositorio, modalidades, partidas, inscricoes);
    }

    @Test
    void deveImpedirExclusaoDeTorneioComPartidas() {
        Torneio torneio = new Torneio();
        torneio.setId(1L);
        when(repositorio.findById(1L)).thenReturn(Optional.of(torneio));
        when(partidas.buscarPorTorneio(1L)).thenReturn(List.of(new Partida()));
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> servico.excluir(1L));
        assertEquals(409, erro.getStatusCode().value());
        verify(repositorio, never()).delete(any());
        verify(repositorio, never()).flush();
    }

    @Test
    void deveExcluirTorneioSemPartidas() {
        Torneio torneio = new Torneio();
        torneio.setId(1L);
        when(repositorio.findById(1L)).thenReturn(Optional.of(torneio));
        when(partidas.buscarPorTorneio(1L)).thenReturn(List.of());
        servico.excluir(1L);
        verify(repositorio).delete(torneio);
        verify(repositorio).flush();
    }

    private TorneioDto dadosValidos() {
        TorneioDto dados = new TorneioDto();
        dados.setNome("Competição de teste");
        dados.setDataInicio(LocalDate.now().plusDays(10));
        dados.setDataFim(LocalDate.now().plusDays(11));
        dados.setPrazoInscricao(LocalDate.now().plusDays(5));
        dados.setLimiteEquipes(4);
        dados.setModalidadeNome("Futebol");
        return dados;
    }

    @Test
    void deveReaproveitarModalidadeExistente() {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(3L);
        modalidade.setNome(" futebol ");
        when(modalidades.findAll()).thenReturn(List.of(modalidade));
        when(repositorio.save(any(Torneio.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals(3L, servico.cadastrar(dadosValidos()).getModalidadeId());
        verify(modalidades, never()).save(any());
    }

    @Test
    void deveExigirJogoParaEsports() {
        Modalidade modalidade = new Modalidade();
        modalidade.setId(4L);
        modalidade.setNome("E-sports");
        TorneioDto dados = dadosValidos();
        dados.setModalidadeId(4L);
        when(modalidades.findById(4L)).thenReturn(Optional.of(modalidade));
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> servico.cadastrar(dados)).getStatusCode().value());
        verify(repositorio, never()).save(any());
    }

    @Test void indicadoresConsultamPeriodoAtualAprovadasEFinalizadas() {
        var hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        when(repositorio.countByDataInicioLessThanEqualAndDataFimGreaterThanEqual(hoje,hoje)).thenReturn(2L);
        when(inscricoes.contarEquipesPorStatus(StatusInscricao.APROVADA)).thenReturn(5L);
        when(partidas.countByStatus(StatusPartida.FINALIZADA)).thenReturn(3L);
        var resultado=servico.indicadores();
        assertEquals(2,resultado.competicoesAtivas());assertEquals(5,resultado.equipesInscritas());assertEquals(3,resultado.partidasRealizadas());
        verify(inscricoes).contarEquipesPorStatus(StatusInscricao.APROVADA);
    }
}
