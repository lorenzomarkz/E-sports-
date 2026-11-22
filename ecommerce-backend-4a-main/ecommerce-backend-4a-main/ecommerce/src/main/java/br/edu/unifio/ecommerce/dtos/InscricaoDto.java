package br.edu.unifio.ecommerce.dtos;

import java.time.LocalDate;
import br.edu.unifio.ecommerce.entidades.StatusInscricao;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InscricaoDto {
    private Long id;
    private LocalDate dataInscricao;
    private StatusInscricao status = StatusInscricao.PENDENTE;
    private Long torneioId;
    private Long equipeId;
}
