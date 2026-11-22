package br.edu.unifio.ecommerce.dtos;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TorneioDto {
    private Long id;
    private String nome;
    private String jogo;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDate prazoInscricao;
    private String local;
    private Integer limiteEquipes;
    private Long modalidadeId;
    private String modalidadeNome;
}
