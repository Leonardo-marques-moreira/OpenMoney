package br.com.openmoney.infrastructure.exception;

/** Violação de regra de negócio (ex: e-mail duplicado, saldo insuficiente). */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String message) {
        super(message);
    }

    public RegraDeNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
