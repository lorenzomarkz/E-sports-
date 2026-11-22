package br.edu.unifio.ecommerce.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"atleta_id", "partida_id"}))
public class EstatisticaAtleta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;
    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;
    // Nulo nos registros legados: nenhuma associação é inferida automaticamente.
    @ManyToOne
    @JoinColumn(name = "participacao_id")
    private ParticipacaoAtleta participacao;
    private Integer gols;
    private Integer pontos;
    private Integer abates;
    private Integer assistencias;
}
