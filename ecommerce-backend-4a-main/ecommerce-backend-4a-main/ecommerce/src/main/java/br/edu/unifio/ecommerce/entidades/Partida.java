package br.edu.unifio.ecommerce.entidades;

import java.time.LocalDateTime;

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
public class Partida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataHora;
    private String local;
    private Integer placarEquipeA;
    private Integer placarEquipeB;

    @Enumerated(EnumType.STRING)
    private StatusPartida status = StatusPartida.AGENDADA;

    @ManyToOne
    @JoinColumn(name = "torneio_id")
    private Torneio torneio;

    @ManyToOne
    @JoinColumn(name = "equipe_a_id")
    private Equipe equipeA;

    @ManyToOne
    @JoinColumn(name = "equipe_b_id")
    private Equipe equipeB;
}

