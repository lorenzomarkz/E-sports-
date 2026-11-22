package br.edu.unifio.ecommerce.servicos;

import java.text.Normalizer;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.entidades.*;

public final class RegrasEsportivas {
    private RegrasEsportivas() {}
    public static final List<String> MODALIDADES = List.of("Futebol", "Futsal", "Basquete", "Vôlei", "Handebol", "E-sports");
    public static final List<String> JOGOS = List.of("League of Legends", "Valorant", "Counter-Strike 2", "Rocket League", "Free Fire");

    public static String normalizar(String nome) {
        if (nome == null) return "";
        return Normalizer.normalize(nome.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT);
    }

    public static boolean modalidade(Torneio torneio, String nome) {
        return torneio.getModalidade() != null
                && normalizar(torneio.getModalidade().getNome()).equals(normalizar(nome));
    }

    public static void validarResultado(Torneio torneio, Integer a, Integer b, StatusPartida status) {
        if (modalidade(torneio, "E-sports") && "Free Fire".equals(torneio.getJogo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Free Fire terá classificação e pontos em uma etapa posterior; não use confronto entre duas equipes.");
        }
        if ((a != null && a < 0) || (b != null && b < 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os resultados não podem ser negativos.");
        }
        if (modalidade(torneio, "Vôlei")) {
            if ((a != null && a > 3) || (b != null && b > 3)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No vôlei, cada equipe pode vencer até 3 sets.");
            }
            if (status == StatusPartida.FINALIZADA && (a == null || b == null
                    || !((a == 3 && b < 3) || (b == 3 && a < 3)))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Finalize o vôlei com 3 sets para a vencedora e de 0 a 2 para a adversária.");
            }
        }
        if (status == StatusPartida.FINALIZADA && (a == null || b == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe os dois resultados para finalizar.");
        }
        if (status == StatusPartida.FINALIZADA
                && (modalidade(torneio, "Basquete") || "Rocket League".equals(torneio.getJogo())) && a.equals(b)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A partida finalizada precisa ter uma equipe vencedora.");
        }
        if (status == StatusPartida.FINALIZADA && modalidade(torneio, "E-sports")
                && List.of("League of Legends", "Valorant", "Counter-Strike 2").contains(torneio.getJogo() == null ? "" : torneio.getJogo())
                && a.equals(b)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma série finalizada precisa ter uma equipe vencedora.");
        }
    }
    // Registros parciais são permitidos; não exigimos estatísticas de todos os atletas.
    public static void validarTotais(Partida partida, List<EstatisticaAtleta> estatisticas) {
        if (partida.getStatus() != StatusPartida.FINALIZADA) return;
        var torneio = partida.getTorneio();
        boolean pontos = modalidade(torneio, "Basquete");
        boolean gols = modalidade(torneio, "Futebol") || modalidade(torneio, "Futsal")
                || modalidade(torneio, "Handebol") || "Rocket League".equals(torneio.getJogo());
        if (!pontos && !gols) return; // Sets e mapas não são pontos ou abates individuais.
        for (var equipe : List.of(partida.getEquipeA(), partida.getEquipeB())) {
            Integer placar = equipe.getId().equals(partida.getEquipeA().getId())
                    ? partida.getPlacarEquipeA() : partida.getPlacarEquipeB();
            if (placar == null) continue;
            long soma = 0;
            for (var registro : estatisticas) {
                // Somente o legado usa a equipe original; novos registros têm vínculo explícito.
                var equipeRegistro = registro.getParticipacao() == null ? registro.getAtleta().getEquipe()
                        : registro.getParticipacao().getEquipe();
                if (equipeRegistro != null && equipe.getId().equals(equipeRegistro.getId())) {
                    Integer valor = pontos ? registro.getPontos() : registro.getGols();
                    if (valor != null) soma += valor;
                }
            }
            if (soma > placar)
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A soma das estatísticas da equipe não pode superar seu placar final.");
        }
    }}
