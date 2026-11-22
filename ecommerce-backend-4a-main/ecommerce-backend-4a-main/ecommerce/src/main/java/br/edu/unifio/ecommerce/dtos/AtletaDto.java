package br.edu.unifio.ecommerce.dtos;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtletaDto {
    private Long id;
    private String nome;
    private LocalDate dataNascimento;
    private Long equipeId;
}
