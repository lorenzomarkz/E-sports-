package br.edu.unifio.ecommerce.entidades;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Torneio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String jogo;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDate prazoInscricao;
    private String local;
    private Integer limiteEquipes;

    @ManyToOne
    @JoinColumn(name = "modalidade_id")
    private Modalidade modalidade;
}

