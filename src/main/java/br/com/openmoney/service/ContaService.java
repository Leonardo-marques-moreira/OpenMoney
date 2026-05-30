package br.com.openmoney.service;

import br.com.openmoney.domain.Conta;
import br.com.openmoney.domain.TipoConta;
import br.com.openmoney.infrastructure.exception.EntidadeNaoEncontradaException;
import br.com.openmoney.infrastructure.exception.RegraDeNegocioException;
import br.com.openmoney.repository.ContaRepository;

import java.math.BigDecimal;
import java.util.List;


public class ContaService {

    private final ContaRepository contaRepo;

    public ContaService(ContaRepository contaRepo) {
        this.contaRepo = contaRepo;
    }


    public Conta criar(Long usuarioId, String nome, TipoConta tipo,
                       String cor, BigDecimal saldoInicial) {
        validarUsuario(usuarioId);
        validarNome(nome);
        if (saldoInicial == null || saldoInicial.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraDeNegocioException("Saldo inicial nao pode ser negativo.");
        }
        if (tipo == null) {
            throw new RegraDeNegocioException("Tipo de conta obrigatorio.");
        }

        Conta c = new Conta();
        c.setNome(nome.trim());
        c.setTipo(tipo);
        c.setCor(cor != null ? cor : "#000000");
        c.setSaldo(saldoInicial);
        c.setUsuarioId(usuarioId);

        return contaRepo.salvar(c);
    }


    public Conta buscarPorId(Long id) {
        return contaRepo.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Conta", id));
    }

    public List<Conta> listarPorUsuario(Long usuarioId) {
        return contaRepo.listarPorUsuario(usuarioId);
    }

    public List<Conta> listarTodos() {
        return contaRepo.listarTodos();
    }


    public Conta atualizar(Long id, String novoNome, TipoConta novoTipo, String novaCor) {
        Conta c = buscarPorId(id);
        validarUsuario(c.getUsuarioId());
        validarNome(novoNome);
        if (novoTipo == null) throw new RegraDeNegocioException("Tipo de conta obrigatorio.");

        c.setNome(novoNome.trim());
        c.setTipo(novoTipo);
        c.setCor(novaCor != null ? novaCor : c.getCor());
        return contaRepo.atualizar(c);
    }


    public void deletar(Long id) {
        buscarPorId(id);
        try {
            contaRepo.deletar(id);
        } catch (Exception e) {
            
            if (e.getMessage() != null && e.getMessage().contains("23503")) {
                throw new RegraDeNegocioException(
                        "Nao e possivel excluir a conta pois existem transacoes vinculadas a ela.");
            }
            throw e;
        }
    }


    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Campo obrigatorio: nome da conta.");
        }
    }

    private void validarUsuario(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new RegraDeNegocioException("Usuario autenticado obrigatorio para cadastrar conta.");
        }
    }
}
