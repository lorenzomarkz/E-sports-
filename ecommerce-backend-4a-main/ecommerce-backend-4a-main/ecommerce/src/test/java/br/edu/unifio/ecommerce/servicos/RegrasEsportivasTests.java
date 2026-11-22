package br.edu.unifio.ecommerce.servicos;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import br.edu.unifio.ecommerce.entidades.*;

class RegrasEsportivasTests {
    private Torneio torneio(String nome, String jogo) {
        Modalidade modalidade = new Modalidade();
        modalidade.setNome(nome);
        Torneio torneio = new Torneio();
        torneio.setModalidade(modalidade);
        torneio.setJogo(jogo);
        return torneio;
    }
    @Test void deveAceitarGolsEPontos() {
        for (String nome : new String[]{"Futebol","Futsal","Handebol","Basquete"})
            assertDoesNotThrow(() -> RegrasEsportivas.validarResultado(torneio(nome,null), 3, 2, StatusPartida.FINALIZADA));
    }
    @Test void deveAceitarVoleiTresADois() {
        assertDoesNotThrow(() -> RegrasEsportivas.validarResultado(torneio("Vôlei",null),3,2,StatusPartida.FINALIZADA));
    }
    @Test void deveRecusarVoleiIncompleto() {
        assertThrows(ResponseStatusException.class, () -> RegrasEsportivas.validarResultado(torneio("Vôlei",null),2,1,StatusPartida.FINALIZADA));
    }
    @Test void deveRecusarVoleiComQuatroSets() {
        assertThrows(ResponseStatusException.class, () -> RegrasEsportivas.validarResultado(torneio("Vôlei",null),4,1,StatusPartida.EM_ANDAMENTO));
    }
    @Test void deveAceitarSeriesComVencedor() {
        for (String jogo : new String[]{"League of Legends","Valorant","Counter-Strike 2"})
            assertDoesNotThrow(() -> RegrasEsportivas.validarResultado(torneio("E-sports",jogo),2,0,StatusPartida.FINALIZADA));
    }
    @Test void deveRecusarEmpateEmSerieFinalizada() {
        for (String jogo : new String[]{"League of Legends","Valorant","Counter-Strike 2"})
            assertThrows(ResponseStatusException.class, () -> RegrasEsportivas.validarResultado(torneio("E-sports",jogo),1,1,StatusPartida.FINALIZADA));
    }
    @Test void deveAceitarGolsRocketLeague() {
        assertDoesNotThrow(() -> RegrasEsportivas.validarResultado(torneio("E-sports","Rocket League"),4,2,StatusPartida.FINALIZADA));
    }
    @Test void deveBloquearConfrontoFreeFire() {
        assertThrows(ResponseStatusException.class, () -> RegrasEsportivas.validarResultado(torneio("E-sports","Free Fire"),null,null,StatusPartida.AGENDADA));
    }
    @Test void deveAceitarEmpateNosEsportesComEmpate() {
        for (String nome : new String[]{"Futebol","Futsal","Handebol"})
            assertDoesNotThrow(() -> RegrasEsportivas.validarResultado(torneio(nome,null),2,2,StatusPartida.FINALIZADA));
    }
    @Test void deveRecusarEmpateFinalBasqueteERocketLeague() {
        assertThrows(ResponseStatusException.class, () -> RegrasEsportivas.validarResultado(torneio("Basquete",null),2,2,StatusPartida.FINALIZADA));
        assertThrows(ResponseStatusException.class, () -> RegrasEsportivas.validarResultado(torneio("E-sports","Rocket League"),2,2,StatusPartida.FINALIZADA));
        assertDoesNotThrow(() -> RegrasEsportivas.validarResultado(torneio("Basquete",null),2,2,StatusPartida.EM_ANDAMENTO));
    }}
