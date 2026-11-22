package br.edu.unifio.ecommerce.servicos;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.unifio.ecommerce.dtos.ModalidadeDto;
import br.edu.unifio.ecommerce.entidades.Modalidade;
import br.edu.unifio.ecommerce.repositorios.ModalidadeRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ModalidadeServico {
    private final ModalidadeRepositorio repositorio;

    @Transactional(readOnly = true)
    public List<ModalidadeDto> listar() {
        return repositorio.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public ModalidadeDto buscarPorId(Long id) {
        return converter(buscarEntidade(id));
    }

    public ModalidadeDto cadastrar(ModalidadeDto dados) {
        validar(dados);
        Modalidade entidade = new Modalidade();
        validarDuplicidade(dados.getNome(), entidade.getId());
        preencher(entidade, dados);
        return converter(repositorio.save(entidade));
    }

    public ModalidadeDto atualizar(Long id, ModalidadeDto dados) {
        Modalidade entidade = buscarEntidade(id);
        validar(dados);
        validarDuplicidade(dados.getNome(), id);
        preencher(entidade, dados);
        return converter(repositorio.save(entidade));
    }

    public void excluir(Long id) {
        repositorio.delete(buscarEntidade(id));
        repositorio.flush();
    }

    private Modalidade buscarEntidade(Long id) {
        return repositorio.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Modalidade não encontrado(a)."));
    }

    private void validar(ModalidadeDto dados) {
        if (dados.getNome() == null || dados.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome é obrigatório.");
        }
    }

    private void validarDuplicidade(String nome, Long id) {
        boolean repetida = repositorio.findAll().stream().anyMatch(m ->
                !java.util.Objects.equals(m.getId(), id)
                && RegrasEsportivas.normalizar(m.getNome()).equals(RegrasEsportivas.normalizar(nome)));
        if (repetida) throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta modalidade já está cadastrada.");
    }

    private void preencher(Modalidade entidade, ModalidadeDto dados) {
        entidade.setNome(dados.getNome());
        entidade.setDescricao(dados.getDescricao());

    }

    private ModalidadeDto converter(Modalidade entidade) {
        ModalidadeDto dados = new ModalidadeDto();
        dados.setId(entidade.getId());
        dados.setNome(entidade.getNome());
        dados.setDescricao(entidade.getDescricao());

        return dados;
    }
}
