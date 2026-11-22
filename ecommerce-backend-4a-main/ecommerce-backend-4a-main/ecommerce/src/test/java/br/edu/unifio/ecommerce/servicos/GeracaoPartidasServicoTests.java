package br.edu.unifio.ecommerce.servicos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.dtos.*;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.repositorios.*;

class GeracaoPartidasServicoTests {
    private final PartidaRepositorio partidas = mock(PartidaRepositorio.class);
    private final TorneioRepositorio torneios = mock(TorneioRepositorio.class);
    private final EquipeRepositorio equipes = mock(EquipeRepositorio.class);
    private final InscricaoRepositorio inscricoes = mock(InscricaoRepositorio.class);
    private final PartidaServico servico = new PartidaServico(partidas, torneios, equipes, inscricoes, mock(EstatisticaAtletaRepositorio.class));
    private final Torneio torneio = new Torneio();
    private final GeracaoPartidasDto dados = new GeracaoPartidasDto();
    private final List<Inscricao> aprovadas = new ArrayList<>();

    @BeforeEach void preparar() {
        Modalidade modalidade = new Modalidade(); modalidade.setId(1L); modalidade.setNome("Futebol");
        torneio.setId(1L); torneio.setModalidade(modalidade);
        torneio.setDataInicio(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(3));
        torneio.setDataFim(torneio.getDataInicio().plusDays(2));
        dados.setInicio(torneio.getDataInicio().atTime(8,0));
        when(torneios.buscarParaGeracao(1L)).thenReturn(Optional.of(torneio));
        when(torneios.findById(1L)).thenReturn(Optional.of(torneio));
        for (long id = 1; id <= 3; id++) {
            Equipe equipe = new Equipe(); equipe.setId(id); equipe.setModalidade(modalidade);
            Inscricao inscricao = new Inscricao(); inscricao.setEquipe(equipe); inscricao.setStatus(StatusInscricao.APROVADA);
            aprovadas.add(inscricao);
            when(equipes.findById(id)).thenReturn(Optional.of(equipe));
            when(inscricoes.existsByTorneioIdAndEquipeIdAndStatus(1L,id,StatusInscricao.APROVADA)).thenReturn(true);
        }
        when(inscricoes.buscarPorTorneio(1L)).thenReturn(aprovadas);
        when(partidas.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
    }

    private Partida existente(long torneioId, int a, int b) {
        Partida partida = new Partida();
        Torneio outro = new Torneio(); outro.setId(torneioId); partida.setTorneio(outro);
        partida.setEquipeA(aprovadas.get(a).getEquipe()); partida.setEquipeB(aprovadas.get(b).getEquipe());
        partida.setDataHora(dados.getInicio());
        return partida;
    }

    @Test void geraTodosContraTodosSemSobreporHorarios() {
        var resultado = servico.gerar(1L, dados);
        assertEquals(3, resultado.size());
        assertEquals(3, resultado.stream().map(p -> Set.of(p.getEquipeAId(),p.getEquipeBId())).distinct().count());
        assertEquals(List.of(dados.getInicio(),dados.getInicio().plusHours(2),dados.getInicio().plusHours(4)),
                resultado.stream().map(PartidaDto::getDataHora).toList());
    }

    @Test void naoDuplicaConfrontoMesmoInvertido() {
        when(partidas.findAll()).thenReturn(List.of(existente(1L,1,0)));
        var resultado = servico.gerar(1L,dados);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().noneMatch(p -> Set.of(p.getEquipeAId(),p.getEquipeBId()).equals(Set.of(1L,2L))));
    }

    @Test void respeitaPartidaDaEquipeEmOutraCompeticao() {
        when(partidas.findAll()).thenReturn(List.of(existente(2L,0,1)));
        assertEquals(dados.getInicio().plusHours(2),servico.gerar(1L,dados).getFirst().getDataHora());
    }

    @Test void faltaDeHorarioNaoGravaParteDaTabela() {
        torneio.setDataFim(torneio.getDataInicio()); dados.setInicio(torneio.getDataInicio().atTime(20,0));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.gerar(1L,dados)).getStatusCode().value());
        verify(partidas,never()).saveAll(any());
    }

    @Test void usaSomenteAprovadas() {
        aprovadas.get(2).setStatus(StatusInscricao.PENDENTE);
        assertEquals(1, servico.gerar(1L,dados).size());
    }

    @Test void freeFireNaoGeraConfrontos() {
        torneio.setJogo("Free Fire");
        assertEquals(400,assertThrows(ResponseStatusException.class,()->servico.gerar(1L,dados)).getStatusCode().value());
        verify(partidas,never()).saveAll(any());
    }
    private PartidaDto cadastroManual() {
        var dto=new PartidaDto();dto.setTorneioId(1L);dto.setEquipeAId(1L);dto.setEquipeBId(2L);
        dto.setDataHora(dados.getInicio());dto.setStatus(StatusPartida.AGENDADA);return dto;
    }
    @Test void cadastroManualRecusaConfrontoInvertido() {
        when(partidas.findAll()).thenReturn(List.of(existente(1L,1,0)));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.cadastrar(cadastroManual())).getStatusCode().value());
        verify(partidas,never()).save(any());
    }
    @Test void cadastroManualRecusaConflitoEntreCompeticoes() {
        when(partidas.findAll()).thenReturn(List.of(existente(2L,0,2)));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->servico.cadastrar(cadastroManual())).getStatusCode().value());
        verify(partidas,never()).save(any());
    }
    @Test void cadastroManualRecusaDataForaDoTorneio() {
        var dto=cadastroManual();dto.setDataHora(torneio.getDataFim().plusDays(1).atTime(8,0));
        assertEquals(400,assertThrows(ResponseStatusException.class,()->servico.cadastrar(dto)).getStatusCode().value());
        verify(partidas,never()).save(any());
    }}
