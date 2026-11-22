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

import br.edu.unifio.ecommerce.dtos.PartidaDto;
import br.edu.unifio.ecommerce.servicos.PartidaServico;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/partidas")
@RequiredArgsConstructor
public class PartidaControlador {
    private final PartidaServico servico;

    @GetMapping
    public List<PartidaDto> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public PartidaDto buscarPorId(@PathVariable Long id) {
        return servico.buscarPorId(id);
    }

    @GetMapping("/torneio/{torneioId}")
    public List<PartidaDto> buscarPorTorneio(@PathVariable Long torneioId) {
        return servico.buscarPorTorneio(torneioId);
    }

    @PostMapping("/torneio/{torneioId}/gerar")
    public List<PartidaDto> gerar(@PathVariable Long torneioId,
            @RequestBody br.edu.unifio.ecommerce.dtos.GeracaoPartidasDto dados) {
        return servico.gerar(torneioId, dados);
    }

    @PostMapping
    public ResponseEntity<PartidaDto> cadastrar(@RequestBody PartidaDto dados) {
        PartidaDto cadastrado = servico.cadastrar(dados);
        return ResponseEntity.created(URI.create("/partidas/" + cadastrado.getId())).body(cadastrado);
    }

    @PutMapping("/{id}")
    public PartidaDto atualizar(@PathVariable Long id, @RequestBody PartidaDto dados) {
        return servico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
