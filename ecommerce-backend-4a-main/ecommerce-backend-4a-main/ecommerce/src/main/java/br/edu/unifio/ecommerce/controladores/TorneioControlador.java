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

import br.edu.unifio.ecommerce.dtos.TorneioDto;
import br.edu.unifio.ecommerce.servicos.TorneioServico;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/torneios")
@RequiredArgsConstructor
public class TorneioControlador {
    private final TorneioServico servico;
    private final br.edu.unifio.ecommerce.servicos.ClassificacaoServico classificacao;

    @GetMapping("/{id}/classificacao")
    public br.edu.unifio.ecommerce.dtos.ClassificacaoDto classificacao(@PathVariable Long id) {
        return classificacao.consultar(id);
    }


    @GetMapping("/indicadores")
    public br.edu.unifio.ecommerce.dtos.IndicadoresDashboardDto indicadores() { return servico.indicadores(); }

    @GetMapping
    public List<TorneioDto> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public TorneioDto buscarPorId(@PathVariable Long id) {
        return servico.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<TorneioDto> cadastrar(@RequestBody TorneioDto dados) {
        TorneioDto cadastrado = servico.cadastrar(dados);
        return ResponseEntity.created(URI.create("/torneios/" + cadastrado.getId())).body(cadastrado);
    }

    @PutMapping("/{id}")
    public TorneioDto atualizar(@PathVariable Long id, @RequestBody TorneioDto dados) {
        return servico.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
