package br.edu.unifio.ecommerce.dtos;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class ParticipacaoAtletaDto {
    private Long id;
    private Long atletaId;
    private Long equipeId;
    private Long torneioId;
    private String atletaNome;
    private String equipeNome;
    private String torneioNome;
}
