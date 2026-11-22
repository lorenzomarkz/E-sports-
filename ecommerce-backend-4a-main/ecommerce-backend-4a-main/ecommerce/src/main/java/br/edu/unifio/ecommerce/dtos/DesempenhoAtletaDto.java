package br.edu.unifio.ecommerce.dtos;

import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DesempenhoAtletaDto {
    private Long atletaId;
    private String atletaNome;
    private List<Competicao> competicoes = new ArrayList<>();

    @Getter @Setter
    public static class Competicao {
        private Long torneioId;
        private String torneioNome;
        private String modalidade;
        private String jogo;
        private List<String> equipes = new ArrayList<>();
        private int partidasComEstatisticas;
        private Map<String, Long> totais = new LinkedHashMap<>();
        private List<Historico> historico = new ArrayList<>();
    }
    @Getter @Setter
    public static class Historico {
        private Long partidaId;
        private LocalDateTime dataHora;
        private String confronto;
        private String equipe;
        private boolean legado;
        private Map<String, Integer> valores = new LinkedHashMap<>();
    }
}
