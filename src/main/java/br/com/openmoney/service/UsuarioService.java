package br.com.openmoney.service;

import br.com.openmoney.domain.Usuario;
import br.com.openmoney.infrastructure.exception.EntidadeNaoEncontradaException;
import br.com.openmoney.infrastructure.exception.RegraDeNegocioException;
import br.com.openmoney.repository.UsuarioRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private static final Pattern TEM_LETRA = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern TEM_NUMERO = Pattern.compile(".*\\d.*");

    public UsuarioService(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    public Usuario cadastrar(String nome, String email, String senha, String confirmacaoSenha) {
        validarNome(nome);
        validarEmail(email);
        validarSenha(senha);
        validarConfirmacaoSenha(senha, confirmacaoSenha);

        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepo.existeEmail(emailNormalizado)) {
            throw new RegraDeNegocioException("Este e-mail ja esta cadastrado no sistema.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(senha);
        usuario.setDataCadastro(LocalDate.now());

        return usuarioRepo.salvar(usuario);
    }

    public Usuario cadastrar(String nome, String email, String senha) {
        return cadastrar(nome, email, senha, senha);
    }

    public Usuario autenticar(String email, String senha) {
        if (email == null || email.isBlank()) {
            throw new RegraDeNegocioException("Campo obrigatorio: e-mail.");
        }
        if (senha == null || senha.isBlank()) {
            throw new RegraDeNegocioException("Campo obrigatorio: senha.");
        }

        Optional<Usuario> usuario = usuarioRepo.buscarPorEmail(email.trim().toLowerCase());
        if (usuario.isEmpty() || !usuario.get().getSenha().equals(senha)) {
            throw new RegraDeNegocioException("E-mail ou senha incorretos. Verifique suas credenciais.");
        }

        return usuario.get();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepo.buscarPorId(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuario", id));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepo.listarTodos();
    }

    public Usuario atualizar(Long id, String novoNome, String novoEmail) {
        Usuario usuario = buscarPorId(id);
        validarNome(novoNome);
        validarEmail(novoEmail);

        String emailNormalizado = novoEmail.trim().toLowerCase();
        if (!usuario.getEmail().equals(emailNormalizado) && usuarioRepo.existeEmail(emailNormalizado)) {
            throw new RegraDeNegocioException("Este e-mail ja esta cadastrado no sistema.");
        }

        usuario.setNome(novoNome.trim());
        usuario.setEmail(emailNormalizado);
        return usuarioRepo.atualizar(usuario);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        usuarioRepo.deletar(id);
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("Campo obrigatorio: nome.");
        }
    }

    private void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RegraDeNegocioException("Campo obrigatorio: e-mail.");
        }
        if (!email.contains("@")) {
            throw new RegraDeNegocioException("E-mail invalido: " + email);
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new RegraDeNegocioException("Campo obrigatorio: senha.");
        }
        if (senha.length() < 8 || !TEM_LETRA.matcher(senha).matches() || !TEM_NUMERO.matcher(senha).matches()) {
            throw new RegraDeNegocioException(
                    "A senha deve ter no minimo 8 caracteres, contendo letras e numeros.");
        }
    }

    private void validarConfirmacaoSenha(String senha, String confirmacaoSenha) {
        if (confirmacaoSenha == null || confirmacaoSenha.isBlank()) {
            throw new RegraDeNegocioException("Campo obrigatorio: confirmar senha.");
        }
        if (!senha.equals(confirmacaoSenha)) {
            throw new RegraDeNegocioException("As senhas informadas nao coincidem.");
        }
    }
}
