package br.edu.unifio.ecommerce.entidades;

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
public class Equipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    @jakarta.persistence.Column(nullable = false, columnDefinition = "varchar(7) default '#7ED957'")
    private String corEscudo = "#7ED957";
    @jakarta.persistence.Column(nullable = false, columnDefinition = "varchar(20) default 'VERDE'")
    private String escudo = "VERDE";
    private String cidade;
    private String nomeResponsavel;

    @ManyToOne
    @JoinColumn(name = "modalidade_id")
    private Modalidade modalidade;
}

