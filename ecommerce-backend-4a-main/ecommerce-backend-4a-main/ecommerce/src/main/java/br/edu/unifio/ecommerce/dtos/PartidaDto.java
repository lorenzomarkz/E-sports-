package br.edu.unifio.ecommerce.dtos;

import java.time.LocalDateTime;
import br.edu.unifio.ecommerce.entidades.StatusPartida;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartidaDto {
    private Long id;
    private String equipeACorEscudo;
    private String equipeBCorEscudo;
    private String equipeANome;
    private String equipeBNome;
    private String equipeAEscudo;
    private String equipeBEscudo;
    private LocalDateTime dataHora;
    private String local;
    private Integer placarEquipeA;
    private Integer placarEquipeB;
    private StatusPartida status = StatusPartida.AGENDADA;
    private Long torneioId;
    private Long equipeAId;
    private Long equipeBId;
}
