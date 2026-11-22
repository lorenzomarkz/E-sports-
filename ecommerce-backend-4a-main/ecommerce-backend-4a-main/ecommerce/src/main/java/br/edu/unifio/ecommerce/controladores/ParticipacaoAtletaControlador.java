package br.edu.unifio.ecommerce.controladores;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.edu.unifio.ecommerce.dtos.ParticipacaoAtletaDto;
import br.edu.unifio.ecommerce.servicos.ParticipacaoAtletaServico;
import lombok.RequiredArgsConstructor;
@RestController @RequestMapping("/participacoes-atletas") @RequiredArgsConstructor
public class ParticipacaoAtletaControlador {
    private final ParticipacaoAtletaServico servico;
    @GetMapping
    public List<ParticipacaoAtletaDto> listar(@RequestParam(required = false) Long partidaId) { return servico.listar(partidaId); }
    @PostMapping
    public ResponseEntity<ParticipacaoAtletaDto> cadastrar(@RequestBody ParticipacaoAtletaDto dados) {
        var salvo = servico.cadastrar(dados);
        return ResponseEntity.created(URI.create("/participacoes-atletas/" + salvo.getId())).body(salvo);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) { servico.excluir(id); return ResponseEntity.noContent().build(); }
}
