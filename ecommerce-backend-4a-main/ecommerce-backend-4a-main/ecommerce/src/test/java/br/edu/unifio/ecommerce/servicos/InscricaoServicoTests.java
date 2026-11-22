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
class InscricaoServicoTests {

    @Mock private InscricaoRepositorio repositorio;
    @Mock private TorneioRepositorio torneios;
    @Mock private EquipeRepositorio equipes;
    private InscricaoServico servico;
    private Torneio torneio;
    private Equipe equipe;
    private InscricaoDto dados;
    private LocalDate hoje;

    @BeforeEach
    void preparar() {
        servico = new InscricaoServico(repositorio, torneios, equipes);
        hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        torneio = new Torneio();
        torneio.setId(1L);
        torneio.setPrazoInscricao(hoje.plusDays(1));
        torneio.setLimiteEquipes(1);
        equipe = new Equipe();
        equipe.setId(10L);
        Modalidade modalidade = new Modalidade();
        modalidade.setId(1L);
        modalidade.setNome("Futebol");
        torneio.setModalidade(modalidade);
        equipe.setModalidade(modalidade);
        dados = new InscricaoDto();
        dados.setEquipeId(10L);
        dados.setTorneioId(1L);
        dados.setDataInscricao(hoje);
        dados.setStatus(StatusInscricao.PENDENTE);
    }

    private Inscricao inscricao(Long id, Long equipeId, StatusInscricao status) {
        Equipe participante = new Equipe();
        participante.setId(equipeId);
        Inscricao inscricao = new Inscricao();
        inscricao.setId(id);
        inscricao.setEquipe(participante);
        inscricao.setTorneio(torneio);
        inscricao.setDataInscricao(hoje);
        inscricao.setStatus(status);
        return inscricao;
    }

    private void prepararConsulta(List<Inscricao> existentes) {
        when(torneios.findById(1L)).thenReturn(Optional.of(torneio));
        when(repositorio.buscarPorTorneio(1L)).thenReturn(existentes);
    }

    private void prepararSalvamento() {
        when(equipes.findById(10L)).thenReturn(Optional.of(equipe));
        when(repositorio.save(any(Inscricao.class))).thenAnswer(invocacao -> {
            Inscricao entidade = invocacao.getArgument(0);
            if (entidade.getId() == null) entidade.setId(100L);
            return entidade;
        });
    }

    @Test
    void deveImpedirInscricaoDuplicada() {
        prepararConsulta(List.of(inscricao(20L, 10L, StatusInscricao.PENDENTE)));
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> servico.cadastrar(dados));
        assertEquals(409, erro.getStatusCode().value());
        assertEquals("Esta equipe já está inscrita neste torneio.", erro.getReason());
        verify(repositorio, never()).save(any());
    }

    @Test
    void deveImpedirDuplicidadeAoAtualizar() {
        Inscricao atual = inscricao(21L, 11L, StatusInscricao.PENDENTE);
        when(repositorio.findById(21L)).thenReturn(Optional.of(atual));
        prepararConsulta(List.of(inscricao(20L, 10L, StatusInscricao.PENDENTE), atual));
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> servico.atualizar(21L, dados));
        assertEquals(409, erro.getStatusCode().value());
        verify(repositorio, never()).save(any());
    }

    @Test
    void deveImpedirInscricaoAposPrazoMesmoComDataAntiga() {
        torneio.setPrazoInscricao(hoje.minusDays(1));
        dados.setDataInscricao(hoje.minusDays(2));
        prepararConsulta(List.of());
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> servico.cadastrar(dados));
        assertEquals(409, erro.getStatusCode().value());
        assertEquals("O prazo de inscrição deste torneio terminou.", erro.getReason());
        verify(repositorio, never()).save(any());
    }

    @Test
    void devePermitirInscricaoNoUltimoDia() {
        torneio.setPrazoInscricao(hoje);
        prepararConsulta(List.of());
        prepararSalvamento();
        InscricaoDto resultado = servico.cadastrar(dados);
        assertEquals(100L, resultado.getId());
        assertEquals(10L, resultado.getEquipeId());
        verify(repositorio).save(any(Inscricao.class));
    }

    @Test
    void deveImpedirCadastroQuandoNaoHaVagas() {
        prepararConsulta(List.of(inscricao(20L, 11L, StatusInscricao.APROVADA)));
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> servico.cadastrar(dados));
        assertEquals(409, erro.getStatusCode().value());
        assertEquals("O torneio atingiu o limite de equipes aprovadas.", erro.getReason());
        verify(repositorio, never()).save(any());
    }

    @Test
    void deveImpedirAprovacaoAcimaDoLimite() {
        Inscricao atual = inscricao(21L, 10L, StatusInscricao.PENDENTE);
        when(repositorio.findById(21L)).thenReturn(Optional.of(atual));
        prepararConsulta(List.of(inscricao(20L, 11L, StatusInscricao.APROVADA), atual));
        dados.setStatus(StatusInscricao.APROVADA);
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> servico.atualizar(21L, dados));
        assertEquals(409, erro.getStatusCode().value());
        assertEquals(StatusInscricao.PENDENTE, atual.getStatus());
        verify(repositorio, never()).save(any());
    }

    @Test
    void deveAtualizarAprovadaSemContarDuasVezes() {
        Inscricao atual = inscricao(21L, 10L, StatusInscricao.APROVADA);
        when(repositorio.findById(21L)).thenReturn(Optional.of(atual));
        prepararConsulta(List.of(atual));
        prepararSalvamento();
        dados.setStatus(StatusInscricao.APROVADA);
        assertEquals(StatusInscricao.APROVADA, servico.atualizar(21L, dados).getStatus());
        verify(repositorio).save(atual);
    }

    @Test
    void devePermitirRejeicaoAposPrazo() {
        Inscricao atual = inscricao(21L, 10L, StatusInscricao.APROVADA);
        torneio.setPrazoInscricao(hoje.minusDays(1));
        dados.setDataInscricao(hoje.minusDays(2));
        dados.setStatus(StatusInscricao.REJEITADA);
        when(repositorio.findById(21L)).thenReturn(Optional.of(atual));
        prepararConsulta(List.of(atual));
        prepararSalvamento();
        assertEquals(StatusInscricao.REJEITADA, servico.atualizar(21L, dados).getStatus());
        verify(repositorio).save(atual);
    }

    @Test
    void deveIniciarPendenteMesmoRecebendoAprovada() {
        prepararConsulta(List.of());
        prepararSalvamento();
        dados.setStatus(StatusInscricao.APROVADA);
        assertEquals(StatusInscricao.PENDENTE, servico.cadastrar(dados).getStatus());
    }

    @Test
    void deveImpedirModalidadesDiferentes() {
        prepararConsulta(List.of());
        Modalidade outra = new Modalidade();
        outra.setId(2L);
        equipe.setModalidade(outra);
        when(equipes.findById(10L)).thenReturn(Optional.of(equipe));
        ResponseStatusException erro = assertThrows(ResponseStatusException.class, () -> servico.cadastrar(dados));
        assertEquals(409, erro.getStatusCode().value());
        verify(repositorio, never()).save(any());
    }

}
