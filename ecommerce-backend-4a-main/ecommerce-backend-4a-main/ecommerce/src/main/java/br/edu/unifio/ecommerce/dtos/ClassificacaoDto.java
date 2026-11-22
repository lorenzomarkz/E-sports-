package br.edu.unifio.ecommerce.dtos;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClassificacaoDto {
    private String nome;
    private String modalidade;
    private String jogo;
    private String regra;
    private String aviso;
    private boolean disponivel;
    private List<Linha> classificacao;
    private List<Jogo> proximos;
    private List<Jogo> resultados;

    @Getter @Setter
    public static class Linha {
        private Long equipeId;
        private String equipe;
        private String escudo;
        private String corEscudo;
        private int jogos;
        private int vitorias;
        private int empates;
        private int derrotas;
        private Integer pontos;
        private int saldo;
    }

    public record Jogo(Long id, String equipeA, String equipeB, LocalDateTime dataHora,
            String local, String status, Integer placarEquipeA, Integer placarEquipeB, String equipeAEscudo, String equipeBEscudo, String equipeACorEscudo, String equipeBCorEscudo) {}
}
