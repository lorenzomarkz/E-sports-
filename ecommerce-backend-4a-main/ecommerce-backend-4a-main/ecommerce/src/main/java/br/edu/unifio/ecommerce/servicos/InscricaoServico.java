package br.edu.unifio.ecommerce.servicos;

import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.unifio.ecommerce.dtos.InscricaoDto;
import br.edu.unifio.ecommerce.entidades.Inscricao;
import br.edu.unifio.ecommerce.entidades.Torneio;
import br.edu.unifio.ecommerce.entidades.StatusInscricao;
import br.edu.unifio.ecommerce.repositorios.InscricaoRepositorio;
import br.edu.unifio.ecommerce.repositorios.TorneioRepositorio;
import br.edu.unifio.ecommerce.repositorios.EquipeRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InscricaoServico {
    private final InscricaoRepositorio repositorio;
    private final TorneioRepositorio torneioRepositorio;
    private final EquipeRepositorio equipeRepositorio;

    @Transactional(readOnly = true)
    public List<InscricaoDto> listar() {
        return repositorio.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public InscricaoDto buscarPorId(Long id) {
        return converter(buscarEntidade(id));
    }

    public InscricaoDto cadastrar(InscricaoDto dados) {
        dados.setStatus(StatusInscricao.PENDENTE);
        validar(dados);
        Inscricao entidade = new Inscricao();
        validarRegras(entidade, dados);
        preencher(entidade, dados);
        return converter(repositorio.save(entidade));
    }

    public InscricaoDto atualizar(Long id, InscricaoDto dados) {
        Inscricao entidade = buscarEntidade(id);
        validar(dados);
        validarRegras(entidade, dados);
        preencher(entidade, dados);
        return converter(repositorio.save(entidade));
    }

    public void excluir(Long id) {
        repositorio.delete(buscarEntidade(id));
        repositorio.flush();
    }

    private Inscricao buscarEntidade(Long id) {
        return repositorio.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscricao não encontrado(a)."));
    }

    private void validar(InscricaoDto dados) {
        if (dados.getDataInscricao() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DataInscricao é obrigatório.");
        }
        if (dados.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O status é obrigatório.");
        }
        if (dados.getTorneioId() == null || dados.getTorneioId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TorneioId deve ser informado e maior que zero.");
        }
        if (dados.getEquipeId() == null || dados.getEquipeId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "EquipeId deve ser informado e maior que zero.");
        }
        if (dados.getDataInscricao().isAfter(LocalDate.now(ZoneId.of("America/Sao_Paulo")))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de inscrição não pode estar no futuro.");
        }
    }

    @Transactional(readOnly = true)
    public List<InscricaoDto> buscarPorTorneio(Long torneioId) {
        if (!torneioRepositorio.existsById(torneioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneio não encontrado.");
        }
        return repositorio.buscarPorTorneio(torneioId).stream().map(this::converter).toList();
    }

    private void validarRegras(Inscricao entidade, InscricaoDto dados) {
        Torneio torneio = torneioRepositorio.findById(dados.getTorneioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneio não encontrado."));
        List<Inscricao> inscricoes = repositorio.buscarPorTorneio(torneio.getId());
        long aprovadas = 0;
        for (Inscricao inscricao : inscricoes) {
            // A própria inscrição não conta como duplicada nem ocupa uma segunda vaga.
            if (Objects.equals(inscricao.getId(), entidade.getId())) {
                continue;
            }
            if (inscricao.getEquipe() != null
                    && Objects.equals(inscricao.getEquipe().getId(), dados.getEquipeId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Esta equipe já está inscrita neste torneio.");
            }
            if (inscricao.getStatus() == StatusInscricao.APROVADA) {
                aprovadas++;
            }
        }

        boolean novaInscricao = entidade.getId() == null
                || entidade.getTorneio() == null || entidade.getEquipe() == null
                || !Objects.equals(entidade.getTorneio().getId(), dados.getTorneioId())
                || !Objects.equals(entidade.getEquipe().getId(), dados.getEquipeId());
        if (torneio.getPrazoInscricao() == null || torneio.getLimiteEquipes() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O torneio precisa ter prazo de inscrição e limite de equipes definidos.");
        }
        // Usar a data atual impede cadastrar após o prazo informando uma data antiga.
        if (novaInscricao && LocalDate.now(ZoneId.of("America/Sao_Paulo")).isAfter(torneio.getPrazoInscricao())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O prazo de inscrição deste torneio terminou.");
        }
        if (dados.getDataInscricao().isAfter(torneio.getPrazoInscricao())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data de inscrição não pode ser posterior ao prazo do torneio.");
        }
        // Inscrições existentes podem ser analisadas após o prazo, respeitando as vagas.
        if ((novaInscricao || dados.getStatus() == StatusInscricao.APROVADA)
                && aprovadas >= torneio.getLimiteEquipes()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O torneio atingiu o limite de equipes aprovadas.");
        }
    }

    private void preencher(Inscricao entidade, InscricaoDto dados) {
        var torneio = torneioRepositorio.findById(dados.getTorneioId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneio não encontrado."));
        var equipe = equipeRepositorio.findById(dados.getEquipeId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipe não encontrada."));
        if (torneio.getModalidade() == null || equipe.getModalidade() == null
                || !Objects.equals(torneio.getModalidade().getId(), equipe.getModalidade().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A modalidade da equipe precisa ser a mesma da competição.");
        }
        entidade.setDataInscricao(dados.getDataInscricao());
        entidade.setStatus(dados.getStatus());
        entidade.setTorneio(torneioRepositorio.findById(dados.getTorneioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Torneio não encontrado(a).")));
        entidade.setEquipe(equipeRepositorio.findById(dados.getEquipeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Equipe não encontrado(a).")));
    }

    private InscricaoDto converter(Inscricao entidade) {
        InscricaoDto dados = new InscricaoDto();
        dados.setId(entidade.getId());
        dados.setDataInscricao(entidade.getDataInscricao());
        dados.setStatus(entidade.getStatus());
        dados.setTorneioId(entidade.getTorneio() == null ? null : entidade.getTorneio().getId());
        dados.setEquipeId(entidade.getEquipe() == null ? null : entidade.getEquipe().getId());
        return dados;
    }
}
