package br.edu.unifio.ecommerce.servicos;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.unifio.ecommerce.dtos.TorneioDto;
import br.edu.unifio.ecommerce.entidades.Torneio;
import br.edu.unifio.ecommerce.entidades.Modalidade;
import br.edu.unifio.ecommerce.repositorios.InscricaoRepositorio;
import java.util.Objects;
import br.edu.unifio.ecommerce.repositorios.TorneioRepositorio;
import br.edu.unifio.ecommerce.repositorios.ModalidadeRepositorio;
import br.edu.unifio.ecommerce.repositorios.PartidaRepositorio;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TorneioServico {
    private final TorneioRepositorio repositorio;
    private final ModalidadeRepositorio modalidadeRepositorio;
    private final PartidaRepositorio partidaRepositorio;
    private final InscricaoRepositorio inscricaoRepositorio;

    @Transactional(readOnly = true)
    public br.edu.unifio.ecommerce.dtos.IndicadoresDashboardDto indicadores() {
        var hoje = java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo"));
        return new br.edu.unifio.ecommerce.dtos.IndicadoresDashboardDto(
                repositorio.countByDataInicioLessThanEqualAndDataFimGreaterThanEqual(hoje, hoje),
                inscricaoRepositorio.contarEquipesPorStatus(br.edu.unifio.ecommerce.entidades.StatusInscricao.APROVADA),
                partidaRepositorio.countByStatus(br.edu.unifio.ecommerce.entidades.StatusPartida.FINALIZADA));
    }

    @Transactional(readOnly = true)
    public List<TorneioDto> listar() {
        return repositorio.findAll().stream().map(this::converter).toList();
    }

    @Transactional(readOnly = true)
    public TorneioDto buscarPorId(Long id) {
        return converter(buscarEntidade(id));
    }

    public TorneioDto cadastrar(TorneioDto dados) {
        validar(dados);
        Torneio entidade = new Torneio();
        preencher(entidade, dados);
        return converter(repositorio.save(entidade));
    }

    public TorneioDto atualizar(Long id, TorneioDto dados) {
        Torneio entidade = buscarEntidade(id);
        validar(dados);
        preencher(entidade, dados);
        return converter(repositorio.save(entidade));
    }

    public void excluir(Long id) {
        Torneio torneio = buscarEntidade(id);
        if (!partidaRepositorio.buscarPorTorneio(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir um torneio que possui partidas cadastradas.");
        }
        repositorio.delete(torneio);
        repositorio.flush();
    }

    private Torneio buscarEntidade(Long id) {
        return repositorio.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Torneio não encontrado(a)."));
    }

    private void validar(TorneioDto dados) {
        if (dados.getNome() == null || dados.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome é obrigatório.");
        }
        if (dados.getDataInicio() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DataInicio é obrigatório.");
        }
        if (dados.getDataFim() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DataFim é obrigatório.");
        }
        if (dados.getPrazoInscricao() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PrazoInscricao é obrigatório.");
        }
        if (dados.getLimiteEquipes() == null || dados.getLimiteEquipes() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O limite de equipes deve ser maior que zero.");
        }
        if ((dados.getModalidadeId() == null || dados.getModalidadeId() <= 0)
                && (dados.getModalidadeNome() == null || dados.getModalidadeNome().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ModalidadeId deve ser informado e maior que zero.");
        }
        if (dados.getDataFim().isBefore(dados.getDataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data final não pode ser anterior à data inicial.");
        }
        if (dados.getPrazoInscricao().isAfter(dados.getDataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O prazo de inscrição não pode ser posterior ao início do torneio.");
        }
    }

    private void preencher(Torneio entidade, TorneioDto dados) {
        Modalidade modalidade;
        if (dados.getModalidadeId() != null) {
            modalidade = modalidadeRepositorio.findById(dados.getModalidadeId()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Modalidade não encontrada."));
        } else {
            String nome = RegrasEsportivas.MODALIDADES.stream()
                    .filter(opcao -> RegrasEsportivas.normalizar(opcao).equals(RegrasEsportivas.normalizar(dados.getModalidadeNome())))
                    .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma modalidade válida."));
            modalidade = modalidadeRepositorio.findAll().stream()
                    .filter(m -> RegrasEsportivas.normalizar(m.getNome()).equals(RegrasEsportivas.normalizar(nome)))
                    .findFirst().orElseGet(() -> {
                        Modalidade nova = new Modalidade();
                        nova.setNome(nome);
                        nova.setDescricao("Modalidade esportiva");
                        return modalidadeRepositorio.save(nova);
                    });
        }
        String jogo = dados.getJogo();
        if (RegrasEsportivas.normalizar(modalidade.getNome()).equals("e-sports")) {
            if (jogo == null || !RegrasEsportivas.JOGOS.contains(jogo)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um jogo válido para E-sports.");
            }
        } else {
            jogo = null;
        }
        if (entidade.getId() != null && (!Objects.equals(entidade.getModalidade() == null ? null : entidade.getModalidade().getId(), modalidade.getId())
                || !Objects.equals(entidade.getJogo(), jogo))
                && (!inscricaoRepositorio.buscarPorTorneio(entidade.getId()).isEmpty()
                || !partidaRepositorio.buscarPorTorneio(entidade.getId()).isEmpty())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não altere modalidade ou jogo de uma competição com inscrições ou partidas.");
        }
        entidade.setModalidade(modalidade);
        entidade.setJogo(jogo);
        entidade.setNome(dados.getNome());
        entidade.setDescricao(dados.getDescricao());
        entidade.setDataInicio(dados.getDataInicio());
        entidade.setDataFim(dados.getDataFim());
        entidade.setPrazoInscricao(dados.getPrazoInscricao());
        entidade.setLocal(dados.getLocal());
        entidade.setLimiteEquipes(dados.getLimiteEquipes());

    }

    private TorneioDto converter(Torneio entidade) {
        TorneioDto dados = new TorneioDto();
        dados.setId(entidade.getId());
        dados.setNome(entidade.getNome());
        dados.setJogo(entidade.getJogo());
        dados.setModalidadeNome(entidade.getModalidade() == null ? null : entidade.getModalidade().getNome());
        dados.setDescricao(entidade.getDescricao());
        dados.setDataInicio(entidade.getDataInicio());
        dados.setDataFim(entidade.getDataFim());
        dados.setPrazoInscricao(entidade.getPrazoInscricao());
        dados.setLocal(entidade.getLocal());
        dados.setLimiteEquipes(entidade.getLimiteEquipes());
        dados.setModalidadeId(entidade.getModalidade() == null ? null : entidade.getModalidade().getId());
        return dados;
    }
}
