package br.edu.unifio.ecommerce.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.unifio.ecommerce.entidades.Modalidade;

public interface ModalidadeRepositorio extends JpaRepository<Modalidade, Long> {
}

