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

import br.edu.unifio.ecommerce.dtos.AtletaDto;
import br.edu.unifio.ecommerce.servicos.AtletaServico;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/atletas")
@RequiredArgsConstructor
public class AtletaControlador {
    private final AtletaServico servico;
    private final br.edu.unifio.ecommerce.servicos.EstatisticaAtletaServico estatisticas;

    @GetMapping("/{id}/desempenho")
    public br.edu.unifio.ecommerce.dtos.DesempenhoAtletaDto desempenho(@PathVariable Long id) {
        return estatisticas.desempenho(id);
    }


    @GetMapping
    public List<AtletaDto> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public AtletaDto buscarPorId(@PathVariable Long id) {
        return servico.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<AtletaDto> cadastrar(@RequestBody AtletaDto dados) {
        AtletaDto cadastrado = servico.cadastrar(dados);
        return ResponseEntity.created(URI.create("/atletas/" + cadastrado.getId())).body(cadastrado);
    }

    @PutMapping("/{id}")
    public AtletaDto atualizar(@PathVariable Long id, @RequestBody AtletaDto dados) {
        return servico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
