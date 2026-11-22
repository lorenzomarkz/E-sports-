package br.edu.unifio.ecommerce.controladores;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.unifio.ecommerce.dtos.ModalidadeDto;
import br.edu.unifio.ecommerce.servicos.ModalidadeServico;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/modalidades")
@RequiredArgsConstructor
public class ModalidadeControlador {
    private final ModalidadeServico servico;

    @GetMapping
    public List<ModalidadeDto> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public ModalidadeDto buscarPorId(@PathVariable Long id) {
        return servico.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<ModalidadeDto> cadastrar(@RequestBody ModalidadeDto dados) {
        ModalidadeDto cadastrado = servico.cadastrar(dados);
        return ResponseEntity.created(URI.create("/modalidades/" + cadastrado.getId())).body(cadastrado);
    }

    @PutMapping("/{id}")
    public ModalidadeDto atualizar(@PathVariable Long id, @RequestBody ModalidadeDto dados) {
        return servico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
