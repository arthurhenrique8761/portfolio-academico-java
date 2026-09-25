import java.util.Scanner;

import java.util.List;
import model.Projeto;
import service.ProjetoService;

public class Main {
    public static void main(String[] args) throws Exception {
        // TO DO:
        // Fix: Busca por categoria e status
        // Add: Confirmação antes de alterar; Contador

        Scanner sc = new Scanner(System.in);

        ProjetoService projetoService = new ProjetoService();
        projetoService.carregar();

        int opcao = -1;

        do {
            System.out.println("==========================\n   SISTEMA DE PROJETOS   \n==========================");

            System.out.println(
                    "1 - Listar\n2 - Buscar projeto\n3 - Cadastrar projeto\n4 - Alterar status\n5 - Excluir projeto\n6 - Busca por categoria\n7 - Busca por status\n0 - Sair\n");
            System.out.print("Escolha: ");
            opcao = sc.nextInt();

            System.out.println();

            switch (opcao) {
                case 1:
                    System.out.println("=== LISTA DE PROJETOS ===");
                    for (Projeto projeto : projetoService.listar()) {
                        projeto.exibirDados();
                    }
                    break;
                case 2:
                    System.out.println("=== BUSCAR POR ID ===");
                    System.out.print("Digite o ID: ");
                    int idBusca = sc.nextInt();
                    Projeto projetoId = projetoService.buscaPorId(idBusca);
                    if (projetoId != null) {
                        projetoId.exibirDados();
                    } else {
                        System.out.println("\nProjeto nao encontrado");
                    }
                    break;
                case 3:
                    System.out.println("=== CADASTRAR PROJETO ===");
                    System.out.print("ID: ");
                    int id = sc.nextInt();

                    sc.nextLine();

                    System.out.print("Nome: ");
                    String nome = sc.nextLine();

                    System.out.print("Descrição: ");
                    String descricao = sc.nextLine();

                    System.out.print("Categoria: ");
                    String categoria = sc.nextLine();

                    System.out.print("Status: ");
                    String status = sc.nextLine();

                    Projeto projeto = new Projeto(id, nome, descricao, categoria, status);
                    boolean cadastrado = projetoService.adicionar(projeto);

                    if (cadastrado) {
                        projetoService.salvar();
                        System.out.println("\nProjeto cadastrado com sucesso");
                    } else {
                        System.out.println("\nNão foi possível cadastrar");
                    }
                case 4:
                    System.out.println("=== ATUALIZAR PROJETO ===");
                    System.out.print("Digite o ID do projeto: ");
                    int idAlterar = sc.nextInt();
                    sc.nextLine();

                    Projeto existente = projetoService.buscaPorId(idAlterar);

                    if (existente == null) {
                        System.out.println("Projeto não encontrado");
                        break;
                    }
                    System.out.println("\nProjeto atual");
                    existente.exibirDados();

                    System.out.print("Novo nome: ");
                    String novoNome = sc.nextLine();

                    System.out.print("Nova descrição: ");
                    String novaDescricao = sc.nextLine();

                    System.out.print("Nova categoria: ");
                    String novaCategoria = sc.nextLine();

                    System.out.print("Novo status: ");
                    String novoStatus = sc.nextLine();

                    Projeto projetoAtualizado = new Projeto(idAlterar, novoNome, novaDescricao, novaCategoria,
                            novoStatus);

                    boolean alterado = projetoService.alterar(projetoAtualizado);

                    if (alterado) {
                        projetoService.salvar();
                        System.out.printf("\nProjeto %d alterado com sucesso\n", idAlterar);
                    } else {
                        System.out.println("\nNão foi possível alterar o projeto");
                    }

                    break;
                case 5:
                    System.out.println("=== REMOVER PROJETO ===");
                    System.out.print("Digite o ID do projeto: ");
                    int idRemover = sc.nextInt();
                    sc.nextLine();

                    Projeto existenteRemover = projetoService.buscaPorId(idRemover);

                    if (existenteRemover == null) {
                        System.out.println("\nProjeto não encontrado");
                        break;
                    }

                    System.out.print("Confirmar a exclusão? (S/N): ");
                    String confirmacao = sc.nextLine();

                    if (confirmacao.equalsIgnoreCase("s")) {
                        boolean excluir = projetoService.removerPorId(idRemover);
                        if (excluir) {
                            projetoService.salvar();
                            System.out.println("\nProjeto excluído com sucesso");
                            break;
                        } else {
                            System.out.println("Exclusão cancelada");
                            break;
                        }
                    }
                case 6:
                    System.out.println("=== BUSCA POR CATEGORIA ===");
                    
                    System.out.print("Digite a categoria: ");
                    String buscaCategoria = sc.next();

                    System.out.println();

                    for (Projeto projetoCategoria : projetoService.buscarPorCategoria(buscaCategoria)) {
                        projetoCategoria.exibirDados();
                    }
                    break;
                case 7:
                    System.out.println("=== BUSCA POR STATUS ===");
                    System.out.print("Digite o status: ");
                    String statusBusca = sc.next();

                    System.out.println();

                    for (Projeto projetoStatus : projetoService.buscarPorStatus(statusBusca)) {
                        projetoStatus.exibirDados();
                    }
                case 0:
                    System.out.println("Encerrando programa...");
                    sc.close();
                    System.exit(0);
            }

        } while (opcao != -1);

    }
}
