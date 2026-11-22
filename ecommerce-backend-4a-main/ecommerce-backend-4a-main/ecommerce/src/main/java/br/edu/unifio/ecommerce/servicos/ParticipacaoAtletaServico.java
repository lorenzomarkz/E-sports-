package br.edu.unifio.ecommerce.servicos;

import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.dtos.ParticipacaoAtletaDto;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.repositorios.*;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor @Transactional
public class ParticipacaoAtletaServico {
    private final ParticipacaoAtletaRepositorio repositorio;
    private final AtletaRepositorio atletas;
    private final EquipeRepositorio equipes;
    private final TorneioRepositorio torneios;
    private final InscricaoRepositorio inscricoes;
    private final PartidaRepositorio partidas;
    private final EstatisticaAtletaRepositorio estatisticas;

    @Transactional(readOnly = true)
    public List<ParticipacaoAtletaDto> listar(Long partidaId) {
        if (partidaId == null) return repositorio.findAll().stream().map(this::converter).toList();
        var partida = partidas.findById(partidaId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada."));
        return repositorio.findByTorneioId(partida.getTorneio().getId()).stream()
                .filter(p -> equipeDoConfronto(p, partida) && aprovada(p))
                .map(this::converter).toList();
    }

    public ParticipacaoAtletaDto cadastrar(ParticipacaoAtletaDto dados) {
        if (dados.getAtletaId() == null || dados.getEquipeId() == null || dados.getTorneioId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione atleta, equipe e competição.");
        var atleta = atletas.findById(dados.getAtletaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atleta não encontrado."));
        var equipe = equipes.findById(dados.getEquipeId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipe não encontrada."));
        var torneio = torneios.findById(dados.getTorneioId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Competição não encontrada."));
        if (!Objects.equals(equipe.getModalidade().getId(), torneio.getModalidade().getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A modalidade da equipe deve ser compatível com a competição.");
        if (repositorio.existsByAtletaIdAndTorneioId(atleta.getId(), torneio.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O atleta já participa desta competição.");
        var participacao = new ParticipacaoAtleta();
        participacao.setAtleta(atleta); participacao.setEquipe(equipe); participacao.setTorneio(torneio);
        if (!aprovada(participacao))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A equipe precisa ter inscrição aprovada na competição.");
        return converter(repositorio.saveAndFlush(participacao));
    }

    @Transactional(readOnly = true)
    public ParticipacaoAtleta validarNaPartida(Long atletaId, Partida partida) {
        var participacao = repositorio.findByAtletaIdAndTorneioId(atletaId, partida.getTorneio().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Associe o atleta à equipe e à competição antes de registrar estatísticas."));
        if (!equipeDoConfronto(participacao, partida) || !aprovada(participacao))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O atleta precisa participar de uma equipe aprovada neste confronto.");
        return participacao;
    }

    public void excluir(Long id) {
        var participacao = repositorio.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Participação não encontrada."));
        if (estatisticas.existsByParticipacaoId(id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A participação possui estatísticas e deve ser preservada.");
        repositorio.delete(participacao); repositorio.flush();
    }

    private boolean aprovada(ParticipacaoAtleta p) {
        return inscricoes.existsByTorneioIdAndEquipeIdAndStatus(p.getTorneio().getId(), p.getEquipe().getId(), StatusInscricao.APROVADA);
    }
    private boolean equipeDoConfronto(ParticipacaoAtleta p, Partida partida) {
        return Objects.equals(p.getEquipe().getId(), partida.getEquipeA().getId())
                || Objects.equals(p.getEquipe().getId(), partida.getEquipeB().getId());
    }
    private ParticipacaoAtletaDto converter(ParticipacaoAtleta p) {
        var dto = new ParticipacaoAtletaDto(); dto.setId(p.getId());
        dto.setAtletaId(p.getAtleta().getId()); dto.setAtletaNome(p.getAtleta().getNome());
        dto.setEquipeId(p.getEquipe().getId()); dto.setEquipeNome(p.getEquipe().getNome());
        dto.setTorneioId(p.getTorneio().getId()); dto.setTorneioNome(p.getTorneio().getNome());
        return dto;
    }
}
