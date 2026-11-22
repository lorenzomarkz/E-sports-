package br.edu.unifio.ecommerce.servicos;

import java.util.*;
import br.edu.unifio.ecommerce.dtos.DesempenhoAtletaDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.dtos.EstatisticaAtletaDto;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.repositorios.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EstatisticaAtletaServico {
    private final EstatisticaAtletaRepositorio repositorio;
    private final AtletaRepositorio atletas;
    private final PartidaRepositorio partidas;
    private final ParticipacaoAtletaServico participacoes;
    private final ParticipacaoAtletaRepositorio participacaoRepositorio;

    @Transactional(readOnly = true)
    public List<EstatisticaAtletaDto> listar(Long partidaId) {
        if (partidaId != null && !partidas.existsById(partidaId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada.");
        return (partidaId == null ? repositorio.findAll() : repositorio.findByPartidaId(partidaId))
                .stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public EstatisticaAtletaDto buscarPorId(Long id) {
        return converter(buscar(id));
    }

    public EstatisticaAtletaDto cadastrar(EstatisticaAtletaDto dados) {
        if (dados.getAtletaId() == null || dados.getPartidaId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe atleta e partida.");
        Atleta atleta = atletas.findById(dados.getAtletaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atleta não encontrado."));
        Partida partida = partidas.findById(dados.getPartidaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrada."));
        var participacao = participacoes.validarNaPartida(atleta.getId(), partida);
        if (repositorio.existsByAtletaIdAndPartidaId(dados.getAtletaId(), dados.getPartidaId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este atleta já possui estatísticas nesta partida.");
        for (Integer valor : new Integer[]{dados.getGols(), dados.getPontos(), dados.getAbates(), dados.getAssistencias()}) {
            if (valor != null && valor < 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As estatísticas não podem ser negativas.");
        }
        Torneio torneio = partida.getTorneio();
        boolean rocket = "Rocket League".equals(torneio.getJogo());
        boolean eletronico = RegrasEsportivas.modalidade(torneio, "E-sports") && !rocket;
        boolean pontos = RegrasEsportivas.modalidade(torneio, "Basquete") || RegrasEsportivas.modalidade(torneio, "Vôlei");
        boolean gols = RegrasEsportivas.modalidade(torneio, "Futebol") || RegrasEsportivas.modalidade(torneio, "Futsal")
                || RegrasEsportivas.modalidade(torneio, "Handebol") || rocket;
        if ("Free Fire".equals(torneio.getJogo()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estatísticas de Free Fire serão disponibilizadas em etapa posterior.");
        if (eletronico) {
            if ((dados.getAbates() == null && dados.getAssistencias() == null) || dados.getGols() != null || dados.getPontos() != null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para jogos eletrônicos, informe abates e/ou assistências.");
        } else if (pontos) {
            if (dados.getPontos() == null || dados.getGols() != null || dados.getAbates() != null
                    || (RegrasEsportivas.modalidade(torneio, "Vôlei") && dados.getAssistencias() != null))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe pontos e, somente no basquete, assistências.");
        } else if (gols) {
            if (dados.getGols() == null || dados.getPontos() != null || dados.getAbates() != null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para esta modalidade, informe gols e/ou assistências.");
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estatísticas ainda não disponíveis para esta modalidade.");
        }
        EstatisticaAtleta entidade = new EstatisticaAtleta();
        entidade.setAtleta(atleta);
        entidade.setParticipacao(participacao);
        entidade.setPartida(partida);
        entidade.setGols(dados.getGols());
        entidade.setPontos(dados.getPontos());
        entidade.setAbates(dados.getAbates());
        entidade.setAssistencias(dados.getAssistencias());
        var todas = new java.util.ArrayList<>(repositorio.findByPartidaId(partida.getId()));
        todas.add(entidade);
        RegrasEsportivas.validarTotais(partida, todas);
        return converter(repositorio.save(entidade));
    }

    @Transactional(readOnly = true)
    public DesempenhoAtletaDto desempenho(Long atletaId) {
        var atleta = atletas.findById(atletaId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atleta não encontrado."));
        var resposta = new DesempenhoAtletaDto();
        resposta.setAtletaId(atletaId); resposta.setAtletaNome(atleta.getNome());
        Map<Long, DesempenhoAtletaDto.Competicao> grupos = new LinkedHashMap<>();
        for (var p : participacaoRepositorio.findByAtletaId(atletaId)) {
            var grupo = grupos.computeIfAbsent(p.getTorneio().getId(), id -> grupo(p.getTorneio()));
            if (!grupo.getEquipes().contains(p.getEquipe().getNome())) grupo.getEquipes().add(p.getEquipe().getNome());
        }
        // Uma partida só conta quando há registro individual, independentemente do status.
        Set<Long> partidasContadas = new HashSet<>();
        for (var registro : repositorio.findByAtletaId(atletaId)) {
            var partida = registro.getPartida();
            if (!partidasContadas.add(partida.getId())) continue;
            var grupo = grupos.computeIfAbsent(partida.getTorneio().getId(), id -> grupo(partida.getTorneio()));
            var linha = new DesempenhoAtletaDto.Historico();
            linha.setPartidaId(partida.getId()); linha.setDataHora(partida.getDataHora());
            linha.setConfronto(partida.getEquipeA().getNome() + " × " + partida.getEquipeB().getNome());
            linha.setLegado(registro.getParticipacao() == null);
            linha.setEquipe(linha.isLegado() ? "Sem participação associada (registro antigo)"
                    : registro.getParticipacao().getEquipe().getNome());
            for (String campo : grupo.getTotais().keySet()) {
                Integer valor = switch (campo) {
                    case "gols" -> registro.getGols();
                    case "pontos" -> registro.getPontos();
                    case "abates" -> registro.getAbates();
                    default -> registro.getAssistencias();
                };
                linha.getValores().put(campo, valor);
                if (valor != null) {
                    Long total = grupo.getTotais().get(campo);
                    grupo.getTotais().put(campo, (total == null ? 0L : total) + valor);
                }
            }
            grupo.getHistorico().add(linha);
            grupo.setPartidasComEstatisticas(grupo.getPartidasComEstatisticas() + 1);
        }
        for (var grupo : grupos.values()) {
            grupo.getHistorico().sort(Comparator.comparing(DesempenhoAtletaDto.Historico::getDataHora,
                    Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(DesempenhoAtletaDto.Historico::getPartidaId));
        }
        resposta.setCompeticoes(new ArrayList<>(grupos.values()));
        return resposta;
    }

    private DesempenhoAtletaDto.Competicao grupo(Torneio torneio) {
        var grupo = new DesempenhoAtletaDto.Competicao();
        grupo.setTorneioId(torneio.getId()); grupo.setTorneioNome(torneio.getNome());
        grupo.setModalidade(torneio.getModalidade().getNome()); grupo.setJogo(torneio.getJogo());
        List<String> campos = List.of();
        String modalidade = RegrasEsportivas.normalizar(grupo.getModalidade());
        if (List.of("futebol", "futsal", "handebol").contains(modalidade) || "Rocket League".equals(torneio.getJogo()))
            campos = List.of("gols", "assistencias");
        else if (modalidade.equals("basquete")) campos = List.of("pontos", "assistencias");
        else if (modalidade.equals("volei")) campos = List.of("pontos");
        else if (modalidade.equals("e-sports") && List.of("League of Legends", "Valorant", "Counter-Strike 2").contains(torneio.getJogo() == null ? "" : torneio.getJogo()))
            campos = List.of("abates", "assistencias");
        // Nulo significa não informado, não uma estatística inventada com valor zero.
        for (String campo : campos) grupo.getTotais().put(campo, null);
        return grupo;
    }

    public void excluir(Long id) {
        repositorio.delete(buscar(id));
        repositorio.flush();
    }

    private EstatisticaAtleta buscar(Long id) {
        return repositorio.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Estatística não encontrada."));
    }

    private EstatisticaAtletaDto converter(EstatisticaAtleta entidade) {
        EstatisticaAtletaDto dados = new EstatisticaAtletaDto();
        dados.setId(entidade.getId());
        dados.setAtletaNome(entidade.getAtleta().getNome());
        dados.setParticipacaoId(entidade.getParticipacao() == null ? null : entidade.getParticipacao().getId());
        dados.setAtletaId(entidade.getAtleta().getId());
        dados.setPartidaId(entidade.getPartida().getId());
        dados.setGols(entidade.getGols());
        dados.setPontos(entidade.getPontos());
        dados.setAbates(entidade.getAbates());
        dados.setAssistencias(entidade.getAssistencias());
        return dados;
    }
}
