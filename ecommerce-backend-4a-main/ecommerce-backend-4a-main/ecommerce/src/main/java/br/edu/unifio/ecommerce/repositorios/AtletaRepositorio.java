package br.edu.unifio.ecommerce.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.unifio.ecommerce.entidades.Atleta;

public interface AtletaRepositorio extends JpaRepository<Atleta, Long> {
}

