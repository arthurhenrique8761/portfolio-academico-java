import model.Projeto;
import service.ProjetoService;

public class Main {
    public static void main(String[] args) {
        System.out.println("===========================");
        System.out.println("    SISTEMA DE PROJETOS");
        System.out.println("===========================");
        System.out.println("   Bem-vindo ao sistema!");
        System.out.println("\nProjeto: Portifólio Acadêmico");
        System.out.println("Desenvolvido em Java");
        System.out.println("Versão: 1.0\n");

        ProjetoService projetoService = new ProjetoService();

        Projeto p1 = new Projeto(1, "Painel de Atendimento", "Painel para atendimento médico", "Web", "Concluído");
        Projeto p2 = new Projeto(2, "Portfolio Academico", "Portfolio Academico em Java", "Software", "Em desenvolvimento");
        Projeto p3 = new Projeto(3, "Aplicativo Mobile", "Aplicativo para prestação de serviço", "Mobile", "Planejado");
        Projeto p4 = new Projeto(4, "Portal de Eventos", "Portal para eventos", "Web", "Em desenvolvimento");
        Projeto p5 = new Projeto(5, "Sistema financeiro", "Sistema financeiro em Java", "Software", "Concluído");

        projetoService.adicionar(p1);
        projetoService.adicionar(p2);
        projetoService.adicionar(p3);
        projetoService.adicionar(p4);
        projetoService.adicionar(p5);

        System.out.println();

        System.out.println("TOTAL DE PROJETOS: " + projetoService.listar().size() + "\n");

        System.out.println("LISTA DE PROJETOS");
        for (Projeto projeto : projetoService.listar()) {
            projeto.exibirDados();
        }

        System.out.println("\nBUSCA PELO ID 3");
        Projeto encontrado = projetoService.buscaPorId(3);
        if (encontrado != null) {
            System.out.println("Projeto encontrado");
            encontrado.exibirDados();
        }
        else {
            System.out.println("Projeto não encontrado");
        }

        System.out.println("\nPROJETOS WEB");
        for (Projeto projeto : projetoService.buscarPorCategoria("Web")) {
            projeto.exibirDados();
        }

        System.out.println("\nPROJETOS CONCLUÍDOS");
        for (Projeto projeto : projetoService.buscarPorStatus("concluído")) {
            projeto.exibirDados();
        }

        System.out.println("\nCADASTRO DE PRODUTO COM ID REPETIDO (1)");
        Projeto p6 = new Projeto(1, "Emulador de Master System", "Emulador desktop de Master System", "Software", "Em desenvolvimento");
        projetoService.adicionar(p6);

        System.out.println("\nREMOVER PROJETO 4");
        projetoService.removerPorId(4);
    }
}
