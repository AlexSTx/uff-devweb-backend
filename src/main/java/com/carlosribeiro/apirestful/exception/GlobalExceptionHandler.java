package com.carlosribeiro.apirestful.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleEntidadeNaoEncontrada(
        EntidadeNaoEncontradaException e,
        HttpServletRequest request) {

        return new ResponseEntity<>(
            new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.name(),
                request.getMethod(),
                request.getRequestURI(),
                null,
                e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException e,
        HttpServletRequest request) {

        Map<String, String> map = new HashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            map.put(fe.getField(), fe.getDefaultMessage());
        }

        return new ResponseEntity<>(
            new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                request.getMethod(),
                request.getRequestURI(),
                map,
                e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
        IllegalStateException e, HttpServletRequest request) {
        return new ResponseEntity<>(
            new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                request.getMethod(),
                request.getRequestURI(),
                null,
                e.getMessage()
            ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleEstoqueInsuficiente(
        EstoqueInsuficienteException e, HttpServletRequest request) {
        // 409 Conflict — o carrinho conflita com o estado atual do estoque.
        // Convert List<ItemProblema> → Map<String, String> para caber no
        // ErrorResponse map. Cada chave é o produtoId e o valor explica o
        // cenário (estoque disponível vs. quantidade pedida).
        Map<String, String> problemas = new HashMap<>();
        for (EstoqueInsuficienteException.ItemProblema p : e.getItens()) {
            problemas.put(
                "produtoId=" + p.produtoId(),
                p.nome() + ": pedido=" + p.quantidadePedida()
                    + ", disponivel=" + p.estoqueDisponivel()
            );
        }
        return new ResponseEntity<>(
            new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.name(),
                request.getMethod(),
                request.getRequestURI(),
                problemas,
                e.getMessage()
            ), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EstoqueInsuficienteCarrinhoException.class)
    public ResponseEntity<ErrorResponse> handleEstoqueInsuficienteCarrinho(
        EstoqueInsuficienteCarrinhoException e, HttpServletRequest request) {
        // Mesmo formato/HTTP do handler de EstoqueInsuficienteException, para
        // o frontend reaproveitar o parser de "produtoId=…" → "nome: pedido=N, disponivel=M".
        Map<String, String> problemas = new HashMap<>();
        for (EstoqueInsuficienteCarrinhoException.ItemProblema p : e.getItens()) {
            problemas.put(
                "produtoId=" + p.produtoId(),
                p.nome() + ": pedido=" + p.quantidadePedida()
                    + ", disponivel=" + p.estoqueDisponivel()
            );
        }
        return new ResponseEntity<>(
            new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.name(),
                request.getMethod(),
                request.getRequestURI(),
                problemas,
                e.getMessage()
            ), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleSQLIntegrityConstraintViolation(
        SQLIntegrityConstraintViolationException e, HttpServletRequest request) {
        return new ResponseEntity<>(
            new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                request.getMethod(),
                request.getRequestURI(),
                null,
                e.getMessage()
            ), HttpStatus.BAD_REQUEST);
    }
}
