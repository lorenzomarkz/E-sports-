package br.edu.unifio.ecommerce.servicos;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.dtos.ClassificacaoDto;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.repositorios.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassificacaoServico {
    private final TorneioRepositorio torneios;
    private final InscricaoRepositorio inscricoes;
    private final PartidaRepositorio partidas;

    public ClassificacaoDto consultar(Long id) {
        Torneio torneio = torneios.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Competição não encontrada."));
        ClassificacaoDto dto = new ClassificacaoDto();
        dto.setNome(torneio.getNome());
        dto.setModalidade(torneio.getModalidade() == null ? "" : torneio.getModalidade().getNome());
        dto.setJogo(torneio.getJogo());
        dto.setClassificacao(List.of());
        dto.setProximos(List.of());
        dto.setResultados(List.of());
        if ("Free Fire".equals(torneio.getJogo())) {
            dto.setDisponivel(false);
            dto.setRegra("A classificação de Free Fire ficará para uma etapa posterior.");
            return dto;
        }
        String modalidade = RegrasEsportivas.normalizar(dto.getModalidade());
        boolean futebol = List.of("futebol", "futsal", "handebol").contains(modalidade);
        boolean basquete = modalidade.equals("basquete");
        boolean pontos = futebol || basquete;
        dto.setDisponivel(futebol || basquete || modalidade.equals("volei") || modalidade.equals("e-sports"));
        if (!dto.isDisponivel()) {
            dto.setRegra("Classificação ainda não disponível para esta modalidade.");
            return dto;
        }
        dto.setRegra((futebol ? "Vitória: 3 pontos; empate: 1; derrota: 0."
                : basquete ? "Vitória: 2 pontos; derrota: 1. Não há empates."
                : "Ordenação por vitórias, sem pontos de futebol. Não há empates.")
                + " Desempate: vitórias, saldo do resultado e nome da equipe.");
        Map<Long, ClassificacaoDto.Linha> tabela = new LinkedHashMap<>();
        inscricoes.buscarPorTorneio(id).stream().filter(i -> i.getStatus() == StatusInscricao.APROVADA)
                .forEach(i -> incluir(tabela, i.getEquipe(), pontos));
        List<Partida> jogos = partidas.buscarPorTorneio(id).stream()
                .sorted(Comparator.comparing(Partida::getDataHora, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Partida::getId)).toList();
        List<ClassificacaoDto.Jogo> proximos = new ArrayList<>();
        List<ClassificacaoDto.Jogo> resultados = new ArrayList<>();
        int ignorados = 0;
        for (Partida partida : jogos) {
            if (partida.getStatus() != StatusPartida.FINALIZADA) {
                proximos.add(converter(partida));
                continue;
            }
            resultados.add(converter(partida));
            Integer a = partida.getPlacarEquipeA(), b = partida.getPlacarEquipeB();
            if (a == null || b == null || a < 0 || b < 0 || (!futebol && a.equals(b))) {
                ignorados++;
                continue;
            }
            try {
                RegrasEsportivas.validarResultado(torneio, a, b, partida.getStatus());
            } catch (ResponseStatusException erro) {
                ignorados++;
                continue;
            }
            var linhaA = incluir(tabela, partida.getEquipeA(), pontos);
            var linhaB = incluir(tabela, partida.getEquipeB(), pontos);
            linhaA.setJogos(linhaA.getJogos() + 1);
            linhaB.setJogos(linhaB.getJogos() + 1);
            linhaA.setSaldo(linhaA.getSaldo() + a - b);
            linhaB.setSaldo(linhaB.getSaldo() + b - a);
            if (a.equals(b)) {
                linhaA.setEmpates(linhaA.getEmpates() + 1);
                linhaB.setEmpates(linhaB.getEmpates() + 1);
                linhaA.setPontos(linhaA.getPontos() + 1);
                linhaB.setPontos(linhaB.getPontos() + 1);
            } else {
                var vencedora = a > b ? linhaA : linhaB;
                var perdedora = a > b ? linhaB : linhaA;
                vencedora.setVitorias(vencedora.getVitorias() + 1);
                perdedora.setDerrotas(perdedora.getDerrotas() + 1);
                if (pontos) {
                    vencedora.setPontos(vencedora.getPontos() + (basquete ? 2 : 3));
                    perdedora.setPontos(perdedora.getPontos() + (basquete ? 1 : 0));
                }
            }
        }
        dto.setClassificacao(tabela.values().stream().sorted(
                Comparator.comparingInt((ClassificacaoDto.Linha l) -> pontos ? l.getPontos() : l.getVitorias()).reversed()
                .thenComparing(Comparator.comparingInt(ClassificacaoDto.Linha::getVitorias).reversed())
                .thenComparing(Comparator.comparingInt(ClassificacaoDto.Linha::getSaldo).reversed())
                .thenComparing(ClassificacaoDto.Linha::getEquipe)).toList());
        dto.setProximos(proximos);
        dto.setResultados(resultados);
        if (ignorados > 0) dto.setAviso(ignorados + " resultado(s) incompatível(is) não pontuaram. Os registros foram preservados.");
        return dto;
    }

    private ClassificacaoDto.Linha incluir(Map<Long, ClassificacaoDto.Linha> tabela, Equipe equipe, boolean pontos) {
        return tabela.computeIfAbsent(equipe.getId(), id -> {
            var linha = new ClassificacaoDto.Linha();
            linha.setEquipeId(id);
            linha.setEquipe(equipe.getNome());
            linha.setEscudo(equipe.getEscudo());
            linha.setCorEscudo(equipe.getCorEscudo());
            linha.setPontos(pontos ? 0 : null);
            return linha;
        });
    }

    private ClassificacaoDto.Jogo converter(Partida partida) {
        return new ClassificacaoDto.Jogo(partida.getId(), partida.getEquipeA().getNome(), partida.getEquipeB().getNome(),
                partida.getDataHora(), partida.getLocal(), partida.getStatus().name(),
                partida.getPlacarEquipeA(), partida.getPlacarEquipeB(), partida.getEquipeA().getEscudo(), partida.getEquipeB().getEscudo(),
                partida.getEquipeA().getCorEscudo(), partida.getEquipeB().getCorEscudo());
    }
}
