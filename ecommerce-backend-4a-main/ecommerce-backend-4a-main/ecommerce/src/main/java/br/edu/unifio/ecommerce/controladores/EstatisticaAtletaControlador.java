package br.edu.unifio.ecommerce.controladores;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.edu.unifio.ecommerce.dtos.EstatisticaAtletaDto;
import br.edu.unifio.ecommerce.servicos.EstatisticaAtletaServico;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/estatisticas")
@RequiredArgsConstructor
public class EstatisticaAtletaControlador {
    private final EstatisticaAtletaServico servico;
    @GetMapping
    public List<EstatisticaAtletaDto> listar(@RequestParam(required = false) Long partidaId) { return servico.listar(partidaId); }
    @GetMapping("/{id}")
    public EstatisticaAtletaDto buscarPorId(@PathVariable Long id) { return servico.buscarPorId(id); }
    @PostMapping
    public ResponseEntity<EstatisticaAtletaDto> cadastrar(@RequestBody EstatisticaAtletaDto dados) {
        EstatisticaAtletaDto salvo = servico.cadastrar(dados);
        return ResponseEntity.created(URI.create("/estatisticas/" + salvo.getId())).body(salvo);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
