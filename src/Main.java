import java.util.List;

import model.Projeto;
import service.ProjetoService;
import dao.ProjetoCSV;

public class Main {
    public static void main(String[] args) throws Exception {
        // TO DO: 
        // terminar o menu (encapsular em um while true)
        // criar alterar status

        ProjetoService projetoService = new ProjetoService();
        projetoService.carregar();

        System.out.println("==========================");
        System.out.println("   SISTEMA DE PROJETOS");
        System.out.println("==========================\n");

        System.out.println("1 - Listar\n2 - Buscar projeto\n3 - Cadastrar projeto\n4 - Alterar status\n5 - Excluir projeto\n0 - Sair\n");
        System.out.println("Escolha: (println placeholder)");

        System.out.println("Projetos carregados: " + projetoService.listar().size());


        // Projeto p1 = new Projeto(1, "Painel de Atendimento", "Painel para atendimento médico", "Web", "Concluído");
        // Projeto p2 = new Projeto(2, "Portfolio Academico", "Portfolio Academico em Java", "Software", "Em desenvolvimento");
        // Projeto p3 = new Projeto(3, "Aplicativo Mobile", "Aplicativo para prestação de serviço", "Mobile", "Planejado");
        
        // // exibi os projetos????
        // List<Projeto> listaDeProjetos = dao.listar();
        // listaDeProjetos.stream().forEach(n -> n.exibirDados());
        
    }
}
