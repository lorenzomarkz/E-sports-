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

import br.edu.unifio.ecommerce.dtos.EquipeDto;
import br.edu.unifio.ecommerce.servicos.EquipeServico;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/equipes")
@RequiredArgsConstructor
public class EquipeControlador {
    private final EquipeServico servico;

    @GetMapping
    public List<EquipeDto> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public EquipeDto buscarPorId(@PathVariable Long id) {
        return servico.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<EquipeDto> cadastrar(@RequestBody EquipeDto dados) {
        EquipeDto cadastrado = servico.cadastrar(dados);
        return ResponseEntity.created(URI.create("/equipes/" + cadastrado.getId())).body(cadastrado);
    }

    @PutMapping("/{id}")
    public EquipeDto atualizar(@PathVariable Long id, @RequestBody EquipeDto dados) {
        return servico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
