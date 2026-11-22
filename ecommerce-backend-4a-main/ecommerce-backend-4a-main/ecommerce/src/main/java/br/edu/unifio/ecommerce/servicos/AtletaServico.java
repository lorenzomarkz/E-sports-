package br.edu.unifio.ecommerce.servicos;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.unifio.ecommerce.dtos.AtletaDto;
import br.edu.unifio.ecommerce.entidades.Atleta;
import br.edu.unifio.ecommerce.repositorios.AtletaRepositorio;
import br.edu.unifio.ecommerce.repositorios.EquipeRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AtletaServico {
    private final AtletaRepositorio repositorio;
    private final EquipeRepositorio equipeRepositorio;
    private final br.edu.unifio.ecommerce.repositorios.EstatisticaAtletaRepositorio estatisticas;

    @Transactional(readOnly = true)
    public List<AtletaDto> listar() {
        return repositorio.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public AtletaDto buscarPorId(Long id) {
        return converter(buscarEntidade(id));
    }

    public AtletaDto cadastrar(AtletaDto dados) {
        validar(dados);
        Atleta entidade = new Atleta();
        preencher(entidade, dados);
        return converter(repositorio.save(entidade));
    }

    public AtletaDto atualizar(Long id, AtletaDto dados) {
        Atleta entidade = buscarEntidade(id);
        validar(dados);
        preencher(entidade, dados);
        return converter(repositorio.save(entidade));
    }

    public void excluir(Long id) {
        repositorio.delete(buscarEntidade(id));
        repositorio.flush();
    }

    private Atleta buscarEntidade(Long id) {
        return repositorio.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Atleta não encontrado(a)."));
    }

    private void validar(AtletaDto dados) {
        if (dados.getNome() == null || dados.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome é obrigatório.");
        }
        if (dados.getDataNascimento() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DataNascimento é obrigatório.");
        }
        if (dados.getEquipeId() != null && dados.getEquipeId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EquipeId deve ser informado e maior que zero.");
        }
        if (dados.getDataNascimento().isAfter(java.time.LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de nascimento não pode estar no futuro.");
        }
    }

    private void preencher(Atleta entidade, AtletaDto dados) {
        entidade.setNome(dados.getNome());
        entidade.setDataNascimento(dados.getDataNascimento());
        Long equipeAnterior = entidade.getEquipe() == null ? null : entidade.getEquipe().getId();
        if (entidade.getId() != null && !java.util.Objects.equals(equipeAnterior, dados.getEquipeId())
                && estatisticas.existsByAtletaIdAndParticipacaoIsNull(entidade.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Preserve a equipe do cadastro antigo: existem estatísticas legadas. Use participações para novas competições.");
        entidade.setEquipe(dados.getEquipeId() == null ? null : equipeRepositorio.findById(dados.getEquipeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Equipe não encontrado(a).")));
    }

    private AtletaDto converter(Atleta entidade) {
        AtletaDto dados = new AtletaDto();
        dados.setId(entidade.getId());
        dados.setNome(entidade.getNome());
        dados.setDataNascimento(entidade.getDataNascimento());
        dados.setEquipeId(entidade.getEquipe() == null ? null : entidade.getEquipe().getId());
        return dados;
    }
}
