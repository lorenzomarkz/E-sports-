package br.edu.unifio.ecommerce.dtos;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class EstatisticaAtletaDto {
    private Long id;
    private Long participacaoId;
    private String atletaNome;
    private Long atletaId;
    private Long partidaId;
    private Integer gols;
    private Integer pontos;
    private Integer abates;
    private Integer assistencias;
}
