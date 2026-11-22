package br.edu.unifio.ecommerce.servicos;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.unifio.ecommerce.dtos.EquipeDto;
import br.edu.unifio.ecommerce.entidades.Equipe;
import br.edu.unifio.ecommerce.repositorios.EquipeRepositorio;
import br.edu.unifio.ecommerce.repositorios.ModalidadeRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipeServico {
    private final EquipeRepositorio repositorio;
    private final ModalidadeRepositorio modalidadeRepositorio;
    private final br.edu.unifio.ecommerce.repositorios.InscricaoRepositorio inscricaoRepositorio;

    @Transactional(readOnly = true)
    public List<EquipeDto> listar() {
        return repositorio.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public EquipeDto buscarPorId(Long id) {
        return converter(buscarEntidade(id));
    }

    public EquipeDto cadastrar(EquipeDto dados) {
        validar(dados);
        Equipe entidade = new Equipe();
        preencher(entidade, dados);
        return converter(repositorio.save(entidade));
    }

    public EquipeDto atualizar(Long id, EquipeDto dados) {
        Equipe entidade = buscarEntidade(id);
        validar(dados);
        if (entidade.getModalidade() != null && !entidade.getModalidade().getId().equals(dados.getModalidadeId())
                && inscricaoRepositorio.existsByEquipeId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não altere a modalidade de uma equipe com inscrições.");
        }
        preencher(entidade, dados);
        return converter(repositorio.save(entidade));
    }

    public void excluir(Long id) {
        repositorio.delete(buscarEntidade(id));
        repositorio.flush();
    }

    private Equipe buscarEntidade(Long id) {
        return repositorio.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipe não encontrado(a)."));
    }

    private void validar(EquipeDto dados) {
        if (dados.getCorEscudo() == null || !List.of(
                "#7ED957", "#228B55", "#3478F6", "#1C3978", "#55C5F4", "#E53935", "#8C2645", "#F6CE3E",
                "#C99B2D", "#F28C32", "#9256DB", "#C39AEF", "#EF75B0", "#222222", "#F5F5F5", "#89939A"
        ).contains(dados.getCorEscudo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma cor válida para o escudo.");
        }
        if (dados.getEscudo() == null || !List.of("VERDE", "AZUL", "VERMELHO", "AMARELO", "ROXO", "LARANJA").contains(dados.getEscudo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um escudo válido.");
        }
        if (dados.getNome() == null || dados.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome é obrigatório.");
        }
        if (dados.getModalidadeId() == null || dados.getModalidadeId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ModalidadeId deve ser informado e maior que zero.");
        }
    }

    private void preencher(Equipe entidade, EquipeDto dados) {
        entidade.setNome(dados.getNome());
        entidade.setEscudo(dados.getEscudo());
        entidade.setCorEscudo(dados.getCorEscudo());
        entidade.setCidade(dados.getCidade());
        entidade.setNomeResponsavel(dados.getNomeResponsavel());
        entidade.setModalidade(modalidadeRepositorio.findById(dados.getModalidadeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Modalidade não encontrado(a).")));
    }

    private EquipeDto converter(Equipe entidade) {
        EquipeDto dados = new EquipeDto();
        dados.setId(entidade.getId());
        dados.setNome(entidade.getNome());
        dados.setEscudo(entidade.getEscudo());
        dados.setCorEscudo(entidade.getCorEscudo());
        dados.setCidade(entidade.getCidade());
        dados.setNomeResponsavel(entidade.getNomeResponsavel());
        dados.setModalidadeId(entidade.getModalidade() == null ? null : entidade.getModalidade().getId());
        return dados;
    }
}
