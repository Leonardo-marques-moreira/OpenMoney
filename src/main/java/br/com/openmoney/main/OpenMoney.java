package br.com.openmoney.main;

import br.com.openmoney.domain.*;
import br.com.openmoney.infrastructure.exception.RegraDeNegocioException;
import br.com.openmoney.repository.*;
import br.com.openmoney.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;


public class OpenMoney {


    private static final UsuarioService usuarioService;
    private static final ContaService   contaService;

    static {
        UsuarioRepository usuarioRepo = new UsuarioRepository();
        ContaRepository   contaRepo   = new ContaRepository();

        usuarioService = new UsuarioService(usuarioRepo);
        contaService   = new ContaService(contaRepo);
    }

    private static final Scanner scanner = new Scanner(System.in);
    private static Usuario usuarioLogado = null;

  
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("         Bem-vindo ao OpenMoney!        ");
        System.out.println("========================================");

        int opcao;
        do {
            exibirMenuInicial();
            opcao = lerInt();
            switch (opcao) {
                case 1 -> cadastrarUsuario();
                case 2 -> login();
                case 0 -> System.out.println("\nAte logo!");
                default -> System.out.println("Opcao invalida.");
            }
        } while (opcao != 0);

        scanner.close();
    }


    static void exibirMenuInicial() {
        System.out.println("\n----------------------------------------");
        System.out.println("  1. Cadastrar usuario");
        System.out.println("  2. Login");
        System.out.println("  0. Sair");
        System.out.print("Escolha: ");
    }


    static void cadastrarUsuario() {
        System.out.println("\n--- Cadastro ---");
        System.out.print("Nome: ");  String nome  = scanner.nextLine();
        System.out.print("Email: "); String email = scanner.nextLine();
        System.out.print("Senha: "); String senha = scanner.nextLine();
        System.out.print("Confirmar senha: "); String confirmarSenha = scanner.nextLine();
        try {
            usuarioService.cadastrar(nome, email, senha, confirmarSenha);
            System.out.println("Registro gravado com sucesso!");
        } catch (RegraDeNegocioException e) {
            System.out.println("[ERRO] " + e.getMessage());
        }
    }


    static void login() {
        System.out.println("\n--- Login ---");
        System.out.print("Email: "); String email = scanner.nextLine();
        System.out.print("Senha: "); String senha = scanner.nextLine();
        try {
            usuarioLogado = usuarioService.autenticar(email, senha);
            System.out.println("Bem-vindo(a), " + usuarioLogado.getNome() + "!");
            menuPrincipal();
        } catch (RegraDeNegocioException e) {
            System.out.println("[ERRO] " + e.getMessage());
        } finally {
            usuarioLogado = null;
        }
    }


    static void menuPrincipal() {
        int opcao;
        do {
            System.out.println("\n========================================");
            System.out.println("  Ola, " + usuarioLogado.getNome() + "!");
            System.out.println("========================================");
            System.out.println("  1. Nova conta");
            System.out.println("  2. Listar contas");
            System.out.println("  0. Sair da conta");
            System.out.print("Escolha: ");
            opcao = lerInt();
            switch (opcao) {
                case 1 -> criarConta();
                case 2 -> listarContas();
                case 0 -> System.out.println("Saindo da conta...");
                default -> System.out.println("Opcao invalida.");
            }
        } while (opcao != 0);
    }


    static void criarConta() {
        System.out.print("Nome da conta: "); String nome = scanner.nextLine();
        System.out.println("Tipo: 1-CONTA CORRENTE  2-POUPANCA  3-CARTEIRA  4-CARTAO DE CREDITO");
        System.out.print("Escolha: ");
        TipoConta tipo = switch (lerInt()) {
            case 1 -> TipoConta.CONTA_CORRENTE;
            case 2 -> TipoConta.POUPANCA;
            case 3 -> TipoConta.CARTEIRA;
            case 4 -> TipoConta.CARTAO_CREDITO;
            default -> null;
        };
        System.out.print("Saldo inicial: R$ "); BigDecimal saldo = lerDecimal();
        try {
            Conta c = contaService.criar(usuarioLogado.getId(), nome, tipo, "#000000", saldo);
            System.out.println("Registro gravado com sucesso!");
            System.out.println("Conta '" + c.getNome() + "' criada com ID=" + c.getId());
        } catch (RegraDeNegocioException e) {
            System.out.println("[ERRO] " + e.getMessage());
        }
    }

    static List<Conta> listarContas() {
        List<Conta> contas = contaService.listarPorUsuario(usuarioLogado.getId());
        if (contas.isEmpty()) { System.out.println("Nenhuma conta cadastrada."); return contas; }
        System.out.println("\nContas:");
        for (int i = 0; i < contas.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + contas.get(i));
        }
        return contas;
    }

   
    static int lerInt() {
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    static BigDecimal lerDecimal() {
        try { return new BigDecimal(scanner.nextLine().trim().replace(",", ".")); }
        catch (Exception e) { System.out.println("Valor invalido, usando 0.00"); return BigDecimal.ZERO; }
    }

    static LocalDate lerData() {
        try {
            String[] p = scanner.nextLine().trim().split("/");
            return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
        } catch (Exception e) { System.out.println("Data invalida, usando hoje."); return LocalDate.now(); }
    }
}
