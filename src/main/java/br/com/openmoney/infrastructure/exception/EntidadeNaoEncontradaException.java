package br.com.openmoney.infrastructure.exception;


public class EntidadeNaoEncontradaException extends RuntimeException {

    public EntidadeNaoEncontradaException(String entidade, Long id) {
        super(entidade + " com id=" + id + " nao encontrada.");
    }

    public EntidadeNaoEncontradaException(String message) {
        super(message);
    }
}
