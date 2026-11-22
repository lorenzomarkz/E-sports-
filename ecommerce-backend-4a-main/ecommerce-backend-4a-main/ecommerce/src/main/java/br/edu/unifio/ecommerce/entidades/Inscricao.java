package br.edu.unifio.ecommerce.entidades;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Inscricao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataInscricao;

    @Enumerated(EnumType.STRING)
    private StatusInscricao status = StatusInscricao.PENDENTE;

    @ManyToOne
    @JoinColumn(name = "torneio_id")
    private Torneio torneio;

    @ManyToOne
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;
}

