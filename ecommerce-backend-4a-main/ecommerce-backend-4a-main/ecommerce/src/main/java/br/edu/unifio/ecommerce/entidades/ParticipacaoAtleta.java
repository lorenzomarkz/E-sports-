package br.edu.unifio.ecommerce.entidades;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"atleta_id", "torneio_id"}))
public class ParticipacaoAtleta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "atleta_id", nullable = false)
    private Atleta atleta;
    @ManyToOne(optional = false) @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;
    @ManyToOne(optional = false) @JoinColumn(name = "torneio_id", nullable = false)
    private Torneio torneio;
}
