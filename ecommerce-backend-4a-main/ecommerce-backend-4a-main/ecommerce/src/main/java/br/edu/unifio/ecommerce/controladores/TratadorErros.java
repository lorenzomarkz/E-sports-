package br.edu.unifio.ecommerce.controladores;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class TratadorErros {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> tratarStatus(ResponseStatusException erro) {
        String mensagem = erro.getReason() == null ? "Não foi possível realizar a operação." : erro.getReason();
        return ResponseEntity.status(erro.getStatusCode()).body(Map.of("mensagem", mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarJsonInvalido(HttpMessageNotReadableException erro) {
        return ResponseEntity.badRequest().body(Map.of("mensagem",
                "JSON inválido. Verifique os tipos, status e datas (AAAA-MM-DD ou AAAA-MM-DDTHH:mm:ss)."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> tratarConflito(DataIntegrityViolationException erro) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem",
                "A operação viola uma restrição do banco. Verifique os vínculos e os dados informados."));
    }
}
