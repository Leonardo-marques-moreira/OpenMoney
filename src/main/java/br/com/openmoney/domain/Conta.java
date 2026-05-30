package br.com.openmoney.domain;

import java.math.BigDecimal;

public class Conta {

    private Long id;
    private String nome;
    private TipoConta tipo;
    private String cor;
    private BigDecimal saldo;
    private Long usuarioId;

    public Conta() {}

    public Conta(Long id, String nome, TipoConta tipo, String cor,
                 BigDecimal saldo, Long usuarioId) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.cor = cor;
        this.saldo = saldo;
        this.usuarioId = usuarioId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoConta getTipo() {
        return tipo;
    }

    public void setTipo(TipoConta tipo) {
        this.tipo = tipo;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    

    @Override
    public String toString() {
        return String.format("Conta{nome='%s', tipo=%s, saldo=R$%.2f}", nome, tipo, saldo);
    }
}
