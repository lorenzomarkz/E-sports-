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

import br.edu.unifio.ecommerce.dtos.InscricaoDto;
import br.edu.unifio.ecommerce.servicos.InscricaoServico;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inscricoes")
@RequiredArgsConstructor
public class InscricaoControlador {
    private final InscricaoServico servico;

    @GetMapping
    public List<InscricaoDto> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public InscricaoDto buscarPorId(@PathVariable Long id) {
        return servico.buscarPorId(id);
    }

    @GetMapping("/torneio/{torneioId}")
    public List<InscricaoDto> buscarPorTorneio(@PathVariable Long torneioId) {
        return servico.buscarPorTorneio(torneioId);
    }

    @PostMapping
    public ResponseEntity<InscricaoDto> cadastrar(@RequestBody InscricaoDto dados) {
        InscricaoDto cadastrado = servico.cadastrar(dados);
        return ResponseEntity.created(URI.create("/inscricoes/" + cadastrado.getId())).body(cadastrado);
    }

    @PutMapping("/{id}")
    public InscricaoDto atualizar(@PathVariable Long id, @RequestBody InscricaoDto dados) {
        return servico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
