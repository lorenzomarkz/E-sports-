package br.edu.unifio.ecommerce.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipeDto {
    private Long id;
    private String nome;
    private String corEscudo;
    private String escudo;
    private String cidade;
    private String nomeResponsavel;
    private Long modalidadeId;
}
