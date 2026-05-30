package br.com.openmoney.domain;

public enum TipoConta {
    CONTA_CORRENTE("Conta Corrente"),
    POUPANCA("Poupanca"),
    CARTEIRA("Carteira"),
    CARTAO_CREDITO("Cartao de Credito");

    private final String descricao;

    TipoConta(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
