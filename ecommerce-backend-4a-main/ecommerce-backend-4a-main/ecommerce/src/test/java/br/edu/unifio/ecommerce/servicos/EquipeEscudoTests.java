package br.edu.unifio.ecommerce.servicos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.dtos.EquipeDto;
import br.edu.unifio.ecommerce.entidades.*;
import br.edu.unifio.ecommerce.repositorios.*;

class EquipeEscudoTests {
    private final EquipeRepositorio equipes = mock(EquipeRepositorio.class);
    private final ModalidadeRepositorio modalidades = mock(ModalidadeRepositorio.class);
    private final EquipeServico servico = new EquipeServico(equipes, modalidades, mock(InscricaoRepositorio.class));
    private EquipeDto dados(String escudo) {
        var dto = new EquipeDto();
        dto.setNome("Equipe de teste"); dto.setModalidadeId(1L); dto.setEscudo(escudo); dto.setCorEscudo("#7ED957");
        return dto;
    }
    @Test void exigeEscudoNoCadastro() {
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> servico.cadastrar(dados(null))).getStatusCode().value());
        verify(equipes, never()).save(any());
    }
    @Test void rejeitaOpcaoDesconhecida() {
        assertThrows(ResponseStatusException.class, () -> servico.cadastrar(dados("IMAGEM_EXTERNA")));
        verify(equipes, never()).save(any());
    }
    @Test void salvaEscudoSelecionado() {
        when(modalidades.findById(1L)).thenReturn(Optional.of(new Modalidade()));
        when(equipes.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("AZUL", servico.cadastrar(dados("AZUL")).getEscudo());
    }
    @Test void permiteTrocarEscudo() {
        var equipe = new Equipe(); equipe.setId(1L);
        when(equipes.findById(1L)).thenReturn(Optional.of(equipe));
        when(modalidades.findById(1L)).thenReturn(Optional.of(new Modalidade()));
        when(equipes.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("ROXO", servico.atualizar(1L, dados("ROXO")).getEscudo());
    }
    @Test void exigeCor() {
        var dto = dados("AZUL"); dto.setCorEscudo(null);
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> servico.cadastrar(dto)).getStatusCode().value());
        verify(equipes, never()).save(any());
    }
    @Test void rejeitaCorForaDaLista() {
        var dto = dados("AZUL"); dto.setCorEscudo("#123456");
        assertThrows(ResponseStatusException.class, () -> servico.cadastrar(dto));
        verify(equipes, never()).save(any());
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
        "#7ED957", "#228B55", "#3478F6", "#1C3978", "#55C5F4", "#E53935", "#8C2645", "#F6CE3E",
        "#C99B2D", "#F28C32", "#9256DB", "#C39AEF", "#EF75B0", "#222222", "#F5F5F5", "#89939A"
    })
    void salvaCadaCorPermitida(String cor) {
        when(modalidades.findById(1L)).thenReturn(Optional.of(new Modalidade()));
        when(equipes.save(any())).thenAnswer(i -> i.getArgument(0));
        var dto = dados("AZUL"); dto.setCorEscudo(cor);
        assertEquals(cor, servico.cadastrar(dto).getCorEscudo());
    }}
