package br.edu.unifio.ecommerce.servicos;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.unifio.ecommerce.dtos.PartidaDto;
import br.edu.unifio.ecommerce.entidades.Partida;
import br.edu.unifio.ecommerce.entidades.StatusPartida;
import br.edu.unifio.ecommerce.entidades.StatusInscricao;
import br.edu.unifio.ecommerce.repositorios.InscricaoRepositorio;
import br.edu.unifio.ecommerce.repositorios.PartidaRepositorio;
import br.edu.unifio.ecommerce.repositorios.TorneioRepositorio;
import br.edu.unifio.ecommerce.repositorios.EquipeRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PartidaServico {
    private final PartidaRepositorio repositorio;
    private final TorneioRepositorio torneioRepositorio;
    private final EquipeRepositorio equipeRepositorio;
    private final InscricaoRepositorio inscricaoRepositorio;
    private final br.edu.unifio.ecommerce.repositorios.EstatisticaAtletaRepositorio estatisticas;

    @Transactional(readOnly = true)
    public List<PartidaDto> listar() {
        return repositorio.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public PartidaDto buscarPorId(Long id) {
        return converter(buscarEntidade(id));
    }

    public PartidaDto cadastrar(PartidaDto dados) {
        validar(dados);
        Partida entidade = new Partida();
        preencher(entidade, dados);
        validarAgenda(entidade);
        RegrasEsportivas.validarTotais(entidade, entidade.getId() == null ? List.of() : estatisticas.findByPartidaId(entidade.getId()));
        return converter(repositorio.save(entidade));
    }

    public PartidaDto atualizar(Long id, PartidaDto dados) {
        Partida entidade = buscarEntidade(id);
        if (estatisticas.existsByPartidaId(id)
                && (!java.util.Objects.equals(entidade.getTorneio().getId(), dados.getTorneioId())
                || !java.util.Objects.equals(entidade.getEquipeA().getId(), dados.getEquipeAId())
                || !java.util.Objects.equals(entidade.getEquipeB().getId(), dados.getEquipeBId())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Preserve as equipes e a competição de uma partida com estatísticas.");
        validar(dados);
        preencher(entidade, dados);
        validarAgenda(entidade);
        RegrasEsportivas.validarTotais(entidade, entidade.getId() == null ? List.of() : estatisticas.findByPartidaId(entidade.getId()));
        return converter(repositorio.save(entidade));
    }

    public void excluir(Long id) {
        repositorio.delete(buscarEntidade(id));
        repositorio.flush();
    }


    public List<PartidaDto> gerar(Long torneioId, br.edu.unifio.ecommerce.dtos.GeracaoPartidasDto dados) {
        var torneio = torneioRepositorio.buscarParaGeracao(torneioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Competição não encontrada."));
        if ("Free Fire".equals(torneio.getJogo()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Free Fire não utiliza confrontos entre duas equipes.");
        if (dados.getInicio() == null || torneio.getDataInicio() == null || torneio.getDataFim() == null
                || dados.getInicio().toLocalDate().isBefore(torneio.getDataInicio())
                || dados.getInicio().toLocalDate().isAfter(torneio.getDataFim())
                || dados.getInicio().isBefore(java.time.LocalDateTime.now(java.time.ZoneId.of("America/Sao_Paulo"))))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um início futuro dentro das datas da competição.");
        var equipes = inscricaoRepositorio.buscarPorTorneio(torneioId).stream()
                .filter(i -> i.getStatus() == StatusInscricao.APROVADA)
                .map(i -> i.getEquipe()).filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(e -> e.getId(), e -> e, (a,b) -> a))
                .values().stream().sorted(java.util.Comparator.comparing(e -> e.getId())).toList();
        if (equipes.size() < 2)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "São necessárias pelo menos duas equipes aprovadas.");
        var ocupadas = new java.util.ArrayList<>(repositorio.findAll());
        var novas = new java.util.ArrayList<Partida>();
        java.time.LocalDateTime horario = ajustarHorario(dados.getInicio());
        for (int a = 0; a < equipes.size(); a++) {
            for (int b = a + 1; b < equipes.size(); b++) {
                Long equipeA = equipes.get(a).getId();
                Long equipeB = equipes.get(b).getId();
                boolean existe = ocupadas.stream().anyMatch(p -> p.getTorneio().getId().equals(torneioId)
                        && ((p.getEquipeA().getId().equals(equipeA) && p.getEquipeB().getId().equals(equipeB))
                        || (p.getEquipeA().getId().equals(equipeB) && p.getEquipeB().getId().equals(equipeA))));
                if (existe) continue;
                while (!horario.toLocalDate().isAfter(torneio.getDataFim())
                        && conflito(ocupadas, horario, equipeA, equipeB)) {
                    horario = ajustarHorario(horario.plusMinutes(120));
                }
                if (horario.toLocalDate().isAfter(torneio.getDataFim()))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Não há horários suficientes. Nenhuma partida foi gerada; ajuste as datas ou o início.");
                PartidaDto partida = new PartidaDto();
                partida.setTorneioId(torneioId);
                partida.setEquipeAId(equipeA);
                partida.setEquipeBId(equipeB);
                partida.setDataHora(horario);
                partida.setLocal(torneio.getLocal());
                partida.setStatus(StatusPartida.AGENDADA);
                validar(partida);
                Partida entidade = new Partida();
                preencher(entidade, partida);
                novas.add(entidade);
                ocupadas.add(entidade);
                horario = ajustarHorario(horario.plusMinutes(120));
            }
        }
        // Só grava depois de encontrar espaço para todos os confrontos.
        return repositorio.saveAll(novas).stream().map(this::converter).toList();
    }

    private java.time.LocalDateTime ajustarHorario(java.time.LocalDateTime horario) {
        if (horario.toLocalTime().isBefore(java.time.LocalTime.of(8, 0)))
            return horario.toLocalDate().atTime(8, 0);
        if (horario.toLocalTime().isAfter(java.time.LocalTime.of(20, 0)))
            return horario.toLocalDate().plusDays(1).atTime(8, 0);
        return horario;
    }

    private boolean conflito(List<Partida> partidas, java.time.LocalDateTime inicio, Long a, Long b) {
        return partidas.stream().anyMatch(p -> p.getDataHora() != null
                && (p.getEquipeA().getId().equals(a) || p.getEquipeA().getId().equals(b)
                    || p.getEquipeB().getId().equals(a) || p.getEquipeB().getId().equals(b))
                && inicio.isBefore(p.getDataHora().plusMinutes(120))
                && p.getDataHora().isBefore(inicio.plusMinutes(120)));
    }

    private void validarAgenda(Partida partida) {
        var torneio = partida.getTorneio();
        var dia = partida.getDataHora().toLocalDate();
        if ((torneio.getDataInicio() != null && dia.isBefore(torneio.getDataInicio()))
                || (torneio.getDataFim() != null && dia.isAfter(torneio.getDataFim())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A partida deve ocorrer dentro das datas do torneio.");
        var outras = repositorio.findAll().stream()
                .filter(p -> partida.getId() == null || !partida.getId().equals(p.getId())).toList();
        Long a = partida.getEquipeA().getId(), b = partida.getEquipeB().getId();
        boolean duplicada = outras.stream().anyMatch(p -> p.getTorneio().getId().equals(torneio.getId())
                && ((p.getEquipeA().getId().equals(a) && p.getEquipeB().getId().equals(b))
                || (p.getEquipeA().getId().equals(b) && p.getEquipeB().getId().equals(a))));
        if (duplicada)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este confronto já está cadastrado no torneio.");
        if (conflito(outras, partida.getDataHora(), a, b))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Uma das equipes já possui partida neste intervalo de duas horas.");
    }
    private Partida buscarEntidade(Long id) {
        return repositorio.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida não encontrado(a)."));
    }

    private void validar(PartidaDto dados) {
        if (dados.getDataHora() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DataHora é obrigatório.");
        }
        if (dados.getPlacarEquipeA() != null && dados.getPlacarEquipeA() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O placar não pode ser negativo.");
        }
        if (dados.getPlacarEquipeB() != null && dados.getPlacarEquipeB() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O placar não pode ser negativo.");
        }
        if (dados.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O status é obrigatório.");
        }
        if (dados.getTorneioId() == null || dados.getTorneioId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TorneioId deve ser informado e maior que zero.");
        }
        if (dados.getEquipeAId() == null || dados.getEquipeAId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EquipeAId deve ser informado e maior que zero.");
        }
        if (dados.getEquipeBId() == null || dados.getEquipeBId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EquipeBId deve ser informado e maior que zero.");
        }
        if (dados.getEquipeAId().equals(dados.getEquipeBId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Uma equipe não pode jogar contra ela mesma.");
        }
        if (dados.getStatus() == StatusPartida.FINALIZADA
                && (dados.getPlacarEquipeA() == null || dados.getPlacarEquipeB() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe os dois placares para finalizar a partida.");
        }
    }

    @Transactional(readOnly = true)
    public List<PartidaDto> buscarPorTorneio(Long torneioId) {
        if (!torneioRepositorio.existsById(torneioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneio não encontrado.");
        }
        return repositorio.buscarPorTorneio(torneioId).stream().map(this::converter).toList();
    }

    private void preencher(Partida entidade, PartidaDto dados) {
        var torneio = torneioRepositorio.findById(dados.getTorneioId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneio não encontrado."));
        RegrasEsportivas.validarResultado(torneio, dados.getPlacarEquipeA(), dados.getPlacarEquipeB(), dados.getStatus());
        for (Long equipeId : List.of(dados.getEquipeAId(), dados.getEquipeBId())) {
            var equipe = equipeRepositorio.findById(equipeId).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipe não encontrada."));
            if (!inscricaoRepositorio.existsByTorneioIdAndEquipeIdAndStatus(torneio.getId(), equipeId, StatusInscricao.APROVADA)
                    || equipe.getModalidade() == null || torneio.getModalidade() == null
                    || !equipe.getModalidade().getId().equals(torneio.getModalidade().getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "As duas equipes precisam ter inscrições APROVADAS nesta competição e modalidade compatível.");
            }
        }
        entidade.setDataHora(dados.getDataHora());
        entidade.setLocal(dados.getLocal());
        entidade.setPlacarEquipeA(dados.getPlacarEquipeA());
        entidade.setPlacarEquipeB(dados.getPlacarEquipeB());
        entidade.setStatus(dados.getStatus());
        entidade.setTorneio(torneioRepositorio.findById(dados.getTorneioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Torneio não encontrado(a).")));
        entidade.setEquipeA(equipeRepositorio.findById(dados.getEquipeAId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "EquipeA não encontrado(a).")));
        entidade.setEquipeB(equipeRepositorio.findById(dados.getEquipeBId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "EquipeB não encontrado(a).")));
    }

    private PartidaDto converter(Partida entidade) {
        PartidaDto dados = new PartidaDto();
        dados.setId(entidade.getId());
        dados.setEquipeACorEscudo(entidade.getEquipeA() == null ? "#7ED957" : entidade.getEquipeA().getCorEscudo());
        dados.setEquipeBCorEscudo(entidade.getEquipeB() == null ? "#7ED957" : entidade.getEquipeB().getCorEscudo());
        dados.setEquipeANome(entidade.getEquipeA() == null ? null : entidade.getEquipeA().getNome());
        dados.setEquipeBNome(entidade.getEquipeB() == null ? null : entidade.getEquipeB().getNome());
        dados.setEquipeAEscudo(entidade.getEquipeA() == null ? "VERDE" : entidade.getEquipeA().getEscudo());
        dados.setEquipeBEscudo(entidade.getEquipeB() == null ? "VERDE" : entidade.getEquipeB().getEscudo());
        dados.setDataHora(entidade.getDataHora());
        dados.setLocal(entidade.getLocal());
        dados.setPlacarEquipeA(entidade.getPlacarEquipeA());
        dados.setPlacarEquipeB(entidade.getPlacarEquipeB());
        dados.setStatus(entidade.getStatus());
        dados.setTorneioId(entidade.getTorneio() == null ? null : entidade.getTorneio().getId());
        dados.setEquipeAId(entidade.getEquipeA() == null ? null : entidade.getEquipeA().getId());
        dados.setEquipeBId(entidade.getEquipeB() == null ? null : entidade.getEquipeB().getId());
        return dados;
    }
}
