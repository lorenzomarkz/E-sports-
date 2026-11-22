package br.edu.unifio.ecommerce.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.unifio.ecommerce.entidades.Torneio;

public interface TorneioRepositorio extends JpaRepository<Torneio, Long> {
    long countByDataInicioLessThanEqualAndDataFimGreaterThanEqual(java.time.LocalDate inicio, java.time.LocalDate fim);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from Torneio t where t.id = :id")
    java.util.Optional<Torneio> buscarParaGeracao(@org.springframework.data.repository.query.Param("id") Long id);

}

